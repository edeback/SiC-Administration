# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Starsector mod (`xepel_sicadmin`) that adds an "Administration" aptitude to the
**Second-in-Command** mod. It is a mod-of-a-mod: SiC provides the executive-officer
framework, this repo provides one aptitude and its ten skills. The README lists the
designed effect of every skill and is the reference for intended behaviour.

The repo lives inside a full Starsector install at `../../` — the game, its API, and every
other installed mod are siblings under `../`. That layout is load-bearing for the IntelliJ
project and the run configurations.

## Building and running

There is no Gradle/Maven build. IntelliJ compiles `src/` and the **"Create .jar"** artifact
(`.idea/artifacts/Create__jar.xml`) packs the module output into `jars/SiC Administration.jar`.
`build-on-make` is on, so a normal Make builds the jar. The artifact packs *module output only* —
library jars are never bundled, which is what makes optional cross-mod compilation safe.

Two run configurations in `.run/` launch the real game with this module on the classpath
(working directory `../../starsector-core`, bundled JRE at `../../jre`).
`Run Starsector w/o Launcher` additionally opens a JDWP debug socket on port 5005 and skips
the launcher dialog.

There are no tests. Verification means launching the game and reading
`../../starsector-core/starsector.log`.

`jars/SiC Administration.jar` **is committed**, and must stay that way: the release workflow
builds its zip from `git ls-files`, so an un-committed jar ships an empty mod. Releases fire
on pushing a tag (`.github/workflows/release.yml`); tags containing `dev`, `qa`, or `unstable`
are marked prerelease.

To compile-check without IntelliJ:

```bash
javac -nowarn -encoding UTF-8 -d /tmp/out -cp "../../starsector-core/*;../Second-in-Command-2.0.0/jars/SecondInCommand.jar;../zz AoTD - Theory of Toolbox-1.0.11/jars/AoTDToolboxTheory.jar" $(find src -name '*.java')
```

## Reading the game's source

The base game is closed-source, but `../../starsector-core/starfarer.api.zip` contains the
full source of `starfarer.api.jar`, including the `impl` package — that is, `CoreScript`,
the intel classes, the industry implementations. Unzip and grep it before guessing at
vanilla behaviour. For other mods, several ship source under `mods/<mod>/src` or
`jars/src.rar`; where they don't, `javap -c -p` against their jar is usually enough to find
the hook you need.

## Architecture

### Skills are registered in two places, with two different id schemes

Every skill needs an entry in `data/config/secondInCommand/SCSkills.csv` (id prefix
**`sc_admin_`**) pointing at a `SCBaseSkillPlugin` subclass in
`src/xepel/sic_admin/skills/administration/`. `AptitudeAdministration.createSections()`
then arranges those ids into the three unlock tiers shown in the officer UI, and the
aptitude itself is declared in `SCAptitudes.csv` plus a matching
`executive_officer_<aptitudeId>` post in `data/world/factions/default_ranks.json`.

Skills whose effects apply to *colonies* additionally need a vanilla skill: a `.skill` file
in `data/characters/skills/` and a row in `skill_data.csv`, both using id prefix
**`sic_admin_`** (note `sic`, not `sc` — the two prefixes are deliberate and easy to
confuse). The `.skill` file points at a nested `*Effect` static class implementing
`MarketSkillEffect` / `CharacterStatsSkillEffect`, and the SiC plugin's `onActivation`
turns it on with `stats.setSkillLevel("sic_admin_...", 1)`. This indirection exists because
vanilla owns colony/market stat application and its own persistence; the SiC plugin cannot
reach markets directly.

Fleet-scoped skills (`SupplyRoutes`, `HyperspaceBuoys`, `HyperspaceSensors`) skip the
vanilla layer entirely and just modify `FleetMemberAPI` stats from `advance()`.

### Skill plugin lifecycle

`SCSkillSpec` instantiates each plugin **once per game process** and reuses that singleton, so
per-officer state must never be stored on the plugin (SCBaseSkillPlugin says as much).
`onActivation`/`onDeactivation` fire on assignment changes, but **not** reliably on save load —
SiC's `SCData` is serialized into the save along with its fleet event listener, so the
re-activation path in `SCUtils.getFleetData` usually doesn't run. Anything that must exist
after a load belongs in `SiCAdministrationPlugin.onGameLoad`, keyed off the vanilla skill
level rather than off activation callbacks.

Corollary: never `Global.getSector().getListenerManager().addListener(this)` from a skill
plugin. `addListener` defaults to non-transient, which serializes the plugin into the save;
the instance that comes back on load is a different object, so `removeListener(this)` can
never match it and the listener leaks forever.

### Ship production hooks (`src/xepel/sic_admin/production/`)

`OptimizedShipbuilding` grants an extra permanent hullmod slot by building the hidden
`sic_admin_optimized_hull` hullmod into newly produced ships. There is no vanilla
"ship produced" listener, and different mods produce ships in incompatible ways, so this is
structured as one shared stamper plus one hook per production system:

- `ProducedShipStamper` — the only place that touches a variant. Clones non-`REFIT` variants
  before writing so a stock variant shared by many ships is never modified, and checks the
  player's `sic_admin_optimized_shipbuilding` skill level at fire time.
- `VanillaProductionHook` — an `EconomyTickListener`. `CoreScript.doCustomProduction()` runs
  from its own month-end listener and posts a `ProductionReportIntel` holding the same
  `FleetMemberAPI` instances it just put into storage; running after it and reading that
  report is the only handle on those ships.
- `AoTDProductionHook` — Ashes of the Domain replaces custom production wholesale with a
  continuous, every-frame system that posts its own `AoTDProductionReportIntel` (extends
  `FleetLogIntel`, *not* `ProductionReportIntel`), so the vanilla hook never sees anything.
  AoTD fires `AoTDProductionListenerAPI.onShipProductionFinished` from
  `AoTDProductionOrderData.addReward()`, after `variant.clear()` and before the ship enters
  storage, which is exactly where a permamod survives.
- `ProductionHooks` — registers both from `onGameLoad`, transiently.

`com.fs.starfarer.api.impl.campaign.intel.misc.HackProductionReport` is a deliberate
package-injection trick: `ProductionReportIntel` keeps its data in protected fields with no
getters, so declaring a class in the game's own package gets package-level access to them
without reflection. Keep it in `src/xepel/sic_admin/` despite the package name.

### Optional dependencies on other mods

`AoTDProductionHook` implements an interface from another mod's jar. The pattern that keeps
that safe when the mod is absent: put the foreign type in its own class, reference it only
from a bare `invokestatic` guarded by
`Global.getSettings().getModManager().isModEnabled(...)` and wrapped in `catch (Throwable)`.
JVM constant-pool resolution is lazy, so the class is never loaded and never fails to
verify. Add the foreign jar as an IntelliJ project library (see
`.idea/libraries/AoTDToolboxTheory.xml`) — **not** as a `mod_info.json` dependency, which
would make it mandatory.

Library paths embed the sibling mod's versioned folder name (`Second-in-Command-2.0.0`,
`zz AoTD - Theory of Toolbox-1.0.11`), so they break whenever those mods update.

## Version bumps

Three files must agree: `mod_info.json` (`version`), `xepel_sicadmin.version`
(`modVersion`, and `directDownloadURL`, which pins the release tag), and the git tag that
triggers the release.

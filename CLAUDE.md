# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Starsector mod (`sic_admin_xo`, "SiC Admin XO") that adds an "Administration" aptitude to the
**Second-in-Command** mod. It is a mod-of-a-mod: SiC provides the executive-officer framework,
this repo provides one aptitude and its ten skills. The README lists the designed effect of
every skill and is the reference for intended behaviour.

The repo must sit inside a full Starsector install as `<Starsector>/mods/SiC-Administration`.
`starsectorPath` in `build.gradle.kts` is `../../` and everything else keys off it: the game,
its API, and every other installed mod are siblings under `../`. The mod also has to be in
`mods/` for the game to load it at all.

## Building

Gradle, via the wrapper. The config knobs are the `val`s at the top of `build.gradle.kts`
(`starsectorPath`, `jarName`, `zipName`, `modDependencies`, `packageIncludes`, `javaVersion`);
everything below the `/// BUILD PIPELINE` banner is machinery that rarely needs touching.

```bash
./gradlew jar
```

The jar lands at `jars/SiCAdminXO.jar`, which `mod_info.json` points at. `runStarsector` and
`runStarsectorNoLauncher` build and launch the real game; both parse `vmparams` for the JVM
flags and classpath, so they follow the install rather than a frozen copy. The `.run/`
configurations are thin IntelliJ wrappers around those two tasks.

**`./gradlew clean jar` as a single command fails.** `stageStarsectorApi()` and
`stageModDependency()` write the staged API and mod-dependency jars into `build/` at
*configuration* time; `clean` then deletes them before `compileJava` runs, and you get ~100
errors like `package com.fs.starfarer.api.campaign.econ does not exist`. Run the two as
separate invocations.

There are no tests (`tasks.test` is disabled). Verification means launching the game and
reading `../../starsector-core/starsector.log`.

### How dependencies resolve

`modDependencies` lists bare jar filenames; the build searches every `mods/*/jars/**/` for a
match, so nothing hardcodes a versioned folder name. Each match is staged into a local Maven
repo under `build/modDepsRepo/` alongside a `-sources.jar` built from the `.java`/`.kt` files
that Starsector mod jars bundle, which is what gives IntelliJ working navigation into SiC's
code. The game API gets the same treatment in `build/starsector-api/`, using
`starfarer.api.zip` as its sources jar.

All of these are `compileOnly` — they are never bundled into the mod jar.

Note that `modDependencies` currently also lists LazyLib, MagicLib, and LunaLib, which no
source file imports. They are leftovers from the template and could be dropped.

### Reading the game's source

The base game is closed-source, but `../../starsector-core/starfarer.api.zip` contains the full
source of `starfarer.api.jar`, including the `impl` package — `CoreScript`, the intel classes,
the industry implementations. Unzip and grep it before guessing at vanilla behaviour. For other
mods, several ship source under `mods/<mod>/src` or `jars/src.rar`; where they don't,
`javap -c -p` against their jar is usually enough to find the hook you need.

## Architecture

### Skills are registered in two places, with two different id schemes

Every skill needs an entry in `data/config/secondInCommand/SCSkills.csv` (id prefix
**`sc_admin_`**) pointing at a `SCBaseSkillPlugin` subclass in
`src/sic_admin_xo/skills/administration/`. `AptitudeAdministration.createSections()` then
arranges those ids into the three unlock tiers shown in the officer UI, and the aptitude itself
is declared in `SCAptitudes.csv` plus a matching `executive_officer_<aptitudeId>` post in
`data/world/factions/default_ranks.json`.

Skills whose effects apply to *colonies* additionally need a vanilla skill: a `.skill` file in
`data/characters/skills/` and a row in `skill_data.csv`, both using id prefix **`sic_admin_`**
(note `sic`, not `sc` — the two prefixes are deliberate and easy to confuse). The `.skill` file
points at a nested `*Effect` static class implementing `MarketSkillEffect` /
`CharacterStatsSkillEffect`, and the SiC plugin's `onActivation` turns it on with
`stats.setSkillLevel("sic_admin_...", 1)`. This indirection exists because vanilla owns
colony/market stat application and its own persistence; the SiC plugin cannot reach markets
directly.

Fleet-scoped skills (`SupplyRoutes`, `HyperspaceBuoys`, `HyperspaceSensors`) skip the vanilla
layer entirely and just modify `FleetMemberAPI` stats from `advance()`.

Class names in the CSVs, the `.skill` files, and `hull_mods.csv` are plain strings — nothing
checks them at build time, so a rename that misses one fails only at runtime.

### Skill plugin lifecycle

`SCSkillSpec` instantiates each plugin **once per game process** and reuses that singleton, so
per-officer state must never be stored on the plugin (SCBaseSkillPlugin says as much).
`onActivation`/`onDeactivation` fire on assignment changes, but **not** reliably on save load —
SiC's `SCData` is serialized into the save along with its fleet event listener, so the
re-activation path in `SCUtils.getFleetData` usually doesn't run. Anything that must exist
after a load belongs in `SiCAdministrationPlugin.onGameLoad`, keyed off the vanilla skill level
rather than off activation callbacks.

Corollary: never `Global.getSector().getListenerManager().addListener(this)` from a skill
plugin. `addListener` defaults to non-transient, which serializes the plugin into the save; the
instance that comes back on load is a different object, so `removeListener(this)` can never
match it and the listener leaks forever.

### Ship production hooks (`src/sic_admin_xo/production/`)

`OptimizedShipbuilding` grants an extra permanent hullmod slot by building the hidden
`sic_admin_optimized_hull` hullmod into newly produced ships. There is no vanilla
"ship produced" listener, and different mods produce ships in incompatible ways, so this is
structured as one shared stamper plus one hook per production system:

- `ProducedShipStamper` — the only place that touches a variant. Clones non-`REFIT` variants
  before writing so a stock variant shared by many ships is never modified, and checks the
  player's `sic_admin_optimized_shipbuilding` skill level at fire time.
- `VanillaProductionHook` — an `EconomyTickListener`. `CoreScript.doCustomProduction()` runs
  from its own month-end listener and posts a `ProductionReportIntel` holding the same
  `FleetMemberAPI` instances it just put into storage; running after it and reading that report
  is the only handle on those ships.
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
without reflection. It lives at `src/sic_admin_xo/HackProductionReport.java` despite its package
declaration — the `src` source root is flat, so javac does not care.

### Optional dependencies on other mods

`AoTDProductionHook` implements an interface from another mod's jar. The pattern that keeps that
safe when the mod is absent: put the foreign type in its own class, reference it only from a
bare `invokestatic` guarded by `Global.getSettings().getModManager().isModEnabled(...)` and
wrapped in `catch (Throwable)`. JVM constant-pool resolution is lazy, so the class is never
loaded and never fails to verify. Add the foreign jar to `modDependencies` — **not** to
`mod_info.json`, whose `dependencies` are hard runtime requirements that block the player from
enabling the mod. `mod_info.json` should list only Second-in-Command.

## Releasing

The built jar is **not** committed (`/jars/` is gitignored) and there is no release CI — a
runner has no Starsector install, and neither the game API nor the mod jars the build compiles
against are redistributable. Releases are built and published from a local checkout with
`./gradlew packageMod` followed by `gh release create`; the README has the steps.

`packageIncludes` / `packageIncludeExtensions` / `packageExcludes` in `build.gradle.kts` are the
single definition of what goes in the zip. The zip's inner folder comes from the project
directory name, so it currently extracts as `SiC-Administration/`.

Version bumps have to keep three things in agreement: `mod_info.json` (`version`),
`sic_admin_xo.version` (`modVersion`, plus `directDownloadURL`, which pins both the release tag
and the zip name), and the git tag passed to `gh release create`.

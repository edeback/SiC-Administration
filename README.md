# Administration XO for Second-in-Command

## Description

This adds an Administration XO to Second-in-Command. The intent is to provide a colony-focused XO that provides a lot of late-game utility to players with multiple colonies. Right now SiC avoids all colony-related abilities, so this exists to fill that gap.

***Requires the Second-in-Command mod (if that wasn't obvious)***

The Admin XO is a logistics specialist with the following abilities:

*Innate skill:*

**Multitasking**
>Can personally administer two additional colonies without penalties

---

**Charismatic Leadership**

*Affects: governed colonies*

>Population growth increased by +10
>
>+20% accessibility

**Hyperspace Buoys**

*Affects: fleet*

>Controlled nav buoys affect hyperspace within 10 ly
>
>Controlled nav buoys reduce terrain movement penalty by 50% (base) / 30% (makeshift)

**Hyperspace Sensors**

*Affects: fleet*

>Controlled sensor relays affect hyperspace within 10 ly
>
>Controlled sensor relays reduce detected-at range by 50% (base) / 30% (makeshift)

**Supply Routes**

*Affects: fleet*

>Supply and fuel costs are reduced by up to 80% depending on how close you are to a colony, diminishing to zero at 10 light-years.

---
*(Must have 2 skills from above)*

**Business Acumen**

*Affects: governed colonies*

>+25% income

**Entrenchment**

*Affects: governed colonies*

>Increases ground defenses by x2
>
>Increases fleet size by x1.5

**Optimized Shipbuilding**

*Affects: all colonies*

>+50% maximum value of custom ship and weapon production per month
>
>+20% ship quality factionwide

*Affects: Fleet*

>Able to build 1 more permanent hullmod into ships you custom produce

---
*(Must have 4 skills from above)*

**Vertical Integration**

*Affects: governed colonies*

>All industries supply 1 more unit of all the commodities they produce
>
>All industries demand 1 less unit of all the commodities they require

**Miniaturized Factories**

*Affects: governed colonies*

>+1 Industry Slot
>
>-20% colony upkeep cost

---

## Building

Requires a Starsector install with this repo checked out into its `mods/` folder, which is
where `starsectorPath` in `build.gradle.kts` expects to find the game. Second-in-Command must
be installed too, since the build compiles against its jar.

```bash
./gradlew jar
```

`./gradlew runStarsector` builds and launches the game; `runStarsectorNoLauncher` skips the
launcher window.

Note that `./gradlew clean jar` as a single command fails: the build stages the Starsector API
into `build/` at configuration time, and `clean` then deletes it before compilation runs. Run
the two as separate invocations.

## Releasing

The mod jar is not committed, so releases are built and published from a local checkout rather
than by CI. `packageMod` is the single source of truth for what goes in the zip - see
`packageIncludes` in `build.gradle.kts`.

1. Bump the version in `mod_info.json` and `sic_admin_xo.version`. They must match, and
   `directDownloadURL` in the `.version` file has to point at the tag you are about to create.
2. Build the zip:

   ```bash
   ./gradlew packageMod
   ```

3. Tag and publish:

   ```bash
   gh release create v0.2.0 SiCAdminXO.zip --title v0.2.0 --generate-notes
   ```

`gh release create` creates the tag if it does not exist yet, but it tags whatever the remote's
default branch currently points at - so commit and push first.

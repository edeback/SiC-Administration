# Development

Building and releasing SiC Admin XO. This file is excluded from the packaged zip.

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

1. Bump the version in `mod_info.json` and `sic_admin_xo.version`. They must match.
   `directDownloadURL` in the `.version` file points at `releases/latest/download/SiCAdminXO.zip`,
   so it needs no change as long as the zip name stays the same.
2. Build the zip:

   ```bash
   ./gradlew packageMod
   ```

3. Tag and publish:

   ```bash
   gh release create vX.Y.Z SiCAdminXO.zip --title vX.Y.Z --generate-notes
   ```

`gh release create` creates the tag if it does not exist yet, but it tags whatever the remote's
default branch currently points at - so commit and push first.

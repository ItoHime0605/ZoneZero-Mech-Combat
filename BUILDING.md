# Building ZoneZero Mech Combat (unverified)

**A Gradle prototype JAR now builds successfully, but this is not a stable or gameplay-tested release.** Requires Java 21, Gradle and access to the NeoForge / Minecraft dependency repositories.

For local compilation, place the original mod files in `libs/` as:

- `libs/sable.jar` — Sable 2.0.6 NeoForge 1.21.1
- `libs/sable-companion.jar` — Sable 2.0.6 embedded Sable Companion 1.6.0
- `libs/create.jar` — Create 6.0.10 NeoForge 1.21.1
- `libs/let_it_walk.jar` — Let It Walk! 0.1.5

The dependencies above are compile inputs, not redistributed source files. You do **not** need the newer Sable Hostile Takeover car modules, Drive-by-Wire, Offroad, Simulated, or SableCat.

Run `gradle build` from this repository with the NeoForge ModDev environment. Only after compilation and client/server startup tests should you copy a JAR from `build/libs/` to `mods/`.

## Source-only tests

`MechHealthMath` is a pure Java class; you can compile it with the included `MechHealthMathTest` using any Java 21 JDK, independent of Minecraft dependencies.

## Current status

- [x] Sable 2.0.6 and Let It Walk 0.1.5 JAR method signatures inspected
- [x] Shared HP + material sums + Sable user-data tag persistence source
- [x] Mob hit rerouting to shared HP, replacing original block-breaking
- [x] Low HP sparks and low gear speed modifier (Mixin source)
- [x] Zero-HP movement guard (Mixin source)
- [x] Repair item registration, right-click repair and recipe (source)
- [x] Original upstream MIT license + NOTICE retained
- [x] NeoForge / Gradle compile successful (GitHub Actions)
- [ ] Mixin successfully applied at runtime
- [ ] Damage and repair independently tested on moving multipart walkers
- [ ] Multiplayer/saving and performance verified
- [ ] Actual ragdoll/collapse behavior validated

### Performance note

The v1.0.1 block nearest-target search still traverses many blocks in each Sable sublevel, so do not enable on a high-mob-count server until benchmarked.

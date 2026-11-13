# Building (experimental)

Requires **Java 21**, **Gradle**, and internet access for NeoForge and Mojang dependencies.

Place the **original, unmodified** dependency JARs in the following local paths:

- `libs/sable.jar` — Sable NeoForge 1.21.1 **2.0.6**
- `libs/sable-companion.jar` — extract `META-INF/jarjar/sable-companion-common-1.21.1-1.6.0.jar` from the Sable 2.0.6 JAR

The `libs/` JARs are local compile dependencies and are intentionally **not** committed to GitHub. No separate car/vehicle libraries are required by this reduced source tree.

Run `gradle build` with a compatible Gradle version and an available dependency network. Build output would be in `build/libs/`.

**Status:** source-only prototype. No successful Gradle/NeoForge build or server test has yet been completed. Do not install in a production world.

If you also have the original Sable Mob Interactions installed, remove it before testing to avoid duplicate NPC targeting goals.

## Intended behavior

- Only Sable structures with a `let_it_walk:walker_core` block are attackable.
- Vanilla zombie variants attempt to strike adjacent armor blocks.
- `undead_revamp2` hostile mobs get a generic target; their actual attack behavior requires testing.
- A walker core is not broken in this prototype.
- Per-block damage is not the future shared HP system.
- No enemy cars, hostile pilots, world-structure spawners, or additional vehicle mod dependencies.

## Performance

The inherited v1.0.1 block search can scan entire sublevel chunks. Stress test against the server's five-zone zombie populations before enabling it widely.

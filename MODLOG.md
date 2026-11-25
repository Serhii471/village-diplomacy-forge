# v0.1 guard integration package

## Current release candidate — 2026-11-25

- Release base: `6a88cf80b64909acf84a45da323c803860e8cdaf` (standalone v0.1 integration merge).
- Release branch: `release/v0.1`.
- The guard package remains isolated from resident, profession, recruitment, and garrison functionality planned for 0.2.x.
- The confirmed guard-skin redistribution permission is recorded in `docs/development/v0.1-guard-assets.md` and `CREDITS.txt`.
- The real Forge client exposed a missing Village Guard Spawn Egg item model; `2a145a8` adds the standard spawn-egg model and makes the package verifier require it.
- Final automated evidence: 89 unit tests passed, all 25 required guard GameTests passed, and the dedicated-server/two-client guard harness passed all phases including full server restart/reconnect.
- Real-client smoke evidence: metadata, item icon, leadership, specialization, discovery, all four Diplomacy Table tabs, guard spawn/rendering, and save/reload passed. The final client log contained no new error, exception, missing-model, or crash entry.
- Verified reproducible JAR: `build/libs/villagediplomacy-0.1.0.jar`, 314108 bytes, SHA-256 `095c4f0063811fe88fdc792cea433319d9fec52d0d3a2ce83d2a0926d8cce8f3`; two forced rebuilds produced identical bytes.
- Remaining human acceptance is intentionally separate: full partial-handover/reward presentation, agreements/alliances/routes, and combat-animation rehearsal.

## Original package scope — 2026-11-19

- Prepare four independent local commits for Arthur: patrol/diplomacy, hostile targeting, regional textures/renderer, guard verification.
- Original base: `daaf840b61f2b72dd946e3e4559aea3def095e2c` (`origin/master` at preparation time).
- Original branch: `codex/v0.1-guards`, in an isolated managed worktree. Preserve `2nd-test` (`830d027`), local `master`, local `test`, and all unrelated working-tree changes in the original checkout.
- The original preparation did not include merging into master, pushing, or creating a release/tag.
- Source guard behavior from `af6c861`, hostility from `29430e0`, and the pre-garrison uniforms/renderer from `02c4bf0`. Adapt only where the current shared core requires it.
- Do not import `ResidentData`, resident appearances, recruitment, garrison menus/networking, or their tests. Keep shared trade, menus, localization and settlement persistence unchanged.
- Runtime: existing Forge loader API, Minecraft 1.20.1 / Forge 47.4.28 / Java 17. No new production dependency or asset generation is needed.
- Run existing unit tests after each production commit, then isolated guard GameTests, optional disposable-server/two-client uniform checks, and a regular release build/package inspection. Record limitations separately from verified results.
- Never open original user saves or reuse normal run directories for automated verification. Test worlds/configuration live only under this worktree's ignored `build/` directory.

## Historical verification on the original base

The following results were recorded for the original `daaf840`-based package and were not rerun after replaying the commits on `10d4fbd`.

### C1 — patrol and diplomacy

- Ported only `VillageGuardEntity` from pre-garrison `af6c861`. No resident snapshot, recruitment, menu, texture or shared-core dependency is added.
- Baseline `test build --no-daemon --offline`: 89 unit tests passed. C1 `test --no-daemon --offline`: 89 unit tests passed.

### C2 — hostile mob targets

- Ported only the two auto-registered `entity/ai` event/goal classes from `29430e0`. No changes to the mod entrypoint, trade, menus or networking.
- `test --no-daemon --offline`: 89 unit tests passed. Dedicated guard regressions are prepared in C4.

### C3 — regional uniforms

- Ported the pre-garrison renderer, synchronized specialization field, and four 64×64 runtime PNGs from `02c4bf0`. Armor and outer skin layers remain enabled; missing homes select the base texture.
- `test --no-daemon --offline`: 89 unit tests passed. PNG headers, hashes and absence of resident/garrison references were checked.
- The original package did not record base-skin redistribution permission. Serhii confirmed the necessary rights on 2026-11-24; see `docs/development/v0.1-guard-assets.md`.

### C4 — reproducible verification and handoff

- Added only guard GameTests (25 cases), the original arena, optional two-client skin harness, isolated Gradle configuration and packaging/verification scripts. Default release builds exclude both optional source sets.
- Kept the shared build change to one `apply from` statement. The applied verification script resolves ForgeGradle's lazy launch tokens through its plugin classloader; a compile-time class lookup unavailable in applied scripts was corrected before launching clients.
- New-base `test build --no-daemon --offline`: PASS; all 89 existing unit tests have zero failures/errors/skips. Guard GameTest server: all 25 required scenarios passed in a fresh world.
- Two independent Forge dev clients and a dedicated localhost server passed initial, changed, removed-home and full server restart/reconnect phases. All four Java test processes exited normally. Actual production renderer paths, four skins, outer layers and real chainmail were checked. Initial and reloaded raw Minecraft screenshots were visually inspected.
- Regular JAR: `build/libs/villagediplomacy-0.1.0.jar`, 297231 bytes, SHA-256 `24ea2a66fbc295b25caf2577e87c4757e7048676a4904935d219db68bf12ee42`. Exact four PNGs present; GameTest/harness code, test arenas and resident/garrison assets/classes absent. Trade screens and DiplomacyNetwork retained.
- Full diplomacy/trade presentation playthrough and combat animations were not exercised in the historical package. This candidate was not a public release.
- Verification evidence is generated in ignored `build/`; the handoff export includes only four patches, a Git bundle, candidate JAR, concise reports and two screenshots. It excludes launch manifests (which contain local environment variables), worlds, configs, logs and caches.

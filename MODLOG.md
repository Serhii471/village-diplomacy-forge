# v0.1 guard integration package

## Authorized scope — 2026-11-19

- Prepare four independent local commits for Arthur: patrol/diplomacy, hostile targeting, regional textures/renderer, guard verification.
- Base: `daaf840b61f2b72dd946e3e4559aea3def095e2c` (`origin/master` at preparation time).
- Work in `codex/v0.1-guards`, in an isolated managed worktree. Preserve `2nd-test` (`830d027`), local `master`, local `test`, and all unrelated working-tree changes in the original checkout.
- No merge into master, push, release/tag creation, or message to Arthur is part of this preparation.
- Source guard behavior from `af6c861`, hostility from `29430e0`, and the pre-garrison uniforms/renderer from `02c4bf0`. Adapt only where the current shared core requires it.
- Do not import `ResidentData`, resident appearances, recruitment, garrison menus/networking, or their tests. Keep shared trade, menus, localization and settlement persistence unchanged.
- Runtime: existing Forge loader API, Minecraft 1.20.1 / Forge 47.4.28 / Java 17. No new production dependency or asset generation is needed.
- Run existing unit tests after each production commit, then isolated guard GameTests, optional disposable-server/two-client uniform checks, and a regular release build/package inspection. Record limitations separately from verified results.
- Never open original user saves or reuse normal run directories for automated verification. Test worlds/configuration live only under this worktree's ignored `build/` directory.

## C1 — patrol and diplomacy

- Ported only `VillageGuardEntity` from pre-garrison `af6c861`. No resident snapshot, recruitment, menu, texture or shared-core dependency is added.
- Baseline `test build --no-daemon --offline`: 89 unit tests passed. C1 `test --no-daemon --offline`: 89 unit tests passed.

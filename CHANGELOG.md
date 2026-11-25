# Changelog

All notable changes to Village Diplomacy are documented in this file.

## 0.1.0 Release candidate

### Added

- Persistent settlements with leaders, terrain specializations, local wood selection, and world-level saved data.
- Day-based settlement production and capacity-limited virtual warehouses.
- Trial delivery and exchange contracts with stock reservation, partial progress, deadlines, failure handling, and one-time rewards.
- Server-authoritative physical handover of ordinary Minecraft items at settlement Bells.
- Diplomacy table GUI with settlement, relations, trade, and route tabs.
- Trade-agreement and diplomatic-alliance lifecycles with eligibility rules and player or server decisions.
- Persistent trade routes, route maps, configurable road signposts, and marked-route reward bonuses.
- Leadership Seal, Diplomatic Scroll, Diplomacy Table, Route Signpost, and survival recipes.
- English and Ukrainian localization.
- Standalone v0.1 settlement guards with bounded patrols, diplomacy-aware targeting, hostile-mob reactions, synchronized regional uniforms, and four 64×64 runtime textures.
- Isolated guard GameTests and optional client/server rendering harnesses that are excluded from regular release builds.
- Automated unit coverage for settlement persistence, production, contracts, handover, agreements, alliances, routes, snapshots, and network action IDs.

### Changed

- Replaced the early sealed-cargo design with direct transport of ordinary Minecraft item stacks.
- Bounded menu snapshots to remain below the Forge network payload budget.
- Updated GUI and route-signpost textures with credited CC0 sources.
- Listed both project contributors in mod metadata.
- Removed machine-specific IntelliJ run configurations; ForgeGradle now generates them locally.

### Fixed

- Isolated warehouse reservations between contracts.
- Prevented duplicate contract rewards.
- Preserved delivered goods in destination storage.
- Preserved selected trade-offer state across GUI refreshes.
- Prevented large menu action IDs from being truncated in vanilla button packets.
- Added the missing item model for the Village Guard Spawn Egg, removing the missing-model warning and restoring its inventory icon.

### Validation completed

- 89 unit tests passed with zero failures, errors, or skipped tests.
- All 25 required guard GameTests passed in a fresh disposable world.
- A localhost dedicated server and two Forge clients passed guard texture, armor-layer, live specialization, fallback, reconnect, and full server-restart checks.
- The regular JAR passed package inspection: all four 64×64 guard textures and the spawn-egg model are present; GameTest, harness, resident, and garrison content are absent.
- A real Forge client passed mod metadata, item/model, leadership, settlement discovery, Diplomacy Table, guard spawning, and save/reload smoke checks without a new error or stack trace in the final client log.

### Human acceptance still required

- Rehearse partial contract handover, one-time reward, agreements, alliances, route/map/signpost flow, and guard combat animation using a clean copy of the presentation world.
- Perform the final two-person presentation rehearsal before publishing the GitHub Release.

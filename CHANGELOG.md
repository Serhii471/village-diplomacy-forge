# Changelog

All notable changes to Village Diplomacy are documented in this file.

## 0.2.2 Unreleased

- Add the village guard spawn egg's inventory model using the vanilla tinted egg template.
- Explain the diplomacy table's placement rules in its item tooltip and preserve placement or access errors in chat.
- Show where to obtain a configured route map when a creative inventory copy is blank.

## 0.2.1 Unreleased

- Show the next available step in the contract handover screen.
- Render villagers with regional outfits for every vanilla profession and stable identities.
- Train, cancel training for, and demobilize residents through the diplomacy table's Garrison screen. Training preserves the villager's identity and uses supplied gear.
- Keep the Garrison action on the Settlement tab so trade, diplomacy, and route controls remain accessible.

## 0.1.0 Unreleased

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

### Release validation still required

- Run the complete unit, package, guard GameTest, single-player, dedicated-server, and save/reload checks.
- Rehearse the presentation using a clean copy of the prepared demo world.

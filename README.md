# Village Diplomacy

Village Diplomacy is a Minecraft Forge 1.20.1 mod that turns villages into persistent settlements with leaders, resource specializations, physical trade, diplomatic agreements, alliances, routes, and guards.

Version `0.1.0` is an integrated release candidate. The diplomacy/trade core and the standalone guard package have passed the automated suite, guard GameTests, a two-client guard-rendering check, and a real-client single-player smoke test. The full presentation rehearsal and an end-to-end real-client trade-contract run remain release acceptance steps.

## Requirements

- Java 17
- Minecraft 1.20.1
- Minecraft Forge `1.20.1-47.4.28`
- No additional mods are required.

## Installation

### Client and single-player

1. Install Java 17 and Minecraft Forge `1.20.1-47.4.28`.
2. Launch the Forge profile once, then close Minecraft.
3. Copy `villagediplomacy-0.1.0.jar` into the active profile's `mods` folder.
4. Remove older Village Diplomacy JARs from that folder; keep only one version.
5. Launch the Forge profile and confirm that **Village Diplomacy 0.1.0** appears in the Mods menu with authors **Arthur, Serhii**.

Default `mods` locations:

- Windows: `%APPDATA%\.minecraft\mods`
- Linux: `~/.minecraft/mods`
- macOS: `~/Library/Application Support/minecraft/mods`

### Dedicated server

Install the same Forge version on the server, place the exact same Village Diplomacy JAR in the server's `mods` folder, and install that JAR on every connecting client. Back up the world before installing, updating, or removing the mod.

For complete Windows/Linux/macOS, server, update, uninstall, first-start, and troubleshooting instructions, see [`INSTALLATION_UA.md`](INSTALLATION_UA.md) (Ukrainian).

## Main components in version 0.1

| Component | What it provides |
|---|---|
| Settlement core | Persistent settlements centered on Bells, server-owned saved data, leaders, known settlements, and relations. |
| Leadership | A two-step Leadership Seal confirmation, one-settlement leadership limit, and server-side permission checks. |
| Terrain specialization | Plains, mountain, forest, or mixed profiles determined from the terrain around the settlement. |
| Production and warehouses | Day-based virtual production, bounded storage, specialization resources, and unloaded-time progress. |
| Discovery | Diplomatic Scroll discovery with coordinates, distance, leader, resources, specialization, and relations. |
| Diplomacy Table | Four GUI tabs for settlement data, relations, trade, agreements, alliances, and routes. |
| Physical trade | Delivery and exchange offers, ordinary Minecraft item stacks, partial handover, deadlines, reputation, and one-time rewards. |
| Trade agreements | Proposal, acceptance, rejection, withdrawal, termination, persistence, and server-side eligibility checks. |
| Diplomatic alliances | Alliance eligibility based on relations, reputation, trade, and successful deliveries. |
| Routes | Persistent routes, Trade Route Maps, configurable Route Signposts, direction/distance display, and route bonuses. |
| Settlement guards | Settlement homes, bounded patrols, diplomacy-aware targeting, hostile-mob reactions, armor rendering, persistence, and four regional uniforms. |
| Localization and configuration | English and Ukrainian text plus server configuration for specialization, production, warehouse, and contract balance. |

Version 0.1 intentionally does **not** include human resident models, professions, recruitment, training, or garrison management. Those systems belong to the 0.2.x development line.

## Main items and blocks

| Item | Action |
|---|---|
| Leadership Seal | Right-click a Bell twice within 10 seconds to confirm leadership. One player can lead one settlement. |
| Diplomatic Scroll | Right-click a foreign Bell to discover the settlement and display its information. |
| Diplomacy Table | Place within the registered settlement and no farther than 32 blocks from its central Bell. |
| Trade Route Map | Records both route endpoints and refreshes its authoritative route status. |
| Route Signpost | Bind to a known settlement and display direction, distance, relations, and route state. |
| Village Guard Spawn Egg | Creative-only development item for spawning a guard. |

## Development setup

```powershell
.\gradlew.bat genIntellijRuns
.\gradlew.bat test build --no-daemon
```

Import the project as a Gradle project. ForgeGradle generates machine-local run configurations, so `.idea/runConfigurations` is intentionally not tracked.

## Verification

The regular unit and package check is:

```powershell
.\gradlew.bat test build --no-daemon
```

The verified candidate passed 89 unit tests, 25 required guard GameTests, a localhost dedicated-server/two-client guard check with a full server restart, package inspection, and a real Forge-client smoke test including save/reload. The remaining human acceptance scope is the complete presentation flow, especially partial contract handover, one-time reward, agreements, alliances, routes, and combat animation. Follow [`V0.1_MANUAL_TEST_GUIDE_UA.md`](V0.1_MANUAL_TEST_GUIDE_UA.md) and record the exact commit and JAR hash that were tested.

## Documentation

- [`INSTALLATION_UA.md`](INSTALLATION_UA.md) — complete installation, server, update, uninstall, first-start, and troubleshooting guide for users in Ukrainian.
- [`docs/design/v0.1-concept.md`](docs/design/v0.1-concept.md) — approved gameplay scope and readiness criteria.
- [`docs/demo/v0.1-presentation-runbook.md`](docs/demo/v0.1-presentation-runbook.md) — deterministic demo-world setup and rehearsal sequence.
- [`V0.1_AUDIT_AND_COMPLETION_GUIDE_UA.md`](V0.1_AUDIT_AND_COMPLETION_GUIDE_UA.md) — team responsibilities and integration checklist in Ukrainian.
- [`V0.1_MANUAL_TEST_GUIDE_UA.md`](V0.1_MANUAL_TEST_GUIDE_UA.md) — complete manual release-candidate verification in Ukrainian.
- [`CHANGELOG.md`](CHANGELOG.md) — release-facing change history.

## Roadmap

- [ ] Diplomat NPC (quest giver, relation broker)
- [ ] Courier / Trade caravan AI
- [ ] Autonomous events (raids, siege, allied reinforcements)
- [ ] Village auto-detection on world load
- [ ] Human residents, professions, recruitment, and garrison management (`0.2.x`)

## Authors

- Arthur — settlement, production, trade, diplomacy, route, GUI, networking, and integration work.
- Serhii — initial Forge scaffold, guard systems, resident and garrison research, and visual assets.

## License

Project code is released under the MIT license. Third-party and CC0 asset credits are listed in `CREDITS.txt`.

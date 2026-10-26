# Village Diplomacy

Village Diplomacy is a Minecraft Forge 1.20.1 mod that turns villages into persistent settlements with leaders, resource specializations, physical trade, diplomatic agreements, alliances, routes, and guards.

Version `0.1.0` is currently in release-candidate validation. The diplomacy/trade core and the standalone guard package are integrated; the complete automated, single-player, dedicated-server, save/reload, and presentation checks must still be run on the merged commit before tagging.

## Requirements

- Java 17
- Minecraft 1.20.1
- Minecraft Forge `1.20.1-47.4.28`

## Development setup

```powershell
.\gradlew.bat genIntellijRuns
.\gradlew.bat test build --no-daemon
```

Import the project as a Gradle project. ForgeGradle generates machine-local run configurations, so `.idea/runConfigurations` is intentionally not tracked.

## Version 0.1 scope

- **Settlements and leadership** — claim a settlement at its central Bell, with all leadership permissions checked on the server.
- **Terrain specializations** — plains, mountain, forest, and mixed settlements receive persistent production profiles.
- **Virtual production and warehouses** — capped resource production continues using saved game time without loading settlement chunks.
- **Discovery** — use a Diplomatic Scroll on a foreign Bell to learn its coordinates, resources, leader, relations, and distance.
- **Diplomacy table** — inspect server data, offers, contract history, relations, agreements, alliances, and routes through four GUI tabs.
- **Physical trade** — accept delivery or exchange contracts, carry ordinary Minecraft item stacks, hand them over partially, and claim each reward once.
- **Trade agreements** — propose, accept, decline, withdraw, and terminate persistent agreements with server-side eligibility checks.
- **Diplomatic alliances** — unlock alliance benefits after trade, reputation, and successful-delivery requirements are met.
- **Routes and maps** — create persistent routes, issue route maps, and configure road signposts with direction and distance labels.
- **Settlement guards** — bounded patrol, diplomacy-aware targeting, hostile-mob reactions, and four specialization-aware regional textures.

## Main items and blocks

| Item | Action |
|---|---|
| Leadership Seal | Right-click a Bell twice within 10 seconds to confirm leadership. One player can lead one settlement. |
| Diplomatic Scroll | Right-click a foreign Bell to discover the settlement and display its information. |
| Diplomacy Table | Place within the registered settlement and no farther than 32 blocks from its central Bell. |
| Trade Route Map | Records both route endpoints and refreshes its authoritative route status. |
| Route Signpost | Bind to a known settlement and display direction, distance, relations, and route state. |
| Village Guard Spawn Egg | Creative-only development item for spawning a guard. |

### Residents and garrison (0.2.0)
- **Human residents** — vanilla villagers retain their professions, trading and breeding while using human models, regional clothing and stable identity palettes.
- **Resident recruitment** — open the diplomacy table's Garrison, supply a chosen sword, chestplate and six bread, and train an adult unemployed resident for one game day. Queued recruits count toward the 25% military cap; at least four adult civilians must remain.
- **Reversible service** — the soldier retains the original resident's name and face. Cancel training to refund supplies, or demobilize a recruited soldier to restore the original villager and return current equipment once. Training and origin data survive saving/loading.

See [resident and garrison behavior, screenshots and verification](docs/development/residents-garrison.md).


## Verification

The regular unit and package check is:

```powershell
.\gradlew.bat test build --no-daemon
```

The release is not considered complete until the integrated candidate passes its guard GameTests, a single-player smoke test, a dedicated-server smoke test, save/reload verification, and the complete presentation rehearsal. Follow [`V0.1_MANUAL_TEST_GUIDE_UA.md`](V0.1_MANUAL_TEST_GUIDE_UA.md) and record the exact commit and JAR hash that were tested.

## Documentation

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

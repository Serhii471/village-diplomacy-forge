# Village Diplomacy

Village Diplomacy is a Minecraft Forge 1.20.1 mod that turns villages into persistent settlements with leaders, resource specializations, physical trade, diplomatic agreements, alliances, routes, and guards.

Version `0.2.1` brings together the diplomacy and trade core, settlement guards, human-looking residents, and garrison management.

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

## Diplomacy, trade, and guards

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

## Residents and garrison

- **Human residents** — vanilla villagers retain their professions, trading and breeding while using human models, regional clothing and stable identity palettes.
- **Recognizable professions** — all 13 vanilla trading professions have individual outfits and pixel accessories: smiths wear tool aprons, librarians carry a book motif and glasses, farmers wear straw caps, and other trades have their own colors and emblems. Three regions and three identity palettes provide 135 outfits, including unemployed residents and nitwits. Clothing updates when a villager changes profession.
- **Resident recruitment** — open the diplomacy table's Garrison, supply a chosen sword, chestplate and six bread, and train an adult unemployed resident for one game day. Queued recruits count toward the 25% military cap; at least four adult civilians must remain.
- **Reversible service** — the soldier retains the original resident's name and face. Cancel training to refund supplies, or demobilize a recruited soldier to restore the original villager and return current equipment once. Training and origin data survive saving/loading.

See [resident and garrison behavior](docs/development/residents-garrison.md) and [profession outfit details](docs/development/resident-professions.md).

## Verification

The regular unit and package check is:

```powershell
.\gradlew.bat test build --no-daemon
```

Run the isolated Forge GameTests for guards and recruitment with `.\gradlew.bat -PguardGameTests runGameTestServer --offline`. The client scenarios in the documentation cover the corresponding in-game rendering and save/reload checks.

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

## Authors

- Arthur — settlement, production, trade, diplomacy, route, GUI, networking, and integration work.
- Serhii — initial Forge scaffold, guard systems, resident and garrison features, and visual assets.

## License

Project code is released under the MIT license. Third-party and CC0 asset credits are listed in `CREDITS.txt`.

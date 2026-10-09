# 🏹 Projectile and ranged triggers

These triggers separate the shooter (`SOURCE`) from the projectile or other carrier (`SKILL`).

Each trigger section below documents the runtime context created by the current implementation.

| Context | Meaning |
| --- | --- |
| `PLAYER` | The player whose active enchantment is being evaluated. |
| `SOURCE` | The actor or owner responsible for the event. |
| `SKILL` | The direct carrier/intermediate entity, such as a projectile, hook, firework, or Warden. |
| `TARGET` | The final affected or inspected entity. It may be unavailable for some events. |
| `BLOCK` | The event block, when the trigger supplies one. |
| `LOCATION` | The main event location used by location-aware conditions and abilities. |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | The item and equipment slot that selected the active enchantment. |
| `EVENT` | The underlying Bukkit/Paper event. Trigger-specific state not copied into another field remains available through the event-aware implementation. |

When a role is marked **player**, it resolves to the same entity as `PLAYER`. A missing role resolves to `null`; conditions or abilities requiring it will fail or skip safely.

## `on-shoot`

Runs for `EntityShootBowEvent` when the shooter is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` | shooter |
| `SKILL` / `TARGET` | spawned projectile entity |
| `LOCATION` | Projectile location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Bow/crossbow and event hand |
| Extra context | `bow_force`, `bow`, `consumable`, `hand`, `projectile` |
| Difference | Does not start EnchantmentReform projectile continuation |

## `on-shoot-bow`

Uses the same initial context as `on-shoot`, then starts projectile tracking for the final `SKILL` entity when it is a projectile.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` | shooter |
| `SKILL` / `TARGET` | Original projectile, or a replacement entity returned by abilities |
| `LOCATION` | Projectile location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Bow/crossbow and event hand |
| Extra context | `bow_force`, `bow`, `consumable`, `hand`, `projectile` |
| Difference | Enables `on-projectile-tick` and tracked `on-projectile-hit` continuation |

## `on-load-crossbow`

Runs when a player loads a crossbow.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Crossbow and event hand |
| Extra behavior | Cancelling the power can cancel/deny the underlying Paper event where supported |

## `on-projectile-launch`

Runs for a projectile launch whose shooter is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` | shooter |
| `SKILL` / `TARGET` | launched projectile |
| `LOCATION` | Projectile location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Current main-hand item / `HAND` |
| Extra context | `projectile` |
| Note | This generic launch trigger does not itself create tracked continuation |

## `on-projectile-tick`

Runs once per tick for a projectile tracked by `on-shoot-bow`.

| Context | Value |
| --- | --- |
| `PLAYER` | Player who launched/captured the projectile power |
| `SOURCE` | Current shooter entity |
| `SKILL` / `TARGET` | Tracked projectile |
| `LOCATION` / `BLOCK` | Projectile location and block at that location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Captured firing item and slot |
| `TICK` | Runtime tick |
| Extra context | `projectile`; supports `interval` |

## `on-projectile-hit`

Runs when a projectile hits after tracked data is resolved, or when a player-shot projectile can be resolved directly.

| Context | Value |
| --- | --- |
| `PLAYER` | Captured owner or direct player shooter |
| `SOURCE` | Resolved shooter |
| `SKILL` | Projectile |
| `TARGET` | Hit entity; missing when a block is hit |
| `BLOCK` | Hit block; missing when an entity is hit |
| `LOCATION` | Center of hit block, hit entity location, or projectile location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Captured firing item and slot when tracked |
| Extra context | `projectile`; tracking ends when this event is handled |

## `on-riptide`

Runs when a player uses Riptide.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Riptide item / `HAND` |
| Extra context | The `PlayerRiptideEvent` remains available |

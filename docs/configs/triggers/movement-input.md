# 🕹️ Movement and input triggers

These triggers expose player movement or input state. High-frequency powers should use strict conditions and conservative abilities.

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

## `on-move`

Runs only when `PlayerMoveEvent.hasChangedBlock()` is true.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | moving player |
| `FROM` / `TO` | Previous and destination locations |
| `LOCATION` | Destination location |
| `BLOCK` | Block at the destination |

## `on-input`

Runs when Paper reports a player input update.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | Forward, backward, left, right, jump, sprint, and sneak input state remains on `PlayerInputEvent` |

## `on-jump`

Runs for Paper's player jump event.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | jumping player |
| `FROM` / `TO` | Jump start and destination |
| `LOCATION` | Destination |
| `BLOCK` | Block at the destination |

## `on-toggle-flight`

Runs when a player toggles flight.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | New flight state remains on `PlayerToggleFlightEvent`; cancellation is applied to cancellable events |

## `on-toggle-sneak`

Runs when a player toggles sneaking.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | New sneak state remains on `PlayerToggleSneakEvent` |

# 🧱 Block and interaction triggers

These triggers usually keep all entity selectors on the player and provide the meaningful target through `BLOCK`, `LOCATION`, or `TRIGGER_ITEM`.

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

## `on-block-break`

Runs when a player breaks a block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Broken block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand tool / `HAND` |
| Extra context | `original_experience`, optional `internal_block_break`; mutable experience result |

## `on-block-damage`

Runs when a player begins damaging a block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Damaged block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Event item / `HAND` |

## `on-block-break-progress-update`

Runs for Paper block-break progress updates produced by a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand tool / `HAND` |
| Extra context | `block_break_progress` |

## `on-block-drop-item`

Runs when item entities are produced from a broken block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Block position and center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand tool / `HAND` |
| Extra context | `broken_block_state` preserves the pre-break block state; drop entities remain on the event |

## `on-block-place`

Runs when a player places a block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Placed block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Placed item and event hand |
| Extra context | Replaced block and placement details remain on the event |

## `on-inside-block`

Runs for Paper's inside-block event when the entity is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Block whose hitbox contains/processes the player and its center |

## `on-interact`

Runs for `PlayerInteractEvent`.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` | Clicked block, or missing for air interaction |
| `LOCATION` | Center of clicked block when present |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Event item and hand |
| Extra context | Action, clicked face, interaction result, and other details remain on the event |

## `on-consume`

Runs when a player consumes an item.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Consumed item and hand |

## `on-name-entity`

Runs for Paper's player-name-entity event.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | player |
| `TARGET` | Entity being named |
| `LOCATION` | Target entity location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Name tag from main or off hand when found |

## `on-vibration-receive`

Runs when a Sculk sensor is about to receive a vibration (game event) caused by a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | player who caused the vibration |
| `BLOCK` / `LOCATION` | Sculk sensor block and its location |
| Extra context | `GameEvent` remains on `BlockReceiveGameEvent`; cancelling prevents the sensor from activating, stopping the vibration from propagating to Sculk shriekers |

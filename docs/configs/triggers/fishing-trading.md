# 🎣 Fishing and trading triggers

These triggers expose the fishing hook, caught object, merchant, trade result, and mutable reward data.

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

## `on-fish`

Runs for every `PlayerFishEvent` state.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` | fishing player |
| `SKILL` | Fishing hook |
| `TARGET` | Caught entity or item entity; missing for states without a caught object |
| `LOCATION` | Hook location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Fishing rod and event hand |
| Extra context | `original_experience`; mutable fishing experience; event state and caught data remain on the event |

Use a fishing-state condition when a power should run only for states such as catching, biting, reeling, or failed attempts.

## `on-purchase`

Runs for Paper's player purchase event.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | buyer |
| `TARGET` | Merchant when it is an entity; otherwise missing |
| `LOCATION` | Merchant location when it is an entity |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Trade result / `HAND` |
| Extra context | Merchant recipe, uses, and purchase state remain on the event |

## `on-trade`

Runs for Paper's player trade event.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | trading player |
| `TARGET` | Merchant entity |
| `LOCATION` | Merchant location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Trade result / `HAND` |
| Extra context | Merchant recipe and trade result data remain on the event |

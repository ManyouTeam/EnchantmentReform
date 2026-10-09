# 🎯 Targeting and special-entity triggers

These triggers cover monster targeting, creeper priming, Enderman gaze aggression, phantom pre-spawn, and Warden anger.

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

## `on-target`

Runs when an entity selects a player as its target.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | targeted player |
| `SOURCE` / `SKILL` | targeting entity |
| `LOCATION` | Player location |
| Extra context | `target_reason`, `target_count`; cancelling removes the remembered target relation |

## `on-untag`

Runs when an entity that was remembered as targeting a player changes to a non-player or clears its target.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | previously targeted player |
| `SOURCE` / `SKILL` | entity that stopped targeting the player |
| `LOCATION` | Player location |
| Extra context | `target_reason` |

## `on-creeper-explode`

Runs for an explosion-prime event when the priming entity is a player or a creeper currently targeting a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | Player entity, or creeper's current player target |
| `SOURCE` / `SKILL` | Priming entity, normally the creeper |
| `LOCATION` | Priming entity location |
| Extra context | `original_radius`; mutable explosion radius |
| Note | This is not a generic explosion trigger and does not expose explosion yield |

## `on-enderman-attack-player`

Runs for Paper's Enderman attack-player event when an Enderman checks whether a player is looking directly at it. Cancelling prevents gaze-triggered aggression without preventing retaliation when the player attacks first.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | player being checked |
| `SOURCE` / `SKILL` | Enderman performing the check |
| `LOCATION` | Player location |
| Extra behavior | Cancelling prevents the Enderman from becoming hostile because of eye contact |

## `on-phantom-pre-spawn`

Runs for Paper's phantom pre-spawn event when the spawning entity is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player for whom the phantom would spawn |
| `LOCATION` / `BLOCK` | Proposed spawn location and its block |
| Extra behavior | Cancelling sets `shouldAbortSpawn` |

## `on-warden-anger-change`

Runs when Paper reports a Warden anger change whose target is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` | player who is the Warden's anger target |
| `SKILL` / `TARGET` | Warden |
| `LOCATION` | Warden location |
| Extra context | Old/new anger values remain on `WardenAngerChangeEvent`; mutable `warden_anger` modifier context |

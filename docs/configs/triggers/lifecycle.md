# ⏱️ Tick and lifecycle triggers

These triggers run periodically or when the player's active enchantment set changes.

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

## `on-tick`

Runs periodically for each online player with an active enchantment. Configure the interval inside the trigger section:

```yaml
powers:
  on-tick:
    interval: 20
    abilities: {}
```

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` | Block at the player's current location |
| `TICK` | Current runtime tick |
| Extra behavior | `interval` is clamped to at least 1 tick |

## `on-target-tick`

Runs periodically when the player is looking at an entity found by a 32-block entity ray trace.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | player |
| `TARGET` | Entity currently hit by the ray trace |
| `LOCATION` / `BLOCK` | Target location and the block at that location |
| `TICK` | Current runtime tick |
| Extra behavior | Does not run when no entity is found; supports `interval` |

## `on-spawn`

Runs one tick after a player joins, and also one tick after a player respawns.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `LOCATION` | Respawn location for the respawn path; normal player location on join |
| `BLOCK` | Block at the resolved location |
| Extra behavior | This is a player lifecycle trigger, not a generic entity-spawn trigger |

## `on-respawn`

Runs one tick after `PlayerRespawnEvent`.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | respawning player |
| `LOCATION` | Respawn location |
| `BLOCK` | Block at the respawn location |

## `on-activate`

Runs when an enchantment becomes active in an eligible equipment slot.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` | Block at the player's location |
| `TRIGGER_ITEM` | Item containing the enchantment that became active |
| `TRIGGER_SLOT` | Slot in which it became active |
| Extra behavior | Executes specifically for the transitioning enchantment |

## `on-deactivate`

Runs when an enchantment stops being active.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` | Block at the player's location |
| `TRIGGER_ITEM` | Previous item containing the enchantment |
| `TRIGGER_SLOT` | Slot from which it became inactive |
| Extra behavior | Executes specifically for the transitioning enchantment |

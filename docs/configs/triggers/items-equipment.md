# 🎒 Item, enchanting, and equipment triggers

These triggers focus on an item or equipment transition. Check `TRIGGER_ITEM` and `TRIGGER_SLOT` before assuming an entity target exists.

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

## `on-item-damage`

Runs when one of the player's items takes durability damage.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` | Damaged item |
| `TRIGGER_SLOT` | Slot found for that item |
| Extra context | `original_item_damage`; mutable item-damage result |

## `on-item-group-cooldown`

Runs for Paper's item cooldown-group event.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `LOCATION` | Player location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main/off-hand item matching the cooldown material when the event is `PlayerItemCooldownEvent`; otherwise may be missing |
| Extra context | Cooldown group and duration remain on the Paper event |

## `on-item-held`

Runs when the selected hotbar slot changes.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Item in the new hotbar slot / `HAND` |
| Extra context | Previous and new slot indexes remain on the event |

## `on-swap-hand`

Runs when main-hand and off-hand items are swapped.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Event's main-hand item / `HAND` |
| Extra context | Both resulting hand items remain on the event |

## `on-enchant-item`

Runs when a player enchants an item through `EnchantItemEvent`.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | enchanter |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Item being enchanted / `HAND` |
| Extra context | `original_experience` is the displayed level requirement; `enchantment_level_cost` is the 1/2/3 levels actually consumed; `original_lapis` is the selected lapis cost; mutable experience result; offered enchantments remain on the event |

## `on-elytra-boost`

Runs when a player uses a firework to boost Elytra flight.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | player |
| `SKILL` | Firework entity |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Firework item and event hand |

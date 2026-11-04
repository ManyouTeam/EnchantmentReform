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

Supported `count`: `events`.

Runs when a player breaks a block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Broken block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand tool / `HAND` |
| Extra context | `original_experience`, optional `internal_block_break`; mutable experience result |

## `on-block-damage`

Supported `count`: `events`.

Runs when a player begins damaging a block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Damaged block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Event item / `HAND` |

## `on-block-break-progress-update`

Supported `count`: `events`.

Runs for Paper block-break progress updates produced by a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand tool / `HAND` |
| Extra context | `block_break_progress` |

## `on-block-drop-item`

Supported `count`: `events`, `items`.

Runs when item entities are produced from a broken block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Block position and center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand tool / `HAND` |
| Extra context | `broken_block_state` preserves the pre-break block state; drop entities remain on the event |

## `on-block-place`

Supported `count`: `events`, `blocks`.

Runs when a player places a block.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Placed block and its center |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Placed item and event hand |
| Extra context | Replaced block and placement details remain on the event |

## `on-inside-block`

Supported `count`: `events`.

Runs for Paper's inside-block event when the entity is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` / `LOCATION` | Block whose hitbox contains/processes the player and its center |

## `on-interact`

Supported `count`: `events`.

Runs for `PlayerInteractEvent`.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `BLOCK` | Clicked block, or missing for air interaction |
| `LOCATION` | Center of clicked block when present |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Event item and hand |
| Extra context | Action, clicked face, interaction result, and other details remain on the event |

## `on-consume`

Supported `count`: `events`.

Runs when a player consumes an item.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Consumed item and hand |

## `on-name-entity`

Supported `count`: `events`.

Runs for Paper's player-name-entity event.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | player |
| `TARGET` | Entity being named |
| `LOCATION` | Target entity location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Name tag from main or off hand when found |

## `on-vibration-receive`

Supported `count`: `events`.

Runs when a Sculk sensor is about to receive a vibration (game event) caused by a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | player who caused the vibration |
| `BLOCK` / `LOCATION` | Sculk sensor block and its location |
| Extra context | `GameEvent` remains on `BlockReceiveGameEvent`; cancelling prevents the sensor from activating, stopping the vibration from propagating to Sculk shriekers |

## `on-brew`

Supported `count`: `events`, `items`. Runs on brewing completion, using the resulting potions and excluding unchanged bottles. `events` counts each batch once; `items` counts matching output quantities. Use `count: items` and `COUNTED_ITEM` matching to select specific potions. `TRIGGER_ITEM` is the first changed output; its slot is absent.

## `on-cook`

Supported `count`: `events`, `items`. Runs on furnace smelting completion, including furnaces, smokers, and blast furnaces. `events` counts each completion once; `items` counts matching output quantities. Use `COUNTED_ITEM` matching for food such as `COOKED_CHICKEN`; without filtering, other smelting outputs also count. `TRIGGER_ITEM` is the output, with no slot. Collecting existing output does not trigger it again. Campfires are excluded.

### Player attribution for brewing and cooking

Completion events have no player. After a click, shift-click, or drag, the plugin checks input slots and attributes the workstation to the player who actually added input. Opening the interface, adding fuel, or collecting output does not transfer attribution. Brewing inputs include bottle and ingredient slots; furnace input is its smelting slot.

The player's UUID is stored in the workstation PDC and survives restarts, without a long-lived player-object cache. Empty input slots clear attribution, as does automated inventory input such as a hopper. Events without a recorded online operator and cancelled events do not count. Shared workstations credit the last player who actually added input; individual item ownership is not tracked. `PLAYER` / `SOURCE` is the operator; `BLOCK` / `LOCATION` is the workstation. Available on Paper and Spigot.

## `on-craft-item`

Supported `count`: `events`, `items`. Runs when crafted output is successfully taken. `events` counts each crafting click once; `items` counts outputs, including shift crafting constrained by inventory capacity. Creative cloning does not count.

## `on-fertilize-block`

Supported `count`: `events`, `blocks`. Runs when player-applied bone meal successfully changes blocks. `events` counts uses; `blocks` counts changed blocks. `BLOCK` is the fertilized target; the original interaction supplies the item and hand. Non-player events do not count.

## `on-forge-item`

Supported `count`: `events`, `items`. Runs when output is successfully taken from a smithing table, including upgrades and trims. Previewing output does not count.

## `on-grindstone-item`

Supported `count`: `events`, `items`. Runs when output is successfully taken from a grindstone, including repairs and disenchanting. Inserting items or viewing the preview does not count.

## `on-milk-mob`

Supported `count`: `events`, `items`. Runs on a successful milk-bucket fill and supplies the interacted cow, mooshroom, or goat as `TARGET`. Failed, cancelled, and baby-animal interactions do not count.

## `on-honey-extract`

Supported `count`: `events`, `items`. Runs when a glass bottle harvests a full bee nest or beehive. A next-tick check confirms reduced honey and the bottle-use result. The trigger item is the glass bottle; output is a honey bottle.

## `on-honey-comb-extract`

Supported `count`: `events`, `items`. Runs when shears harvest honeycomb from a bee nest or beehive. Outputs come from the Paper shear-block event. Requires Paper with that event available.

## `on-anvil-repair`

Supported `count`: `events`, `items`. Runs when successfully collected anvil output has less durability damage. Renaming alone does not trigger it.

## `on-anvil-enchant`

Supported `count`: `events`, `items`. Runs when successfully collected anvil output adds an enchantment or increases its level, including enchanted-book merging. Unchanged enchantments do not count.

## `on-anvil-rename`

Supported `count`: `events`, `items`. Runs when successfully collected anvil output changes the custom name, including adding, editing, or clearing a name. Repairing or enchanting without a name change does not count.

Use `COUNTED_ITEM` to filter quantity-counted outputs. Smithing, grindstone, and anvil trigger items are outputs. One anvil action can independently trigger repair, enchant, and rename according to its actual changes. Inventory-output triggers check consumed inputs on the next tick; cancelled or unsuccessful collection does not count. Honey-bottle triggering happens after completion and cannot cancel the original harvest. All entries above except honeycomb support Paper and Spigot.

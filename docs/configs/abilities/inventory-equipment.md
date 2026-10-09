# 🎒 Inventory and Equipment Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `consume_food`
* `auto_feed`
* `enhance_equipment`
* `enhance_helditem`
* `replace_item`
* `shuffle_inventory`
* `change_item`
* `cost_price`
* `drop_item`
* `give_item`
* `give_loot_table_item`
* `preserve_inventory`
* `preserve_item`
* `preserve_experience`
* `use_on`

---

## `consume_food`

**Purpose:** Consumes player saturation and/or food, including fractional food accumulation.

**Context:** Default `target`: `TARGET`; selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `0.25` | Combined amount; supports `{food}` and `{saturation}`. |
| `saturation-first` | `true` | Consume saturation before food in combined mode. |
| `saturation-amount` | unset | Explicit saturation consumption. |
| `food-amount` | unset | Explicit food consumption. |

### Example

```yaml
type: consume_food
target: TARGET
amount: 1.5
saturation-first: true
```

### Behavior and limits

* Fractional food consumption is stored per player until it reaches a whole point.
* When either explicit amount exists, explicit mode controls actual reductions.

---

## `auto_feed`

**Purpose:** Finds and consumes the first eligible food item in a player's storage inventory through the NMS bridge.

**Context:** Default `target`: `SOURCE`; target must be a living online player with food below 20.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `threshold` | `10` | Maximum food level at which auto-feeding may run. |
| `health-not-full-threshold` | `threshold` | Threshold used when current health is below maximum. |
| `success-abilities` | empty | Child abilities executed after successful consumption. |

### Example

```yaml
type: auto_feed
target: SOURCE
threshold: 10
health-not-full-threshold: 16
success-abilities:
  sound:
    type: sound
    sound: ENTITY_GENERIC_EAT
```

### Behavior and limits

* Scans storage slots in order and uses the first edible item or item with a food component.
* Requires NMS `FINISH_USING_ITEM`; container remainders are given or dropped.
* On failed NMS use, the removed food item is returned.

---

## `enhance_equipment`

**Purpose:** Replaces selected armor pieces with newly created armor and random compatible enchantments.

**Context:** Default `target`: `SOURCE`; requires a living entity with equipment.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `armor-material` | `NETHERITE` | Material prefix combined with armor piece names. |
| `pieces` | all armor | List of `HELMET`, `CHESTPLATE`, `LEGGINGS`, and `BOOTS`. |
| `enchant.min-amount` | `1` | Minimum distinct enchantments. |
| `enchant.max-amount` | `3` | Maximum distinct enchantments. |
| `enchant.min-level` | `1` | Minimum level. |
| `enchant.max-level` | `4` | Maximum level, capped by enchantment max. |
| `enchant.enchantments` | all compatible | Optional allow-list of Bukkit enchantments. |

### Example

```yaml
type: enhance_equipment
target: SOURCE
armor-material: DIAMOND
pieces:
  - HELMET
  - CHESTPLATE
enchant:
  min-amount: 1
  max-amount: 2
  min-level: 1
  max-level: 3
  enchantments:
    - PROTECTION
    - UNBREAKING
```

### Behavior and limits

* Existing selected equipment is replaced.
* Non-player equipment drop chances for replaced pieces are set to zero.

---

## `enhance_helditem`

**Purpose:** Adds random compatible enchantments to a held item.

**Context:** Default `target`: `SOURCE`; requires a living entity with equipment and a non-air hand item.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `hand` | `MAIN_HAND` | `MAIN_HAND` or `OFF_HAND`. |
| `enchant.min-amount` | `1` | Minimum distinct enchantments. |
| `enchant.max-amount` | `3` | Maximum distinct enchantments. |
| `enchant.min-level` | `1` | Minimum level. |
| `enchant.max-level` | `4` | Maximum level, capped by enchantment maximum. |

### Example

```yaml
type: enhance_helditem
target: SOURCE
hand: MAIN_HAND
enchant:
  min-amount: 1
  max-amount: 3
  min-level: 1
  max-level: 4
```

### Behavior and limits

* Compatible enchantments are selected without duplicates.
* For non-player equipment, both hand drop chances are set to zero.

---

## `replace_item`

**Purpose:** Builds an item from nested ItemFormat and places it in an equipment slot.

**Context:** Default `target`: `SOURCE`; requires a living entity with equipment.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `slot` | `MAIN_HAND` | `MAIN_HAND`, `OFF_HAND`, `HELMET`, `CHESTPLATE`, `LEGGINGS`, or `BOOTS`. |
| `item` | required | Nested ItemFormat used to build the replacement. |

### Example

```yaml
type: replace_item
target: SOURCE
slot: MAIN_HAND
item:
  material: DIAMOND_SWORD
  name: '<red>Elite Blade'
```

### Behavior and limits

* Non-player drop chance for the replaced slot is set to zero.
* Unsupported slot values do not change equipment.

---

## `shuffle_inventory`

**Purpose:** Randomly permutes occupied player inventory slots inside a configured range.

**Context:** Default `target`: `TARGET`; selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `start-slot` | `0` | First player-inventory index, minimum 0. |
| `end-slot` | `35` | Last index, capped by inventory size. |

### Example

```yaml
type: shuffle_inventory
target: TARGET
start-slot: 0
end-slot: 35
```

### Behavior and limits

* Only occupied slots participate, so empty slots stay empty.
* Fewer than two occupied slots produces no change.

---

## `change_item`

**Purpose:** Applies EnchantmentReform ChangesManager mutations to every selected matching item.

**Context:** Uses the extended item selector and optional equipment holder from the common ability item fields.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `item / item.selector` | `CONTEXT` | Item selector; may select one or multiple equipment items. |
| `item-holder / item.holder` | default target | Holder selector for equipment items. |
| `match-item` | match all | MatchItemFormat filter. |
| `changes` | empty | ChangesManager mutations. |

### Example

```yaml
type: change_item
item:
  selector: MAIN_HAND
  holder: SOURCE
match-item:
  material-tag:
    - minecraft:damageable
changes:
  damage:
    operation: SUBTRACT
    value: 2
```

### Behavior and limits

* Every selected item is checked independently.
* The active-enchantment cache is cleared afterward so item changes are re-evaluated.

---

## `cost_price`

**Purpose:** Checks and optionally charges a configured price, then executes success or failure child abilities.

**Context:** `payer` must resolve to a player; provider-backed prices require their integration.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `payer` | `PLAYER` | Player selector used for affordability and payment. |
| `cost / amount / price.cost` | `0` | Required amount; first present location wins. |
| `take / price.take` | `true` | Whether to actually remove the price. |
| `cost-every` | `1` | Charge only every Nth activation; other activations run success children for free. |
| `price` | required | Price definition: hook item, vanilla item, MatchItemFormat, hook economy, vanilla economy, placeholder, reserve, or free. |
| `abilities` | required | Success child abilities. |
| `else-abilities` | empty | Failure branch; preferred fallback name. |
| `failure-abilities / fail-abilities` | empty | Additional failure aliases. |

### Example

```yaml
type: cost_price
payer: PLAYER
cost: 10
take: true
price:
  economy-plugin: Vault
  economy-type: default
abilities:
  reward:
    type: potion_effect
    target: SOURCE
    potion: HASTE
    duration: 100
failure-abilities:
  message:
    type: send_message
    message: '<red>You cannot afford this ability.'
```

### Behavior and limits

* Negative cost fails.
* When no payer resolves, the failure branch runs.
* Price detection is based on fields present in `price`; invalid configuration logs an error.
* The execution return value propagates from the chosen child branch.

---

## `drop_item`

**Purpose:** Moves an amount from a selected existing item stack into a dropped item entity.

**Context:** Requires a selected item and resolved world location.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `item / item.selector` | `CONTEXT` | Item selector from the common item-selection system. |
| `item-holder / item.holder` | default target | Equipment holder selector. |
| `amount` | entire stack | Amount removed and dropped, clamped to `1..available`. |
| `naturally` | `true` | Use natural random drop motion instead of exact drop. |
| `pickup-delay` | unset | Optional pickup delay, minimum 0. |

### Example

```yaml
type: drop_item
item:
  selector: MAIN_HAND
  holder: SOURCE
amount: 1
naturally: true
pickup-delay: 20
```

### Behavior and limits

* This ability does not build a new ItemFormat item; it consumes from the selected stack.
* Invalid/empty selected items are skipped.

---

## `give_item`

**Purpose:** Builds an ItemFormat item and either gives it to a player or drops it in the world.

**Context:** `INVENTORY` delivery requires a player target. `DROP` delivery requires a resolved location.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `PLAYER` | Player who receives the item. |
| `item` | required | ItemFormat section used to build the item. |
| `amount` | built stack amount | Override for the built stack amount, with a minimum of 1. |
| `delivery` | `INVENTORY` | `INVENTORY` gives the item to `target`; `DROP` spawns it at `location`. |
| `location` | `CONTEXT` | Drop location selector when `delivery: DROP` is used. |
| `naturally` | `true` | Give the dropped item natural random motion. |
| `pickup-delay` | unset | Optional pickup delay for the dropped item, in ticks. |
| `drop-overflow` | `true` | Drops inventory leftovers naturally at the player's location. |

### Example

```yaml
type: give_item
target: PLAYER
amount: 1
random: 0.25
item:
  material: ARROW
```

```yaml
type: give_item
delivery: DROP
location: SOURCE
naturally: true
item:
  material: ARROW
  amount: 1
```

### Behavior and limits

* An absent or invalid `item` section skips execution.
* When `drop-overflow` is false, leftovers returned by the inventory are not spawned in the world.

---

## `give_loot_table_item`

**Purpose:** Rolls a loot table and either gives the resulting item(s) to a player or drops them in the world. Unlike `give_item`, this does not build a concrete item from the configuration; it draws random items directly from the specified loot table.

**Context:** `INVENTORY` delivery requires a player target. `DROP` delivery requires a resolved location.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `loot-table` | required | Namespaced key of the loot table to roll, e.g. `minecraft:gameplay/piglin_bartering`. |
| `target` | `PLAYER` | Player who receives the rolled items. |
| `amount` | `1` | Number of loot table rolls to perform. |
| `luck` | `0` | Luck value passed to the loot context, affecting some loot table entries. |
| `delivery` | `INVENTORY` | `INVENTORY` gives loot to `target`; `DROP` spawns it at `location`. |
| `location` | `CONTEXT` | Drop location selector when `delivery: DROP` is used. |
| `naturally` | `true` | Give dropped loot natural random motion. |
| `pickup-delay` | unset | Optional pickup delay for dropped loot, in ticks. |
| `drop-overflow` | `true` | Drops inventory leftovers naturally at the player's location. |

### Example

```yaml
type: give_loot_table_item
target: PLAYER
loot-table: minecraft:gameplay/piglin_bartering
amount: 1
random: 0.25
```

```yaml
type: give_loot_table_item
delivery: DROP
location: SOURCE
loot-table: minecraft:gameplay/piglin_bartering
amount: 1
```

### Behavior and limits

* An absent or invalid `loot-table` key skips execution.
* `lootedEntity` in the loot context is set from the trigger's target entity when available.
* When `drop-overflow` is false, leftovers returned by the inventory are not spawned in the world.

---

## `preserve_inventory`

**Purpose:** Keeps inventory and/or experience through a player death event.

**Context:** Requires `PlayerDeathEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `inventory` | `true` | Enable keep-inventory and clear event drops. |
| `experience` | `true` | Enable keep-level and set dropped experience result to zero. |

### Example

```yaml
type: preserve_inventory
inventory: true
experience: true
```

### Behavior and limits

* This directly edits the death event and should be tested with gamerules and other keep-inventory plugins.

---

## `preserve_item`

**Purpose:** Preserves the current context item through player death.

**Context:** Requires player `SOURCE`, `PlayerDeathEvent`, and non-null `context.item()`.

### Fields

_No type-specific fields._

### Example

```yaml
type: preserve_item
```

### Behavior and limits

* A clone of the context item is removed from death drops by similarity and returned one tick later.
* Inventory overflow is dropped naturally at the player.

---

## `preserve_experience`

**Purpose:** Preserves a percentage of the player's total experience through death.

**Context:** Requires player `SOURCE` and `PlayerDeathEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `ratio` | `0.5` | Fraction retained, clamped to `0..1`. |

### Example

```yaml
type: preserve_experience
ratio: 0.75
```

### Behavior and limits

* Retained experience is subtracted from the mutable dropped-experience result.
* The retained points are given back one tick later.

---

## `use_on`

**Purpose:** Uses a hand or matched inventory item on a block through the NMS use-on bridge.

**Context:** Requires a player, supported hand, resolvable block, valid face, and NMS implementation.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `player` | `SOURCE` | Player selector. |
| `hand` | `CONTEXT` | `CONTEXT`, trigger-item aliases, `MAIN_HAND`, or `OFF_HAND`. |
| `block-offset.x/y/z` | `0` | Relative offset from context/resolved block. |
| `face` | `UP` | Cardinal block face; `TOP`/`BOTTOM` aliases are supported. |
| `hit.x/y/z` | `0.5` | Hit coordinates inside block, clamped `0..1`. |
| `inside` | `false` | Whether hit is considered inside the block. |
| `inventory-items / inventory-item` | unset | MatchItemFormat used to find a temporary inventory item to use. |
| `success-abilities` | empty | Children after successful NMS use. |
| `failure-abilities` | empty | Children after failure. |

### Example

```yaml
type: use_on
player: SOURCE
hand: CONTEXT
face: UP
block-offset:
  y: -1
hit:
  x: 0.5
  y: 1
  z: 0.5
inventory-items:
  material:
    - BONE_MEAL
success-abilities:
  sound:
    type: sound
    sound: ITEM_BONE_MEAL_USE
```

### Behavior and limits

* When matching an inventory item, it is temporarily moved into the selected hand and the remaining stack is restored to its slot.
* Only main/off hand are accepted.
* The chosen success/failure branch executes with resolved block context.
* Success means the native interaction returned success; it does not guarantee a placed block survives later breaks or physics updates. For replanting, explicitly run [`prevent_block_break`](blocks-world.md#prevent_block_break) in `success-abilities` to protect the new crop briefly.

---

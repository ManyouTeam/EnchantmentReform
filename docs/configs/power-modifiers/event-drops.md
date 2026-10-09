# 🎁 Event and Drop Modifiers

Every modifier on this page is documented independently. All entries also support the common modifier fields from the [Power Modifiers](README.md) page.

## Registry keys on this page

* `elytra_boost_not_consume`
* `modify_drops`

---

## `elytra_boost_not_consume`

**Purpose:** Controls whether an Elytra boost consumes its firework item.

**Context:** Requires Paper `PlayerElytraBoostEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `consume` | `false` | Value passed to `event.setShouldConsume`. |

### Example

```yaml
type: elytra_boost_not_consume
consume: false
```

### Behavior and limits

* Despite the registry name, `consume: true` explicitly enables consumption.

---

## `modify_drops`

**Purpose:** Transforms and redistributes block or entity drops.

**Context:** Requires `BlockDropItemEvent` or `EntityDeathEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `clear` | `false` | Remove all original drops. |
| `multiplier` | `1` | Scale surviving stack amounts. |
| `smelt` | `false` | Replace drops with furnace-smelted results when recipes exist. |
| `replacement` | empty | Material, `EVENT_BLOCK`, `MATCHING_PLANKS`, or `RANDOM_CONFIGURED`. |
| `replacement-amount` | `1` | Number of replacement selections/items. |
| `append` | `false` | Keep transformed originals after adding replacements. |
| `replacement-items` | empty | Weighted ItemFormat entries for random replacement. |
| `bonus-items` | empty | Weighted ItemFormat entries appended after originals. |
| `bonus-amount` | `1` | Number of bonus selections. |
| `destination` | event-specific | `EVENT`, `WORLD`, `SOURCE`, `TARGET`, `INVENTORY`, or `ENDER_CHEST`. |

### Example

```yaml
type: modify_drops
smelt: true
multiplier: 2
bonus-amount: 1
bonus-items:
  gem:
    rate: 1
    material: EMERALD
destination: SOURCE
```

### Behavior and limits

* Block drop entities are removed before transformed drops are distributed.
* Replacement and bonus weights must be positive and finite.
* Stacks are split to legal maximum sizes and inventory overflow is dropped naturally.

---

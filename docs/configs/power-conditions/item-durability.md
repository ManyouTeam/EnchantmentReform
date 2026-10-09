# 🛠️ Item and Durability Conditions

Every registered condition on this page supports the common fields documented on the [Power Conditions](README.md) page.

## Registry keys on this page

* `item_damage`

---

## `item_damage`

**Purpose:** Checks the current or projected durability damage of a selected damageable item.

**Context:** Default `item`: `TRIGGER_ITEM`; default equipment holder: `PLAYER`. The selected item must use Bukkit's `Damageable` item meta and have positive maximum durability.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `item` | `TRIGGER_ITEM` | Item selector string, or nested section containing `selector` and `holder`. |
| `item-holder` | `PLAYER` | Holder selector when using the flat item form. |
| `mode` | `DAMAGE` | Value derived from the selected item's durability state. See the mode table below. |
| `include-event-damage` | `false` | During `PlayerItemDamageEvent`, include the mutable durability damage that the current event will apply. |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative numeric comparison such as `>=`, `<`, or `=`. |

### Modes

| Mode | Returned value |
| --- | --- |
| `DAMAGE` | Current used durability points. Projected event damage may make this value exceed maximum durability. |
| `REMAINING` / `DURABILITY` | Remaining durability points, clamped to `0..maximum`. |
| `DAMAGE_PERCENT` / `USED_PERCENT` | Used durability percentage from `0` to `100`. |
| `REMAINING_PERCENT` / `DURABILITY_PERCENT` | Remaining durability percentage from `0` to `100`. |
| `MAX` / `MAXIMUM` / `MAX_DAMAGE` / `MAX_DURABILITY` | Registered maximum durability of the item material. |

Hyphens and underscores are interchangeable in mode names.

### Detect an item that will break from the current damage event

```yaml
conditions:
  about-to-break:
    type: item_damage
    item: TRIGGER_ITEM
    mode: REMAINING
    include-event-damage: true
    max: 0
```

For an item with `1` durability point remaining and an event applying `1` point of damage, the checked remaining value is `0`, so the condition matches.

### Check remaining durability percentage

```yaml
conditions:
  low-durability:
    type: item_damage
    item:
      selector: MAIN_HAND
      holder: PLAYER
    mode: REMAINING_PERCENT
    compare: '<='
    value: 10
```

### Behavior and limits

* The first selected item whose meta implements `Damageable` is checked. Single-item selectors such as `TRIGGER_ITEM`, `CONTEXT`, `MAIN_HAND`, and individual equipment slots are recommended.
* A missing, non-damageable, or zero-maximum-durability item does not match.
* `include-event-damage` only adds event damage when the selected item is the current `PlayerItemDamageEvent` item. `TRIGGER_ITEM` always refers to that item for `on-item-damage`.
* Event damage is read through the mutable trigger result. A condition attached to a later modifier or ability can therefore observe item-damage changes made by an earlier modifier.
* The checked numeric value is available as `{original}` and `{current}` while resolving `min`, `max`, and `value`.

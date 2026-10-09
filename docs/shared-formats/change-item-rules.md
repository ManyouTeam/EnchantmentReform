# 🔧 Change Item Format

The `change_item` Ability passes its nested `changes` section use Change Item Format. Change rules run in YAML order against the current item, so a later rule sees metadata written by an earlier rule.

```yaml
type: change_item
item:
  selector: MAIN_HAND
  holder: PLAYER
changes: {}
```

The change item format of EnchantmentReform is almost same as MythicChanger's one, so please click [here](https://mythicchanger.superiormc.cn/change-rules/empty) to view it's Wiki to know basic format of them, this page will only show the thing availble in this plugin.

## Power variables inside `changes`

This allows a reusable variable to depend on the item currently being changed:

```yaml
variables:
  repair-amount: '{max-damage} * {level} * 0.1'
  enchant-level-change: -1

powers:
  on-item-damage:
    abilities:
      emergency-repair:
        type: change_item
        item: TRIGGER_ITEM
        changes:
          modify-enchants:
            enchantmentreform:emergency_repair: '{enchant-level-change}'
          repair-damage: '{repair-amount}'
```

At execution time, `{repair-amount}` first expands to `{max-damage} * {level} * 0.1`; the current item's maximum durability and the triggering enchantment level are then substituted before the expression is calculated.

Variables may also contain numeric trigger-context placeholders such as `{original_item_damage}` when the current trigger provides them. Field-specific placeholders are resolved by their owning rule afterward, so a `modify-enchants` variable may also contain `{current-level}` or `{max-level}`.

Because rules execute in YAML order and `ObjectSingleChange` keeps its current `ItemMeta` synchronized, a variable used by a later rule observes metadata changes made by earlier rules in the same `changes` section.

## `modify-enchants`

Changes the current levels of normal item enchantments without rebuilding or serializing the item.

### Additive shorthand

A scalar value is treated as a level delta:

```yaml
changes:
  modify-enchants:
    minecraft:looting: -1
    minecraft:unbreaking: 2
```

For an item with Looting III and Unbreaking I, the result is Looting II and Unbreaking III.

The `minecraft:` namespace may be omitted for vanilla enchantments:

```yaml
changes:
  modify-enchants:
    looting: -1
```

Custom enchantments use their complete namespaced key:

```yaml
changes:
  modify-enchants:
    enchantmentreform:example: 1
```

### Operation form

Each enchantment may instead use an operation section:

```yaml
changes:
  modify-enchants:
    minecraft:looting:
      operation: SUBTRACT
      value: 1

    minecraft:unbreaking:
      operation: MULTIPLY
      value: 2

    minecraft:sharpness:
      operation: SET
      value: 'min({current-level} + {level}, 10)'
```

| Field       | Default | Description                                                             |
| ----------- | ------- | ----------------------------------------------------------------------- |
| `operation` | `ADD`   | `ADD`, `SUBTRACT`, `MULTIPLY`, or `SET`. Unknown values use `ADD`.      |
| `value`     | `0`     | Operand. Supports mathematical expressions and ChangeItem placeholders. |

### Field-specific placeholders

| Placeholder       | Meaning                                                                           |
| ----------------- | --------------------------------------------------------------------------------- |
| `{current-level}` | The enchantment level currently stored on the item. Missing enchantments use `0`. |
| `{max-level}`     | The registered maximum level of that enchantment.                                 |

The normal ChangeItem placeholders remain available, including `{level}`, `{amount}`, `{max-stack}`, `{damage}`, `{max-damage}`, and `{original-damage}`.

### Behavior and limits

* The calculated result is rounded to the nearest integer.
* A result less than or equal to `0` removes the enchantment.
* A positive result adds or replaces the enchantment using unsafe level handling, so compatibility and vanilla maximum-level restrictions are not enforced.
* A missing enchantment starts at level `0`; a positive additive result may therefore add it.
* Unknown enchantment keys are skipped.
* Invalid or non-finite expressions are reported and skipped instead of removing the enchantment.
* This rule changes normal item enchantments. Stored enchantments on enchanted books are not affected.
* Registry lookups are cached by normalized enchantment key, and each execution reads the requested level directly from `ItemMeta` rather than serializing or scanning the whole item format.

### Decrease one level and remove at zero

```yaml
changes:
  modify-enchants:
    minecraft:looting: -1
```

| Before      | After               |
| ----------- | ------------------- |
| Looting III | Looting II          |
| Looting II  | Looting I           |
| Looting I   | Enchantment removed |
| No Looting  | No change           |

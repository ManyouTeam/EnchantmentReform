# 🥕 Custom Attributes

Every `.yml` file directly under `plugins/EnchantmentReform/attributes` defines one persistent per-player integer attribute. Subdirectories are not scanned. Its file name without `.yml` is the globally unique attribute ID. Its base value and persistent modifiers are stored in the player's PDC. The final value after modifiers is passed to the configured power as `{level}`.

```yaml
enabled: true
name: '&cPower Training'
description: '&8Adds &a{bonus_damage} &8attack damage.'

minimum-value: 0
maximum-value: 10
default-value: 0
show-in-attribute-gui: true

skip-power-at-default-value: true
skip-power-at-zero: true

skill-menu:
  show: true

allocation:
  show-in-menu: true
  price:
    '1': 8
    '2': 16
    '3': 32
    '4': 64
  conditions:
    strength-required:
      type: attribute_value
      attribute: strength
      compare: '>'
      value: 10
  levels:
    '5':
      conditions:
        defense-required:
          type: attribute_value
          attribute: defense
          min: 20

variables:
  bonus_damage: '{level} * 0.5'

powers:
  on-attack:
    modifiers:
      attribute-damage:
        type: damage
        operation: ADD
        value: '{bonus_damage}'
```

## Fields

| Field                         | Default         | Description                                                                                                                                                             |
| ----------------------------- | --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `enabled` | `true` | Whether this attribute is loaded. When false, it is unavailable in menus, commands, and powers. |
| `name`                        | file ID         | Display name; supports `{lang:...}`.                                                                                                                                    |
| `description`                 | none            | Optional description; supports `{lang:...}`, `{level}`, and entries from `variables`. In descriptions, `{level}` is the viewing player's current final attribute value. |
| `minimum-value`               | `0`             | Lowest value that can be stored.                                                                                                                                        |
| `maximum-value`               | integer maximum | Highest value that can be stored.                                                                                                                                       |
| `default-value`               | `0`             | Base value returned when the player has no stored PDC value.                                                                                                            |
| `show-in-attribute-gui`       | `true`          | Whether this attribute appears in the read-only attribute information GUI. This is independent from the allocation-menu switch.                                       |
| `skill-menu.show`             | `true`          | Displays this attribute and allows allocation in every skill-detail menu. Attributes are no longer linked to individual skills.                                      |
| `skip-power-at-default-value` | `false`         | Does not add this source to power execution when its final value equals `default-value`.                                                                                |
| `skip-power-at-zero`          | `false`         | Does not add this source to power execution when its final value is zero.                                                                                               |
| `allocation.show-in-menu`     | `true`          | Whether this attribute appears in the attribute-point allocation menu.                                                                                                 |
| `allocation.maximum-value`    | `maximum-value` | Highest base value obtainable through point allocation. Modifiers may still raise the final value up to `maximum-value`.                                               |
| `allocation.price`            | `1`             | Points consumed for each target base-value level. Accepts one number or a level-selector map. This is configured on the attribute, not in the menu file.                |
| `allocation.conditions`       | none            | Common Power Conditions checked before every allocated level.                                                                                                          |
| `allocation.levels.<level>.conditions` | none   | Additional Power Conditions checked when the player's base value reaches that exact level.                                                                             |
| `allocation.levels.<level>.requirement-display` | none | Optional localized requirement text shown for that next level. When omitted, a direct `skill_level` condition is rendered with the shared `skill-allocation-skill-level-requirement` language template. |
| `variables`                   | none            | Shared value/formula variables available to both the description and powers, using the same syntax as enchantment variables.                                            |
| `powers`                      | none            | Optional direct powers using the same trigger, condition, modifier, and ability format as enchantments and custom items. Capacity-style attributes may omit this section and be consumed by another source. |

Base and final values outside the configured range are clamped, except for values explicitly assigned with the administrative `setattribute ... -ignore` override. Modifier calculations are rounded to the nearest integer after all operations. The two skip options are independent, even when the default value is zero.

When one click adds multiple levels, every intermediate target level must pass both the common conditions and its exact-level conditions. The whole allocation is rejected without spending points if any condition fails. Use `type: attribute_value` to compare another custom attribute's final value. It accepts `attribute`, inclusive `min`/`max`, or `compare` with `value`; for example, `compare: '>'` and `value: 10` requires a value strictly greater than 10. The allocation GUI derives visibility and price from this section and shows both base and final values before and after each click option.

Use `type: skill_level` with `skill` and the numeric comparison fields to require progression in a skill. For example, `skill: agility` with `min: 10` requires Agility level 10 before that target attribute level can be purchased.

Descriptions are rendered at the player's current final value, so they update after the base value or a modifier changes. Prefix a description formula with `%:` to calculate and append a percent sign; for example, `'%:{level} / 10'` renders as `50%` at value `500`.

## Modifiers

Each modifier has a namespaced ID, a decimal amount, and one operation. Modifiers are calculated in this order:

1. `ADD_VALUE`: add the amount directly.
2. `ADD_MULTIPLIED_BASE`: add the base value multiplied by the amount.
3. `ADD_MULTIPLIED_TOTAL`: multiply the accumulated value by `1 + amount`; multiple modifiers multiply successively.

For a base value of `100`, modifiers `ADD_VALUE 20`, `ADD_MULTIPLIED_BASE 0.5`, and `ADD_MULTIPLIED_TOTAL 0.1` result in `(100 + 20 + 100 × 0.5) × 1.1 = 187`.

Command-created modifiers persist in player PDC. Temporary modifiers created by `refresh_attribute` remain runtime-only and are removed when they expire, their enchantment/custom-item source stops being active, or the ability unloads.

Code can use `AttributeManager.attributeManager` to access `getBaseValue`, final `getValue`, `setValue`, `addValue`, `resetValue`, `getModifiers`, `addModifier`, `setModifier`, and `removeModifier`.

Administrators can change an online player's value with:

```
/enchantmentreform setattribute <player> <attribute> <value>
/enchantmentreform setattribute <player> <attribute> <value> -ignore
/enchantmentreform addattribute <player> <attribute> <delta>
/enchantmentreform addattributemodifier <player> <attribute> <modifier-id> <amount> <operation>
/enchantmentreform setattributemodifier <player> <attribute> <modifier-id> <amount> <operation>
```

`setattribute` and `addattribute` change the base value. Appending `-ignore` to `setattribute` stores a value outside the configured minimum/maximum and lets that value and its modifiers remain outside the configured range. A later `setattribute` without `-ignore`, `addattribute`, or reset returns the value to normal limit enforcement. `addattributemodifier` refuses an existing modifier ID, matching vanilla modifier-add behavior. `setattributemodifier` creates or replaces that ID.

Players can use `/enchantmentreform menu attribute-info` to view every custom attribute, including their own base value, final value, range, and active modifiers.

With PlaceholderAPI installed, `%enchantmentreform_attribute_<id>%` displays the player's current value. For example, `%enchantmentreform_attribute_strength%` displays `strength`.

The `set_attribute` and `refresh_attribute` abilities accept custom attribute IDs as well as Bukkit attributes. A custom attribute may be written as `strength`, `custom_attribute:strength`, or `enchantmentreform:strength`; its target must be a player. `set_attribute` changes its base value, while `refresh_attribute` applies a temporary modifier and accepts both the new operation names above and the legacy Bukkit names.

Numeric and formula fields in abilities may read a player's final custom attribute value with `{attribute:<id>}`. Use `{attribute:<id>:<variable>}` to resolve a variable from that attribute at its final value, for example `{attribute:dodge:melee_dodge_chance_decimal}`.

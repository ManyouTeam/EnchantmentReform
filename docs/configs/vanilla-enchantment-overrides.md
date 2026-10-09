# 🧩 Vanilla Enchantment Overrides

Vanilla Enchantment Overrides let EnchantmentReform change selected existing `minecraft:` enchantments and attach EnchantmentReform runtime powers to them.

Override files are loaded from:

```
plugins/EnchantmentReform/vanilla_enchantments/
```

For example:

```
plugins/EnchantmentReform/vanilla_enchantments/protection.yml
```

An override does **not** register a second enchantment. It targets the existing vanilla registry entry, such as `minecraft:protection`.

{% hint style="warning" %}
Registry-level Vanilla Override fields are applied during Paper's enchantment registry bootstrap. A full server restart is required. `/enchantmentreform reload` cannot rebuild the vanilla enchantment registry.
{% endhint %}

{% hint style="info" %}
The runtime `variables` and `powers` layer is separate from registry replacement. Registry fields described on this page rely on the Paper registry implementation; do not assume plain Spigot can rewrite the existing vanilla registry entry.
{% endhint %}

## Minimal override

The smallest useful override can attach a Power to an existing vanilla enchantment without changing its original registry fields or native behavior:

```yaml
enabled: true
key: minecraft:sharpness

variables:
  bonus-chance: '0.05 * {level}'

powers:
  on-attack:
    abilities:
      particles:
        type: particle
        target: TARGET
        particle: CRIT
        random: '{bonus-chance}'
```

Because `effects`, `max-level`, item sets, enchanting costs, and other registry fields are absent, their original vanilla values remain unchanged.

## Complete structure

```yaml
enabled: true
key: minecraft:protection

name: Protection Plus
description: '&8Protection rewritten by EnchantmentReform.'

supported-items: minecraft:enchantable/armor
primary-items: minecraft:enchantable/armor
exclusive-with:
  - '#minecraft:exclusive_set/armor'

max-level: 6
rarity: COMMON
weight: 10
anvil-cost: 1

minimum-cost:
  base: 1
  per-level: 11
maximum-cost:
  base: 12
  per-level: 11

active-slots:
  - ARMOR

execution-priority: 0

# Present and empty: remove every original native effect component.
effects: {}

variables:
  reduction: '{level}'

powers:
  on-damage:
    modifiers:
      protection:
        type: damage
        operation: SUBTRACT
        value: 'min({reduction}, {original} * 0.5)'
```

Only fields explicitly present in the file replace the corresponding vanilla registry value. The other original values remain unchanged.

## File name and `key`

When `key` is omitted, the file name is used automatically:

```
protection.yml -> minecraft:protection
sharpness.yml  -> minecraft:sharpness
```

You may write the key explicitly:

```yaml
key: minecraft:protection
```

The key must use the `minecraft` namespace. A custom namespace such as `enchantmentreform:protection` is not a Vanilla Override; use a normal custom enchantment file for that.

Keep one override file per vanilla enchantment key.

## Partial override behavior

A Vanilla Override is field-based. Omitting a registry field means **keep the current vanilla value**.

| Configuration       | Result                                                                                                                  |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Field is absent     | Keep the original vanilla registry value.                                                                               |
| Field is present    | Replace that registry value with the configured value.                                                                  |
| `effects` is absent | Keep all original native enchantment effects.                                                                           |
| `effects: {}`       | Replace the native effect map with an empty map, removing the original mechanic.                                        |
| Populated `effects` | Replace the complete native effect map with the configured components.                                                  |
| `enabled: false`    | Disable the override enchantment by clearing its supported/primary item sets and native effects during Paper bootstrap. |

`effects` is a complete replacement, not a component-by-component merge. When retaining part of a vanilla enchantment's native behavior, copy every native effect component that should remain into the new `effects` section.

## Supported fields

### Bootstrap registry fields

| Field             | Applied behavior                                                                                                                                         |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `enabled`         | Defaults to `true`. When `false`, runtime powers are disabled and Paper clears supported items, primary items, and native effects for the vanilla entry. |
| `key`             | Target vanilla enchantment key. Defaults to `minecraft:<file-name>` and must use the `minecraft` namespace.                                              |
| `name`            | Replaces the enchantment registry description when present. Must be a non-empty string.                                                                  |
| `supported-items` | Replaces the item set on which the enchantment is valid.                                                                                                 |
| `primary-items`   | Replaces the preferred item subset used by vanilla enchanting behavior.                                                                                  |
| `exclusive-with`  | Replaces the enchantment incompatibility set.                                                                                                            |
| `weight`          | Replaces the vanilla selection weight. Valid configured value is `1..1024`.                                                                              |
| `max-level`       | Replaces the vanilla maximum level. When omitted, EnchantmentReform reads the original registered maximum.                                               |
| `minimum-cost`    | Replaces the minimum enchanting cost formula. Requires `base` and `per-level`.                                                                           |
| `maximum-cost`    | Replaces the maximum enchanting cost formula. Requires `base` and `per-level`.                                                                           |
| `anvil-cost`      | Replaces the base anvil cost.                                                                                                                            |
| `active-slots`    | Replaces the native active equipment slot groups and also controls EnchantmentReform Power activation.                                                   |
| `effects`         | Replaces the complete Minecraft-native enchantment effect-component map.                                                                                 |

### Plugin metadata and runtime fields

| Field                | Purpose                                                                                                                                                                                           |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `description`        | Text used by EnchantmentReform-compatible UI or lore integrations; it does not replace the native registry description.                                                                           |
| `rarity`             | EnchantmentReform rarity metadata used for display/order/default metadata. It does not by itself replace the original vanilla registry weight. Set `weight` explicitly to change registry weight. |
| `execution-priority` | Order in which several active EnchantmentReform powers process the same event. Higher values run first.                                                                                           |
| `variables`          | Reusable values for descriptions and `powers`.                                                                                                                                                    |
| `allow-duplicate`    | Defaults to `false`. When `true`, each equipped occurrence of the enchantment executes its power.                                                                                                |
| `powers`             | EnchantmentReform triggers, conditions, modifiers, abilities, limits, cooldowns, chance, and state.                                                                                               |

## Item sets

A single string is treated as an item tag:

```yaml
supported-items: minecraft:enchantable/armor
primary-items: minecraft:enchantable/armor
```

A list is treated as explicit item keys:

```yaml
supported-items:
  - minecraft:diamond_sword
  - minecraft:netherite_sword
```

Do not mix a tag entry beginning with `#` and direct item keys in the same list.

## Exclusivity

Use one enchantment tag:

```yaml
exclusive-with: '#minecraft:exclusive_set/armor'
```

Or use direct enchantment keys:

```yaml
exclusive-with:
  - minecraft:fire_protection
  - minecraft:blast_protection
  - minecraft:projectile_protection
```

Do not mix an enchantment tag with direct enchantment keys in the same `exclusive-with` list.

## Active slots

When `active-slots` is omitted, EnchantmentReform runtime powers consider the override active in any equipment slot containing the enchanted item.

Common values include:

* `HAND`
* `MAINHAND`
* `OFFHAND`
* `HEAD`
* `CHEST`
* `LEGS`
* `FEET`
* `BODY`
* `ARMOR`
* `ANY`

`MAIN_HAND` and `MAINHAND` are normalized to `HAND`; `OFFHAND` is normalized to `OFF_HAND`.

When the field is present, Paper also replaces the vanilla enchantment's active slot groups, which affects native equipment-dependent components such as `minecraft:attributes`.

## Native `effects` behavior

### Keep the original vanilla mechanic

Do not declare the field:

```yaml
# No effects section.
```

This is appropriate when only adding EnchantmentReform powers or changing metadata such as `max-level`.

### Remove the original vanilla mechanic

Use an empty map:

```yaml
effects: {}
```

The bundled `protection.yml` uses this pattern so the original Protection Effect Components are removed and damage reduction is implemented through `powers` instead.

### Replace the original mechanic

Provide a complete new native effect map:

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:vanilla_override_speed
      attribute: minecraft:movement_speed
      amount:
        type: minecraft:linear
        base: 0.01
        per_level_above_first: 0.01
      operation: add_value
```

Read [Native Enchantment Effects](native-enchantment-effects.md) for component schemas, level-based values, predicates, JSON-to-YAML conversion, and validation rules.

## Example: increase a vanilla maximum level only

```yaml
enabled: true
key: minecraft:unbreaking
max-level: 5
```

The original name, supported items, active slots, costs, exclusivity, and native effects remain unchanged.

## Example: preserve vanilla behavior and add a Power

```yaml
enabled: true
key: minecraft:sharpness

variables:
  ignite-chance: '0.03 * {level}'

powers:
  on-attack:
    abilities:
      ignite:
        type: fire
        target: TARGET
        fire-ticks: 60
        random: '{ignite-chance}'
```

Because no `effects` field is present, Sharpness keeps its vanilla native damage effect.

## Example: completely rewrite Protection

```yaml
enabled: true
key: minecraft:protection
name: '{lang:vanilla-enchantment-protection-name}'
description: '{lang:vanilla-enchantment-protection-description}'
max-level: 6
rarity: COMMON

# Remove the original vanilla protection components.
effects: {}

variables:
  reduction: '{level}'

powers:
  on-damage:
    modifiers:
      reduction:
        type: damage
        operation: SUBTRACT
        value: 'min({reduction}, {original} * 0.5)'
```

## `enabled: false`

```yaml
enabled: false
key: minecraft:binding_curse
```

On Paper bootstrap, this keeps the registry key but clears supported items, primary items, and native effects. The enchantment is therefore no longer naturally usable, and EnchantmentReform does not initialize its runtime Power.

This is stronger than merely omitting `powers`.

## Reload and validation

Registry fields are handled before the registry is frozen. After changing any of the following, perform a full server restart:

* `enabled`;
* `key`;
* `name`;
* item sets;
* exclusivity;
* weight and levels;
* enchanting/anvil costs;
* active slots;
* native `effects`.

Common startup errors include:

* a non-`minecraft` key;
* an unknown vanilla enchantment key;
* weight outside `1..1024`;
* mixed tag and direct-key lists;
* invalid item, enchantment, attribute, effect-component, or predicate keys;
* native `effects` copied from a different Minecraft version.

The file path is included in validation messages, for example:

```
vanilla_enchantments/protection.yml: key must use the minecraft namespace
```

## Related pages

* [Enchantments](enchantment-configuration.md)
* [Native Enchantment Effects](native-enchantment-effects.md)
* [Power configuration](powers/)

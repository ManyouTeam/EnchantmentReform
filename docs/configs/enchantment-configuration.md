# 📄 Enchantments

{% hint style="info" %}
## Want to display enchantment description in my lore?

Go to [EnchantmentSlots](https://www.spigotmc.org/resources/enchantmentslots-add-enchantment-slot-feature-to-your-server-1-20-5.113048/).
{% endhint %}

{% hint style="info" %}
## Want mob to be able to use enchantments as well?

Because it is somewhat unreliable for enchantments to work on both mobs and players simultaneously, this function has been separated into [EnchantedMobs](https://www.spigotmc.org/resources/enchantedmobs-dynamic-mob-abilities-and-player-scaling-difficulty-61-built-in-power-1-21-3.133242/).
{% endhint %}

Custom enchantments are read recursively from:

```
plugins/EnchantmentReform/enchantments/
```

Subdirectories may be used to organize armor, melee, ranged, tools, fishing, curses, or any custom category. The YAML `key` is the authoritative Minecraft registry key; the file path is an administrative identifier and should also remain unique.

Selected vanilla definitions can be customized from:

```
plugins/EnchantmentReform/vanilla_enchantments/
```

Vanilla overrides are also bootstrap data and require a restart. Read [Vanilla Enchantment Overrides](vanilla-enchantment-overrides.md) for partial replacement rules, available fields, `effects` behavior, disabling vanilla enchantments, and complete examples.

## Registry and runtime layers

An enchantment has two different layers:

1. **Registry data** is created during server bootstrap. It includes the namespaced key, supported and primary items, max level, weight, enchanting costs, anvil cost, active slots, exclusivity, curse state, obtaining sources, and native `effects` components.
2. **Runtime power data** is evaluated while the server is running. It includes variables, triggers, conditions, modifiers, abilities, cooldowns, chances, and state.

Registry data is frozen after bootstrap. Runtime data may be re-read where the current reload implementation supports it, but reload cannot add or remove a registry entry or rebuild native `effects`.

## Complete structure

```yaml
enabled: true
key: enchantmentreform:example

name: Example
description: '&8Deals additional damage at high target health.'

supported-items: enchantmentreform:sword
primary-items: enchantmentreform:sword
exclusive-with:
  - minecraft:sharpness

max-level: 3
rarity: RARE
weight: 8
anvil-cost: 4

minimum-cost:
  base: 8
  per-level: 10
maximum-cost:
  base: 28
  per-level: 10

active-slots:
  - HAND
obtaining-sources:
  - ENCHANTING_TABLE
  - VILLAGER_TRADE
  - RANDOM_LOOT

execution-priority: 0

conditions:
  permission:
    type: permission
    permission: enchantmentreform.example.use

variables:
  multiplier:
    '==1': 1.15
    '==2': 1.3
    '>=3': 1.5

effects:
  minecraft:attributes:
    - id: enchantmentreform:example_attack_speed
      attribute: minecraft:attack_speed
      amount:
        type: minecraft:linear
        base: 0.1
        per_level_above_first: 0.05
      operation: add_value

powers:
  enabled: true
  limit:
    random: 1
    cooldown: 0
    times: 0

  on-attack:
    conditions:
      high-health-target:
        type: health_percent
        target: TARGET
        min: 80

    modifiers:
      damage:
        type: damage
        operation: MULTIPLY
        value: '{multiplier}'

    abilities:
      effect:
        type: particle
        target: TARGET
        particle: CRIT
```

## Vanilla enchantment overrides

Files under `vanilla_enchantments/` target an existing `minecraft:` enchantment rather than registering a new key:

```yaml
enabled: true
key: minecraft:unbreaking
max-level: 5
```

Vanilla overrides are **partial**:

* an omitted registry field keeps the original vanilla value;
* a present field replaces that value during Paper registry bootstrap;
* omitting `effects` keeps the original native mechanic;
* `effects: {}` removes all original native effect components;
* a populated `effects` section completely replaces the original effect map;
* `variables` and `powers` add EnchantmentReform runtime behavior independently of the native registry effect.

The file name is used as the default key, so `protection.yml` targets `minecraft:protection`. An explicit override `key` must use the `minecraft` namespace.

Read [Vanilla Enchantment Overrides](vanilla-enchantment-overrides.md) before changing an existing vanilla enchantment. It documents platform support, all supported fields, inheritance behavior, item sets, exclusivity, active slots, disabling, reload requirements, and complete examples.

## Registry fields

| Field                           | Purpose                                                                               |
| ------------------------------- | ------------------------------------------------------------------------------------- |
| `enabled`                       | Disables loading when `false`.                                                        |
| `key`                           | Required namespaced Minecraft enchantment key. Keep it unique and stable.             |
| `name`                          | Display text or language placeholder.                                                 |
| `description`                   | Description used by compatible UI or lore integrations.                               |
| `supported-items`               | Item tag or supported-item definition accepted by the enchantment.                    |
| `primary-items`                 | Preferred subset used by vanilla enchanting behavior where supported.                 |
| `exclusive-with`                | Enchantment keys or exclusivity definitions that cannot coexist.                      |
| `max-level`                     | Maximum registered level.                                                             |
| `rarity`                        | Looks up defaults from `config.yml -> rarity`.                                        |
| `weight`                        | Candidate selection weight; may override the rarity default.                          |
| `anvil-cost`                    | Base anvil cost; may override the rarity default.                                     |
| `minimum-cost` / `maximum-cost` | Enchanting power formulas by level.                                                   |
| `active-slots`                  | Equipment slots in which powers and equipment-dependent native effects are active.    |
| `obtaining-sources`             | Natural acquisition routes allowed for this enchantment.                              |
| `execution-priority`            | Cross-enchantment runtime order; higher values run first.                             |
| `effects`                       | Minecraft-native enchantment effect components decoded by the current server version. |

Registry fields require a full restart after changes. Ensure these fields are valid, otherwise the server may fail while loading the enchantment registry.

## Runtime fields

| Field       | Purpose                                                                                          |
| ----------- | ------------------------------------------------------------------------------------------------ |
| `variables` | Reusable level-based or calculated values used by descriptions and powers.                       |
| `allow-duplicate` | Defaults to `false`. When `true`, each equipped occurrence of the enchantment executes its power. |
| `conditions` | Conditions required to use the enchantment's powers. Flat named conditions act as a binary gate. With numeric level keys, power conditions are checked downward from the level on the item; the first passing level is used and the power is inactive when none pass. The two forms cannot be mixed. |
| `powers`    | Runtime triggers, power-wide limits, conditions, modifiers, abilities, and activation abilities. |

The complete `powers` syntax is documented separately on the [Power configuration](powers/) page. Keeping the runtime reference there avoids mixing registry fields with event execution rules.

## Supported and primary items

Prefer reusable tags declared in `config.yml -> supported-items.tags`:

```yaml
supported-items: enchantmentreform:tools
primary-items: enchantmentreform:pickaxe
```

Depending on the field implementation, a list may also be accepted. Use namespaced keys consistently and confirm the resulting item set in the catalogue.

## Active enchantments

An enchantment becomes active only when its item is in a configured `active-slots` slot. Typical values include:

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

Paper equipment lifecycle changes can fire:

* `on-activate`: the enchantment has become active;
* `on-deactivate`: the enchantment is no longer active.

The active item and slot determine which enchantments participate when an event trigger is processed. Minecraft also uses the registered active slots for native equipment-dependent `effects`, such as attribute modifiers.

## Obtaining sources

Common source values used by bundled enchantments include:

* `ENCHANTING_TABLE`
* `VILLAGER_TRADE`
* `RANDOM_LOOT`
* `TRADED_EQUIPMENT`

Only list routes in which the enchantment should naturally participate. Commands or other plugins may still apply registered enchantments directly.

## Rarity inheritance

When `weight`, enchanting costs, or `anvil-cost` are omitted, EnchantmentReform can inherit them from the selected rarity in `config.yml`.

This lets many enchantments share a progression model while still allowing per-enchantment overrides.

## Execution priority

`execution-priority` controls the order when several active enchantments handle the same event. Higher values execute first; equal values are ordered deterministically by enchantment key.

Use distinct priorities when one enchantment intentionally prepares context for another, such as changing a block before a second enchantment inspects it.

## Native effects

The root `effects` section is a direct YAML representation of Minecraft's native enchantment effect-component data:

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:example_health
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 2.0
        per_level_above_first: 1.0
      operation: add_value
```

`effects` and `powers` are different systems:

* `effects` is handled by Minecraft's native enchantment engine and must match the current server version's native codec;
* `powers` is handled by EnchantmentReform and supports plugin triggers, conditions, modifiers, abilities, cooldowns, chance, and state;
* both sections may be used in the same enchantment.

Plugin expressions such as `{level}` and root variables are not expanded inside `effects`. Native level scaling must use Minecraft structures such as `minecraft:linear`.

Read [Native Enchantment Effects](native-enchantment-effects.md) for JSON-to-YAML conversion, field rules, attribute effects, conditional damage immunity, location effects, validation errors, and complete examples.

## Variables

Root variables are reusable in descriptions and powers:

```yaml
variables:
  chance: '0.1 + {level} * 0.05'
  roman:
    '==1': I
    '==2': II
    '>=3': III+
```

String variables are substituted as text. Numeric variables may be evaluated as expressions by fields that support mathematical values.

Variables are declared at the enchantment root, not inside `powers`. They are not expanded inside native `effects`.

## Powers

The `powers` section follows the same trigger → condition → modifier → ability model used by EnchantedMobs, but it belongs inside an enchantment file:

```yaml
powers:
  limit:
    random: 1
    cooldown: 0
    times: 0

  on-attack:
    conditions: {}
    modifiers: {}
    abilities: {}
```

Power-wide limits belong under `powers.limit`. Trigger sections contain trigger-specific options plus `conditions`, `modifiers`, and `abilities`.

Read [Power configuration](powers/) for the execution order, variables, limits, trigger sections, activation abilities, projectile continuation, and complete examples.

## Related pages

* [Vanilla Enchantment Overrides](vanilla-enchantment-overrides.md)
* [Native Enchantment Effects](native-enchantment-effects.md)
* [Power configuration](powers/)
* [Power Triggers](triggers.md)
* [Power Conditions](power-conditions/)
* [Power Modifiers](power-modifiers.md)
* [Abilities](abilities/)

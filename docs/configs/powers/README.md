# 💥 Power configuration

An enchantment power defines:

* whether the runtime power is enabled;
* the shared activation limits used by its triggers;
* when the power activates;
* which conditions, modifiers, and abilities run for each trigger.

Unlike EnchantedMobs, EnchantmentReform does not store each power in a separate file. The power is the `powers` section inside an enchantment file located under:

```
plugins/EnchantmentReform/enchantments/
```

The surrounding enchantment file defines registry information such as supported items, levels, rarity, and active slots. This page focuses only on its runtime `variables` and `powers` sections.

## Quick start

```yaml
powers:
  on-melee-attack:
    abilities:
      ignite-target:
        type: fire
        target: TARGET
        fire-ticks: 100
```

When the enchanted item is active and its owner directly attacks an entity, the target burns for 100 ticks.

## Power structure

```yaml
# Reusable values are declared at the enchantment root.
variables:
  damage-multiplier: '1 + {level} * 0.1'

powers:
  # Enables or disables all runtime behavior of this enchantment.
  enabled: true

  # Shared limits checked before a trigger section executes.
  limit:
    random: 1
    cooldown: 0
    times: 0

  # Trigger sections.
  on-attack:
    conditions: {}
    modifiers: {}
    abilities: {}
```

| Section                       | Purpose                                                                                                  |
| ----------------------------- | -------------------------------------------------------------------------------------------------------- |
| `variables`                   | Defines reusable level-based or calculated values for the enchantment. This section is outside `powers`. |
| `powers.enabled`              | Enables or disables the enchantment's runtime power without changing its registry entry.                 |
| `powers.limit`                | Controls power-wide chance, cooldown, and usage count.                                                   |
| `powers.on-...`               | Defines one trigger's conditions, modifiers, and abilities.                                              |
| `powers.activation-abilities` | Defines special abilities evaluated while deciding whether an enchantment is active.                     |

The keys directly below `powers` are normally built-in triggers such as `on-attack`, `on-damage`, `on-block-break`, or `on-fish`. The exceptions are the common options above and the special `activation-abilities` section.

## Execution flow

When a matching event occurs, EnchantmentReform processes an active enchantment in this order:

```
active enchantment and trigger lookup
  → trigger-specific checks such as hand or tick interval
  → powers.limit
  → trigger conditions
  → trigger modifiers
  → trigger abilities
  → final event result
```

* Conditions at the same level must all match.
* Modifiers execute in YAML order and update supported event result data.
* Abilities execute in YAML order after modifiers.
* A trigger or child entry that requires unavailable context is skipped.
* Tracked projectile continuation may reuse the power captured at launch instead of rerolling the power-wide limit.

{% hint style="warning" %}
`powers.limit` is checked before trigger conditions. A configured cooldown or usage count can therefore be acquired even when a later condition causes the trigger section to be skipped.
{% endhint %}

See [Power Triggers](../triggers.md) for the context supplied by each trigger and the meaning of `SOURCE`, `SKILL`, and `TARGET`.

## Variables and dynamic values

Variables avoid repeating the same formula throughout an enchantment:

```yaml
variables:
  damage-multiplier:
    '==1': 1.1
    '==2': 1.2
    '>=3': 1.35

powers:
  on-attack:
    modifiers:
      scale-damage:
        type: damage
        operation: MULTIPLY
        value: '{damage-multiplier}'
```

Depending on the field, values may support:

* `{level}` or `level`;
* enchantment variables such as `{damage-multiplier}`;
* formulas such as `4 + {level} * 0.5`;
* ranges such as `1~3` or `0.5~1.5`;
* level selector maps;
* runtime placeholders such as `{source_health}`, `{target_health_percent}`, and `{distance}`;
* PlaceholderAPI when a player context is available.

```yaml
value:
  '==1': 2
  '>=2;;<5': '{level} * 0.5'
  '>=5': 4
```

Not every runtime placeholder is available for every trigger or ability. See [Math Calculate Format](../../shared-formats/math-calculate-format.md) for expression syntax.

## Power-wide limits

```yaml
powers:
  limit:
    random: 0.5
    cooldown: 8
    times: 3

  on-attack:
    abilities:
      effect:
        type: particle
        target: TARGET
        particle: CRIT
```

| Field      | Default | Description                                                                   |
| ---------- | ------- | ----------------------------------------------------------------------------- |
| `random`   | `1`     | Activation chance. `1` means 100%.                                            |
| `cooldown` | `0`     | Shared power cooldown in seconds.                                             |
| `times`    | `0`     | Maximum successful top-level limit passes for the power. `0` means unlimited. |

These options belong under `powers.limit`, not inside an individual `on-...` trigger section.

Supported numeric fields may use formulas, variables, ranges, or level selector maps.

## Trigger section

Every normal trigger uses the same basic layout:

```yaml
powers:
  on-damage:
    conditions:
      fire-damage:
        type: damage_cause
        values:
          - FIRE
          - FIRE_TICK

    modifiers:
      reduce-fire-damage:
        type: damage
        operation: MULTIPLY
        value: 0.5

    abilities:
      smoke:
        type: particle
        target: TARGET
        particle: SMOKE
        count: 10
```

Names such as `fire-damage`, `reduce-fire-damage`, and `smoke` are user-defined entry IDs. They only need to be unique inside their own section. The `type` value selects the registered condition, modifier, or ability implementation.

### `conditions`

Conditions decide whether the trigger continues. Entries use logical AND by default.

```yaml
conditions:
  in-water:
    type: in_water
    target: SOURCE
  low-health:
    type: health_percent
    target: SOURCE
    max: 35
```

Both conditions must pass. Use the `any` condition when one of several branches should be sufficient.

See [Power Condition Reference](../power-conditions/).

### `modifiers`

Modifiers change mutable data supplied by the current trigger before abilities run.

Examples include:

* `damage` on attack or damage triggers;
* `food` on food-level changes;
* `item_damage` on item durability damage;
* fishing modifiers on compatible `on-fish` states;
* trade modifiers on merchant trade triggers;
* `warden_anger` on Warden anger changes.

A valid modifier used with an unrelated trigger has no compatible result to change. Use the plural key `modifiers`.

See [Power Modifier Reference](../power-modifiers/).

### `abilities`

Abilities perform actions in YAML order. They may consume entities, items, blocks, locations, projectiles, or other data supplied by the trigger.

```yaml
powers:
  on-block-break:
    abilities:
      vein:
        type: break_blocks
        shape: VEIN
        center: EVENT_BLOCK
        breaker: SOURCE
        same-type: true
        max-blocks: '8 + {level} * 8'
```

`EVENT_BLOCK` only works when the selected trigger supplies a block. Individual abilities may also support their own conditions, chance, cooldown, usage limit, or child abilities as documented by that ability.

See [Ability Reference](../abilities/).

## Trigger-specific options

Some triggers add options beside `conditions`, `modifiers`, and `abilities`. For example, tick triggers support `interval`:

```yaml
powers:
  on-tick:
    interval: 20
    abilities:
      effect:
        type: particle
        target: SOURCE
        particle: HAPPY_VILLAGER
```

Trigger-specific options are documented on the [Power Triggers](../triggers.md) page.

## Activation abilities

`activation-abilities` is not an event trigger. It is evaluated while EnchantmentReform scans an item and decides which enchantments are active.

The built-in `disable_enchantments` ability can suppress matching enchantments found on the same item:

```yaml
powers:
  activation-abilities:
    disable-curses:
      type: disable_enchantments
      enchantments:
        - '*_curse'
      exclude-enchantments:
        - enchantmentreform:allowed_curse
```

The enchantment declaring the rule does not disable itself. Normal trigger conditions, modifiers, and `powers.limit` do not apply to this special section.

## Multiple triggers

One enchantment may define multiple trigger sections:

```yaml
powers:
  on-shoot-bow:
    abilities:
      capture-power:
        type: mark

  on-projectile-tick:
    abilities:
      trail:
        type: particle
        target: SKILL
        particle: WHITE_ASH
        count: 6

  on-projectile-hit:
    abilities:
      remove-projectile:
        type: remove
        target: SKILL
```

The launch trigger captures the power for projectile continuation, the tick trigger creates a trail, and the hit trigger handles the final impact.

## Complete example

```yaml
variables:
  bonus-damage: '1.5 * {level}'

powers:
  enabled: true
  limit:
    random: 1
    cooldown: 0
    times: 0

  on-attack:
    conditions:
      wounded-target:
        type: health_percent
        target: TARGET
        max: 30

    modifiers:
      finishing-damage:
        type: damage
        operation: ADD
        value: '{bonus-damage}'

    abilities:
      sound:
        type: sound
        target: TARGET
        sound: ENTITY_PLAYER_ATTACK_CRIT
        volume: 1
        pitch: 1.2

      particles:
        type: particle
        target: TARGET
        particle: CRIT
        amount: '5 + {level} * 3'
```

The enchantment adds damage when the target is below 30% health, then plays sound and particle feedback.

## Related references

* [Enchantments](../enchantment-configuration.md): registry fields and the complete enchantment file structure.
* [Power Triggers](../triggers.md): trigger contexts, event differences, result compatibility, and projectile continuation.
* [Power Condition Reference](../power-conditions/): every built-in condition.
* [Power Modifier Reference](../power-modifiers/): every built-in modifier.
* [Ability Reference](../abilities/): every built-in ability.

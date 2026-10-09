# ➗ Math Calculate Format

When `config.yml -> math.enabled` is true, supported numeric fields may use expressions instead of fixed numbers.

```yaml
amount: '2 + level * 0.5'
random: '0.1 + level * 0.02'
```

## Available values

Depending on the receiving field, expressions may include:

* `level` / `{level}`;
* root `variables` values;
* runtime context such as `{distance}`;
* source/target health, maximum health, health percentage, horizontal speed, and fall distance;
* numeric trigger context placeholders listed below;
* field-specific values such as `{original}`, `{now}`, `{max}`, `{air}`, or `{max-air}`;
* numeric PlaceholderAPI results when player context exists.
* The **Math Calculate Format** provided by **EnchantmentReform** are almost identical to those in UltimateShop. Therefore, they will not be elaborated on in this wiki. Please refer to UltimateShop's wiki for a detailed introduction about them. Click [here](https://ultimateshop.superiormc.cn/format) to view.

## Numeric trigger context placeholders

Every numeric `BuiltinContextKeys` entry is automatically exposed using its key name inside braces. A placeholder is replaced only when the current trigger or parent Ability supplied that context value; unavailable values remain unresolved instead of becoming `0`.

| Placeholder | Number type | Context value |
| --- | --- | --- |
| `{original_damage}` | `Double` | Original damage captured by a compatible damage trigger. |
| `{original_amount}` | `Double` | Original generic mutable amount supplied by the trigger. |
| `{original_duration}` | `Float` | Original duration supplied by a duration-capable trigger. |
| `{original_yield}` | `Float` | Original explosion or drop yield supplied by the trigger. |
| `{original_radius}` | `Float` | Original radius supplied by the trigger. |
| `{block_break_progress}` | `Float` | Current block-breaking progress. |
| `{original_air}` | `Integer` | Original remaining-air value. |
| `{previous_air}` | `Integer` | Player remaining-air value before an air-change event. |
| `{original_food_level}` | `Integer` | Original food-level value. |
| `{original_experience}` | `Integer` | Original experience amount or experience stored in a picked-up orb. |
| `{enchantment_level_cost}` | `Integer` | Experience levels actually consumed by the enchanting-table option: 1, 2, or 3. |
| `{original_lapis}` | `Integer` | Lapis lazuli cost of the selected enchanting-table option. |
| `{original_item_damage}` | `Integer` | Original item durability damage. |
| `{state_previous}` | `Double` | State value before the current `state` Ability operation. Available to its triggered children. |
| `{state_current}` | `Double` | Updated state value before grouped threshold consumption. Available to its triggered children. |
| `{state_trigger_count}` | `Long` | Complete threshold-group count calculated by the current `state` Ability. Available to its triggered children. |
| `{state_remainder}` | `Double` | State value left after complete threshold groups are consumed. Available to its triggered children. |
| `{bow_force}` | `Float` | Bow draw force supplied by a compatible projectile trigger. |
| `{target_count}` | `Integer` | Target count supplied by a compatible targeting context. |

The same placeholders work in context-aware string fields and in numeric fields resolved through `getDouble(...)` or `getInt(...)`.

```yaml
amount: 'min({source_health} + {original_experience} / 3, {source_max_health} * 0.5)'
value: '{original_damage} * 0.25'
```

State-result placeholders are added only to the child context created for `state.trigger-abilities`. They can scale one nested execution by the number of complete threshold groups:

```yaml
type: state
key: stored-experience
operation: ADD
amount: '{original_experience}'
trigger-at: 3
consume-trigger-value: true
trigger-abilities:
  heal:
    type: set_health
    target: PLAYER
    amount: 'min({health} + {state_trigger_count}, {max-health} * 0.5)'
```

Root Power variables are resolved first. This means a variable may safely reference a numeric trigger context value:

```yaml
variables:
  converted-damage: '{original_damage} * 0.25'

powers:
  on-attack:
    abilities:
      example:
        type: set_health
        amount: '{source_health} + {converted-damage}'
```

Adding another numeric `ContextKey` through the built-in key factory automatically adds a placeholder with the same key name; no separate parser registration is required.

## Random ranges

```yaml
amount: 2~5
```

Chooses a value between the endpoints using the resolver expected by the field.

## Level selectors

```yaml
amount:
  '==1': 2
  '>=2;;<=4': '2 + level'
  '>=5': 10
```

Entries are evaluated from top to bottom; the first matching selector is used. Quote selector keys in YAML.

## Variables

```yaml
variables:
  bonus: '1 + level * 0.25'
  label:
    '==1': I
    '==2': II
    '>=3': III+
```

Numeric consumers evaluate numeric variable results. Text consumers substitute string values without treating them as a formula.

## Functions

`math.enable-function` enables functions registered by EnchantmentReform, such as common aggregate/helper functions supported by the current implementation. Disable it only when diagnosing compatibility or when functions are intentionally not needed.

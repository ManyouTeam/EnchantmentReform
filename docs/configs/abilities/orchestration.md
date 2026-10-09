# 🧩 Control-flow Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `delay`
* `conditional`
* `any_of`
* `limit`
* `repeat`
* `state`


---

## `delay`

**Purpose:** Runs nested abilities after a scheduled delay.

**Context:** Child abilities reuse the original `PowerContext`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `ticks` | `delay` or `1` | Delay in server ticks, minimum 1. |
| `delay` | `1` | Legacy alias used when `ticks` is absent. |
| `abilities` | required | Nested ability entries. |

### Example

```yaml
type: delay
ticks: 20
abilities:
  strike:
    type: lightning
    target: TARGET
```

### Behavior and limits

* Entities and items can become invalid before execution.
* Scheduling follows the appropriate entity/region scheduler.

---

## `conditional`

**Purpose:** Executes every matching case and otherwise runs fallback abilities.

**Context:** Child conditions and abilities reuse the same context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `cases` | empty | Map of case sections. |
| `cases.<id>.conditions` | empty | AND condition group. |
| `cases.<id>.abilities` | empty | Children for a matching case. |
| `else-abilities` | empty | Children executed once when no case matches. |

### Example

```yaml
type: conditional
cases:
  wounded:
    conditions:
      low:
        type: health_percent
        target: SOURCE
        max: 25
    abilities:
      heal:
        type: set_health
        target: SOURCE
        amount: 10
else-abilities:
  sound:
    type: sound
    sound: BLOCK_NOTE_BLOCK_BASS
```

### Behavior and limits

* Cases are evaluated in configuration order.
* All matching cases execute; matching does not stop after the first.
* Cancellation from any child is propagated.

---

## `any_of`

**Purpose:** Selects one or more weighted child abilities without replacement.

**Context:** Children reuse the same context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `1` | Number of different children to select. |
| `abilities` | required | Child map. |
| `abilities.<id>.rate` | `1` | Positive finite relative weight. |

### Example

```yaml
type: any_of
amount: 1
abilities:
  fire:
    type: fire
    rate: 5
    fire-ticks: 80
  lightning:
    type: lightning
    rate: 1
```

### Behavior and limits

* Invalid/non-positive weights are excluded.
* A selected child still runs its own checks; failure does not cause a replacement draw.
* Cancellation from selected children is propagated.

---

## `limit`

**Purpose:** Wraps nested abilities with one shared ability-level chance, cooldown, and execution count.

**Context:** Children reuse the same context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `abilities` | required | Nested abilities. |
| `random` | `1` | Wrapper probability. |
| `cooldown` | `0` | Wrapper cooldown in seconds. |
| `times` | `0` | Maximum wrapper attempts; zero is unlimited. |

### Example

```yaml
type: limit
cooldown: 10
times: 3
abilities:
  sound:
    type: sound
    sound: ENTITY_ENDERMAN_SCREAM
```

### Behavior and limits

* Children keep their own independent limits in addition to wrapper limits.
* Cancellation from children is propagated.

---

## `repeat`

**Purpose:** Executes nested abilities immediately and then repeatedly for a configured duration.

**Context:** Children reuse the original context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `abilities` | required | Nested abilities. |
| `duration` | `duration-ticks` or `0` | Total duration; non-positive executes only once. |
| `duration-ticks` | `0` | Alias for duration. |
| `interval` | `period` or `20` | Ticks between executions, minimum 1. |
| `period` | `20` | Alias for interval. |

### Example

```yaml
type: repeat
duration: 100
interval: 20
abilities:
  pulse:
    type: particle
    particle: SONIC_BOOM
```

### Behavior and limits

* The first execution is immediate.
* Scheduled children must tolerate invalid entities/items/locations.

---

## `state`

**Purpose:** Stores numeric state scoped to the current power and optionally target, then triggers children at a threshold.

**Context:** Requires stable power/source identity for the state key. Triggered children receive the original context plus state-result placeholders.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `owner` | `SOURCE` | Entity selector for the state owner. Use `PLAYER` when sharing player state across triggers. |
| `key` | `default` | State-pool name. |
| `per-target` | `false` | Include target identity in the key. |
| `state-target` | `TARGET` | Entity selector stored in the key when `per-target: true`. Cross-trigger readers must resolve the same entity. |
| `operation` | `ADD` | `ADD`, `SET`, `SUBTRACT`, or `CLEAR`. |
| `amount` | `1` | Operand. |
| `maximum` | unlimited | Maximum stored value. |
| `duration` | `0` | State lifetime in seconds used by the state store; non-positive means no timed expiry. |
| `trigger-at` | unlimited | Threshold for child execution. |
| `trigger-abilities` | empty | Children executed when the updated state reaches the threshold. |
| `on-increase` | empty | Children executed when the stored value actually increases. |
| `on-decrease` | empty | Children executed when the stored value actually decreases. |
| `on-unchanged` | empty | Children executed when clamping leaves the stored value unchanged. |
| `clear-on-trigger` | `true` | Remove the complete state after execution when grouped consumption is disabled. |
| `consume-trigger-value` | `false` | Consume every complete `trigger-at` group and store only the remainder before executing children. Requires a positive finite threshold. |

### Triggered-child placeholders

| Placeholder | Type | Meaning |
| --- | --- | --- |
| `{state_previous}` | `Double` | Stored value before this Ability applied its operation. |
| `{state_current}` | `Double` | Updated value before threshold groups are consumed. |
| `{state_trigger_count}` | `Long` | Number of complete `trigger-at` groups in the updated value. |
| `{state_remainder}` | `Double` | Value left after removing all complete threshold groups. |

These placeholders are available to `trigger-abilities` and the matching `on-increase`, `on-decrease`, or `on-unchanged` callback executed by the current `state` Ability. Change callbacks run after `trigger-abilities` and only when the updated value reaches `trigger-at`.

### Basic example

```yaml
type: state
key: combo
per-target: true
operation: ADD
amount: 1
maximum: 5
duration: 4
trigger-at: 5
clear-on-trigger: true
trigger-abilities:
  burst:
    type: damage_entity
    amount: 8
```

### Grouped-consumption example

The following example accumulates experience across multiple orb pickups. Every complete group of 3 experience restores 1 health point, while an incomplete remainder is retained for later pickups.

```yaml
type: state
key: experience-lifeblood
operation: ADD
amount: '{original_experience}'
trigger-at: 3
consume-trigger-value: true
trigger-abilities:
  restore-health:
    type: set_health
    target: PLAYER
    amount: 'min({health} + {state_trigger_count}, {max-health} * 0.5)'
```

For example, a stored remainder of `1` followed by an orb worth `7` produces `{state_current} = 8`, `{state_trigger_count} = 2`, and `{state_remainder} = 2`. The nested healing Ability executes once and restores 2 health points.

### Behavior and limits

* `CLEAR` removes state immediately.
* The `state_value` condition can read a state; its `owner`, `key`, `per-target`, and resolved `state-target` entity must match the writer.
* Without `consume-trigger-value`, threshold behavior remains compatible with the original implementation: children run once and `clear-on-trigger` decides whether the whole state is removed.
* With `consume-trigger-value`, all complete groups are consumed before child execution and `clear-on-trigger` is ignored so the remainder can persist.
* Triggered children execute once per outer state update, regardless of `{state_trigger_count}`. Use the count in a numeric expression to scale the result.
* A non-positive or non-finite `trigger-at` does not execute grouped consumption.
* State is stored in memory and cleaned during runtime unload/entity cleanup.

---

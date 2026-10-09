# 📈 Result and Resource Modifiers

Every modifier on this page is documented independently. All entries also support the common modifier fields from the [Power Modifiers](README.md) page.

## Registry keys on this page

* `damage`
* `heal`
* `duration`
* `yield`
* `radius`
* `cooldown_time`
* `item_damage`
* `food`
* `exhaustion`
* `experience`
* `lunge_power`
* `air`

---

## `damage`

**Purpose:** Changes the current mutable damage result.

**Context:** Damage-producing triggers such as incoming/outgoing damage.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: damage
operation: MULTIPLY
value: 1.25
```

### Behavior and limits

* Earlier modifiers in YAML order affect `{original}`.
* Final damage is applied after matching powers finish.

---

## `heal`

**Purpose:** Changes the current health-regain amount.

**Context:** `on-regain` or another trigger with a regain result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: heal
operation: ADD
value: 2
```

### Behavior and limits

* Earlier modifiers in YAML order affect `{original}`.
* The value is written back to the regain event result.

---

## `duration`

**Purpose:** Changes the current combustion duration.

**Context:** `on-combust` or another trigger with a combustion-duration result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: duration
operation: MULTIPLY
value: 2
```

### Behavior and limits

* Earlier modifiers in YAML order affect `{original}`.
* The result is later applied to the combustion event.

---

## `yield`

**Purpose:** Changes explosion yield.

**Context:** An entity-explosion trigger with mutable yield.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: yield
operation: ADD
value: 2
```

### Behavior and limits

* Earlier modifiers in YAML order affect `{original}`.
* Explosion yield affects block drops; it is not explosion radius.

---

## `radius`

**Purpose:** Changes primed-explosion radius.

**Context:** `on-creeper-explode` / `ExplosionPrimeEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: radius
operation: ADD
value: 2
```

### Behavior and limits

* Earlier modifiers in YAML order affect `{original}`.
* The radius is changed before the explosion is created.

---

## `cooldown_time`

**Purpose:** Changes Paper item-group cooldown length.

**Context:** Requires `PlayerItemGroupCooldownEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: cooldown_time
operation: MULTIPLY
value: 0.5
```

### Behavior and limits

* Uses the shared numeric operations.
* Rounded and clamped to `0..40` ticks.

---

## `item_damage`

**Purpose:** Changes the mutable item durability-damage result.

**Context:** Requires a trigger that supplies item damage.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: item_damage
operation: ADD
value: -1
```

### Behavior and limits

* Uses the shared numeric operations.
* Rounded and clamped to at least 0.

---

## `food`

**Purpose:** Changes the mutable food-level result.

**Context:** Requires a food-level-change result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: food
operation: ADD
value: 2
```

### Behavior and limits

* Uses the shared numeric operations.
* Rounded and clamped to `0..20`.

---

## `exhaustion`

**Purpose:** Changes exhaustion added by an exhaustion event.

**Context:** Requires `EntityExhaustionEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: exhaustion
operation: MULTIPLY
value: 0.5
```

### Behavior and limits

* Uses the shared numeric operations.
* Clamped to `0..40`.

---

## `experience`

**Purpose:** Changes the mutable experience amount.

**Context:** Supports exp-change, block break, entity death, fishing, and enchanting events.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: experience
operation: MULTIPLY
value: 2
```

### Behavior and limits

* Uses the shared numeric operations.
* Rounded and clamped to at least 0.

---

## `lunge_power`

**Purpose:** Changes Paper lunge power.

**Context:** Requires `EntityLungeEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: lunge_power
operation: ADD
value: 1
```

### Behavior and limits

* Uses the shared numeric operations.
* Rounded and clamped to at least 0.

---

## `air`

**Purpose:** Changes the mutable remaining-air amount.

**Context:** Requires a trigger that supplies an air result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: air
operation: ADD
value: 40
```

### Behavior and limits

* Uses the shared numeric operations.
* Rounded and clamped to at least 0.

---

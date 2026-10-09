# 🔀 Logical and Random Conditions

Every registered condition on this page is documented as an independent reference entry. All entries also support the common condition fields documented on the [Power Conditions](README.md) page.

## Registry keys on this page

* `any`
* `not`
* `random`
* `player_conditions`

---

## `any`

**Purpose:** Matches when at least one nested typed condition matches.

**Context:** Nested conditions reuse the same `PowerContext`; the nested mapping is evaluated as logical OR.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `conditions` | required | Nested condition entries. Each child keeps its own type, selectors, and fields. |

### Example

```yaml
type: any
conditions:
  wet:
    type: in_water
    target: SOURCE
  raining:
    type: in_rain
    target: SOURCE
```

### Behavior and limits

* A missing nested section is treated as an allowed group by the condition manager, so avoid an empty `conditions` section.
* The common `not: true` field inverts the final OR result.

---

## `not`

**Purpose:** Matches when a nested logical-AND condition group does not fully match.

**Context:** Nested conditions reuse the same `PowerContext` and are evaluated using the normal all-must-pass rule before inversion.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `conditions` | required | Nested typed conditions evaluated as logical AND. |

### Example

```yaml
type: not
conditions:
  in-lava:
    type: in_lava
    target: SOURCE
  burning:
    type: has_potion
    target: SOURCE
    potion: minecraft:fire_resistance
```

### Behavior and limits

* For one child, the child condition's common `not: true` field is usually shorter.
* Applying both `type: not` and common `not: true` inverts twice.

---

## `random`

**Purpose:** Performs a fresh probability roll.

**Context:** No entity, event, block, or item context is required.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `1` | Probability from `0` to `1`; values are clamped to that range. |

### Example

```yaml
type: random
value: 0.25
```

### Behavior and limits

* Every evaluation rolls independently.
* This is separate from power-, modifier-, and ability-level `random` fields.

---

## `player_conditions`

**Purpose:** Runs EnchantmentReform's separate shared player-condition registry.

**Context:** Default `target`: `SOURCE`; the selector must resolve to a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Player selector. |
| `conditions` | required | Nested entries understood by the shared player-condition manager. |

### Example

```yaml
type: player_conditions
target: SOURCE
conditions:
  permission:
    type: permission
    permission: example.use
```

### Behavior and limits

* The available nested types come from the separate player-condition registry.
* A non-player target fails.

---

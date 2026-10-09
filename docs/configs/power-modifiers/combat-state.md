# ⚔️ Combat and State Modifiers

Every modifier on this page is documented independently. All entries also support the common modifier fields from the [Power Modifiers](README.md) page.

## Registry keys on this page

* `armor_pierce`
* `missing_health_damage`
* `stack_damage_modifier`
* `revive`
* `warden_anger`
* `vibration_reduce`

---

## `armor_pierce`

**Purpose:** Increases the current damage result to approximate ignoring a percentage of target armor.

**Context:** Requires living `TARGET` and a damage result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `percent` | `0` | Armor penetration percentage, clamped to `0..100`. |

### Example

```yaml
type: armor_pierce
percent: 35
```

### Behavior and limits

* The target's armor and toughness attributes are read but never changed.
* This compensates for vanilla armor reduction; it is not a separate true-damage channel.

---

## `missing_health_damage`

**Purpose:** Adds percentage damage in steps based on missing health of `SOURCE`.

**Context:** Requires living `SOURCE` and a mutable damage result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `step-percent` | `10` | Missing-health percentage per step; minimum 0.1. |
| `percent-per-step` | `5` | Damage bonus percentage per completed step. |
| `maximum-percent` | `100` | Maximum total bonus percentage. |

### Example

```yaml
type: missing_health_damage
step-percent: 10
percent-per-step: 5
maximum-percent: 50
```

### Behavior and limits

* Steps use `floor(missingPercent / step-percent)`.
* Final damage is multiplied by `1 + bonus/100`.

---

## `stack_damage_modifier`

**Purpose:** Stores stack state and converts it into a percentage damage increase or decrease.

**Context:** Requires a stable owner and a damage result for actual modification; matching `pool` values can share state across trigger sections.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `damage-type` | `DEALT` | `DEALT` or `TAKEN`; controls default scope and message metadata. |
| `stack-action` | `ADD` | `ADD`, `READ`, `CONSUME`, or `CLEAR`. |
| `stack-value` | `CURRENT` | For `ADD`, apply `CURRENT` or `PREVIOUS` stack count. |
| `operation` | `INCREASE` | `INCREASE`, `DECREASE`, or `NONE`. |
| `pool` | current path | State-pool name. |
| `max-stacks` | `5` | Maximum stored stacks, minimum 1. |
| `stack-amount` | `1` | Amount added by `ADD`. |
| `percent-per-stack` | `5` | Damage percentage per effective stack. |
| `maximum-percent` | derived | Final percentage cap. |
| `scope.per-target` | `false` | Separate pools by selected target. |
| `scope.target` | type-specific | Default `TARGET` for dealt and `SOURCE` for taken. |
| `scope.per-damage-cause` | `false` | Separate pools by Bukkit damage cause. |
| `reset.seconds` | `5` | Expiry refreshed by `ADD`; non-positive disables timed expiry. |
| `reset.clear-on` | empty | `AFTER_APPLY`, `AFTER_DAMAGE_DEALT`, or `AFTER_DAMAGE_TAKEN`. |
| `messages.*` | empty | Optional lifecycle messages and recipient/throttle settings. |

### Example

```yaml
type: stack_damage_modifier
pool: combo
damage-type: DEALT
stack-action: ADD
operation: INCREASE
max-stacks: 5
stack-amount: 1
percent-per-stack: 6
reset:
  seconds: 4
scope:
  per-target: true
```

### Behavior and limits

* `ADD` updates state; `READ` applies without changing it; `CONSUME` removes and applies; `CLEAR` only removes.
* State and scheduled messages are cleared on reload/unload and relevant entity unload.

---

## `revive`

**Purpose:** Requests revival during a supported death trigger.

**Context:** Intended for `on-death`; successful application depends on trigger/platform support.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `health` | `20` | Revive health, capped at source maximum health; `{original}` is maximum health. |
| `no-drops` | `false` | Clear death drops when revival is applied. |

### Example

```yaml
type: revive
health: '{original} * 0.5'
no-drops: true
```

### Behavior and limits

* Use a power- or entry-level count limit when revival should happen only once.
* When source is not living, the fallback maximum for expression resolution is 20.

---

## `warden_anger`

**Purpose:** Changes only positive Warden anger increases.

**Context:** Requires Paper `WardenAngerChangeEvent` where new anger is greater than old anger.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | current value | Operand; `{original}` is the current result before this modifier. |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`; division by zero keeps the original. |

### Example

```yaml
type: warden_anger
operation: MULTIPLY
value: 0.5
```

### Behavior and limits

* The operand is the increase amount, not total current anger.
* The resulting new anger is rounded and clamped to `0..150`.

---

## `vibration_reduce`

**Purpose:** Cancels a player-caused Sculk sensor vibration, preventing the sensor from activating and the vibration from propagating to Sculk shriekers (which would otherwise raise the warden warning level).

**Context:** Requires `BlockReceiveGameEvent` whose triggering entity is a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `random` | `1.0` | Chance (0..1) to cancel the vibration. Resolve per enchantment level via a variable. |

### Example

```yaml
type: vibration_reduce
random: '{cancel_chance}'
```

### Behavior and limits

* Cancelling the event stops the Sculk sensor from activating, so no shrieker is triggered and the warden warning level is not raised.
* Pair with an `environment` condition (e.g. `biomes: [minecraft:deep_dark]`) to limit the effect to the Deep Dark.

---

# 🧬 Entity State Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `set_attribute`
* `refresh_attribute`
* `set_health`
* `set_absorption`
* `attribute_layer`
* `set_air`
* `set_food`
* `set_item_cooldown`
* `set_velocity`
* `set_invulnerable`
* `potion_effect`
* `remove_potion_effect`
* `extend_potion_effects`
* `potion_cloud`
* `freeze`
* `fire`
* `experience`
* `skill_experience`

---

## `set_attribute`

**Purpose:** Sets one Bukkit attribute base value or one EnchantmentReform custom attribute value.

**Context:** Default `target`: `SOURCE`; selected entity must expose the requested attribute.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `attribute` | `max_health` | Bukkit attribute key such as `minecraft:max_health`, or a custom attribute ID. |
| `value` | `1` | New value; supports `{now}` for the current value and `{max}` for its maximum. |

### Example

```yaml
type: set_attribute
target: SOURCE
attribute: minecraft:max_health
value: '{max} * 1.25'
```

### Behavior and limits

* When changing maximum health, current health is adjusted/bounded by the platform implementation.
* This changes the base value rather than adding a temporary modifier.
* A custom attribute target must be a player. The resulting integer is clamped to the custom attribute's configured range and stored in player PDC.

---

## `refresh_attribute`

**Purpose:** Adds or refreshes one temporary attribute modifier without stacking duplicate copies.

**Context:** Default `target`: `SOURCE`; target must be living and expose the requested attribute.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `attribute` | `minecraft:max_health` | Namespaced Bukkit attribute key or custom attribute ID. |
| `operation` | `ADD_NUMBER` | Accepts `ADD_VALUE`, `ADD_MULTIPLIED_BASE`, and `ADD_MULTIPLIED_TOTAL`; legacy Bukkit names `ADD_NUMBER`, `ADD_SCALAR`, and `MULTIPLY_SCALAR_1` are also accepted. |
| `amount` | `0` | Modifier amount. |
| `duration` | source lifetime | Optional lifetime in ticks, clamped to at least 2. The modifier is also removed early if its enchantment or custom item stops being active. |
| `transition.enabled` | `false` | Enables a dynamic modifier that rises and falls according to conditions. |
| `transition.rise-duration` | `20` | Ticks required for transition progress to rise from 0 to 1. |
| `transition.fall-duration` | `20` | Ticks required for transition progress to fall from 1 to 0. |
| `transition.update-interval` | `1` | Tick interval for progress and attribute updates, clamped from 1 to 20. |
| `transition.curve` | `LINEAR` | Value curve: `LINEAR`, `EASE_IN`, `EASE_OUT`, or `EASE_IN_OUT`. |
| `transition.active-conditions` | empty | Progress rises while these match and falls while they do not. Empty conditions always match. |
| `transition.on-start` | empty | Child abilities run whenever a falling/inactive transition starts charging again. |
| `transition.on-full` | empty | Child abilities run when progress first reaches 1. |
| `transition.on-decay-start` | empty | Child abilities run when the active conditions stop matching. |
| `transition.on-empty` | empty | Child abilities run when falling progress reaches zero. |

### Example

```yaml
type: refresh_attribute
target: SOURCE
attribute: minecraft:movement_speed
operation: ADD_NUMBER
amount: 0.03
duration: 100
```

### Transition example

Transition mode should normally run under `on-tick`. Activation conditions belong inside `transition.active-conditions`; outer trigger conditions would prevent the ability from running the falling phase.

```yaml
type: refresh_attribute
target: PLAYER
attribute: minecraft:armor
operation: ADD_NUMBER
amount: 6
transition:
  enabled: true
  rise-duration: 60
  fall-duration: 200
  update-interval: 1
  curve: LINEAR
  active-conditions:
    sprinting:
      type: sprinting
      target: PLAYER
      value: true
```

### Behavior and limits

* Identity is based on entity UUID, power ID, ability path, source slot, and attribute, so re-running the same source refreshes its modifier while duplicate sources can stack independently.
* A generation check prevents an older scheduled removal from deleting a refreshed modifier.
* A modifier is removed when its enchantment or custom item stops being active, even when `duration` is configured. When `duration` is omitted, source deactivation is its normal lifetime boundary.
* Modifiers are also removed on entity unload/plugin unload; health is clamped when maximum health falls.
* For a custom attribute, the target must be a player. A runtime-only modifier is refreshed and removed on expiry or source deactivation; it does not persist in PDC.
* In transition mode, `amount` is the modifier value at progress 1; the applied value is `amount × curve(progress)`.
* Transition progress is isolated by target, power, ability path, source slot, and attribute, and is kept only in memory.
* The invoking trigger drives transition updates; `on-tick` is recommended. `duration` does not schedule removal in transition mode.
* Transition state is cleared at zero progress, source deactivation, entity unload, death, or plugin unload.
* Phase callbacks run only when their boundary is crossed; remaining full or empty does not repeatedly run them.

---

## `set_health`

**Purpose:** Sets a living entity's current health.

**Context:** Default `target`: `SOURCE`; target must be living and alive.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `1` | New health; supports current/max-health and trigger-damage placeholders documented by the implementation. |

### Example

```yaml
type: set_health
target: SOURCE
amount: 'min({max-health}, {health} + 6)'
```

### Behavior and limits

* The value is clamped to the legal health range and hard-capped by the implementation.
* A value of zero may kill the entity.

---

## `set_absorption`

**Purpose:** Sets a damageable entity's absorption amount.

**Context:** Default `target`: `SOURCE`; target must be damageable and alive.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `10` | New absorption value; supports `{health}` for current absorption and `{max-health}` for maximum absorption. |

### Example

```yaml
type: set_absorption
target: SOURCE
amount: 'min({max-health}, {health} + 4)'
```

### Behavior and limits

* Values are hard-capped at 2048 and then capped by the entity's maximum absorption.
* Dead or zero-health entities are skipped.

---

## `attribute_layer`

**Purpose:** Uses a custom attribute as a shared capacity or as the level for child abilities.

**Context:** Default `target`: `SOURCE`; target must be a player and the custom attribute must exist.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `attribute` | `absorption` | Custom attribute ID. Its final player value is used as the capacity/level. |
| `operation` | `ADD` | `ADD`, `SYNC`, or `EXECUTE`. |
| `amount` | `1` | Layers added by `ADD`. |
| `health-per-layer` | `1` | Maximum absorption and absorption health supplied per layer (0.5 heart). |
| `duration` | `140` | Inactivity grace period in ticks before layer decay begins; each `ADD` refreshes it. |
| `decay-interval` | `20` | Ticks between removing individual layers after the lifetime ends. |
| `abilities` | empty | With `EXECUTE`, children run at the attribute's final value as `{level}`. |
| `on-increase` | empty | With `ADD`, children run when the layer count increases. |
| `on-maximum` | empty | With `ADD`, children run when already at capacity. |

### Example

```yaml
type: attribute_layer
target: PLAYER
attribute: dodge
operation: EXECUTE
random: '{attribute:dodge:melee_dodge_chance_decimal}'
abilities:
  cancel:
    type: cancel_event
```

### Behavior and limits

* `EXECUTE` skips its children when the final attribute value is zero; common fields such as `random` are still available on the wrapper.
* `ADD` and `SYNC` manage the shared absorption layer pool identified by player and attribute.
* Each `ADD` refreshes the pool lifetime. After it ends, layers are removed one at a time at `decay-interval` intervals.

---

## `set_air`

**Purpose:** Sets remaining air ticks.

**Context:** Default `target`: `TARGET`; selected entity must be living.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | current air | New remaining air; supports `{air}` and `{max-air}`. |

### Example

```yaml
type: set_air
target: TARGET
amount: 0
```

### Behavior and limits

* The value is clamped between the implementation's lower bound and maximum air.
* Negative air can immediately continue drowning behavior when supported.

---

## `set_food`

**Purpose:** Sets a player's food and saturation.

**Context:** Default `target`: `SOURCE`; selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `food` | `1` | New food level, clamped to `0..20`; `{original}` is current food. |
| `saturation` | `0` | New saturation, clamped to `0..20`; `{original}` is current saturation. |
| `cost` | unset | Enables consumption mode: saturation is spent first, then the remaining cost is rounded up and removed from food. Ignores `food` and `saturation`. |

### Example

```yaml
type: set_food
target: SOURCE
food: 20
saturation: 5
```

### Behavior and limits

* Both fields are applied whenever the ability runs.
* When `cost` is configured, the ability uses consumption mode and clamps the cost to zero or higher.

---

## `set_item_cooldown`

**Purpose:** Sets or clears a player's vanilla cooldown for one material.

**Context:** Default `target`: `TARGET`; selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Player selector. |
| `material` | `SHIELD` | Bukkit or namespaced material whose cooldown group is changed. |
| `ticks` | `duration`, then `0` | Cooldown length in ticks; zero clears the cooldown. |
| `duration` | `0` | Alias used when `ticks` is absent. |

### Example

```yaml
type: set_item_cooldown
target: TARGET
material: minecraft:shield
ticks: 100
```

### Behavior and limits

* Uses Bukkit `Player#setCooldown(Material, int)` and therefore follows the material's vanilla cooldown group.
* Non-player targets and invalid or air materials are skipped.
* Negative durations are clamped to zero.

---

## `set_velocity`

**Purpose:** Sets or modifies an entity's velocity.

**Context:** Default `target`: `TARGET`; directional modes may also require `SOURCE`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `operation` | `SET` | `SET`, `ADD`, `MULTIPLY`, or `SCALE`. |
| `direction` | `VECTOR` | `VECTOR`, `LOOK`, `LOOK_HORIZONTAL`, `RANDOM_HORIZONTAL_SIDE`, `SOURCE_TO_TARGET`, `TARGET_TO_SOURCE`, or `UP`. |
| `x / y / z` | `0` | Configured vector for `VECTOR`. |
| `strength` | `1` | Magnitude for directional modes. |
| `vertical` | `0` | Value added to generated Y velocity. |
| `multiplier` | `1` | Scalar for `SCALE`. |
| `preserve-vertical` | `false` | Uses the target's current Y velocity instead of the generated vector's Y component. |
| `minimum-vertical` | unlimited | Minimum Y velocity used while preserving vertical motion. |
| `minimum-x / minimum-y / minimum-z` | unlimited | Optional lower bounds applied to the final velocity components. |
| `maximum-x / maximum-y / maximum-z` | unlimited | Optional upper bounds applied to the final velocity components. |
| `reset-fall-distance` | `false` | Resets the entity's fall distance after applying velocity. |

### Example

```yaml
type: set_velocity
target: TARGET
operation: SET
direction: SOURCE_TO_TARGET
strength: 1.2
vertical: 0.35
```

### Behavior and limits

* Cross-world source/target directional vectors produce no useful direction.
* `MULTIPLY` is component-wise multiplication.
* `LOOK_HORIZONTAL` ignores look pitch and uses only the normalized horizontal look direction.
* `RANDOM_HORIZONTAL_SIDE` selects horizontal left or right relative to the target's look direction with equal probability. `RANDOM_SIDE` is accepted as an alias.
* Component bounds are applied after the selected operation.

---

## `set_invulnerable`

**Purpose:** Enables or disables entity invulnerability, optionally temporarily.

**Context:** Default `target`: `SOURCE`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Desired invulnerable state. |
| `duration` | `0` | Duration in ticks; non-positive uses persistent behavior. |

### Example

```yaml
type: set_invulnerable
target: SOURCE
value: true
duration: 60
```

### Behavior and limits

* Temporary restoration is scheduled by the ability utility and requires the entity to remain valid.

---

## `potion_effect`

**Purpose:** Applies one potion effect to a living entity.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `potion` | `SLOWNESS` | Potion-effect registry key. |
| `duration` | `100` | Duration in ticks. |
| `infinite-duration-threshold` | `999999` | Positive threshold that converts duration to infinite; zero disables conversion. |
| `amplifier` | `0` | Effect amplifier, minimum 0. |
| `ambient` | `false` | Ambient flag. |
| `particles` | `true` | Show particles. |
| `icon` | `true` | Show HUD icon. |
| `accumulate` | `false` | Add existing duration and retain higher amplifier. |

### Example

```yaml
type: potion_effect
target: TARGET
potion: SLOWNESS
duration: 120
amplifier: 1
accumulate: true
```

### Behavior and limits

* Finite duration is clamped to at least 1 tick.
* Accumulated duration saturates at the integer maximum.

---

## `remove_potion_effect`

**Purpose:** Removes selected active potion effects from a living entity.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `potion` | unset | Single exact effect or selector. |
| `potions` | empty | Ordered selectors; supports exact keys and categories such as `ALL`, `BENEFICIAL`, `HARMFUL`, and `NEUTRAL`. |
| `max-effects` | `-1` | Maximum effects removed; negative means unlimited. |

### Example

```yaml
type: remove_potion_effect
target: TARGET
potions:
  - HARMFUL
  - minecraft:slowness
max-effects: 2
```

### Behavior and limits

* Effects are selected deterministically by registry key within selector rules.

---

## `extend_potion_effects`

**Purpose:** Extends or shortens the remaining duration of selected potion effects.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `potion` | `HARMFUL` | Single exact effect or category selector. |
| `potions` | empty | Ordered selectors; supports `ALL`, `BENEFICIAL`, `HARMFUL`, `NEUTRAL`, and exact effect keys. |
| `percentage` | `0` | Percentage added to each selected effect's duration; negative values shorten it. |
| `multiplier` | unset | Direct duration multiplier; when present, overrides `percentage`. Values below `1` shorten, above `1` extend. |
| `max-effects` | `-1` | Maximum effects modified; negative means unlimited. |

### Example

```yaml
type: extend_potion_effects
target: TARGET
potions:
  - minecraft:poison
  - minecraft:slowness
percentage: 20
```

```yaml
# Shorten harmful effects by 30% when a player gains them.
type: extend_potion_effects
target: PLAYER
potions:
  - HARMFUL
percentage: -30
```

### Behavior and limits

* The amplifier, ambient flag, particles, and icon of each effect are preserved.
* Infinite effects are left unchanged.
* Finite durations are rounded to the nearest tick and saturate at the integer maximum.
* When fired from an `on-potion-effect` trigger, the incoming effect is modified directly; a shortened effect whose duration reaches zero is removed (and the event cancelled).
* Effects are selected deterministically by registry key within selector rules.

---

## `potion_cloud`

**Purpose:** Spawns an area-effect cloud with one custom potion effect.

**Context:** Default `target`: `TARGET`; cloud position uses the resolved trigger location.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `3.0` | Cloud radius. |
| `duration` | `120` | Cloud lifetime in ticks. |
| `potion` | `POISON` | Potion-effect key. |
| `potion-duration` | `100` | Effect duration. |
| `potion-amplifier` | `1` | Effect amplifier. |
| `accumulate` | `false` | Add sampled existing duration and keep higher amplifier. |

### Example

```yaml
type: potion_cloud
potion: POISON
radius: 4
duration: 160
potion-duration: 100
potion-amplifier: 0
```

### Behavior and limits

* Accumulation samples existing nearby effect state when the cloud is created; it is not recalculated independently for each future victim.

---

## `freeze`

**Purpose:** Changes a living entity's freeze ticks.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `freeze-ticks` | `60` | Configured freeze ticks, clamped to at least 0. |
| `accumulate` | `false` | Current implementation: `false` adds existing ticks; `true` replaces existing ticks. |

### Example

```yaml
type: freeze
target: TARGET
freeze-ticks: 80
accumulate: false
```

### Behavior and limits

* The historical `accumulate` option name is inverted relative to current implementation behavior.

---

## `fire`

**Purpose:** Changes a living entity's fire ticks.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `fire-ticks` | `60` | Configured fire ticks, clamped to at least 0. |
| `accumulate` | `false` | Current implementation: `false` adds existing ticks; `true` replaces existing ticks. |

### Example

```yaml
type: fire
target: TARGET
fire-ticks: 100
accumulate: false
```

### Behavior and limits

* The historical `accumulate` option name is inverted relative to current implementation behavior.

---

## `experience`

**Purpose:** Gives raw experience points or fixed experience levels to a player.

**Context:** Default `target`: `SOURCE`; selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `1` | Experience amount to grant; negative values follow platform behavior. |
| `mode` | `POINTS` | `POINTS` grants raw experience points; `LEVELS` grants fixed experience levels. |

### Example

```yaml
type: experience
target: SOURCE
amount: 5
mode: POINTS
```

### Behavior and limits

* `POINTS` calls Bukkit `Player#giveExp`; `LEVELS` calls `Player#giveExpLevels`.

---

## `skill_experience`

**Purpose:** Grants experience to one configured EnchantmentReform skill.

**Context:** Default `target`: `TARGET`; selected entity must be a player and the skill ID must exist.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `skill` | required | Skill ID that receives the experience. |
| `source` | none | Optional `manual: true` source ID from that skill. When set, the source controls the XP formula and anti-abuse behavior. |
| `amount` | `0`, or `1` with `source` | Direct positive XP, or the `{amount}` input passed to a manual source. Supports expressions and power variables. |

### Example

```yaml
type: skill_experience
target: PLAYER
skill: agility
source: projectile_dodge
```

### Behavior and limits

* Direct grants apply normal level-up rewards but do not use source-specific anti-abuse limits.
* A referenced source must set `manual: true`. It appears in the skill source menu and uses the normal source feedback and anti-abuse pipeline without being granted automatically by its declared trigger.

---

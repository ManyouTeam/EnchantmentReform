# 🧾 Trigger-data Conditions

Every registered condition on this page is documented as an independent reference entry. All entries also support the common condition fields documented on the [Power Conditions](README.md) page.

## Registry keys on this page

* `damage_value`
* `damage_cause`
* `damage_origin`
* `spawn_reason`
* `combust_duration`
* `regain_amount`
* `explosion_yield`
* `explosion_radius`
* `target_reason`
* `interaction_action`
* `input_type`
* `event_state`
* `smash_attack_lands`
* `food_change`
* `air_change`
* `state_value`
* `combust_origin`
* `knockback_cause`
* `knockback_reason`
* `exhaustion_reason`
* `villager_trade`
* `cooldown`
* `cooldown_material`

---

## `damage_value`

**Purpose:** Checks the current mutable damage result.

**Context:** Requires a damage-backed trigger and result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: damage_value
min: 1
max: 10
```

### Behavior and limits

* When evaluated after earlier modifiers, it reads the already-modified result.
* Unrelated triggers do not match.

---

## `damage_cause`

**Purpose:** Matches the captured Bukkit `EntityDamageEvent.DamageCause`.

**Context:** Requires damage-cause event context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | empty | Single damage-cause enum name. |
| `values` | empty | Accepted cause names. |

### Example

```yaml
type: damage_cause
values:
  - PROJECTILE
  - ENTITY_ATTACK
```

### Behavior and limits

* Matching is case-insensitive after the plugin's value normalization.
* The condition fails when no damage cause is available.

---

## `damage_origin`

**Purpose:** Classifies damage by its captured origin.

**Context:** Requires damage-origin context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `ANY` | `ANY`, `ENTITY`, `BLOCK`, or `OTHER`. |

### Example

```yaml
type: damage_origin
value: ENTITY
```

### Behavior and limits

* `OTHER` means neither entity-origin nor block-origin.
* Unknown values behave as `ANY` in the shared implementation.

---

## `spawn_reason`

**Purpose:** Matches an entity's Bukkit spawn reason.

**Context:** Default `target`: `TARGET`; the selected entity must expose a spawn reason.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `reason` | empty | Single `CreatureSpawnEvent.SpawnReason`. |
| `reasons` | empty | Accepted reason list. |

### Example

```yaml
type: spawn_reason
target: SOURCE
reasons:
  - NATURAL
  - REINFORCEMENTS
```

### Behavior and limits

* Values are compared with enum names.
* The entity's stored spawn reason may be inspected outside the spawn trigger.

---

## `combust_duration`

**Purpose:** Checks the current combustion-duration result.

**Context:** Requires a combust-event result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: combust_duration
min: 1
max: 10
```

### Behavior and limits

* When evaluated after earlier modifiers, it reads the already-modified result.
* Unrelated triggers do not match.

---

## `regain_amount`

**Purpose:** Checks the current health-regain result.

**Context:** Requires a regain-event result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: regain_amount
min: 1
max: 10
```

### Behavior and limits

* When evaluated after earlier modifiers, it reads the already-modified result.
* Unrelated triggers do not match.

---

## `explosion_yield`

**Purpose:** Checks the current explosion-yield result.

**Context:** Requires an entity-explosion result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: explosion_yield
min: 1
max: 10
```

### Behavior and limits

* When evaluated after earlier modifiers, it reads the already-modified result.
* Unrelated triggers do not match.

---

## `explosion_radius`

**Purpose:** Checks the current primed-explosion radius.

**Context:** Requires an explosion-prime result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: explosion_radius
min: 1
max: 10
```

### Behavior and limits

* When evaluated after earlier modifiers, it reads the already-modified result.
* Unrelated triggers do not match.

---

## `target_reason`

**Purpose:** Matches the captured Bukkit entity-target reason.

**Context:** Requires target/untag event context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | empty | Single reason enum name. |
| `values` | empty | Accepted reason list. |

### Example

```yaml
type: target_reason
values:
  - CLOSEST_PLAYER
  - TARGET_ATTACKED_ENTITY
```

### Behavior and limits

* Matching is case-insensitive after normalization.
* The condition fails when no target reason is present.

---

## `interaction_action`

**Purpose:** Matches a `PlayerInteractEvent.Action`.

**Context:** Requires `PlayerInteractEvent`, normally from `on-interact`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `action` | empty | Single action enum name. |
| `actions` | empty | Accepted action list. |

### Example

```yaml
type: interaction_action
actions:
  - RIGHT_CLICK_AIR
  - RIGHT_CLICK_BLOCK
```

### Behavior and limits

* At least one action is required.

---

## `input_type`

**Purpose:** Matches active, newly pressed, or released Paper player-input controls.

**Context:** Requires `PlayerInputEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `input` | empty | Single input: `FORWARD`, `BACKWARD`, `LEFT`, `RIGHT`, `JUMP`, `SNEAK`, or `SPRINT`. |
| `inputs` | empty | Accepted input list. |
| `state` | `ACTIVE` | `ACTIVE`, `PRESSED`, or `RELEASED`. |

### Example

```yaml
type: input_type
inputs:
  - FORWARD
  - SPRINT
state: PRESSED
```

### Behavior and limits

* `PRESSED` and `RELEASED` compare event input with the player's previous/current input state.
* Unknown state values fall back to `ACTIVE`.

---

## `event_state`

**Purpose:** Matches a `PlayerFishEvent.State`.

**Context:** Requires `PlayerFishEvent`, normally from `on-fish`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `state` | empty | Single fishing state. |
| `states` | empty | Accepted fishing-state list. |

### Example

```yaml
type: event_state
states:
  - BITE
  - CAUGHT_FISH
```

### Behavior and limits

* At least one state is required.

---

## `smash_attack_lands`

**Purpose:** Checks whether a Paper mace smash attempt will land successfully.

**Context:** Requires `EntityAttemptSmashAttackEvent`, normally from `on-attempt-smash-attack` on Paper 26.2+.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Expected successful-smash state. |

### Example

```yaml
type: smash_attack_lands
value: true
```

### Behavior and limits

* `ALLOW` matches, `DENY` does not, and `DEFAULT` follows the event's original result.
* This condition is registered safely on Spigot but can only match the Paper event.

---

## `food_change`

**Purpose:** Checks the pending food-level delta.

**Context:** Requires `FoodLevelChangeEvent` for a player and a mutable food result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive delta range; positive increases food and negative decreases it. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: food_change
compare: '<'
value: 0
```

### Behavior and limits

* The value is pending result food level minus the player's current food level.

---

## `air_change`

**Purpose:** Checks the pending remaining-air delta.

**Context:** Requires `EntityAirChangeEvent` for a player and a mutable air result.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min / max` | unbounded | Inclusive delta range; positive increases air and negative decreases it. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: air_change
compare: '<'
value: 0
```

### Behavior and limits

* The value is the current mutable air result minus `{previous_air}`, captured before the event.
* Earlier power modifiers affect the delta read here; unrelated triggers do not match.

---

## `state_value`

**Purpose:** Reads a numeric state saved by the `state` ability that has not expired.

**Context:** Requires the same power, `owner`, `key`, and `per-target` settings used when the state was written.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `owner` | `SOURCE` | Entity selector for the state owner. Use `PLAYER` when sharing player state across triggers. |
| `key` | `default` | State-pool name matching the `state` ability. |
| `per-target` | `false` | Whether the current target participates in the state key; must match the writer. |
| `state-target` | `TARGET` | Entity selector used in the state key when `per-target: true`; it must resolve to the same entity as the writer. |
| `min / max` | unbounded | Inclusive saved-state range. Missing and expired states read as `0`. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: state_value
owner: PLAYER
key: lava-entry-immunity
min: 1
```

### Behavior and limits

* State is isolated by power ID, so another enchantment or custom item may safely reuse the same `key`.
* A different `owner`, `key`, `per-target`, or resolved `state-target` entity reads a different state pool.

---

## `combust_origin`

**Purpose:** Matches whether combustion was caused by an entity, block, or other source.

**Context:** Requires `EntityCombustEvent` context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `origin` | empty | Single value: `ENTITY`, `BLOCK`, or `OTHER`. |
| `origins` | empty | Accepted origin list. |

### Example

```yaml
type: combust_origin
origins:
  - ENTITY
  - BLOCK
```

### Behavior and limits

* Subclass checks distinguish `EntityCombustByEntityEvent` and `EntityCombustByBlockEvent`.

---

## `knockback_cause`

**Purpose:** Matches Paper `EntityKnockbackEvent.Cause`.

**Context:** Requires `EntityKnockbackEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `cause / causes` | empty | Accepted cause name(s). |
| `reason / reasons` | empty | Aliases merged into the same accepted set. |

### Example

```yaml
type: knockback_cause
causes:
  - ENTITY_ATTACK
```

### Behavior and limits

* At least one configured value is required.

---

## `knockback_reason`

**Purpose:** Alias of `knockback_cause`.

**Context:** Requires `EntityKnockbackEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `cause / causes` | empty | Accepted cause name(s). |
| `reason / reasons` | empty | Accepted aliases. |

### Example

```yaml
type: knockback_reason
reason: ENTITY_ATTACK
```

### Behavior and limits

* Uses exactly the same implementation as `knockback_cause`.

---

## `exhaustion_reason`

**Purpose:** Matches Bukkit `EntityExhaustionEvent.ExhaustionReason`.

**Context:** Requires `EntityExhaustionEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `reason` | empty | Single reason. |
| `reasons` | empty | Accepted reason list. |

### Example

```yaml
type: exhaustion_reason
reasons:
  - SPRINT
  - JUMP_SPRINT
```

### Behavior and limits

* At least one reason is required.

---

## `villager_trade`

**Purpose:** Distinguishes an actual villager trade from another Paper purchase event.

**Context:** Requires `PlayerPurchaseEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | `true` requires `PlayerTradeEvent`; `false` requires another purchase-event subtype. |

### Example

```yaml
type: villager_trade
value: true
```

### Behavior and limits

* This checks the event subtype, not recipe contents.

---

## `cooldown`

**Purpose:** Acquires a condition-scoped runtime cooldown and matches only when it can be acquired.

**Context:** Requires a non-null power context; with positive seconds it also requires `SOURCE`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `seconds` | `0` | Cooldown length in seconds; non-positive values always match. |
| `owner` | `SOURCE` | Entity selector that owns the cooldown. |
| `per-target` | `false` | Keep an independent cooldown for each `TARGET`. |

### Example

```yaml
type: cooldown
owner: PLAYER
seconds: 5
per-target: true
```

### Behavior and limits

* The key includes owner identity, power identity, and this condition's configuration path; with `per-target: true`, it also includes target identity.
* This condition changes state; it is not a read-only check.

---

## `cooldown_material`

**Purpose:** Matches the material involved in Paper `PlayerItemCooldownEvent`.

**Context:** Requires `PlayerItemCooldownEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `material` | empty | Single Bukkit material name or namespaced key. |
| `materials` | empty | Accepted material list. |

### Example

```yaml
type: cooldown_material
materials:
  - ENDER_PEARL
  - minecraft:shield
```

### Behavior and limits

* The event material is compared by enum name and uppercase namespaced key.

---

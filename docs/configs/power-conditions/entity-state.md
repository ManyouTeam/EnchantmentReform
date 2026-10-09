# 🧍 Entity and State Conditions

Every registered condition on this page is documented as an independent reference entry. All entries also support the common condition fields documented on the [Power Conditions](README.md) page.

## Registry keys on this page

* `headshot`
* `height`
* `sneaking`
* `not_sneaking`
* `health`
* `health_percent`
* `has_potion`
* `distance`
* `bounding_box_distance`
* `food_level`
* `attribute_value`
* `skill_level`
* `gliding`
* `on_ground`
* `in_air`
* `falling`
* `fall_distance`
* `match_entity`
* `match_item`
* `catch_match_item`
* `min_attack_cooldown`
* `blocking`
* `sprinting`
* `swimming`
* `flying`
* `climbing`
* `riding`
* `game_mode`
* `potion_effect_type`
* `effect_type`
* `potion_effect_cause`
* `potion_effect_action`
* `ageable`
* `first_attack_against_entity`
* `first_attack_from_monster`
* `first_monster_attack`
* `target_category`
* `fatal_damage`
* `stationary`

---

## `headshot`

**Purpose:** Checks whether the current projectile is vertically close to the target's eye position.

**Context:** Requires a living `TARGET` and projectile `SKILL`; these roles are fixed by the implementation.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `tolerance` | `0.45` | Maximum absolute Y difference between projectile location and target eye location. |

### Example

```yaml
type: headshot
tolerance: 0.4
```

### Behavior and limits

* Only vertical position is checked; this is not an exact hitbox intersection test.
* Use it on projectile damage or hit triggers that provide both roles.

---

## `height`

**Purpose:** Checks the block Y coordinate of the resolved trigger location.

**Context:** Uses `PowerContext.location()` rather than an entity selector.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min` | minimum integer | Inclusive minimum Y. |
| `max` | maximum integer | Inclusive maximum Y. |

### Example

```yaml
type: height
min: 60
max: 100
```

### Behavior and limits

* This condition uses only `min` and `max`; it does not use the shared numeric `compare` form.

---

## `sneaking`

**Purpose:** Checks whether a selected player is sneaking.

**Context:** Default `target`: `TARGET`; the selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected sneaking state. |

### Example

```yaml
type: sneaking
target: SOURCE
value: true
```

### Behavior and limits

* Non-player entities do not match.

---

## `not_sneaking`

**Purpose:** Matches a selected player that is not sneaking.

**Context:** Default `target`: `TARGET`; the selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |

### Example

```yaml
type: not_sneaking
target: SOURCE
```

### Behavior and limits

* Equivalent to `sneaking` with `value: false` for player targets.

---

## `health`

**Purpose:** Checks current or maximum health with the shared numeric-comparison syntax.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `mode` | `CURRENT` | `CURRENT`, `MAX`, or `MAXIMUM`. |
| `min / max` | unbounded | Inclusive numeric range. |
| `compare / value` | unset | Alternative comparison such as `>=`, `<`, or `=`. |

### Example

```yaml
type: health
target: SOURCE
mode: CURRENT
compare: '<='
value: 10
```

### Behavior and limits

* The checked value is exposed as `{original}` and `{current}` while resolving numeric fields.

---

## `health_percent`

**Purpose:** Checks current health as a percentage of maximum health.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `min` | `0` | Inclusive minimum percent. |
| `max` | `100` | Inclusive maximum percent. |
| `min-max-health` | `0` | Inclusive minimum maximum-health value. |
| `max-max-health` | unbounded | Inclusive maximum maximum-health value. |

### Example

```yaml
type: health_percent
target: TARGET
min: 0
max: 25
min-max-health: 20
```

### Behavior and limits

* Percentage is calculated as current health divided by maximum health times 100.
* This condition does not use `compare` / `value`.

---

## `has_potion`

**Purpose:** Checks whether a living entity has a specific active potion effect.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `potion` | empty | Potion-effect namespaced key or supported enum-style name. |

### Example

```yaml
type: has_potion
target: TARGET
potion: minecraft:poison
```

### Behavior and limits

* An invalid or empty effect key does not match.
* Use common `not: true` to require that the effect is absent.

---

## `distance`

**Purpose:** Checks Euclidean distance between two context entities.

**Context:** Default `source`: `SOURCE`; default `target`: `TARGET`; both entities must exist in the same world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `source` | `SOURCE` | First entity selector. |
| `target` | `TARGET` | Second entity selector. |
| `min / max` | unbounded | Inclusive distance range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: distance
source: SOURCE
target: TARGET
min: 4
max: 16
```

### Behavior and limits

* Cross-world entities do not match.

---

## `bounding_box_distance`

**Purpose:** Checks the shortest distance from one entity's position to another entity's bounding box.

**Context:** Default `source`: `SOURCE`; default `target`: `TARGET`; both entities must exist in the same world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `source` | `SOURCE` | Entity whose position is used as the measurement point. |
| `target` | `TARGET` | Entity whose bounding box is measured. |
| `source-eye` | `false` | Use the source living entity's eye position instead of its feet position. |
| `min / max` | unbounded | Inclusive shortest-distance range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: bounding_box_distance
source: PLAYER
target: TARGET
source-eye: true
min: 3
max: 6.5
```

### Behavior and limits

* A point inside the target bounding box has distance `0`.
* If `source-eye` is enabled for a non-living source, the source's normal location is used.
* Cross-world entities do not match.

---

## `food_level`

**Purpose:** Checks a player's current food level.

**Context:** Default `target`: `TARGET`; selected entity must be a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `min / max` | unbounded | Inclusive range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: food_level
target: SOURCE
max: 6
```

### Behavior and limits

* Food level is the integer Bukkit value from 0 to 20.

---

## `attribute_value`

**Purpose:** Compares the current player's final custom-attribute value.

**Context:** Requires a player in the Power context. In attribute-allocation conditions, this is the player spending the points.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `attribute` | required | Custom attribute ID. Bare IDs and the `custom_attribute:` / `enchantmentreform:` forms are accepted. |
| `min / max` | unbounded | Inclusive final-value range. |
| `compare / value` | unset | Numeric comparison such as `>`, `>=`, `<`, `<=`, `=`, or `!=`. |

### Example

```yaml
type: attribute_value
attribute: strength
compare: '>'
value: 10
```

### Behavior and limits

* The final value includes persistent, temporary, and item modifiers.
* A missing attribute or player does not match.

---

## `skill_level`

**Purpose:** Compares the current player's level in a configured skill.

**Context:** Requires a player and a loaded skill.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `skill` | required | Skill ID, such as `agility`. |
| `min / max` | unbounded | Inclusive skill-level range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: skill_level
skill: agility
min: 10
```

### Behavior and limits

* A missing player or unknown skill does not match.

---

## `gliding`

**Purpose:** Checks a living entity's gliding state.

**Context:** Default `target`: `TARGET`; requires a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected state. |

### Example

```yaml
type: gliding
target: SOURCE
value: true
```

### Behavior and limits

* Non-living entities do not match.

---

## `on_ground`

**Purpose:** Checks an entity's on-ground state.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected state. |

### Example

```yaml
type: on_ground
target: TARGET
value: true
```

### Behavior and limits

* The value comes from Bukkit's current on-ground flag.

---

## `in_air`

**Purpose:** Checks the inverse of an entity's on-ground state.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected airborne state. |

### Example

```yaml
type: in_air
target: TARGET
value: true
```

### Behavior and limits

* The selected entity must exist.

---

## `falling`

**Purpose:** Checks whether an airborne entity is moving downward.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `maximum-y` | `0` | The entity's Y velocity must be lower than this value. |
| `value` | `true` | Expected falling state. |

### Example

```yaml
type: falling
target: PLAYER
maximum-y: 0
value: true
```

### Behavior and limits

* An entity standing on the ground never counts as falling.

---

## `fall_distance`

**Purpose:** Compares an entity's accumulated Bukkit fall distance.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` / `compare` | unset / `>=` | Optional direct numeric comparison. |
| `min` | unlimited | Inclusive minimum fall distance. |
| `max` | unlimited | Inclusive maximum fall distance. |

### Example

```yaml
type: fall_distance
target: PLAYER
min: 3
```

### Behavior and limits

* The value comes from Bukkit's current fall-distance counter and is measured in blocks.

---

## `match_entity`

**Purpose:** Runs the normal Match Entity rule engine against a selected living entity.

**Context:** Default `target`: `TARGET`; selected entity must be living.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `match` | required | Nested MatchEntityFormat configuration. |

### Example

```yaml
type: match_entity
target: TARGET
match:
  entity-types:
    - PLAYER
  entity-health:
    min: 1
```

### Behavior and limits

* A missing `match` section fails.
* The nested rules are the same rules used by the plugin's normal entity-matching system.

---

## `match_item`

**Purpose:** Runs MatchItemFormat against one or more selected items.

**Context:** Uses an item selector and optional equipment holder; default item is `CONTEXT`, default holder is `TARGET`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `item` | `CONTEXT` | Item selector string, or nested section with `selector` and `holder`. |
| `item-holder` | `TARGET` | Holder selector when using the flat form. |
| `match` | current section | Nested MatchItemFormat rules; when absent, non-control fields on this section are used. |

### Example

```yaml
type: match_item
item:
  selector: MAIN_HAND
  holder: SOURCE
match:
  material:
    - DIAMOND_SWORD
```

### Behavior and limits

* The condition passes when any selected item matches.
* The matching player is `SOURCE` when it is a player, otherwise `context.player()`.

---

## `catch_match_item`

**Purpose:** Runs MatchItemFormat against the item entity caught by fishing.

**Context:** Requires `PlayerFishEvent` with an `Item` in `event.getCaught()`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `match` | current section | Nested MatchItemFormat rules; when absent, non-control fields on this section are used. |

### Example

```yaml
type: catch_match_item
match:
  material:
    - COD
    - SALMON
```

### Behavior and limits

* Non-item catches and unrelated fishing states fail.

---

## `min_attack_cooldown`

**Purpose:** Checks a player's current attack cooldown strength.

**Context:** Default `target`: `TARGET`; selector must resolve to a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Player selector. |
| `value` | `0` | Inclusive minimum cooldown strength. |

### Example

```yaml
type: min_attack_cooldown
target: SOURCE
value: 0.9
```

### Behavior and limits

* Uses Bukkit `Player#getAttackCooldown()`.

---

## `blocking`

**Purpose:** Checks whether a selected living entity is actively blocking with a shield.

**Context:** Default `target`: `TARGET`; selector must resolve to a living entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Living-entity selector. |
| `value` | `true` | Expected blocking state. |

### Example

```yaml
type: blocking
target: TARGET
value: true
```

### Behavior and limits

* Players use Bukkit's active blocking state.
* Other living entities match when a hand is raised and a shield is equipped in either hand.
* Missing or non-living targets fail; use common `not: true` when missing targets must also pass.

---

## `perfect_guard`

**Purpose:** Matches when a player blocks within a short window after raising a shield.

**Context:** Intended for `on-shield-block`. Default `target`: `TARGET`; the selector must resolve to a player who is currently blocking.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Player selector. |
| `window-millis` | `120` | Maximum elapsed milliseconds since the shield was raised. Supports placeholders and expressions. |
| `consume` | `true` | Removes the recorded raise after a successful match, allowing only one perfect guard per raise. |

### Example

```yaml
type: perfect_guard
target: TARGET
window-millis: '120 + {level} * 60'
consume: true
```

### Behavior and limits

* Right-clicks made with a shield are recorded at millisecond precision.
* Use this on `on-shield-block`; that trigger guarantees the hit was actually blocked and came from a valid shield-facing direction.
* With `consume: true`, later checks for the same shield raise fail after the first successful match.

---

## `sprinting`

**Purpose:** Checks whether a selected player is sprinting.

**Context:** Default `target`: `TARGET`; selector must resolve to a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Player selector. |
| `value` | `true` | Expected sprinting state. |

### Example

```yaml
type: sprinting
target: SOURCE
value: true
```

### Behavior and limits

* Non-player entities fail.

---

## `swimming`

**Purpose:** Checks whether a selected player is swimming.

**Context:** Default `target`: `TARGET`; selector must resolve to a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Player selector. |
| `value` | `true` | Expected swimming state. |

### Example

```yaml
type: swimming
target: PLAYER
value: false
```

### Behavior and limits

* Non-player entities fail.

---

## `flying`

**Purpose:** Checks whether a selected player is flying.

**Context:** Default `target`: `TARGET`; selector must resolve to a player.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Player selector. |
| `value` | `true` | Expected flying state. |

### Example

```yaml
type: flying
target: PLAYER
value: false
```

### Behavior and limits

* Uses the Bukkit player flying state; non-player entities fail.

---

## `climbing`

Checks whether the selected living entity is climbing. Fields are `target` (default `TARGET`) and boolean `value` (default `true`).

```yaml
type: climbing
target: PLAYER
value: false
```

---

## `riding`

Checks whether the selected entity is riding another entity. Fields are `target` (default `TARGET`) and boolean `value` (default `true`).

```yaml
type: riding
target: PLAYER
value: false
```

---

## `game_mode`

Checks the selected player's game mode. Use `mode` for one mode or `modes` for a list; at least one is required.

```yaml
type: game_mode
target: PLAYER
modes:
  - SURVIVAL
  - ADVENTURE
```

---

## `potion_effect_type`

**Purpose:** Matches the potion-effect type supplied by an effect event.

**Context:** Requires Paper `EntityEffectTickEvent` or Bukkit `EntityPotionEffectEvent` context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `selector / selectors` | empty | Exact effect selectors. |
| `effect / effects` | empty | Aliases for effect selectors. |
| `type / types` | empty | Additional selector aliases. |
| `category / categories` | empty | Effect categories supported by `PotionEffectSelector`. |

### Example

```yaml
type: potion_effect_type
selectors:
  - minecraft:poison
  - HARMFUL
```

### Behavior and limits

* The current effect is taken from `getType()` for effect ticks and `getModifiedType()` for potion-effect changes.
* An empty selector set does not match.

---

## `effect_type`

**Purpose:** Alias of `potion_effect_type`.

**Context:** Same event requirements as `potion_effect_type`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `selector / selectors` | empty | Effect selectors. |
| `effect / effects` | empty | Selector aliases. |
| `type / types` | empty | Selector aliases. |
| `category / categories` | empty | Effect categories. |

### Example

```yaml
type: effect_type
category: HARMFUL
```

### Behavior and limits

* Uses the same implementation and behavior as `potion_effect_type`.

---

## `potion_effect_cause`

**Purpose:** Matches the cause of a Bukkit potion-effect change event.

**Context:** Requires `EntityPotionEffectEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `cause / causes` | empty | Accepted `EntityPotionEffectEvent.Cause` name(s), such as `FOOD`, `POTION_DRINK`, or `ATTACK`. |

### Example

```yaml
type: potion_effect_cause
cause: FOOD
```

### Behavior and limits

* At least one configured cause is required.
* Cause names are case-insensitive and normalized to Bukkit enum-style names.

---

## `potion_effect_action`

**Purpose:** Matches the action of a Bukkit potion-effect change event.

**Context:** Requires `EntityPotionEffectEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `action / actions` | empty | Accepted actions, such as `ADDED`, `CHANGED`, `REMOVED`, or `CLEARED`. |

### Example

```yaml
type: potion_effect_action
actions:
  - ADDED
  - CHANGED
```

### Behavior and limits

* At least one configured action is required.
* Action names are case-insensitive and normalized to Bukkit enum-style names.

---

## `ageable`

**Purpose:** Checks whether the context block's ageable data is fully grown.

**Context:** Requires `BLOCK` whose `BlockData` implements Bukkit `Ageable`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Expected fully-grown state. |

### Example

```yaml
type: ageable
value: true
```

### Behavior and limits

* This condition checks blocks/crops, not ageable entities.
* Fully grown means current age is at least maximum age.

---

## `first_attack_against_entity`

**Purpose:** Matches only the first qualifying source-player/target pair for this condition path.

**Context:** Requires player `SOURCE`, living `TARGET`, and a non-cancelled current result.

### Fields

_No type-specific fields._

### Example

```yaml
type: first_attack_against_entity
```

### Behavior and limits

* State identity includes attacker UUID, power ID, configuration path, and target UUID.
* The first successful evaluation stores state immediately; state is cleared by runtime cleanup.

---

## `first_attack_from_monster`

**Purpose:** Matches the first tracked attack received from a specific monster for this condition path.

**Context:** Requires `SOURCE` to be a `Monster` and a player defender from `PLAYER` or `TARGET`; cancelled results fail.

### Fields

_No type-specific fields._

### Example

```yaml
type: first_attack_from_monster
```

### Behavior and limits

* State identity includes defender, power ID, condition path, and monster.
* State lasts until runtime cleanup, reload, or relevant entity unload.

---

## `first_monster_attack`

**Purpose:** Alias of `first_attack_from_monster`.

**Context:** Same monster and player context requirements as `first_attack_from_monster`.

### Fields

_No type-specific fields._

### Example

```yaml
type: first_monster_attack
```

### Behavior and limits

* Uses exactly the same state and matching implementation.

---

## `target_category`

**Purpose:** Matches the fixed category of `TARGET`.

**Context:** Requires `TARGET`; no configurable selector is used.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `category` | `ANIMAL` | Supported values: `MONSTER`, `ANIMAL`, or `UNDEAD`. |

### Example

```yaml
type: target_category
category: UNDEAD
```

### Behavior and limits

* The current undead set contains zombie, husk, drowned, skeleton, stray, wither skeleton, wither, zombie villager, zombified piglin, phantom, and bogged.
* Players and unsupported categories do not match.

---

## `fatal_damage`

**Purpose:** Checks whether the current damage event's final damage would kill `SOURCE`.

**Context:** Requires living `SOURCE` and an `EntityDamageEvent`; the entity selector is fixed.

### Fields

_No type-specific fields._

### Example

```yaml
type: fatal_damage
```

### Behavior and limits

* Uses `event.getFinalDamage()` compared with current source health.
* It does not read the plugin's mutable damage result.

---

## `stationary`

**Purpose:** Checks whether a selected entity has remained within a small movement tolerance for a duration.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `seconds` | `3` | Required stationary duration in seconds. |

### Example

```yaml
type: stationary
target: SOURCE
seconds: 5
```

### Behavior and limits

* Moving more than squared distance `0.01` or changing worlds resets the timer.
* State is cleaned after entity unload, plugin unload, or 30 minutes without access.

---

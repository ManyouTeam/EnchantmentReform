# 🏹 Projectile Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `launch_projectile`
* `shulker_bullet`
* `reflect_projectile`
* `ricochet_projectile`
* `homing_projectile`

---

## `launch_projectile`

**Purpose:** Spawns and configures one or more projectile entities from a living shooter.

**Context:** When `source` is present it selects the shooter; otherwise `target` selects the shooter. `TARGET` remains the aim target.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `entity-type` | `ARROW` | Spawnable Bukkit entity type implementing `Projectile`. |
| `speed` | `1.5` | Launch speed. |
| `extra-y` | `0` | Additional Y component. |
| `count` | `1` | Projectile count, minimum 1. |
| `spread-degrees` | `0` | Maximum fan angle; projectiles are evenly distributed. |
| `spawn-offset` | `0` | Forward offset from shooter eye. |
| `damage` | unset | Ability-damage override stored on projectile. |
| `inherit-powers` | `false` | Track projectile for projectile-tick/hit continuation. |
| `fireball-yield` | `1.0` | Yield for explosive projectiles. |
| `fireball-incendiary` | `true` | Incendiary flag for fireballs. |
| `potion-type` | unset | Base potion type for thrown potions. |
| `potion` | unset | Single custom effect when `potion-effects` is absent. |
| `duration` | `100` | Single custom-effect duration. |
| `amplifier` | `0` | Single custom-effect amplifier. |
| `potion-effects` | unset | Map of custom effects with optional potion, duration, and amplifier. |

### Example

```yaml
type: launch_projectile
source: SOURCE
entity-type: SPLASH_POTION
speed: 1.3
count: 3
spread-degrees: 12
inherit-powers: true
potion-effects:
  poison:
    potion: POISON
    duration: 100
    amplifier: 0
```

### Behavior and limits

* A living same-world target is aimed at; otherwise shooter look direction is used.
* Non-projectile spawn results are removed and shulker bullets receive the current target.
* `inherit-powers` tracks the projectile with the current player, trigger item, and equipment slot.

---

## `shulker_bullet`

**Purpose:** Compatibility alias of `launch_projectile`.

**Context:** Uses the same shooter selection, aim target, and fields as `launch_projectile`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `entity-type` | `ARROW` | Normally set to `SHULKER_BULLET`. |
| `speed` | `1.5` | Launch speed. |
| `extra-y` | `0` | Additional Y component. |
| `count` | `1` | Projectile count, minimum 1. |
| `spread-degrees` | `0` | Maximum fan angle; projectiles are evenly distributed. |
| `spawn-offset` | `0` | Forward offset from shooter eye. |
| `damage` | unset | Ability-damage override stored on projectile. |
| `inherit-powers` | `false` | Track projectile for projectile-tick/hit continuation. |
| `fireball-yield` | `1.0` | Yield for explosive projectiles. |
| `fireball-incendiary` | `true` | Incendiary flag for fireballs. |
| `potion-type` | unset | Base potion type for thrown potions. |
| `potion` | unset | Single custom effect when `potion-effects` is absent. |
| `duration` | `100` | Single custom-effect duration. |
| `amplifier` | `0` | Single custom-effect amplifier. |
| `potion-effects` | unset | Map of custom effects with optional potion, duration, and amplifier. |

### Example

```yaml
type: shulker_bullet
entity-type: SHULKER_BULLET
target: SOURCE
speed: 1.2
```

### Behavior and limits

* The spawned shulker bullet automatically targets the current context target when available.
* All other behavior matches this repository's `launch_projectile` implementation.

---

## `reflect_projectile`

**Purpose:** Redirects the projectile from the current context back toward its attacker.

**Context:** `target` defaults to `SKILL` and must resolve to a projectile. `shooter` defaults to `PLAYER`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SKILL` | Entity selector for the projectile to reflect. |
| `shooter` | `PLAYER` | New projectile shooter after reflection. |
| `velocity-multiplier` | `1.0` | Multiplier applied after reversing the incoming velocity. |
| `damage-multiplier` | `1.0` | Multiplier applied when the reflected projectile later deals damage. |
| `reflection-cooldown` | `0` | Per-player reflection cooldown in seconds. |
| `minimum-velocity-squared` | `0.08` | Velocities below this squared length use the fallback direction. |
| `fallback-speed` | `0.95` | Speed used toward the original attacker for a near-stationary projectile. |
| `origin-distance` | `0.55` | Distance in front of the player where the reflected projectile is placed. |
| `abilities` | unset | Nested abilities executed only after a successful reflection. |

### Example

```yaml
type: reflect_projectile
target: SKILL
shooter: PLAYER
random: 0.5
reflection-cooldown: 1.2
velocity-multiplier: 0.85
damage-multiplier: 0.75
abilities:
  effect:
    type: sound
    location: PLAYER
    sound: ITEM_SHIELD_BLOCK
```

### Behavior and limits

* The current hit event is cancelled after reflection succeeds.
* Arrows are replaced with fresh reflected copies so their completed-impact state cannot make them fall; other projectile types are redirected in place.
* Entity type and payload data are preserved while shooter, position, velocity, and damage multiplier are updated.
* Existing projectile power tracking is stopped so the original shooter's projectile abilities do not continue after ownership changes.
* A reflected projectile is marked and cannot be reflected by this ability again.
* Nested abilities are useful for standard `particle` and `sound` effects without duplicating their implementations.

---

## `ricochet_projectile`

**Purpose:** Replaces an arrow that hit a block with a reflected copy that continues travelling away from the impacted surface.

**Context:** Requires `on-projectile-hit`, a block impact, and `SKILL` resolving to an `AbstractArrow`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `maximum-ricochets` | `1` | Maximum chained block ricochets, clamped to `0..12`. |
| `speed-bonus-per-ricochet` | `0.1` | Fractional speed increase applied at every ricochet. |
| `damage-bonus-per-ricochet` | `0` | Flat damage accumulated by the arrow after every ricochet. |
| `minimum-velocity-squared` | `0.09` | Minimum resolved incoming velocity squared required to ricochet. |
| `minimum-live-velocity-squared` | `0.0004` | Below this value, arrow facing supplies the incoming direction. |
| `minimum-post-ricochet-speed` | `0.45` | Minimum speed before applying the per-ricochet bonus. |
| `maximum-post-ricochet-speed` | `1.5` | Maximum final speed after applying the per-ricochet bonus. |
| `maximum-ricochet-distance` | `24` | Maximum cumulative flight distance after each bounce; non-positive disables the limit. |
| `surface-offset` | `0.22` | Spawn offset away from the impacted surface. |
| `direction-offset` | `0.14` | Additional spawn offset along the reflected direction. |
| `abilities` | unset | Nested abilities executed only after a successful ricochet. |

### Example

```yaml
type: ricochet_projectile
maximum-ricochets: 3
speed-bonus-per-ricochet: 0.2
damage-bonus-per-ricochet: 1.5
abilities:
  sparks:
    type: particle
    particle: ELECTRIC_SPARK
    location: CONTEXT
```

### Behavior and limits

* Reflection uses the impacted block face normal.
* Arrow type, potion payload, shooter, pickup state, persistent data, and relevant combat properties are copied to the replacement.
* Projectile power tracking is inherited so later block impacts can ricochet again.
* Flat bonus damage accumulates across ricochets and is added when the arrow damages an entity.
* Use nested standard `particle` and `sound` abilities for impact effects.

---

## `homing_projectile`

**Purpose:** Steers the current projectile toward a locked or newly selected living target.

**Context:** Requires `SKILL` to be a valid projectile; normally used on a projectile-tick trigger.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `16` | Maximum target search/lock radius. |
| `strength` | `0.2` | Steering blend, clamped `0.01..1`. |
| `require-line-of-sight` | `false` | Require line of sight. |
| `max-angle` | `360` | Maximum cone half-angle around current heading. |
| `lead` | `0` | Target-motion prediction factor. |
| `max-ticks` | `100` | Maximum executions; non-positive disables expiry. |
| `remove-on-expire` | `true` | Remove projectile when expired. |
| `disable-gravity` | `false` | Disable gravity while homing. |

### Example

```yaml
type: homing_projectile
radius: 24
strength: 0.18
max-angle: 90
lead: 0.5
max-ticks: 120
```

### Behavior and limits

* The projectile retains its previous valid target and rescans only when needed.
* Creative and spectator players are ignored.
* Target priority is existing lock, source mob target, then best nearby candidate.
* Expiry returns a cancellation request so projectile tracking can stop.

---

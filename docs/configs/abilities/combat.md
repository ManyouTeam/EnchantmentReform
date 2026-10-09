# ⚔️ Combat and Movement Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `explosion`
* `lightning`
* `damage_entity`
* `damage_item`
* `pull_target`
* `pull_location`
* `teleport_near_target`
* `teleport`
* `guardian_beam`
* `sonic_boom`
* `evoker_fangs`
* `arrow_rain`
* `creeper_stats`
* `swap_health`
* `swap_potion_effects`
* `swap_locations`

---

## `explosion`

**Purpose:** Creates an explosion and an explosion-emitter particle at the resolved location.

**Context:** Default `target`: `TARGET`; selected target is supplied as the explosion source where supported.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `yield` | `2.0` | Explosion strength. |
| `set-fire` | `false` | Whether the explosion may ignite blocks. |
| `break-blocks` | `false` | Whether the explosion may destroy blocks. |

### Example

```yaml
type: explosion
target: TARGET
yield: 3
set-fire: false
break-blocks: false
```

### Behavior and limits

* Damage is wrapped as direct ability damage to avoid recursively firing the same power chain.

---

## `lightning`

**Purpose:** Creates visual lightning strikes around a target and directly damages nearby living entities.

**Context:** Default `target`: `TARGET`; `source` defaults to `SOURCE` for damage attribution.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `count` | `2` | Number of strike attempts, minimum 1. |
| `radius` | `3.0` | Horizontal random offset and nearby-damage radius. |
| `damage` | `4.0` | Damage applied near each strike. |

### Example

```yaml
type: lightning
target: TARGET
source: SOURCE
count: 3
radius: 4
damage: 6
```

### Behavior and limits

* The lightning is visual; configured damage is applied separately.
* Overlapping strike areas may damage the same entity more than once.

---

## `damage_entity`

**Purpose:** Deals a separate direct-damage instance to a living entity.

**Context:** Default `target`: `TARGET`; `source` chooses damage attribution and defaults to `SOURCE`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `1` | Damage amount; supports current/original trigger damage placeholders. |

### Example

```yaml
type: damage_entity
target: TARGET
source: SOURCE
amount: '{original} * 0.5'
```

### Behavior and limits

* Amount is clamped to at least zero.
* Direct ability damage is guarded against recursive power triggering.

---

## `damage_item`

**Purpose:** Damages equipped items in selected slots.

**Context:** Default `target`: `TARGET`; requires a living entity with equipment.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `1` | Durability damage; may use trigger damage placeholders. |
| `slots` | empty | `MAIN_HAND`, `OFF_HAND`, armor slots, or `ARMOR`. |
| `mode` | `DIRECT` | `DIRECT` edits item damage metadata; `PAPER` uses Paper item-damage behavior. |

### Example

```yaml
type: damage_item
target: TARGET
amount: 2
mode: PAPER
slots:
  - ARMOR
  - MAIN_HAND
```

### Behavior and limits

* Air, unbreakable, and non-damageable items are skipped.
* Paper mode allows normal platform durability behavior such as Unbreaking and break events.

---

## `pull_target`

**Purpose:** Pulls a target toward the source, pushes it away, or randomly chooses direction.

**Context:** Default `target`: `TARGET`; `source` defaults to `SOURCE` and acts as direction origin.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `direction` | `TOWARD` | `TOWARD`, `AWAY`/`AWAY_FROM_SOURCE`, or `RANDOM`. |
| `speed` | `strength` or `1` | Velocity magnitude. |
| `strength` | `1` | Legacy alias when `speed` is absent. |
| `vertical` | automatic | Exact Y velocity; when absent the implementation supplies a small upward component. |
| `remove-powder-snow` | `false` | Replace powder snow at target feet before moving. |

### Example

```yaml
type: pull_target
source: SOURCE
target: TARGET
direction: TOWARD
speed: 1.1
vertical: 0.25
```

### Behavior and limits

* When source and target overlap, source look direction is used as fallback.

---

## `pull_location`

**Purpose:** Moves a selected entity toward a resolved location.

**Context:** Default `target`: `TARGET`; requires target and destination in the same world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `speed` | `strength` or `1` | Velocity magnitude. |
| `strength` | `1` | Alias used when `speed` is absent. |
| `vertical` | calculated | Optional exact Y velocity. |
| `preserve-momentum` | `false` | Add current X/Z velocity to the generated vector. |
| `location` | `CONTEXT` | Extended location selector and optional offsets. |

### Example

```yaml
type: pull_location
target: TARGET
speed: 0.8
vertical: 0.25
preserve-momentum: true
location:
  target: SOURCE
```

### Behavior and limits

* Non-positive speed, cross-world locations, or nearly identical positions are skipped.
* When `vertical` is absent, Y comes from the normalized direction vector.

---

## `teleport_near_target`

**Purpose:** Teleports a living entity to a random safe location near its current position.

**Context:** Default `target`: `TARGET`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min-radius` | `3.0` | Minimum horizontal distance, at least 1. |
| `max-radius` | `8.0` | Maximum horizontal distance, at least `min-radius`. |
| `max-tries` | `12` | Random candidate attempts, minimum 1. |

### Example

```yaml
type: teleport_near_target
target: SOURCE
min-radius: 4
max-radius: 10
max-tries: 16
```

### Behavior and limits

* Each candidate searches around its Y position for passable feet/head blocks over a solid block.
* Yaw and pitch are preserved and teleporting uses scheduler-safe handling.

---

## `teleport`

**Purpose:** Teleports a selected entity to an extended context or important player location.

**Context:** Default `target`: `SOURCE`; destination may use a normal location selector or an important-location mode.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `location` | `CONTEXT` | Flat selector or nested section with `target`, `owner`, `world`, and `offset`. |
| `location-owner / location.owner` | selected entity | Entity used for player-specific destinations. |
| `world / location.world` | owner/context world | World used by `WORLD_SPAWN`. |
| `offset.x/y/z or location.offset.x/y/z` | `0` | Destination offset. |
| `important locations` | — | `BED_SPAWN`, `RESPAWN`, `WORLD_SPAWN`, `MAIN_WORLD_SPAWN`, `LAST_DEATH`, `COMPASS_TARGET` and documented aliases. |

### Example

```yaml
type: teleport
target: SOURCE
location:
  target: WORLD_SPAWN
  world: world
  offset:
    y: 1
```

### Behavior and limits

* Player-only destinations return no location for non-player owners.
* Missing personal respawn falls back to current-world spawn.
* Teleporting uses `SchedulerUtil.teleport`.

---

## `guardian_beam`

**Purpose:** Displays a virtual guardian beam, waits for charge, then damages the target.

**Context:** Requires living `SOURCE` and living `TARGET`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `range` | `18.0` | Maximum distance at start and completion. |
| `charge-ticks` | `30` | Charge time, minimum 1. |
| `damage` | `6.0` | Final damage, minimum 0. |

### Example

```yaml
type: guardian_beam
target: TARGET
range: 20
charge-ticks: 35
damage: 8
```

### Behavior and limits

* Requires PacketEvents.
* Damage is skipped if entities become invalid/dead, change world, or move out of range before completion.

---

## `sonic_boom`

**Purpose:** Charges and fires a Warden-style sonic boom with particles, sound, damage, and knockback.

**Context:** Requires living `SOURCE` and living `TARGET`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `range-xz` | `15` | Horizontal range. |
| `range-y` | `20` | Maximum vertical difference. |
| `charge-ticks` | `34` | Delay before firing. |
| `charge-volume / charge-pitch` | `3 / 1` | Charge sound. |
| `boom-volume / boom-pitch` | `3 / 1` | Boom sound. |
| `particle-extra-steps` | `7` | Extra particle positions. |
| `damage` | `10` | Direct damage. |
| `knockback-horizontal` | `2.5` | Horizontal knockback multiplier. |
| `knockback-vertical` | `0.5` | Vertical-direction multiplier. |

### Example

```yaml
type: sonic_boom
target: TARGET
charge-ticks: 30
damage: 12
knockback-horizontal: 2
knockback-vertical: 0.4
```

### Behavior and limits

* Range and entity validity are checked again after charging.

---

## `evoker_fangs`

**Purpose:** Creates vanilla evoker-fang attack patterns between source and target.

**Context:** Requires living `SOURCE` and living `TARGET`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `damage` | `6.0` | Damage stored on each fang for direct-damage handling. |
| `close-range` | `3.0` | Below this distance, ring patterns are used; otherwise a line pattern is used. |

### Example

```yaml
type: evoker_fangs
target: TARGET
damage: 8
close-range: 4
```

### Behavior and limits

* Fangs search downward for solid ground.
* The selected living source becomes fang owner.

---

## `arrow_rain`

**Purpose:** Spawns downward-moving arrows in a random square above a target location.

**Context:** Default `target`: `TARGET`; `source` controls shooter attribution.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `count` | `10` | Arrow count, minimum 1. |
| `radius` | `4.0` | Horizontal spread, minimum 0.1. |
| `height` | `12.0` | Spawn height, minimum 2. |
| `spread` | `0.18` | Random X/Z velocity spread. |
| `damage` | `-1` | Arrow base damage; negative keeps Bukkit default. |
| `pierce-level` | `0` | Pierce level, minimum 0. |
| `critical` | `false` | Critical flag. |

### Example

```yaml
type: arrow_rain
target: TARGET
source: SOURCE
count: 16
radius: 5
height: 14
damage: 4
```

### Behavior and limits

* Arrow pickup is disabled.

---

## `creeper_stats`

**Purpose:** Changes selected creeper properties.

**Context:** Default `target`: `SOURCE`; selected entity must be a creeper.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `explosion-radius` | unchanged | New radius, minimum 0. |
| `fuse-ticks` | unchanged | New maximum fuse ticks, minimum 1. |
| `powered` | unchanged | Charged state. |

### Example

```yaml
type: creeper_stats
target: SOURCE
explosion-radius: 6
fuse-ticks: 30
powered: true
```

### Behavior and limits

* Only fields present in configuration are changed.

---

## `swap_health`

**Purpose:** Swaps current health percentages between source and target.

**Context:** Requires living `SOURCE` and living `TARGET`; configurable selectors are not used by the shared implementation.

### Fields

_No type-specific fields._

### Example

```yaml
type: swap_health
```

### Behavior and limits

* Absolute health is recalculated from each entity's own maximum.
* Results are clamped above zero, so this ability does not directly kill either entity.

---

## `swap_potion_effects`

**Purpose:** Transfers or swaps selected active potion effects between source and target.

**Context:** Uses common `source` and `target`; both must be different living entities.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `direction` | `SOURCE_TO_TARGET` | `SOURCE_TO_TARGET`, `TARGET_TO_SOURCE`, or `SWAP` aliases. |
| `potion` | unset | Single effect selector. |
| `potions` | `ALL` | Effect selectors, including categories. |
| `max-effects` | `-1` | Maximum selected types; negative means unlimited. |

### Example

```yaml
type: swap_potion_effects
source: SOURCE
target: TARGET
direction: SWAP
potions:
  - HARMFUL
max-effects: 3
```

### Behavior and limits

* Transfer modes remove effects from origin and add them to destination.
* Swap exchanges each selected effect type; one-sided effects move to the other entity.

---

## `swap_locations`

**Purpose:** Swaps the locations of two entities.

**Context:** Uses common `source` and `target` selectors.

### Fields

_No type-specific fields._

### Example

```yaml
type: swap_locations
source: SOURCE
target: TARGET
```

### Behavior and limits

* Entities must be distinct and in the same world.
* Teleports use scheduler-safe handling.

# 🌍 Environment and Location Conditions

Every registered condition on this page is documented as an independent reference entry. All entries also support the common condition fields documented on the [Power Conditions](README.md) page.

## Registry keys on this page

* `in_water`
* `in_lava`
* `liquid_surface_distance`
* `liquid_transition`
* `in_rain`
* `in_sunlight`
* `in_structure`
* `light_level`
* `clear_weather`
* `storm`
* `thunder`
* `world`
* `night`
* `environment`
* `block_type`
* `block_type_offset`
* `best_tool`
* `preferred_tool`
* `biome_changed`
* `block_break_time`
* `break_time`

---

## `in_water`

**Purpose:** Checks Bukkit's `Entity#isInWater` state.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected state. |

### Example

```yaml
type: in_water
target: SOURCE
value: true
```

### Behavior and limits

* The selected entity must exist.

---

## `in_lava`

**Purpose:** Checks whether the block at the selected entity's feet is lava.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected state. |

### Example

```yaml
type: in_lava
target: SOURCE
value: true
```

### Behavior and limits

* This checks the current feet block material.

---

## `liquid_surface_distance`

**Purpose:** Checks the vertical distance from the selected entity's eyes to the liquid surface directly above.

**Context:** Default `target`: `PLAYER`; requires an entity currently in water or lava.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `PLAYER` | Entity selector. Living entities use eye position; other entities use their own location. |
| `liquid` | `ANY` | Accepts `ANY`, `WATER`, or `LAVA`. |
| `min / max` | unbounded | Vertical distance range to the surface, in blocks. |
| `compare / value` | unset | Alternative numeric comparison. |
| `scan-limit` | world height limit | Maximum upward scan distance; supports math expressions and variables. |

### Example

```yaml
type: liquid_surface_distance
target: PLAYER
liquid: ANY
max: '{level}'
```

### Behavior and limits

* Scans only upward through contiguous liquid in the entity's X/Z column; it does not search sideways.
* Water includes water, bubble columns, kelp, seagrass, and waterlogged blocks; lava includes source and flowing lava.
* Water and lava do not connect to one another. A non-passable block sealing the liquid prevents a match.
* The condition returns `false` when the entity is outside the selected liquid, `liquid` is invalid, or no surface is found within the scan range.

---

## `liquid_transition`

**Purpose:** Checks whether a player has just entered or left a configured liquid while moving.

**Context:** Only applies to `PlayerMoveEvent` supplied by `on-move`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `liquid` | `ANY` | Accepts `ANY`, `WATER`, or `LAVA`. |
| `transition` | `ENTER` | Accepts `ENTER` or `EXIT`; `LEAVE` aliases `EXIT`. |

### Example

```yaml
type: liquid_transition
liquid: LAVA
transition: ENTER
```

### Behavior and limits

* Compares the feet blocks at the movement event's `from` and `to` locations and matches only when the configured state changes.
* `ANY` means either supported liquid. Moving directly from water into lava is not entering `ANY`, but it is entering `LAVA`.
* Water and lava detection follows the same rules as `liquid_surface_distance`.

---

## `in_rain`

**Purpose:** Checks Bukkit's `Entity#isInRain` state.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected state. |

### Example

```yaml
type: in_rain
target: SOURCE
value: true
```

### Behavior and limits

* The selected entity must exist.

---

## `in_sunlight`

**Purpose:** Checks a simplified daylight and sky-light exposure rule.

**Context:** Default `target`: `TARGET`; requires an entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `value` | `true` | Expected state. |

### Example

```yaml
type: in_sunlight
target: SOURCE
value: true
```

### Behavior and limits

* Sunlight is true when world time is below 12300 and sky light at the entity block is at least 15.
* Weather is not checked directly.

---

## `in_structure`

**Purpose:** Checks whether a location lies inside one of the configured generated-structure bounding boxes.

**Context:** Default `target`: `TARGET`; if the entity is unavailable, the trigger location is used.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `TARGET` | Entity selector. |
| `structure` | empty | Single namespaced structure key. |
| `structures` | empty | List of accepted structure keys. |

### Example

```yaml
type: in_structure
target: SOURCE
structures:
  - minecraft:fortress
  - minecraft:bastion_remnant
```

### Behavior and limits

* Only structures reported for the current chunk are inspected.
* The resolved point must be inside the generated structure bounding box.

---

## `light_level`

**Purpose:** Checks total block light.

**Context:** Uses the source/location required by the implementation; no configurable selector in the shared implementation.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `min` | `0` | Inclusive minimum light level. |
| `max` | `15` | Inclusive maximum light level. |

### Example

```yaml
type: light_level
min: 0
max: 7
```

### Behavior and limits

* Reads the combined block light value.

---

## `clear_weather`

**Purpose:** Checks whether the context world has no storm.

**Context:** Requires a resolved location and world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Expected clear-weather state. |

### Example

```yaml
type: clear_weather
value: true
```

### Behavior and limits

* `value: false` requires stormy weather but does not specifically require thunder.

---

## `storm`

**Purpose:** Checks whether the context world currently has a storm.

**Context:** Requires `LOCATION` and a world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Expected storm state. |

### Example

```yaml
type: storm
value: true
```

### Behavior and limits

* A missing location/world fails.

---

## `thunder`

**Purpose:** Checks whether the context world is thundering.

**Context:** Requires a resolved location and world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Expected thundering state. |

### Example

```yaml
type: thunder
value: true
```

### Behavior and limits

* A missing location or world does not match.

---

## `world`

**Purpose:** Matches the world name at the trigger location.

**Context:** Requires a resolved location and world.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `world` | empty | Single world name. |
| `worlds` | empty | List of accepted world names. |

### Example

```yaml
type: world
worlds:
  - world
  - world_nether
```

### Behavior and limits

* At least one name is required.
* Matching uses the actual Bukkit world name.

---

## `night`

**Purpose:** Checks a fixed vanilla-style night time range.

**Context:** Requires a valid world from the source/context.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `value` | `true` | Expected night state. |

### Example

```yaml
type: night
value: true
```

### Behavior and limits

* Night is defined as world time 12300 through 23850 inclusive.
* Weather and sky visibility are not considered.

---

## `environment`

**Purpose:** Matches a world dimension and optionally a biome.

**Context:** Default `target`: `SOURCE`; selected entity must exist.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Entity selector. |
| `dimension` | empty | Bukkit world environment such as `NORMAL`, `NETHER`, or `THE_END`. |
| `biomes` | empty | Accepted biome enum names or namespaced keys. |

### Example

```yaml
type: environment
target: SOURCE
dimension: NETHER
biomes:
  - BASALT_DELTAS
  - minecraft:soul_sand_valley
```

### Behavior and limits

* If `dimension` is configured it must match before biome checks.
* When both dimension and biome lists are empty, the condition fails.

---

## `block_type`

**Purpose:** Matches the event block or pre-break block snapshot against block specifications.

**Context:** Requires `BLOCK` or a captured broken-block state.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `type-name` | empty | Single block specification. |
| `types` | empty | List of block specifications. |

### Example

```yaml
type: block_type
types:
  - DIAMOND_ORE
  - '#minecraft:logs'
```

### Behavior and limits

* The pre-break snapshot is preferred because the live block may already be air.
* Matching uses `BlockPriceUtil`, so supported custom/block syntax is shared with other block filters.

---

## `block_type_offset`

**Purpose:** Matches a block at an offset from the context block.

**Context:** Requires `BLOCK`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `type-name` | empty | Single block specification. |
| `types` | empty | Accepted block specifications. |
| `offset.x / offset.y / offset.z` | `0` | Relative block coordinates. |

### Example

```yaml
type: block_type_offset
types:
  - FARMLAND
offset:
  y: -1
```

### Behavior and limits

* Matching uses `BlockPriceUtil`.

---

## `best_tool`

**Purpose:** Checks Bukkit's preferred-tool result for the context block and a player's held item.

**Context:** Default `target`: `SOURCE`; requires player and `BLOCK`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Player selector. |
| `hand` | `MAIN_HAND` | `MAIN_HAND` or `OFF_HAND`. |
| `value` | `true` | Expected preferred-tool result. |

### Example

```yaml
type: best_tool
target: SOURCE
hand: MAIN_HAND
value: true
```

### Behavior and limits

* Uses `Block#isPreferredTool(ItemStack)`.

---

## `preferred_tool`

**Purpose:** Alias of `best_tool`.

**Context:** Same player, block, and hand requirements as `best_tool`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Player selector. |
| `hand` | `MAIN_HAND` | Hand selector. |
| `value` | `true` | Expected result. |

### Example

```yaml
type: preferred_tool
target: SOURCE
value: true
```

### Behavior and limits

* Uses the same implementation as `best_tool`.

---

## `biome_changed`

**Purpose:** Checks whether a player movement event crossed a biome boundary.

**Context:** Requires `PlayerMoveEvent` context with non-null `to` in the same world.

### Fields

_No type-specific fields._

### Example

```yaml
type: biome_changed
```

### Behavior and limits

* Compares the biomes of the `from` and `to` blocks.

---

## `block_break_time`

**Purpose:** Calculates the number of ticks/time required for a player to break the context block.

**Context:** Default `target`: `SOURCE`; requires player and `BLOCK`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Player selector. |
| `unit` | `TICKS` | `TICKS`, `SECONDS`, or `MILLISECONDS`. |
| `min / max` | unbounded | Inclusive time range. |
| `compare / value` | unset | Alternative numeric comparison. |

### Example

```yaml
type: block_break_time
target: SOURCE
unit: TICKS
max: 10
```

### Behavior and limits

* Uses Bukkit `Block#getBreakSpeed(player)` and `ceil(1 / progressPerTick)`.
* A non-positive break speed is treated as positive infinity.

---

## `break_time`

**Purpose:** Alias of `block_break_time`.

**Context:** Same player and block context as `block_break_time`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Player selector. |
| `unit` | `TICKS` | Time unit. |
| `min / max` | unbounded | Inclusive range. |
| `compare / value` | unset | Alternative comparison. |

### Example

```yaml
type: break_time
unit: SECONDS
max: 1.5
```

### Behavior and limits

* Uses exactly the same calculation as `block_break_time`.

---

# 🧱 Block and World Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `nearby_block`
* `break_blocks`
* `break_block`
* `prevent_block_break`
* `replace_block`
* `change_block_face`
* `grow_crop`
* `accelerate_crops`
* `accelerate_work_blocks`
* `scan_blocks`
* `locate_structure`
* `locate_biome`


---

## `nearby_block`

**Purpose:** Runs child abilities for nearby blocks ordered by distance from a resolved center.

**Context:** Requires a resolved world location and nested abilities.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `1` | Default radius on all axes, minimum 0. |
| `radius-x / radius-y / radius-z` | `radius` | Per-axis radii. |
| `max-blocks` | `0` | Maximum processed blocks; zero means unlimited. |
| `abilities` | required | Children executed with each block as context. |

### Example

```yaml
type: nearby_block
radius-x: 3
radius-y: 1
radius-z: 3
max-blocks: 16
abilities:
  grow:
    type: grow_crop
    stages: 1
```

### Behavior and limits

* Every block in the cuboid is visited nearest-first; this ability does not itself filter materials.
* The result records how many blocks actually changed after child execution.

---

## `break_blocks`

**Purpose:** Selects, filters, and breaks a bounded group of blocks in batches.

**Context:** Requires a player `breaker`, an origin block/location, tool context, and protection permission.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `breaker` | `SOURCE` | Player selector: `SOURCE`, `SKILL`, or `TARGET`. |
| `center` | `EVENT_BLOCK` | Origin selector: `EVENT_BLOCK`, entity role, or resolved location. |
| `shape` | `CUBE` | `CUBE`, `SPHERE`, `PLANE`, `LAYER`, `TUNNEL`/`LINE`, or `VEIN`. |
| `radius` | shape-specific | General radius, capped at 16. |
| `radius-x/y/z` | `radius` | Cube radii. |
| `vertical-radius` | `0` | Layer vertical radius. |
| `depth` | `0` | Plane depth. |
| `length` | `3` | Tunnel length, capped at 32. |
| `size` | unset | Odd cube-style size alias converted to radius. |
| `max-blocks` | global limit | Local cap; vein default 64; always capped by global config. |
| `include-origin` | `false` | Allow the origin block. |
| `same-type` | `false` | Require origin material. |
| `diagonal` | `true` | Use 26-neighbor vein search instead of six directions. |
| `load-chunks` | `false` | Allow blocks in unloaded chunks. |
| `whitelist / blacklist` | empty | Block specifications understood by `BlockPriceUtil`. |
| `use-default-blacklist` | `true` | Exclude protected hard blocks/containers such as bedrock, barriers, portal frames, reinforced deepslate, spawners, and chests. |
| `require-sneaking` | `false` | Require breaker sneaking. |
| `disable-when-sneaking` | `false` | Skip while sneaking. |
| `mode` | `NATURAL` | `NATURAL` uses `breakNaturally(tool)`; `PLAYER` uses `Player#breakBlock`. |
| `blocks-per-tick` | `16` | Batch size, minimum 1. |
| `before-abilities` | empty | Children before each break attempt. |
| `abilities` | empty | Children after each successful break. |

### Example

```yaml
type: break_blocks
breaker: SOURCE
center: EVENT_BLOCK
shape: VEIN
same-type: true
diagonal: true
include-origin: false
max-blocks: 32
blocks-per-tick: 8
whitelist:
  - DIAMOND_ORE
  - DEEPSLATE_DIAMOND_ORE
abilities:
  particles:
    type: particle
    particle: END_ROD
```

### Behavior and limits

* Every candidate is checked against protection hooks; air and invalid blocks are skipped.
* The global `powers.break-block.max-blocks-per-activation` cap defaults to 512.
* Folia natural breaking is scheduled by block region; player mode uses an internal-break recursion marker.
* The tool is cloned for each natural break.

---

## `break_block`

**Purpose:** Breaks the current context block using natural or player break behavior.

**Context:** Requires `BLOCK`, a player breaker, context item/tool, and protection permission.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `breaker` | `SOURCE` | Player selector: `SOURCE`, `SKILL`, or `TARGET`. |
| `mode` | `NATURAL` | Natural aliases or player-break aliases. |

### Example

```yaml
type: break_block
breaker: SOURCE
mode: PLAYER
```

### Behavior and limits

* Natural mode calls `breakNaturally` with a cloned context item.
* Player mode uses `Player#breakBlock` and an internal recursion marker.
* Protection hooks are checked before breaking.

---

## `replace_block`

**Purpose:** Replaces the current context block according to an ordered source-to-destination mapping.

**Context:** Requires `BLOCK` and a non-empty `replacements` section.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `replacements` | required | Map whose keys are source block specifications and values are destination block specifications. |

### Example

```yaml
type: replace_block
replacements:
  WHEAT: AIR
  '#minecraft:logs': OAK_PLANKS
```

### Behavior and limits

* The first matching source specification is selected by `BlockPriceUtil`.
* Invalid/non-string or blank mapping entries are ignored.

---

## `change_block_face`

**Purpose:** Changes facing/rotation on directional or rotatable block data.

**Context:** Requires `BLOCK`; protection-use permission is checked when a player is available.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `mode` | `FIXED` | `FIXED` uses `face`; `NEXT`, `CYCLE`, and `NEXT_AVAILABLE` rotate to the next supported face. |
| `face` | `NORTH` | Configured Bukkit `BlockFace` for fixed mode. |
| `apply-physics` | `false` | Whether setting block data applies physics. |
| `abilities` | empty | Children after successful change. |
| `failure-abilities / else-abilities` | empty | Children when no change can be applied. |

### Example

```yaml
type: change_block_face
mode: NEXT
apply-physics: false
abilities:
  sound:
    type: sound
    sound: BLOCK_LEVER_CLICK
```

### Behavior and limits

* Directional blocks only accept their supported faces; rotatable blocks use 16 horizontal rotations.
* Successful changes increment the changed-block result counter.

---

## `grow_crop`

**Purpose:** Advances an ageable context block toward maximum age.

**Context:** Requires `BLOCK` whose data implements Bukkit `Ageable`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `stages` | `1` | Growth stages added, minimum 1. |

### Example

```yaml
type: grow_crop
stages: 2
```

### Behavior and limits

* Growth is clamped to the block's maximum age.
* Non-ageable blocks are skipped.

---

## `accelerate_crops`

**Purpose:** Scans nearby blocks nearest-first and grows ageable crops that are not mature.

**Context:** Requires a resolved world location; when a player is available, protection use permission can be checked.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `1` | Default radius for every axis, capped at 16. |
| `radius-x / radius-y / radius-z` | `radius` | Per-axis radii, each capped at 16. |
| `chance` | `100` | Percentage chance for each candidate crop to grow, clamped from 0 to 100. |
| `stages` | `1` | Growth stages added on success, minimum 1. |
| `max-blocks` | `128` | Maximum matching crops processed; `0` disables the ability and a negative value is unlimited. |
| `load-chunks` | `false` | Allow scanning chunks that are not already loaded. |
| `check-protection` | `true` | When a player is present, require protection hooks to permit use of the block. |

### Example

```yaml
type: accelerate_crops
radius: 4
radius-y: 1
chance: 25
stages: 1
max-blocks: 128
```

### Behavior and limits

* Wheat, carrots, potatoes, beetroots, nether wart, cocoa, sweet berry bushes, torchflower crops, and pitcher crops are supported; mature blocks are skipped.
* Saplings, sugar cane, cactus, bamboo, and other random-tick plants are not simulated.
* The number of crops changed is recorded in the changed-block result.

---

## `accelerate_work_blocks`

**Purpose:** Scans nearby furnaces and brewing stands nearest-first and advances their current work progress.

**Context:** Requires a resolved world location; when a player is available, protection use permission can be checked.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `1` | Default radius for every axis, capped at 16. |
| `radius-x / radius-y / radius-z` | `radius` | Per-axis radii, each capped at 16. |
| `extra-ticks` | `1` | Additional work ticks applied per execution; `0` disables the ability. |
| `max-blocks` | `128` | Maximum enabled work blocks processed; `0` disables the ability and a negative value is unlimited. |
| `load-chunks` | `false` | Allow scanning chunks that are not already loaded. |
| `check-protection` | `true` | When a player is present, require protection hooks to permit use of the block. |
| `require-active` | `true` | Require burning fuel for furnaces and available fuel for brewing stands. |
| `furnace` | `true` | Process regular furnaces. |
| `smoker` | `true` | Process smokers. |
| `blast-furnace` | `true` | Process blast furnaces. |
| `brewing-stand` | `true` | Process brewing stands. |

### Example

```yaml
type: accelerate_work_blocks
radius: 5
radius-y: 2
extra-ticks: '{level}'
max-blocks: 128
```

### Behavior and limits

* Furnaces, smokers, blast furnaces, and brewing stands are supported and can be toggled separately.
* Progress stops one tick before completion so the server performs normal recipe completion.
* Fuel is neither supplied nor extended, and output items are never created directly.
* The number of work blocks advanced is recorded in the changed-block result.

---

## `scan_blocks`

**Purpose:** Scans a cube around the context block or source player and optionally highlights matches.

**Context:** Requires player `SOURCE`; center is `BLOCK` when present, otherwise the player's block.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `5` | Cube radius, minimum 1. |
| `same-type` | `false` | Match the center material instead of default ore/ancient-debris rules. |
| `max-results` | `256` | Maximum highlighted matches, minimum 1. |
| `highlight` | `true` | Spawn one `END_ROD` particle at each match. |

### Example

```yaml
type: scan_blocks
radius: 6
same-type: false
max-results: 128
highlight: true
```

### Behavior and limits

* Default matching recognizes material names ending `_ORE` plus `ANCIENT_DEBRIS`.
* The scan is a full cube, so cost grows with the third power of radius.

---

## `locate_structure`

**Purpose:** Finds the nearest configured structure type and sends a formatted result message.

**Context:** Requires player `SOURCE` and Paper registry access.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `structures` | empty | Structure type keys; unnamespaced values use `minecraft`. |
| `radius` | `32` | Search radius passed to Bukkit, minimum 1. |
| `find-unexplored` | `false` | Restrict search to unexplored structures. |
| `found` | empty | Success message with `{distance}`, `{direction}`, `{x}`, `{y}`, `{z}`, `{structure}`, `{structure_key}`. |
| `not-found` | empty | Failure message. |
| `structure-names.<key>` | registry key | Display-name mappings. |
| `directions.north/south/east/west` | English key | Localized direction text. |

### Example

```yaml
type: locate_structure
structures:
  - minecraft:village
  - minecraft:ancient_city
radius: 100
find-unexplored: false
found: '<green>{structure} is {distance} blocks {direction} at {x}, {z}.'
not-found: '<red>No structure found.'
```

### Behavior and limits

* Each configured structure is searched separately and the nearest result is selected.
* World-generation searches can be expensive; use a cooldown.

---

## `locate_biome`

**Purpose:** Finds the nearest configured biome and sends a formatted result message.

**Context:** Requires player `SOURCE` and Paper registry access.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `biomes` | empty | Biome registry keys. |
| `radius` | `1024` | Maximum search radius, minimum 1. |
| `horizontal-interval` | `32` | Horizontal sampling interval, minimum 1. |
| `vertical-interval` | `64` | Vertical sampling interval, minimum 1. |
| `found` | empty | Success message with distance, direction, coordinates, biome name/key. |
| `not-found` | empty | Failure message. |
| `biome-names.<key>` | registry key | Display-name mappings. |
| `directions.*` | English key | Localized direction text. |

### Example

```yaml
type: locate_biome
biomes:
  - minecraft:cherry_grove
radius: 1024
horizontal-interval: 32
vertical-interval: 64
found: '<green>{biome} is {distance} blocks {direction}.'
not-found: '<red>No biome found.'
```

### Behavior and limits

* Invalid or duplicate biome keys are ignored.
* Large searches can be expensive; use a cooldown.

---

---

## `prevent_block_break`

**Purpose:** Temporarily prevents players from breaking a configured block position. Useful for replanting, temporary construction, and other placement abilities. Protection is created only when this ability explicitly executes.

| Field | Default | Description |
| --- | --- | --- |
| `location` | `CONTEXT` | Common location selector, including `location.offset.x/y/z`. |
| `duration` | `20` | Positive lifetime in server ticks; supports dynamic numbers. |
| `scope` | `PLAYER` | `PLAYER` prevents the selected `target` player; `ALL` prevents every player. |
| `target` | `SOURCE` | Player selector for `PLAYER` scope. |
| `match-block` | `STATE` | `STATE` invalidates protection when block type or data changes; `TYPE` permits data changes while the type remains unchanged. |
| `abilities` | empty | Children after registration, with the protected position as context. |
| `failure-abilities` | empty | Children for invalid position/player/options or an air block. |

Inside `use_on.success-abilities`, the context is the clicked block. Replanting clicks farmland or soul sand, so protect the newly planted crop above it:

```yaml
success-abilities:
  protect-seedling:
    type: prevent_block_break
    target: SOURCE
    scope: PLAYER
    duration: 2
    match-block: STATE
    location:
      target: CONTEXT
      offset:
        y: 1
```

Cancels matching `BlockBreakEvent` events during protection, including intentional breaks; does not distinguish repeated packets from fresh clicks. Does not prevent explosions, trampling, fluids, or physics updates, and cannot undo completed breaks. Overlapping protection for the same position, player, and matching mode does not shorten its existing lifetime. Expired entries are removed; ability configuration reload and plugin shutdown clear all protection.
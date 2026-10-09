# ✨ Fundamental and Visual Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `mark`
* `cancel_event`
* `remove`
* `place_block`
* `place_temp_block`
* `particle`
* `sound`
* `vanilla_animation`

---

## `mark`

**Purpose:** Marks the selected entity as belonging to the current power chain.

**Context:** Default `target`: `SKILL`; requires the selected entity to exist.

### Fields

_No type-specific fields._

### Example

```yaml
type: mark
target: SKILL
```

### Behavior and limits

* The marker is internal; it does not add a visible scoreboard tag.
* Normally used for projectiles or helper entities that must retain power context.

---

## `cancel_event`

**Purpose:** Requests cancellation of the current trigger event.

**Context:** No entity target is required; the trigger must provide a cancellable event.

### Fields

_No type-specific fields._

### Example

```yaml
type: cancel_event
```

### Behavior and limits

* The cancellation signal is propagated through parent control-flow abilities.
* Cancelling a late notification event may not undo server work that already happened.

---

## `remove`

**Purpose:** Removes the selected entity from the world.

**Context:** Default `target`: `SKILL`; requires an entity.

### Fields

_No type-specific fields._

### Example

```yaml
type: remove
target: SKILL
```

### Behavior and limits

* Players are never intended targets for this cleanup ability.
* Useful for projectiles, temporary helpers, or replaced skill entities.

---

## `place_block`

**Purpose:** Places a configured block and optionally executes child abilities.

**Context:** Uses the extended location selector; a resolved player is used for protection checks when available.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `block` | `STONE` | Block specification understood by `BlockPriceUtil`, including supported custom block syntax. |
| `replace-existing` | `false` | Allow replacing a non-air destination. |
| `abilities` | empty | Child abilities executed after successful placement with the placed block as context. |
| `failure-abilities` | empty | Child abilities when location, replacement, protection, or placement fails. |
| `else-abilities` | empty | Fallback alias used when `failure-abilities` is absent. |

### Example

```yaml
type: place_block
block: COBWEB
replace-existing: false
location:
  target: TARGET
abilities:
  sound:
    type: sound
    sound: BLOCK_WOOL_PLACE
failure-abilities:
  notice:
    type: send_message
    message: '<red>The block could not be placed.'
```

### Behavior and limits

* Protection hooks are checked when a player context is available.
* A successful placement increments the changed-block result counter.
* This is permanent placement; use `place_temp_block` when automatic restoration is required.

---

## `place_temp_block`

**Purpose:** Places a temporary block and restores the previous block when it expires.

**Context:** Uses the extended resolved location; the destination must currently be air.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `block` | `COBWEB` | Bukkit material to place. |
| `duration` | `40` | Lifetime in ticks, clamped to at least 1. |

### Example

```yaml
type: place_temp_block
block: PACKED_ICE
duration: 100
location:
  target: TARGET
```

### Behavior and limits

* Invalid materials, missing worlds, or non-air destinations are skipped.
* Restoration is managed by `TempBlockManager`.

---

## `particle`

**Purpose:** Spawns a Bukkit particle at the resolved location.

**Context:** Default target is ability-specific; positioning uses the trigger/resolved location and common offsets.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `particle` | `FLAME` | Bukkit `Particle` enum name. |
| `count` | `1` | Particle count. |
| `offset-x / offset-y / offset-z` | `0` | Particle spread. |
| `extra` | `0` | Particle-specific speed/extra value. |
| `block` | `STONE` | Block material for block-data particles. |
| `item` | `STONE` | Item material for `ITEM`. |
| `color` | particle-specific | RGB value such as `255,0,0`. |
| `size` | `1` | Dust size or particle-specific size/duration. |
| `from / to` | particle-specific | Transition colors, vibration destination, or other structured particle data. |
| `value` | `1.0` | Float data for particles such as `DRAGON_BREATH` or `SCULK_CHARGE`. |
| `delay` | `20` | Delay data for `SHRIEK`. |
| `duration` | `20` | Travel duration for `VIBRATION`. |

### Example

```yaml
type: particle
particle: DUST
count: 20
offset-x: 0.4
offset-y: 0.8
offset-z: 0.4
color: 255,80,20
size: 1.5
```

### Behavior and limits

* Invalid particle names or incompatible required data cause the ability to do nothing.
* Keep counts small on high-frequency triggers.

---

## `sound`

**Purpose:** Plays a Bukkit sound at the resolved location.

**Context:** Uses the trigger/resolved location and common offsets.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `sound` | `ENTITY_PLAYER_LEVELUP` | Bukkit `Sound` enum name. |
| `volume` | `1` | Sound volume and audible range. |
| `pitch` | `1` | Sound pitch. |

### Example

```yaml
type: sound
sound: ENTITY_WARDEN_SONIC_BOOM
volume: 2
pitch: 1
```

### Behavior and limits

* Invalid sound names are ignored.
* Very large volume values increase audible range.

---

## `vanilla_animation`

**Purpose:** Plays a Bukkit `EntityEffect` animation on a selected entity.

**Context:** Default `target`: `SOURCE`; requires an entity compatible with the selected effect.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `animation` | `HURT` | Bukkit `EntityEffect` enum name. |
| `entity-effect` | `HURT` | Alias used when `animation` is absent. |

### Example

```yaml
type: vanilla_animation
target: SOURCE
animation: TOTEM_RESURRECT
```

### Behavior and limits

* Invalid animation/effect names are ignored.
* The animation is client-visible and does not itself apply damage or another mechanic.

---

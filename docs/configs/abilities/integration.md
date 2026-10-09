# 🔌 Integration and Targeting Abilities

Every registered ability on this page is documented as a complete reference entry. All entries also support the common ability fields from the [Abilities](README.md) page.

## Registry keys on this page

* `nearby_entities`
* `summon`
* `mythic_skill`
* `send_message`
* `execute_command`
* `execute_action`
* `auto_fishing`
* `disable_enchantments`


---

## `nearby_entities`

**Purpose:** Finds nearby entities and runs children with each accepted entity as the new `TARGET`.

**Context:** Default base `target`: `SOURCE`; falls back to resolved location when the entity is unavailable.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `radius` | `5` | Default radius for all axes. |
| `radius-x / radius-y / radius-z` | `radius` | Per-axis radii. |
| `max-targets` | `0` | Maximum accepted entities; zero means unlimited. |
| `include-source` | `false` | Allow original source. |
| `exclude-target` | `false` | Exclude the original context target from the candidates. |
| `include-items` | `false` | Allow dropped items. |
| `include-non-living` | `false` | Allow other non-living entities. |
| `include-living` | `true` | Allow living entities. |
| `include-armor-stands` | `false` | Allow armor stands when living entities are enabled. |
| `direction-source` | `SOURCE` | Entity whose facing direction is used by `max-angle`. |
| `max-angle` | unset | Maximum cone half-angle in degrees; unset disables cone filtering. |
| `match-entity` | match all | Match Entity rules for living candidates. |
| `abilities` | required | Nested abilities. |
| `after-abilities` | unset | Abilities run once after at least one candidate is accepted. Receives `{target_count}`. |

### Example

```yaml
type: nearby_entities
target: SOURCE
radius: 8
max-targets: 3
include-source: false
exclude-target: true
max-angle: 45
match-entity:
  entity-types:
    - PLAYER
abilities:
  damage:
    type: damage_entity
    amount: 4
after-abilities:
  feedback:
    type: send_message
    message: '<green>Hit {target_count} nearby targets.'
```

### Behavior and limits

* Candidates are ordered nearest-first.
* Child context location is updated to each selected target.
* `max-angle` is a half-angle: `45` accepts a 90-degree frontal cone.
* `after-abilities` runs only when at least one candidate was accepted and keeps the original target context.

---

## `summon`

**Purpose:** Spawns one living Bukkit entity and configures its basic stats.

**Context:** Spawn location uses the resolved ability location.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `entity` | empty | Single Bukkit entity type. |
| `entity-type` | empty | Alias for `entity`. |
| `entities` | empty | Equal-probability entity list; takes priority. |
| `max-health` | `-1` | Set maximum health when positive. |
| `health` | `-1` | Set current health when positive. |
| `attack-damage` | `-1` | Set attack-damage base when non-negative. |
| `set-target` | unset | Context entity selector assigned as target to spawned mobs. |
| `set-none-drops` | `false` | Clear loot table, disable pickup, and set equipment drop chances to zero. |
| `creeper.explosion-radius` | `-1` | Creeper radius. |
| `creeper.fuse-ticks` | `-1` | Creeper max fuse. |
| `creeper.powered` | unset | Creeper charged state. |

### Example

```yaml
type: summon
entities:
  - ZOMBIE
  - SKELETON
set-target: TARGET
set-none-drops: true
max-health: 30
attack-damage: 6
```

### Behavior and limits

* The selected type must be spawnable and produce a living entity.
* Unlike EnchantedMobs, this implementation does not assign EnchantedMobs power IDs.

---

## `mythic_skill`

**Purpose:** Casts one or more MythicMobs skills.

**Context:** Caster is `SOURCE`; default `target`: `TARGET` and must exist.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `skill` | empty | Single skill used when `skills` is empty. |
| `skills` | empty | Ordered skill-name list. |
| `power` | `1.0` | MythicMobs skill power multiplier. |

### Example

```yaml
type: mythic_skill
target: TARGET
skills:
  - Fireball
  - GroundSlash
power: 1.5
```

### Behavior and limits

* Requires MythicMobs.
* Target entity and resolved location are supplied to the integration.

---

## `send_message`

**Purpose:** Sends one or more formatted messages to `context.player()` or nearby players.

**Context:** Target-player mode uses the current player context; nearby mode searches around `SOURCE`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `mode` | `target-player` | `target-player` or `nearby`. |
| `radius` | `16` | Nearby-player radius. |
| `message` | empty | Single message when `messages` is empty. |
| `messages` | empty | Message list. |
| `placeholders` | empty | Custom message-placeholder map. Values resolve context variables and are evaluated as math when possible. |

### Example

```yaml
type: send_message
mode: target-player
messages:
  - '<green>Activated at level {level}'
  - '<gray>Target: {target}'
placeholders:
  scaled_amount: '{original_experience} * 0.5'
```

### Behavior and limits

* Recipient-aware parsing supports `{player}`, `{target}`, level/context variables, language parsing, and PlaceholderAPI where available.
* Placeholder keys can be used as `{key}` in `message` or `messages`. Numeric expressions are evaluated automatically and insignificant trailing zeroes are removed.
* Offline or dead nearby players are skipped.

---

## `execute_command`

**Purpose:** Runs commands for the source mob's current player target or nearby players.

**Context:** Target-player mode resolves the current player target of mob `SOURCE`; nearby mode searches around `SOURCE`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `mode` | `target-player` | `target-player` or `nearby`. |
| `radius` | `16` | Nearby-player search radius. |
| `as-console` | `true` | Run as console; false makes each recipient execute the command. |
| `command` | empty | Single command used when `commands` is empty. |
| `commands` | empty | Command list. |

### Example

```yaml
type: execute_command
mode: target-player
as-console: true
commands:
  - 'effect give {player} minecraft:slowness 5 1'
```

### Behavior and limits

* A leading slash is removed.
* Commands execute once per selected online, living player and support player/target/context placeholders.

---

## `execute_action`

**Purpose:** Runs the shared ActionManager action format for a selected player.

**Context:** Default `target`: `SOURCE`; selected entity must be a player and `actions` must exist.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `target` | `SOURCE` | Player selector. |
| `actions` | required | Nested shared action entries. |

### Example

```yaml
type: execute_action
target: SOURCE
actions:
  message:
    type: message
    message: '<green>Action executed.'
```

### Behavior and limits

* Available action types and fields come from the shared action manager.
* The child action context uses the selected player.

---

## `auto_fishing`

**Purpose:** Automatically retrieves and recasts a fishing rod after a bite.

**Context:** Requires `PlayerFishEvent.State.BITE` and a captured main/off-hand fishing rod slot.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `retrieve-delay-ticks` | `5` | Delay before retrieval, minimum 1. |
| `recast-delay-ticks` | `10` | Delay before recast, minimum 1. |
| `method` | `LEGACY` | `LEGACY` uses PacketEvents interaction; `NMS` uses the fishing NMS bridge. |

### Example

```yaml
type: auto_fishing
retrieve-delay-ticks: 5
recast-delay-ticks: 10
method: NMS
```

### Behavior and limits

* Player, hook, hand, and rod are revalidated before each action.
* Legacy mode requires PacketEvents; NMS mode requires fishing-rod capability.

---

## `disable_enchantments`

**Purpose:** Prevents matching enchantments on the same item from becoming active.

**Context:** Activation-only ability evaluated by the active-enchantment manager before normal trigger execution.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `enchantments` | empty | String or list of case-insensitive glob patterns to disable. |
| `exclude-enchantments` | empty | Patterns exempted from disabling. |

### Example

```yaml
type: disable_enchantments
enchantments:
  - 'enchantmentreform:*_curse'
exclude-enchantments:
  - enchantmentreform:allowed_curse
```

### Behavior and limits

* Patterns are matched against logical file name, key value, and full namespaced key.
* `*` matches any text and `?` matches one character.
* This ability does nothing when executed as a normal trigger ability; place it under activation abilities.

---

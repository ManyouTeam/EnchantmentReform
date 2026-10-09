# 🏹 Projectile Modifiers

Every modifier on this page is documented independently. All entries also support the common modifier fields from the [Power Modifiers](README.md) page.

## Registry keys on this page

* `modify_projectile`
* `replace_projectile`

---

## `modify_projectile`

**Purpose:** Edits the current projectile in place.

**Context:** Uses projectile `SKILL`, falling back to projectile `TARGET`; normally used on shoot, launch, or projectile-tick triggers.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `speed-multiplier` | `1` | Multiplies current velocity. |
| `draw-speed-multiplier` | unset | Scales velocity relative to captured bow force when available. |
| `spread-degrees` | unset | Random X/Y angular spread. |
| `accuracy` | unset | Blends direction toward source living entity look direction; clamped `0..1`. |
| `gravity` | unchanged | Enable or disable gravity. |
| `fire-ticks` | unchanged | Set projectile fire ticks, minimum 0. |
| `damage-multiplier` | `1` | Multiplies `AbstractArrow` base damage. |
| `critical` | current | Sets arrow critical state. |
| `pierce-level` | current | Sets pierce level, clamped `0..127`. |

### Example

```yaml
type: modify_projectile
speed-multiplier: 1.25
spread-degrees: 3
gravity: false
damage-multiplier: 1.5
pierce-level: 2
```

### Behavior and limits

* Absent options leave their property unchanged, except neutral speed/damage multipliers.
* `accuracy` aims along the shooter's look direction, not directly at `TARGET`.

---

## `replace_projectile`

**Purpose:** Removes the current projectile and replaces it with TNT or another projectile type while preserving core motion and ownership.

**Context:** Reads the old projectile from `SKILL` or captured projectile context and writes the replacement as the new skill entity.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `projectile` | required | `TNT` aliases or a spawnable Bukkit projectile type. |
| `fuse` | `40` | TNT fuse ticks, minimum 1. |
| `fireball-yield` | `1` | Yield for fireball replacements. |
| `fireball-incendiary` | `true` | Incendiary state for fireballs. |
| `potion-type` | unset | Base potion type for thrown potions. |
| `potion-effects` | unset | Custom potion effects with `duration` (100) and `amplifier` (0). |

### Example

```yaml
type: replace_projectile
projectile: SPLASH_POTION
potion-type: HARMING
potion-effects:
  slowness:
    duration: 80
    amplifier: 1
```

### Behavior and limits

* Velocity and gravity are copied to projectile replacements.
* The captured living source becomes shooter.
* Replacing with a projectile transfers tracking; TNT/non-projectile replacement ends projectile continuation.
* TNT velocity is multiplied by 1.2.

---

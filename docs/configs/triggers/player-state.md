# ❤️ Effects, air, food, and experience triggers

These triggers observe or modify player state. Most use the default player selectors and expose their mutable value through trigger result data.

Each trigger section below documents the runtime context created by the current implementation.

| Context | Meaning |
| --- | --- |
| `PLAYER` | The player whose active enchantment is being evaluated. |
| `SOURCE` | The actor or owner responsible for the event. |
| `SKILL` | The direct carrier/intermediate entity, such as a projectile, hook, firework, or Warden. |
| `TARGET` | The final affected or inspected entity. It may be unavailable for some events. |
| `BLOCK` | The event block, when the trigger supplies one. |
| `LOCATION` | The main event location used by location-aware conditions and abilities. |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | The item and equipment slot that selected the active enchantment. |
| `EVENT` | The underlying Bukkit/Paper event. Trigger-specific state not copied into another field remains available through the event-aware implementation. |

When a role is marked **player**, it resolves to the same entity as `PLAYER`. A missing role resolves to `null`; conditions or abilities requiring it will fail or skip safely.

Numeric extra-context names documented on trigger pages are also expression placeholders when the current trigger provides them. For example, `original_air` becomes `{original_air}`. See [Math Calculate Format](../../shared-formats/math-calculate-format.md#numeric-trigger-context-placeholders) for the complete built-in list and resolution rules.

## `on-effect-tick`

Runs for Paper's instant or periodic effect-tick event when the affected entity is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | affected player |
| Extra context | Effect type and effect instance remain on `EntityEffectTickEvent` |

## `on-potion-effect`

Runs when a potion effect is added, changed, or removed from a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | affected player |
| `SOURCE` / `SKILL` | Effect source, or the player when no source exists |
| Extra context | `original_duration` is the new effect duration in ticks; old/new effect, action, and cause remain on `EntityPotionEffectEvent` |

## `on-exhaustion`

Runs when Paper reports exhaustion for a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | Exhaustion amount and reason remain on `EntityExhaustionEvent` |

## `on-food-level-change`

Runs when a player's food level changes.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | `original_food_level`; mutable food-level result |

`{original_food_level}` may be used in context-aware strings and numeric expressions.

## `on-air-change`

Runs when a player's remaining air changes.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | `original_air`, `previous_air`; mutable air result |

`{original_air}` is the new air value originally requested by the event; `{previous_air}` is the player's air value before the event. Both may be used in context-aware strings and numeric expressions.

## `on-exp-change`

Runs when a player's experience amount changes.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | player |
| Extra context | `original_experience`; mutable experience result |

`{original_experience}` resolves to the experience amount before modifiers are applied.

## `on-pickup-experience`

Runs when a player picks up a Paper experience orb.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | player |
| `SKILL` | Experience orb |
| `LOCATION` | Orb location |
| Extra context | `original_experience` is the orb's stored experience |

The orb value may be accumulated across pickups with the [`state`](../abilities/orchestration.md#state) Ability. Complete threshold groups can change one nested Ability's numeric result, while an incomplete remainder remains stored for later pickups:

```yaml
abilities:
  accumulate-experience:
    type: state
    key: experience-lifeblood
    operation: ADD
    amount: '{original_experience}'
    trigger-at: 3
    consume-trigger-value: true
    trigger-abilities:
      restore-health:
        type: set_health
        target: PLAYER
        amount: 'min({health} + {state_trigger_count}, {max-health} * 0.5)'

  consume-experience-orb:
    type: remove
    target: SKILL

  prevent-normal-experience:
    type: cancel_event
```

A stored remainder of `1` followed by an orb worth `7` produces a total of `8`: two complete groups restore 2 health points and the remaining `2` experience stays in the state pool. The experience orb is removed and the original pickup event is cancelled so the same experience is not also awarded normally.

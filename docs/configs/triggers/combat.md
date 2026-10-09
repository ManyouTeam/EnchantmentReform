# ⚔️ Combat, damage, and death triggers

Use these triggers for outgoing attacks, incoming damage, healing, death, shields, and Paper combat actions.

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

## `on-attack`

Runs when the enchanted player is responsible for entity damage, including attributable projectile or trident damage.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` | attacking player |
| `SKILL` | Direct damager: player, projectile, or trident |
| `TARGET` | Damaged entity |
| `LOCATION` | Target location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand weapon, or the firing weapon/slot recovered for a tracked projectile |
| Extra context | `original_damage`, `damage_cause`; mutable `damage` result |

## `on-melee-attack`

Runs only when the direct damager is the player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | attacking player |
| `TARGET` | Damaged entity |
| `LOCATION` | Target location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Main-hand item / `HAND` |
| Extra context | `original_damage`, `damage_cause`; mutable `damage` result |

## `on-damage`

Runs when the enchanted player receives any damage, including environmental damage.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | damaged player |
| `SOURCE` | Resolved attacker/owner; missing for environmental damage |
| `SKILL` | Direct damager; player itself when there is no direct damager |
| `LOCATION` | Damaged player's location |
| Extra context | `original_damage`, `damage_cause`, `damage_by_entity`, `damage_by_block`; mutable `damage` result |

## `on-damage-by-entity`

Runs only when the enchanted player receives entity-caused damage.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | damaged player |
| `SOURCE` | Resolved attacker or projectile owner |
| `SKILL` | Direct damager |
| `LOCATION` | Damaged player's location |
| Extra context | `original_damage`, `damage_cause`, `damage_by_entity=true`; mutable `damage` result |

## `on-kill`

Runs when a player is recorded as the killer of an entity.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | killer |
| `TARGET` | killed entity |
| `LOCATION` | Killed entity's location |
| Extra context | `original_experience`; mutable dropped experience |

## `on-death`

Runs when the enchanted player dies.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | dead player |
| `SOURCE` / `SKILL` | killer, or missing when no player killer exists |
| `LOCATION` | Dead player's location |
| Extra context | `original_experience`, death drops; supports clear-drops and Paper revive-health behavior |

## `on-regain`

Runs when the enchanted player regains health.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | healed player |
| Extra context | `original_amount`; mutable regain amount; regain reason remains on the event |

## `on-combust`

Runs when the enchanted player is set on fire.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | burning player |
| Extra context | `original_duration`; mutable combustion duration; event subtype identifies the combustion origin |

## `on-knockback`

Runs for Paper's entity knockback event when the knocked entity is the enchanted player.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | knocked player |
| `SOURCE` / `SKILL` | Pushing entity for attack knockback; otherwise the player |
| Extra context | Knockback cause and vector remain available on the Paper event |

## `on-attempt-smash-attack`

Runs when a player attempts a Paper smash attack.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | attacking player |
| `TARGET` | Intended smash target |
| `LOCATION` | Target location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Event weapon / `HAND` |
| Extra behavior | Cancelling the power denies the smash attempt |

## `on-lunge`

Runs for Paper's entity lunge event when the entity is a player.

| Context | Value |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | lunging player |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Active-use item and hand, or the currently selected hand item |
| Extra context | Lunge-specific values remain on the Paper event |

## `on-shield-block`

Runs for incoming entity damage processed as a shield block.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | blocking player |
| `SOURCE` | Resolved attacker/owner |
| `SKILL` | Direct damager |
| `LOCATION` | Blocking player's location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Active shield and hand |
| Extra context | `original_damage` is the blocked amount; `damage_cause`; mutable `damage` result |

## `on-shield-disable`

Runs when Paper reports that a player's shield is disabled.

| Context | Value |
| --- | --- |
| `PLAYER` / `TARGET` | player whose shield is disabled |
| `SOURCE` / `SKILL` | disabling damager |
| `LOCATION` | Player location |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Active shield and hand |

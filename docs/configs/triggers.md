# ⚡ Power Triggers

A trigger is a section under an enchantment's `powers`. It decides when the power runs and creates the context consumed by Power Conditions, Power Modifiers, and Abilities.

```yaml
powers:
  on-attack:
    conditions: {}
    modifiers: {}
    abilities: {}
```

EnchantmentReform currently registers **63 built-in triggers**.

## Reference pages

* [Tick and lifecycle triggers](triggers/lifecycle.md)
* [Combat, damage, and death triggers](triggers/combat.md)
* [Projectile and ranged triggers](triggers/projectiles.md)
* [Block and interaction triggers](triggers/blocks-interactions.md)
* [Item, enchanting, and equipment triggers](triggers/items-equipment.md)
* [Movement and input triggers](triggers/movement-input.md)
* [Effects, air, food, and experience triggers](triggers/player-state.md)
* [Fishing and trading triggers](triggers/fishing-trading.md)
* [Targeting and special-entity triggers](triggers/targeting-special.md)

## Common context

Every trigger creates a `TriggerData` context. The exact value of each role depends on the selected trigger.

| Context | Meaning |
| --- | --- |
| `PLAYER` | The player whose active enchanted item supplied the power. This is always present. |
| `SOURCE` | The actor or owner responsible for the event. |
| `SKILL` | The direct carrier or intermediate entity, such as a projectile, hook, firework, or Warden. |
| `TARGET` | The final affected or inspected entity. |
| `BLOCK` | The event block when the trigger supplies one. |
| `LOCATION` | Main event location used by location-aware conditions and abilities. |
| `FROM` / `TO` | Movement origin and destination when supplied. |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | Item and slot that selected the active enchantment. |
| `EVENT` | Underlying Bukkit/Paper event. |
| `TICK` | Runtime tick for periodic triggers. |

`SOURCE`, `SKILL`, `TARGET`, `BLOCK`, movement locations, and trigger-item data are optional. A condition or ability that requires missing context fails or skips safely.

{% hint style="info" %}
Do not assume the same selector has the same meaning across every trigger. For example, `on-attack` uses `SOURCE` for the attacking player and `SKILL` for the direct projectile/damager, while `on-damage` uses `TARGET` for the enchanted player receiving damage.
{% endhint %}

## Trigger list

### Tick and lifecycle triggers

* [`on-tick`](triggers/lifecycle.md#on-tick)
* [`on-target-tick`](triggers/lifecycle.md#on-target-tick)
* [`on-spawn`](triggers/lifecycle.md#on-spawn)
* [`on-respawn`](triggers/lifecycle.md#on-respawn)
* [`on-activate`](triggers/lifecycle.md#on-activate)
* [`on-deactivate`](triggers/lifecycle.md#on-deactivate)

### Combat, damage, and death triggers

* [`on-attack`](triggers/combat.md#on-attack)
* [`on-melee-attack`](triggers/combat.md#on-melee-attack)
* [`on-damage`](triggers/combat.md#on-damage)
* [`on-damage-by-entity`](triggers/combat.md#on-damage-by-entity)
* [`on-kill`](triggers/combat.md#on-kill)
* [`on-death`](triggers/combat.md#on-death)
* [`on-regain`](triggers/combat.md#on-regain)
* [`on-combust`](triggers/combat.md#on-combust)
* [`on-knockback`](triggers/combat.md#on-knockback)
* [`on-attempt-smash-attack`](triggers/combat.md#on-attempt-smash-attack)
* [`on-lunge`](triggers/combat.md#on-lunge)
* [`on-shield-block`](triggers/combat.md#on-shield-block)
* [`on-shield-disable`](triggers/combat.md#on-shield-disable)

### Projectile and ranged triggers

* [`on-shoot`](triggers/projectiles.md#on-shoot)
* [`on-shoot-bow`](triggers/projectiles.md#on-shoot-bow)
* [`on-load-crossbow`](triggers/projectiles.md#on-load-crossbow)
* [`on-projectile-launch`](triggers/projectiles.md#on-projectile-launch)
* [`on-projectile-tick`](triggers/projectiles.md#on-projectile-tick)
* [`on-projectile-hit`](triggers/projectiles.md#on-projectile-hit)
* [`on-riptide`](triggers/projectiles.md#on-riptide)

### Block and interaction triggers

* [`on-block-break`](triggers/blocks-interactions.md#on-block-break)
* [`on-block-damage`](triggers/blocks-interactions.md#on-block-damage)
* [`on-block-break-progress-update`](triggers/blocks-interactions.md#on-block-break-progress-update)
* [`on-block-drop-item`](triggers/blocks-interactions.md#on-block-drop-item)
* [`on-block-place`](triggers/blocks-interactions.md#on-block-place)
* [`on-inside-block`](triggers/blocks-interactions.md#on-inside-block)
* [`on-interact`](triggers/blocks-interactions.md#on-interact)
* [`on-consume`](triggers/blocks-interactions.md#on-consume)
* [`on-name-entity`](triggers/blocks-interactions.md#on-name-entity)
* [`on-vibration-receive`](triggers/blocks-interactions.md#on-vibration-receive)

### Item, enchanting, and equipment triggers

* [`on-item-damage`](triggers/items-equipment.md#on-item-damage)
* [`on-item-group-cooldown`](triggers/items-equipment.md#on-item-group-cooldown)
* [`on-item-held`](triggers/items-equipment.md#on-item-held)
* [`on-swap-hand`](triggers/items-equipment.md#on-swap-hand)
* [`on-enchant-item`](triggers/items-equipment.md#on-enchant-item)
* [`on-elytra-boost`](triggers/items-equipment.md#on-elytra-boost)

### Movement and input triggers

* [`on-move`](triggers/movement-input.md#on-move)
* [`on-input`](triggers/movement-input.md#on-input)
* [`on-jump`](triggers/movement-input.md#on-jump)
* [`on-toggle-flight`](triggers/movement-input.md#on-toggle-flight)
* [`on-toggle-sneak`](triggers/movement-input.md#on-toggle-sneak)

### Effects, air, food, and experience triggers

* [`on-effect-tick`](triggers/player-state.md#on-effect-tick)
* [`on-potion-effect`](triggers/player-state.md#on-potion-effect)
* [`on-exhaustion`](triggers/player-state.md#on-exhaustion)
* [`on-food-level-change`](triggers/player-state.md#on-food-level-change)
* [`on-air-change`](triggers/player-state.md#on-air-change)
* [`on-exp-change`](triggers/player-state.md#on-exp-change)
* [`on-pickup-experience`](triggers/player-state.md#on-pickup-experience)

### Fishing and trading triggers

* [`on-fish`](triggers/fishing-trading.md#on-fish)
* [`on-purchase`](triggers/fishing-trading.md#on-purchase)
* [`on-trade`](triggers/fishing-trading.md#on-trade)

### Targeting and special-entity triggers

* [`on-target`](triggers/targeting-special.md#on-target)
* [`on-untag`](triggers/targeting-special.md#on-untag)
* [`on-creeper-explode`](triggers/targeting-special.md#on-creeper-explode)
* [`on-enderman-attack-player`](triggers/targeting-special.md#on-enderman-attack-player)
* [`on-phantom-pre-spawn`](triggers/targeting-special.md#on-phantom-pre-spawn)
* [`on-warden-anger-change`](triggers/targeting-special.md#on-warden-anger-change)

## Choosing compatible conditions and modifiers

Choose the trigger first, then open its category page and verify the available Context.

Examples:

* the `damage` modifier requires a trigger with mutable damage data;
* `item_damage` requires `on-item-damage`;
* fishing result modifiers require `on-fish` and the appropriate fishing state;
* trade modifiers require `on-purchase` or `on-trade`;
* `warden_anger` requires `on-warden-anger-change`;
* block conditions require a trigger that supplies `BLOCK`;
* entity-targeted abilities must use a selector that is present for that trigger.

## Extension triggers

Other plugins may register custom triggers through the EnchantmentReform API. Custom trigger keys and their Context are defined by the extension and are not included in this built-in list.

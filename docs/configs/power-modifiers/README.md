# 🔧 Power Modifiers

EnchantmentReform registers **28 built-in modifier keys**. Modifiers run in YAML order after trigger conditions and before abilities.

## Reference pages

* [Result and Resource Modifiers](result-values.md)
* [Combat and State Modifiers](combat-state.md)
* [Event and Drop Modifiers](event-drops.md)
* [Projectile Modifiers](projectiles.md)
* [Fishing and Trading Modifiers](fishing-trading.md)

## Common format

```yaml
modifiers:
  unique-entry-id:
    type: damage
    conditions: {}
    random: 1
    cooldown: 0
    operation: MULTIPLY
    value: 1.25
```

## Common fields

| Field | Default | Description |
| --- | --- | --- |
| `type` | required | Registered modifier key. Keys are case-insensitive and `-` is normalized to `_`. |
| `conditions` | empty | Typed Power Conditions; every child must pass. |
| `random` | `1` | Application probability. |
| `cooldown` | `0` | Per-source, per-power, per-config-path cooldown in seconds. |

Common checks run in this order: conditions, probability, then cooldown acquisition. A modifier whose event-specific implementation later finds incompatible context may still have acquired its common cooldown.

## Shared numeric operations

| Operation | Result |
| --- | --- |
| `SET` | operand |
| `ADD` | original + operand |
| `SUBTRACT` | original - operand |
| `MULTIPLY` | original × operand |
| `DIVIDE` | original ÷ operand; zero keeps original |
| `MIN` | minimum of original and operand |
| `MAX` | maximum of original and operand |

## Type index

| Type | Reference |
| --- | --- |
| `damage` | [Result and Resource Modifiers](result-values.md#damage) |
| `heal` | [Result and Resource Modifiers](result-values.md#heal) |
| `duration` | [Result and Resource Modifiers](result-values.md#duration) |
| `yield` | [Result and Resource Modifiers](result-values.md#yield) |
| `radius` | [Result and Resource Modifiers](result-values.md#radius) |
| `cooldown_time` | [Result and Resource Modifiers](result-values.md#cooldown-time) |
| `item_damage` | [Result and Resource Modifiers](result-values.md#item-damage) |
| `food` | [Result and Resource Modifiers](result-values.md#food) |
| `exhaustion` | [Result and Resource Modifiers](result-values.md#exhaustion) |
| `experience` | [Result and Resource Modifiers](result-values.md#experience) |
| `lunge_power` | [Result and Resource Modifiers](result-values.md#lunge-power) |
| `air` | [Result and Resource Modifiers](result-values.md#air) |
| `armor_pierce` | [Combat and State Modifiers](combat-state.md#armor-pierce) |
| `missing_health_damage` | [Combat and State Modifiers](combat-state.md#missing-health-damage) |
| `stack_damage_modifier` | [Combat and State Modifiers](combat-state.md#stack-damage-modifier) |
| `revive` | [Combat and State Modifiers](combat-state.md#revive) |
| `warden_anger` | [Combat and State Modifiers](combat-state.md#warden-anger) |
| `vibration_reduce` | [Combat and State Modifiers](combat-state.md#vibration-reduce) |
| `elytra_boost_not_consume` | [Event and Drop Modifiers](event-drops.md#elytra-boost-not-consume) |
| `modify_drops` | [Event and Drop Modifiers](event-drops.md#modify-drops) |
| `modify_projectile` | [Projectile Modifiers](projectiles.md#modify-projectile) |
| `replace_projectile` | [Projectile Modifiers](projectiles.md#replace-projectile) |
| `trade_uses_return` | [Fishing and Trading Modifiers](fishing-trading.md#trade-uses-return) |
| `trade_emerald_return` | [Fishing and Trading Modifiers](fishing-trading.md#trade-emerald-return) |
| `fishing_catch` | [Fishing and Trading Modifiers](fishing-trading.md#fishing-catch) |
| `fishing_extra_catch` | [Fishing and Trading Modifiers](fishing-trading.md#fishing-extra-catch) |
| `fishing_replace_catch` | [Fishing and Trading Modifiers](fishing-trading.md#fishing-replace-catch) |
| `fishing_smelt_catch` | [Fishing and Trading Modifiers](fishing-trading.md#fishing-smelt-catch) |
| `fishing_hook_timing` | [Fishing and Trading Modifiers](fishing-trading.md#fishing-hook-timing) |

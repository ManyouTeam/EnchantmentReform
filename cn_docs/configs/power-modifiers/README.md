# 🔧 能力修改器

EnchantmentReform 注册了 **28 个内置修改器键**。能力修改器会在能力条件之后、能力之前，按照 YAML 中的顺序依次执行。

## 参考页面

* [结果与资源修改器](result-values.md)
* [战斗与状态修改器](combat-state.md)
* [事件与掉落修改器](event-drops.md)
* [投射物修改器](projectiles.md)
* [钓鱼与交易修改器](fishing-trading.md)

## 通用格式

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

## 通用字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `type` | 必填 | 已注册的修改器键。键不区分大小写，且 `-` 会被规范化为 `_`。 |
| `conditions` | 空 | 带类型的能力条件；每个子条件都必须通过。 |
| `random` | `1` | 应用概率。 |
| `cooldown` | `0` | 按来源、能力和配置路径分别计算的冷却时间，单位为秒。 |

通用检查按照以下顺序执行：条件、概率，然后获取冷却。某个修改器的事件专用实现即使之后发现当前上下文不兼容，也可能已经获取了通用冷却。

## 共用数字操作

| 操作 | 结果 |
| --- | --- |
| `SET` | 操作数 |
| `ADD` | 原始值 + 操作数 |
| `SUBTRACT` | 原始值 - 操作数 |
| `MULTIPLY` | 原始值 × 操作数 |
| `DIVIDE` | 原始值 ÷ 操作数；操作数为零时保留原始值 |
| `MIN` | 原始值与操作数中的较小值 |
| `MAX` | 原始值与操作数中的较大值 |

## 类型索引

| 类型 | 参考页面 |
| --- | --- |
| `damage` | [结果与资源修改器](result-values.md#damage) |
| `heal` | [结果与资源修改器](result-values.md#heal) |
| `duration` | [结果与资源修改器](result-values.md#duration) |
| `yield` | [结果与资源修改器](result-values.md#yield) |
| `radius` | [结果与资源修改器](result-values.md#radius) |
| `cooldown_time` | [结果与资源修改器](result-values.md#cooldown-time) |
| `item_damage` | [结果与资源修改器](result-values.md#item-damage) |
| `food` | [结果与资源修改器](result-values.md#food) |
| `exhaustion` | [结果与资源修改器](result-values.md#exhaustion) |
| `experience` | [结果与资源修改器](result-values.md#experience) |
| `lunge_power` | [结果与资源修改器](result-values.md#lunge-power) |
| `air` | [结果与资源修改器](result-values.md#air) |
| `armor_pierce` | [战斗与状态修改器](combat-state.md#armor-pierce) |
| `missing_health_damage` | [战斗与状态修改器](combat-state.md#missing-health-damage) |
| `stack_damage_modifier` | [战斗与状态修改器](combat-state.md#stack-damage-modifier) |
| `revive` | [战斗与状态修改器](combat-state.md#revive) |
| `warden_anger` | [战斗与状态修改器](combat-state.md#warden-anger) |
| `vibration_reduce` | [战斗与状态修改器](combat-state.md#vibration-reduce) |
| `elytra_boost_not_consume` | [事件与掉落修改器](event-drops.md#elytra-boost-not-consume) |
| `modify_drops` | [事件与掉落修改器](event-drops.md#modify-drops) |
| `modify_projectile` | [投射物修改器](projectiles.md#modify-projectile) |
| `replace_projectile` | [投射物修改器](projectiles.md#replace-projectile) |
| `trade_uses_return` | [钓鱼与交易修改器](fishing-trading.md#trade-uses-return) |
| `trade_emerald_return` | [钓鱼与交易修改器](fishing-trading.md#trade-emerald-return) |
| `fishing_catch` | [钓鱼与交易修改器](fishing-trading.md#fishing-catch) |
| `fishing_extra_catch` | [钓鱼与交易修改器](fishing-trading.md#fishing-extra-catch) |
| `fishing_replace_catch` | [钓鱼与交易修改器](fishing-trading.md#fishing-replace-catch) |
| `fishing_smelt_catch` | [钓鱼与交易修改器](fishing-trading.md#fishing-smelt-catch) |
| `fishing_hook_timing` | [钓鱼与交易修改器](fishing-trading.md#fishing-hook-timing) |
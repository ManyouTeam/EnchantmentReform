# 🧾 触发器数据条件

本页面会将每一种已注册条件作为独立条目进行说明。所有条目也支持[能力条件](README.md)页面中介绍的通用条件字段。

## 本页面的注册键

* `damage_value`
* `damage_cause`
* `damage_origin`
* `spawn_reason`
* `combust_duration`
* `regain_amount`
* `explosion_yield`
* `explosion_radius`
* `target_reason`
* `interaction_action`
* `input_type`
* `event_state`
* `smash_attack_lands`
* `food_change`
* `air_change`
* `state_value`
* `combust_origin`
* `knockback_cause`
* `knockback_reason`
* `exhaustion_reason`
* `villager_trade`
* `cooldown`
* `cooldown_material`

---

## `damage_value`

**用途：**检查当前可修改的伤害结果。

**上下文：**需要提供伤害数据的触发器和伤害结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: damage_value
min: 1
max: 10
```

### 行为与限制

* 在更早的能力修改器之后检查时，会读取已经修改过的结果。
* 不相关的触发器不会匹配。

---

## `damage_cause`

**用途：**匹配已记录的 Bukkit `EntityDamageEvent.DamageCause`。

**上下文：**需要伤害原因事件上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 空 | 单个伤害原因枚举名称。 |
| `values` | 空 | 接受的伤害原因名称列表。 |

### 示例

```yaml
type: damage_cause
values:
  - PROJECTILE
  - ENTITY_ATTACK
```

### 行为与限制

* 插件规范化配置值后，匹配不区分大小写。
* 没有可用伤害原因时条件失败。

---

## `damage_origin`

**用途：**按照已记录的伤害来源类型对伤害进行分类。

**上下文：**需要伤害来源上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `ANY` | 可填写 `ANY`、`ENTITY`、`BLOCK` 或 `OTHER`。 |

### 示例

```yaml
type: damage_origin
value: ENTITY
```

### 行为与限制

* `OTHER` 表示伤害既不来源于实体，也不来源于方块。
* 在共用实现中，未知值会按照 `ANY` 处理。

---

## `spawn_reason`

**用途：**匹配实体的 Bukkit 生成原因。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须能够提供生成原因。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `reason` | 空 | 单个 `CreatureSpawnEvent.SpawnReason`。 |
| `reasons` | 空 | 接受的生成原因列表。 |

### 示例

```yaml
type: spawn_reason
target: SOURCE
reasons:
  - NATURAL
  - REINFORCEMENTS
```

### 行为与限制

* 配置值会与枚举名称进行比较。
* 即使不在生成触发器中，也可能读取实体已经记录的生成原因。

---

## `combust_duration`

**用途：**检查当前燃烧持续时间结果。

**上下文：**需要燃烧事件结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: combust_duration
min: 1
max: 10
```

### 行为与限制

* 在更早的能力修改器之后检查时，会读取已经修改过的结果。
* 不相关的触发器不会匹配。

---

## `regain_amount`

**用途：**检查当前生命恢复结果。

**上下文：**需要生命恢复事件结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: regain_amount
min: 1
max: 10
```

### 行为与限制

* 在更早的能力修改器之后检查时，会读取已经修改过的结果。
* 不相关的触发器不会匹配。

---

## `explosion_yield`

**用途：**检查当前爆炸掉落倍率结果。

**上下文：**需要实体爆炸结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: explosion_yield
min: 1
max: 10
```

### 行为与限制

* 在更早的能力修改器之后检查时，会读取已经修改过的结果。
* 不相关的触发器不会匹配。

---

## `explosion_radius`

**用途：**检查当前爆炸准备阶段的爆炸半径。

**上下文：**需要爆炸准备事件结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: explosion_radius
min: 1
max: 10
```

### 行为与限制

* 在更早的能力修改器之后检查时，会读取已经修改过的结果。
* 不相关的触发器不会匹配。

---

## `target_reason`

**用途：**匹配已记录的 Bukkit 实体选择目标原因。

**上下文：**需要目标选择或取消目标事件上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 空 | 单个原因枚举名称。 |
| `values` | 空 | 接受的原因列表。 |

### 示例

```yaml
type: target_reason
values:
  - CLOSEST_PLAYER
  - TARGET_ATTACKED_ENTITY
```

### 行为与限制

* 规范化配置值后，匹配不区分大小写。
* 没有目标原因时条件失败。

---

## `interaction_action`

**用途：**匹配 `PlayerInteractEvent.Action`。

**上下文：**需要 `PlayerInteractEvent`，通常来自 `on-interact`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `action` | 空 | 单个操作枚举名称。 |
| `actions` | 空 | 接受的操作列表。 |

### 示例

```yaml
type: interaction_action
actions:
  - RIGHT_CLICK_AIR
  - RIGHT_CLICK_BLOCK
```

### 行为与限制

* 必须至少配置一个操作。

---

## `input_type`

**用途：**匹配 Paper 玩家输入中当前激活、刚刚按下或刚刚松开的控制按键。

**上下文：**需要 `PlayerInputEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `input` | 空 | 单个输入：`FORWARD`、`BACKWARD`、`LEFT`、`RIGHT`、`JUMP`、`SNEAK` 或 `SPRINT`。 |
| `inputs` | 空 | 接受的输入列表。 |
| `state` | `ACTIVE` | 可填写 `ACTIVE`、`PRESSED` 或 `RELEASED`。 |

### 示例

```yaml
type: input_type
inputs:
  - FORWARD
  - SPRINT
state: PRESSED
```

### 行为与限制

* `PRESSED` 和 `RELEASED` 会将事件输入与玩家之前或当前的输入状态进行比较。
* 未知的状态值会回退为 `ACTIVE`。

---

## `event_state`

**用途：**匹配 `PlayerFishEvent.State`。

**上下文：**需要 `PlayerFishEvent`，通常来自 `on-fish`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `state` | 空 | 单个钓鱼状态。 |
| `states` | 空 | 接受的钓鱼状态列表。 |

### 示例

```yaml
type: event_state
states:
  - BITE
  - CAUGHT_FISH
```

### 行为与限制

* 必须至少配置一个状态。

---

## `smash_attack_lands`

**用途：**检查 Paper 重锤猛击尝试是否会成功命中。

**上下文：**需要 `EntityAttemptSmashAttackEvent`，通常来自 Paper 26.2+ 的 `on-attempt-smash-attack`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的猛击成功状态。 |

### 示例

```yaml
type: smash_attack_lands
value: true
```

### 行为与限制

* `ALLOW` 匹配，`DENY` 不匹配，`DEFAULT` 使用事件的原始结果。
* 此条件可在 Spigot 上安全注册，但只有收到对应 Paper 事件时才能匹配。

---

## `food_change`

**用途：**检查即将应用的饥饿值变化量。

**上下文：**需要作用于玩家的 `FoodLevelChangeEvent`，以及可修改的饥饿值结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的变化量范围；正数表示增加饥饿值，负数表示减少饥饿值。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: food_change
compare: '<'
value: 0
```

### 行为与限制

* 检查值等于即将应用的结果饥饿值减去玩家当前饥饿值。

---

## `air_change`

**用途：**检查即将应用的剩余空气值变化量。

**上下文：**需要作用于玩家的 `EntityAirChangeEvent`，以及可修改的空气值结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min / max` | 无限制 | 包含边界值的变化量范围；正数表示增加空气值，负数表示减少空气值。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: air_change
compare: '<'
value: 0
```

### 行为与限制

* 检查值等于当前可修改的空气结果减去事件发生前的 `{previous_air}`。
* 更早执行的能力修改器会影响这里读取到的变化量；不相关的触发器不会匹配。

---

## `state_value`

**用途：**读取 `state` ability 保存且尚未过期的数字状态。

**上下文：**需要与写入状态时相同的 Power、`owner`、`key` 和 `per-target` 设置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `owner` | `SOURCE` | 状态所有者的实体选择器。跨触发器共享玩家状态时建议使用 `PLAYER`。 |
| `key` | `default` | 与 `state` ability 相同的状态池名称。 |
| `per-target` | `false` | 是否把当前目标加入状态键，必须与写入时一致。 |
| `state-target` | `TARGET` | `per-target: true` 时用于状态键的实体选择器；必须与写入端解析到同一实体。 |
| `min / max` | 无限制 | 已保存状态的包含边界值范围。不存在或已过期的状态值为 `0`。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: state_value
owner: PLAYER
key: lava-entry-immunity
min: 1
```

### 行为与限制

* 状态按照 Power ID 隔离，另一个附魔或自定义物品使用相同 `key` 不会冲突。
* `owner`、`key`、`per-target` 或解析出的 `state-target` 实体与写入端不一致时会读取另一个状态池。

---

## `combust_origin`

**用途：**匹配燃烧是由实体、方块还是其他来源引起。

**上下文：**需要 `EntityCombustEvent` 上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `origin` | 空 | 单个值：`ENTITY`、`BLOCK` 或 `OTHER`。 |
| `origins` | 空 | 接受的来源类型列表。 |

### 示例

```yaml
type: combust_origin
origins:
  - ENTITY
  - BLOCK
```

### 行为与限制

* 通过事件子类区分 `EntityCombustByEntityEvent` 和 `EntityCombustByBlockEvent`。

---

## `knockback_cause`

**用途：**匹配 Paper 的 `EntityKnockbackEvent.Cause`。

**上下文：**需要 `EntityKnockbackEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `cause / causes` | 空 | 接受的原因名称。 |
| `reason / reasons` | 空 | 合并到同一接受集合中的别名字段。 |

### 示例

```yaml
type: knockback_cause
causes:
  - ENTITY_ATTACK
```

### 行为与限制

* 必须至少配置一个值。

---

## `knockback_reason`

**用途：**`knockback_cause` 的别名。

**上下文：**需要 `EntityKnockbackEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `cause / causes` | 空 | 接受的原因名称。 |
| `reason / reasons` | 空 | 接受的别名字段。 |

### 示例

```yaml
type: knockback_reason
reason: ENTITY_ATTACK
```

### 行为与限制

* 使用与 `knockback_cause` 完全相同的实现。

---

## `exhaustion_reason`

**用途：**匹配 Bukkit 的 `EntityExhaustionEvent.ExhaustionReason`。

**上下文：**需要 `EntityExhaustionEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `reason` | 空 | 单个消耗度原因。 |
| `reasons` | 空 | 接受的原因列表。 |

### 示例

```yaml
type: exhaustion_reason
reasons:
  - SPRINT
  - JUMP_SPRINT
```

### 行为与限制

* 必须至少配置一个原因。

---

## `villager_trade`

**用途：**区分真正的村民交易和其他 Paper 购买事件。

**上下文：**需要 `PlayerPurchaseEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | `true` 要求事件为 `PlayerTradeEvent`；`false` 要求事件为其他购买事件子类型。 |

### 示例

```yaml
type: villager_trade
value: true
```

### 行为与限制

* 此条件检查事件子类型，而不是交易配方内容。

---

## `cooldown`

**用途：**尝试获取仅属于当前条件的运行时冷却，并且只在成功获取冷却时匹配。

**上下文：**需要非空能力上下文；当 `seconds` 为正数时还需要 `SOURCE`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `seconds` | `0` | 冷却时间，单位为秒；填写非正数时始终匹配。 |
| `owner` | `SOURCE` | 拥有该冷却的实体选择器。 |
| `per-target` | `false` | 是否为每个 `TARGET` 分别记录冷却。 |

### 示例

```yaml
type: cooldown
owner: PLAYER
seconds: 5
per-target: true
```

### 行为与限制

* 冷却键包含所有者身份、能力身份和此条件的配置路径；启用 `per-target` 时还会包含目标身份。
* 此条件会修改运行时状态，并不是只读检查。

---

## `cooldown_material`

**用途：**匹配 Paper `PlayerItemCooldownEvent` 中涉及的物品材质。

**上下文：**需要 `PlayerItemCooldownEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `material` | 空 | 单个 Bukkit 材质名称或命名空间键。 |
| `materials` | 空 | 接受的材质列表。 |

### 示例

```yaml
type: cooldown_material
materials:
  - ENDER_PEARL
  - minecraft:shield
```

### 行为与限制

* 事件材质会按照枚举名称和转换为大写的命名空间键进行比较。

---

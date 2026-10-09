# ⚔️ 战斗与状态修改器

本页面会分别说明每一种修改器。所有条目也支持[能力修改器](README.md)页面中介绍的通用修改器字段。

## 本页面的注册键

* `armor_pierce`
* `missing_health_damage`
* `stack_damage_modifier`
* `revive`
* `warden_anger`
* `vibration_reduce`

---

## `armor_pierce`

**用途：**提高当前伤害结果，以近似实现忽略目标一定比例护甲的效果。

**上下文：**需要作为 `TARGET` 的生物实体，以及可修改的伤害结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `percent` | `0` | 护甲穿透百分比，数值限制在 `0..100`。 |

### 示例

```yaml
type: armor_pierce
percent: 35
```

### 行为与限制

* 修改器会读取目标的护甲值和盔甲韧性属性，但不会修改这些属性本身。
* 此修改器通过补偿原版护甲减伤来实现穿甲效果，并不是独立的真实伤害通道。

---

## `missing_health_damage`

**用途：**根据 `SOURCE` 已损失的生命值百分比，按阶段增加百分比伤害。

**上下文：**需要作为 `SOURCE` 的生物实体，以及可修改的伤害结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `step-percent` | `10` | 每个阶段所需的已损失生命值百分比，最小值为 0.1。 |
| `percent-per-step` | `5` | 每完成一个阶段增加的伤害百分比。 |
| `maximum-percent` | `100` | 伤害加成百分比的最大值。 |

### 示例

```yaml
type: missing_health_damage
step-percent: 10
percent-per-step: 5
maximum-percent: 50
```

### 行为与限制

* 阶段数量按照 `floor(missingPercent / step-percent)` 计算。
* 最终伤害会乘以 `1 + bonus/100`。

---

## `stack_damage_modifier`

**用途：**保存堆叠状态，并将堆叠数量转换为伤害百分比的增加或减少。

**上下文：**需要一个稳定的状态所有者；实际修改伤害时还需要可修改的伤害结果。不同触发器部分使用相同的 `pool` 值时，可以共享同一个状态池。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `damage-type` | `DEALT` | 可填写 `DEALT` 或 `TAKEN`；控制默认作用范围和消息元数据。 |
| `stack-action` | `ADD` | 可填写 `ADD`、`READ`、`CONSUME` 或 `CLEAR`。 |
| `stack-value` | `CURRENT` | 使用 `ADD` 时，选择应用 `CURRENT` 当前堆叠数或 `PREVIOUS` 添加前的堆叠数。 |
| `operation` | `INCREASE` | 可填写 `INCREASE`、`DECREASE` 或 `NONE`。 |
| `pool` | 当前配置路径 | 状态池名称。 |
| `max-stacks` | `5` | 最多保存的堆叠数量，最小值为 1。 |
| `stack-amount` | `1` | `ADD` 操作每次增加的堆叠数量。 |
| `percent-per-stack` | `5` | 每个有效堆叠对应的伤害百分比。 |
| `maximum-percent` | 自动推导 | 最终百分比的上限。 |
| `scope.per-target` | `false` | 是否按照选中的目标分别建立状态池。 |
| `scope.target` | 由类型决定 | `DEALT` 默认使用 `TARGET`，`TAKEN` 默认使用 `SOURCE`。 |
| `scope.per-damage-cause` | `false` | 是否按照 Bukkit 伤害原因分别建立状态池。 |
| `reset.seconds` | `5` | 由 `ADD` 刷新的过期时间；填写非正数时禁用定时过期。 |
| `reset.clear-on` | 空 | 可填写 `AFTER_APPLY`、`AFTER_DAMAGE_DEALT` 或 `AFTER_DAMAGE_TAKEN`。 |
| `messages.*` | 空 | 可选的生命周期消息，以及接收者和节流设置。 |

### 示例

```yaml
type: stack_damage_modifier
pool: combo
damage-type: DEALT
stack-action: ADD
operation: INCREASE
max-stacks: 5
stack-amount: 1
percent-per-stack: 6
reset:
  seconds: 4
scope:
  per-target: true
```

### 行为与限制

* `ADD` 会更新状态；`READ` 会应用状态但不修改它；`CONSUME` 会移除并应用状态；`CLEAR` 只会移除状态。
* 插件重载、卸载或相关实体卸载时，状态和已经安排的消息都会被清除。

---

## `revive`

**用途：**在支持的死亡触发器中请求复活。

**上下文：**主要用于 `on-death`；能否成功应用取决于触发器和服务端平台是否支持。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `health` | `20` | 复活后的生命值，最大不会超过来源实体的生命值上限；`{original}` 表示最大生命值。 |
| `no-drops` | `false` | 应用复活时是否清空死亡掉落物。 |

### 示例

```yaml
type: revive
health: '{original} * 0.5'
no-drops: true
```

### 行为与限制

* 只希望复活生效一次时，应为整个能力或该条目设置次数限制。
* 当 `SOURCE` 不是生物实体时，表达式解析使用的备用最大生命值为 20。

---

## `warden_anger`

**用途：**仅修改监守者愤怒值的正向增加量。

**上下文：**需要 Paper 的 `WardenAngerChangeEvent`，且新的愤怒值必须大于旧值。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: warden_anger
operation: MULTIPLY
value: 0.5
```

### 行为与限制

* 修改器的操作对象是本次增加的愤怒值，而不是监守者当前的总愤怒值。
* 计算得到的新愤怒值会四舍五入，并限制在 `0..150`。

---

## `vibration_reduce`

**用途：**取消由玩家产生的幽匿传感器振动，阻止传感器激活，并避免振动传播到幽匿尖啸器（否则会提升监守者警告等级）。

**上下文：**需要触发实体为玩家的 `BlockReceiveGameEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `random` | `1.0` | 取消振动的概率（0..1）。可通过变量按附魔等级解析。 |

### 示例

```yaml
type: vibration_reduce
random: '{cancel_chance}'
```

### 行为与限制

* 取消事件会阻止幽匿传感器激活，因此不会触发尖啸器，也不会提升监守者警告等级。
* 建议搭配 `environment` 条件（例如 `biomes: [minecraft:deep_dark]`）将效果限定在深暗之域。

---
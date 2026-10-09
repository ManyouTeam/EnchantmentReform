# 🧩 流程控制能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `delay`
* `conditional`
* `any_of`
* `limit`
* `repeat`
* `state`

---

## `delay`

**用途：**经过计划的延迟后执行嵌套能力。

**上下文：**子能力复用原始 `PowerContext`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `ticks` | `delay` 或 `1` | 延迟时间，单位为服务器 Tick，最小值为 1。 |
| `delay` | `1` | 未配置 `ticks` 时使用的旧版别名。 |
| `abilities` | 必填 | 嵌套能力条目。 |

### 示例

```yaml
type: delay
ticks: 20
abilities:
  strike:
    type: lightning
    target: TARGET
```

### 行为与限制

* 执行前，实体和物品可能已经失效。
* 调度会使用适合当前实体或区域的调度器。

---

## `conditional`

**用途：**执行所有匹配的分支；没有任何分支匹配时执行后备能力。

**上下文：**子条件和子能力复用同一个上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `cases` | 空 | 分支配置映射。 |
| `cases.<id>.conditions` | 空 | 使用逻辑 AND 的条件组。 |
| `cases.<id>.abilities` | 空 | 分支匹配时执行的子能力。 |
| `else-abilities` | 空 | 没有任何分支匹配时执行一次的子能力。 |

### 示例

```yaml
type: conditional
cases:
  wounded:
    conditions:
      low:
        type: health_percent
        target: SOURCE
        max: 25
    abilities:
      heal:
        type: set_health
        target: SOURCE
        amount: 10
else-abilities:
  sound:
    type: sound
    sound: BLOCK_NOTE_BLOCK_BASS
```

### 行为与限制

* 分支按照配置中的顺序检查。
* 所有匹配的分支都会执行；不会在第一个匹配项后停止。
* 任意子能力返回的取消信号都会向上传播。

---

## `any_of`

**用途：**根据权重无放回选择一个或多个不同的子能力。

**上下文：**子能力复用同一个上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `1` | 要选择的不同子能力数量。 |
| `abilities` | 必填 | 子能力映射。 |
| `abilities.<id>.rate` | `1` | 正数且为有限值的相对权重。 |

### 示例

```yaml
type: any_of
amount: 1
abilities:
  fire:
    type: fire
    rate: 5
    fire-ticks: 80
  lightning:
    type: lightning
    rate: 1
```

### 行为与限制

* 无效或非正数的权重会被排除。
* 选中的子能力仍会执行自己的检查；执行失败不会重新抽取替代能力。
* 选中子能力返回的取消信号会向上传播。

---

## `limit`

**用途：**使用一组共享的能力层级概率、冷却和执行次数限制包装嵌套能力。

**上下文：**子能力复用同一个上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `abilities` | 必填 | 嵌套能力。 |
| `random` | `1` | 包装器执行概率。 |
| `cooldown` | `0` | 包装器冷却时间，单位为秒。 |
| `times` | `0` | 包装器允许的最大尝试次数；零表示无限制。 |

### 示例

```yaml
type: limit
cooldown: 10
times: 3
abilities:
  sound:
    type: sound
    sound: ENTITY_ENDERMAN_SCREAM
```

### 行为与限制

* 除了包装器限制外，各子能力仍保留自己的独立限制。
* 子能力返回的取消信号会向上传播。

---

## `repeat`

**用途：**立即执行一次嵌套能力，并在配置的持续时间内重复执行。

**上下文：**子能力复用原始上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `abilities` | 必填 | 嵌套能力。 |
| `duration` | `duration-ticks` 或 `0` | 总持续时间；填写非正数时只执行一次。 |
| `duration-ticks` | `0` | `duration` 的别名。 |
| `interval` | `period` 或 `20` | 每次执行之间的 Tick 数，最小值为 1。 |
| `period` | `20` | `interval` 的别名。 |

### 示例

```yaml
type: repeat
duration: 100
interval: 20
abilities:
  pulse:
    type: particle
    particle: SONIC_BOOM
```

### 行为与限制

* 第一次执行会立即发生。
* 计划执行的子能力必须能够处理已经失效的实体、物品或位置。

---

## `state`

**用途：**保存限定于当前能力、并可选择按目标区分的数字状态，在达到阈值时触发子能力。

**上下文：**状态键需要稳定的能力和来源身份。触发的子能力会收到原始上下文，以及状态结果占位符。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `owner` | `SOURCE` | 状态所有者的实体选择器。跨触发器共享玩家状态时建议使用 `PLAYER`。 |
| `key` | `default` | 状态池名称。 |
| `per-target` | `false` | 是否将目标身份加入状态键。 |
| `state-target` | `TARGET` | `per-target: true` 时写入状态键的实体选择器。跨触发器读取时必须解析到同一实体。 |
| `operation` | `ADD` | 可填写 `ADD`、`SET`、`SUBTRACT` 或 `CLEAR`。 |
| `amount` | `1` | 操作数。 |
| `maximum` | 无限制 | 保存数值的最大值。 |
| `duration` | `0` | 状态存储使用的有效时间，单位为秒；非正数表示不按时间过期。 |
| `trigger-at` | 无限制 | 触发子能力的阈值。 |
| `trigger-abilities` | 空 | 更新后的状态达到阈值时执行的子能力。 |
| `on-increase` | 空 | 保存的数值实际增加时执行的子能力。 |
| `on-decrease` | 空 | 保存的数值实际减少时执行的子能力。 |
| `on-unchanged` | 空 | 数值受上下限约束而没有变化时执行的子能力。 |
| `clear-on-trigger` | `true` | 未启用分组消耗时，执行后是否移除完整状态。 |
| `consume-trigger-value` | `false` | 消耗更新值中每个完整的 `trigger-at` 分组，并在执行子能力前只保存余数。要求阈值为正数且为有限值。 |

### 触发子能力占位符

| 占位符 | 类型 | 含义 |
| --- | --- | --- |
| `{state_previous}` | `Double` | 当前能力执行操作前保存的数值。 |
| `{state_current}` | `Double` | 消耗阈值分组前的更新后数值。 |
| `{state_trigger_count}` | `Long` | 更新后数值中包含的完整 `trigger-at` 分组数量。 |
| `{state_remainder}` | `Double` | 移除所有完整阈值分组后剩余的数值。 |

这些占位符可供当前 `state` 能力执行的 `trigger-abilities` 以及对应的 `on-increase`、`on-decrease` 或 `on-unchanged` 回调使用。变化回调在 `trigger-abilities` 之后运行，并且只会在更新值达到 `trigger-at` 时执行。

### 基础示例

```yaml
type: state
key: combo
per-target: true
operation: ADD
amount: 1
maximum: 5
duration: 4
trigger-at: 5
clear-on-trigger: true
trigger-abilities:
  burst:
    type: damage_entity
    amount: 8
```

### 分组消耗示例

下方示例会在多次拾取经验球之间累计经验。每积累完整的 3 点经验就恢复 1 点生命值，不完整的余数会保留到后续拾取时继续累计。

```yaml
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
```

例如，原本保存的余数为 `1`，随后拾取一个价值 `7` 点经验的经验球，会得到 `{state_current} = 8`、`{state_trigger_count} = 2` 和 `{state_remainder} = 2`。嵌套的治疗能力只执行一次，并恢复 2 点生命值。

### 行为与限制

* `CLEAR` 会立即移除状态。
* `state_value` 条件可以读取状态；其 `owner`、`key`、`per-target` 和解析出的 `state-target` 实体必须与写入端一致。
* 未启用 `consume-trigger-value` 时，阈值行为与原始实现保持兼容：子能力执行一次，并由 `clear-on-trigger` 决定是否移除整个状态。
* 启用 `consume-trigger-value` 时，会在执行子能力前消耗所有完整分组，并忽略 `clear-on-trigger`，使余数可以继续保存。
* 无论 `{state_trigger_count}` 是多少，每次外层状态更新只会执行一次触发子能力。需要根据分组数量缩放结果时，应在数值表达式中使用该占位符。
* `trigger-at` 为非正数或非有限值时，不会执行分组消耗。
* 状态保存在内存中，并会在运行时卸载或实体清理时删除。

---

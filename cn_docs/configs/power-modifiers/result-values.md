# 📈 结果与资源修改器

本页面会分别说明每一种修改器。所有条目也支持[能力修改器](README.md)页面中介绍的通用修改器字段。

## 本页面的注册键

* `damage`
* `heal`
* `duration`
* `yield`
* `radius`
* `cooldown_time`
* `item_damage`
* `food`
* `exhaustion`
* `experience`
* `lunge_power`
* `air`

---

## `damage`

**用途：**修改当前可变的伤害结果。

**上下文：**造成伤害的触发器，例如造成伤害或受到伤害的触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: damage
operation: MULTIPLY
value: 1.25
```

### 行为与限制

* YAML 中先执行的修改器会影响后续修改器读取到的 `{original}`。
* 所有匹配能力执行完成后，最终伤害才会应用到事件。

---

## `heal`

**用途：**修改当前恢复的生命值数量。

**上下文：**`on-regain`，或其他提供生命恢复结果的触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: heal
operation: ADD
value: 2
```

### 行为与限制

* YAML 中先执行的修改器会影响后续修改器读取到的 `{original}`。
* 修改后的数值会写回生命恢复事件结果。

---

## `duration`

**用途：**修改当前的燃烧持续时间。

**上下文：**`on-combust`，或其他提供燃烧持续时间结果的触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: duration
operation: MULTIPLY
value: 2
```

### 行为与限制

* YAML 中先执行的修改器会影响后续修改器读取到的 `{original}`。
* 最终结果之后会应用到燃烧事件。

---

## `yield`

**用途：**修改爆炸掉落倍率。

**上下文：**提供可修改爆炸掉落倍率的实体爆炸触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: yield
operation: ADD
value: 2
```

### 行为与限制

* YAML 中先执行的修改器会影响后续修改器读取到的 `{original}`。
* 爆炸掉落倍率影响方块掉落，不代表爆炸半径。

---

## `radius`

**用途：**修改进入爆炸准备状态的实体所使用的爆炸半径。

**上下文：**`on-creeper-explode` / `ExplosionPrimeEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: radius
operation: ADD
value: 2
```

### 行为与限制

* YAML 中先执行的修改器会影响后续修改器读取到的 `{original}`。
* 爆炸生成前会先修改半径。

---

## `cooldown_time`

**用途：**修改 Paper 物品冷却组的冷却时长。

**上下文：**需要 `PlayerItemGroupCooldownEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: cooldown_time
operation: MULTIPLY
value: 0.5
```

### 行为与限制

* 使用共用数字操作。
* 结果会四舍五入，并限制在 `0..40` Tick。

---

## `item_damage`

**用途：**修改可变的物品耐久损耗结果。

**上下文：**需要能够提供物品耐久损耗结果的触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: item_damage
operation: ADD
value: -1
```

### 行为与限制

* 使用共用数字操作。
* 结果会四舍五入，且最小值限制为 0。

---

## `food`

**用途：**修改可变的饥饿值结果。

**上下文：**需要能够提供饥饿值变化结果的触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: food
operation: ADD
value: 2
```

### 行为与限制

* 使用共用数字操作。
* 结果会四舍五入，并限制在 `0..20`。

---

## `exhaustion`

**用途：**修改消耗度事件增加的消耗度数值。

**上下文：**需要 `EntityExhaustionEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: exhaustion
operation: MULTIPLY
value: 0.5
```

### 行为与限制

* 使用共用数字操作。
* 结果限制在 `0..40`。

---

## `experience`

**用途：**修改可变的经验值数量。

**上下文：**支持经验值变化、方块破坏、实体死亡、钓鱼和物品附魔事件。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: experience
operation: MULTIPLY
value: 2
```

### 行为与限制

* 使用共用数字操作。
* 结果会四舍五入，且最小值限制为 0。

---

## `lunge_power`

**用途：**修改 Paper 的突进强度。

**上下文：**需要 `EntityLungeEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: lunge_power
operation: ADD
value: 1
```

### 行为与限制

* 使用共用数字操作。
* 结果会四舍五入，且最小值限制为 0。

---

## `air`

**用途：**修改可变的剩余空气值。

**上下文：**需要能够提供空气值结果的触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 当前值 | 操作数；`{original}` 表示此修改器执行前的当前结果。 |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`；除数为零时保留原始值。 |

### 示例

```yaml
type: air
operation: ADD
value: 40
```

### 行为与限制

* 使用共用数字操作。
* 结果会四舍五入，且最小值限制为 0。

---
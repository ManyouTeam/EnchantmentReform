# 🔀 逻辑与随机条件

本页面会将每一种已注册条件作为独立条目进行说明。所有条目也支持[能力条件](README.md)页面中介绍的通用条件字段。

## 本页面的注册键

* `any`
* `not`
* `random`
* `player_conditions`

---

## `any`

**用途：**至少一个嵌套的带类型条件匹配时，该条件匹配。

**上下文：**嵌套条件会复用同一个 `PowerContext`；嵌套映射按照逻辑 OR（或）进行检查。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `conditions` | 必填 | 嵌套条件条目。每个子条件保留自己的类型、选择器和字段。 |

### 示例

```yaml
type: any
conditions:
  wet:
    type: in_water
    target: SOURCE
  raining:
    type: in_rain
    target: SOURCE
```

### 行为与限制

* 条件管理器会将缺失的嵌套部分视为允许通过的条件组，因此应避免填写空的 `conditions` 部分。
* 通用字段 `not: true` 会反转最终的 OR 结果。

---

## `not`

**用途：**当一个嵌套的逻辑 AND 条件组没有全部匹配时，该条件匹配。

**上下文：**嵌套条件会复用同一个 `PowerContext`，先按照普通的“全部必须通过”规则检查，再反转结果。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `conditions` | 必填 | 按照逻辑 AND（与）检查的嵌套带类型条件。 |

### 示例

```yaml
type: not
conditions:
  in-lava:
    type: in_lava
    target: SOURCE
  burning:
    type: has_potion
    target: SOURCE
    potion: minecraft:fire_resistance
```

### 行为与限制

* 仅有一个子条件时，直接在该子条件中使用通用字段 `not: true` 通常更简洁。
* 同时使用 `type: not` 和通用字段 `not: true` 会进行两次反转。

---

## `random`

**用途：**进行一次新的概率判定。

**上下文：**不需要实体、事件、方块或物品上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `1` | 取值范围为 `0` 到 `1` 的概率；超出范围的值会被限制在该范围内。 |

### 示例

```yaml
type: random
value: 0.25
```

### 行为与限制

* 每次检查都会独立进行一次随机判定。
* 此条件与能力、能力修改器和能力条目层级的 `random` 字段相互独立。

---

## `player_conditions`

**用途：**调用 EnchantmentReform 独立的共享玩家条件注册表。

**上下文：**默认 `target` 为 `SOURCE`；选择器必须解析为玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 玩家选择器。 |
| `conditions` | 必填 | 由共享玩家条件管理器识别的嵌套条目。 |

### 示例

```yaml
type: player_conditions
target: SOURCE
conditions:
  permission:
    type: permission
    permission: example.use
```

### 行为与限制

* 可用的嵌套类型来自独立的玩家条件注册表。
* 目标不是玩家时，条件不匹配。

---
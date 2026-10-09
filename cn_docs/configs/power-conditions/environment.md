# 🌍 环境与位置条件

本页面会将每一种已注册条件作为独立条目进行说明。所有条目也支持[能力条件](README.md)页面中介绍的通用条件字段。

## 本页面的注册键

* `in_water`
* `in_lava`
* `liquid_surface_distance`
* `liquid_transition`
* `in_rain`
* `in_sunlight`
* `in_structure`
* `light_level`
* `clear_weather`
* `storm`
* `thunder`
* `world`
* `night`
* `environment`
* `block_type`
* `block_type_offset`
* `best_tool`
* `preferred_tool`
* `biome_changed`
* `block_break_time`
* `break_time`

---

## `in_water`

**用途：**检查 Bukkit 的 `Entity#isInWater` 状态。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的状态。 |

### 示例

```yaml
type: in_water
target: SOURCE
value: true
```

### 行为与限制

* 选中的实体必须存在。

---

## `in_lava`

**用途：**检查选中实体脚下的方块是否为熔岩。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的状态。 |

### 示例

```yaml
type: in_lava
target: SOURCE
value: true
```

### 行为与限制

* 此条件检查实体当前脚部位置的方块材质。

---

## `liquid_surface_distance`

**用途：**检查选中实体眼睛到正上方液面的垂直距离。

**上下文：**默认 `target` 为 `PLAYER`；需要实体处于水或岩浆中。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `PLAYER` | 实体选择器。生物使用眼睛位置，其他实体使用自身位置。 |
| `liquid` | `ANY` | 接受 `ANY`、`WATER` 或 `LAVA`。 |
| `min / max` | 无限制 | 到液面的垂直距离范围，单位为格。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |
| `scan-limit` | 世界高度上限 | 向上扫描的最大格数，可使用数学表达式与变量。 |

### 示例

```yaml
type: liquid_surface_distance
target: PLAYER
liquid: ANY
max: '{level}'
```

### 行为与限制

* 只沿实体所在的同一 X/Z 列向上扫描连续液体，不会横向寻找液面。
* 水包括水、气泡柱、海带、海草和含水方块；岩浆包括静止和流动岩浆。
* 水与岩浆不会互相连接；液体上方若被非可穿过方块封住，则不匹配。
* 实体不在所选液体中、`liquid` 无效或扫描范围内没有液面时返回 `false`。

---

## `liquid_transition`

**用途：**检查玩家移动时是否刚进入或离开指定液体。

**上下文：**仅适用于 `on-move` 提供的 `PlayerMoveEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `liquid` | `ANY` | 接受 `ANY`、`WATER` 或 `LAVA`。 |
| `transition` | `ENTER` | 接受 `ENTER`、`EXIT`；`LEAVE` 是 `EXIT` 的别名。 |

### 示例

```yaml
type: liquid_transition
liquid: LAVA
transition: ENTER
```

### 行为与限制

* 比较移动事件 `from` 与 `to` 的脚部方块；只有匹配状态发生变化时才返回 `true`。
* `ANY` 表示任意受支持液体；直接从水进入岩浆不会被视为进入 `ANY`，但会被视为进入 `LAVA`。
* 水和岩浆的识别规则与 `liquid_surface_distance` 相同。

---

## `in_rain`

**用途：**检查 Bukkit 的 `Entity#isInRain` 状态。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的状态。 |

### 示例

```yaml
type: in_rain
target: SOURCE
value: true
```

### 行为与限制

* 选中的实体必须存在。

---

## `in_sunlight`

**用途：**按照简化的白天时间和天空光照暴露规则进行检查。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的状态。 |

### 示例

```yaml
type: in_sunlight
target: SOURCE
value: true
```

### 行为与限制

* 当世界时间小于 12300，且实体所在方块的天空光照至少为 15 时，视为处于阳光下。
* 不会直接检查天气。

---

## `in_structure`

**用途：**检查某个位置是否位于配置的任意已生成结构边界框内。

**上下文：**默认 `target` 为 `TARGET`；实体不存在时使用触发器位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `structure` | 空 | 单个结构命名空间键。 |
| `structures` | 空 | 接受的结构键列表。 |

### 示例

```yaml
type: in_structure
target: SOURCE
structures:
  - minecraft:fortress
  - minecraft:bastion_remnant
```

### 行为与限制

* 只检查当前区块报告的结构。
* 解析出的位置点必须位于已生成结构的边界框内。

---

## `light_level`

**用途：**检查方块的总光照等级。

**上下文：**使用实现所要求的来源或位置；共用实现中不提供可配置选择器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min` | `0` | 包含边界的最小光照等级。 |
| `max` | `15` | 包含边界的最大光照等级。 |

### 示例

```yaml
type: light_level
min: 0
max: 7
```

### 行为与限制

* 读取合并后的方块光照值。

---

## `clear_weather`

**用途：**检查上下文所在世界是否没有暴风雨。

**上下文：**需要解析出有效位置和世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的晴朗天气状态。 |

### 示例

```yaml
type: clear_weather
value: true
```

### 行为与限制

* `value: false` 要求世界处于暴风雨天气，但不特别要求正在打雷。

---

## `storm`

**用途：**检查上下文所在世界当前是否存在暴风雨。

**上下文：**需要 `LOCATION` 和有效世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的暴风雨状态。 |

### 示例

```yaml
type: storm
value: true
```

### 行为与限制

* 缺少位置或世界时条件失败。

---

## `thunder`

**用途：**检查上下文所在世界是否正在打雷。

**上下文：**需要解析出有效位置和世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的雷暴状态。 |

### 示例

```yaml
type: thunder
value: true
```

### 行为与限制

* 缺少位置或世界时不会匹配。

---

## `world`

**用途：**匹配触发位置所在世界的名称。

**上下文：**需要解析出有效位置和世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `world` | 空 | 单个世界名称。 |
| `worlds` | 空 | 接受的世界名称列表。 |

### 示例

```yaml
type: world
worlds:
  - world
  - world_nether
```

### 行为与限制

* 必须至少配置一个名称。
* 使用实际的 Bukkit 世界名称进行匹配。

---

## `night`

**用途：**检查固定的原版风格夜间时间范围。

**上下文：**需要从来源或上下文中获得有效世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的夜间状态。 |

### 示例

```yaml
type: night
value: true
```

### 行为与限制

* 夜间定义为世界时间 12300 到 23850，包含两个边界值。
* 不考虑天气和天空可见性。

---

## `environment`

**用途：**匹配世界维度，并可选择同时匹配生物群系。

**上下文：**默认 `target` 为 `SOURCE`；选中的实体必须存在。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 实体选择器。 |
| `dimension` | 空 | Bukkit 世界环境，例如 `NORMAL`、`NETHER` 或 `THE_END`。 |
| `biomes` | 空 | 接受的生物群系枚举名称或命名空间键。 |

### 示例

```yaml
type: environment
target: SOURCE
dimension: NETHER
biomes:
  - BASALT_DELTAS
  - minecraft:soul_sand_valley
```

### 行为与限制

* 配置了 `dimension` 时，必须先匹配维度，之后才会检查生物群系。
* 维度和生物群系列表都为空时，条件失败。

---

## `block_type`

**用途：**使用方块规范匹配事件方块或方块破坏前的快照。

**上下文：**需要 `BLOCK` 或已记录的被破坏方块状态。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `type-name` | 空 | 单个方块规范。 |
| `types` | 空 | 方块规范列表。 |

### 示例

```yaml
type: block_type
types:
  - DIAMOND_ORE
  - '#minecraft:logs'
```

### 行为与限制

* 优先使用方块破坏前的快照，因为实时方块此时可能已经变为空气。
* 匹配使用 `BlockPriceUtil`，因此支持的自定义方块和方块语法与其他方块过滤器相同。

---

## `block_type_offset`

**用途：**匹配相对于上下文方块存在一定偏移的方块。

**上下文：**需要 `BLOCK`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `type-name` | 空 | 单个方块规范。 |
| `types` | 空 | 接受的方块规范。 |
| `offset.x / offset.y / offset.z` | `0` | 相对方块坐标。 |

### 示例

```yaml
type: block_type_offset
types:
  - FARMLAND
offset:
  y: -1
```

### 行为与限制

* 匹配使用 `BlockPriceUtil`。

---

## `best_tool`

**用途：**检查上下文方块和玩家手持物品对应的 Bukkit 首选工具结果。

**上下文：**默认 `target` 为 `SOURCE`；需要玩家和 `BLOCK`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 玩家选择器。 |
| `hand` | `MAIN_HAND` | 可填写 `MAIN_HAND` 或 `OFF_HAND`。 |
| `value` | `true` | 期望的首选工具结果。 |

### 示例

```yaml
type: best_tool
target: SOURCE
hand: MAIN_HAND
value: true
```

### 行为与限制

* 使用 `Block#isPreferredTool(ItemStack)`。

---

## `preferred_tool`

**用途：**`best_tool` 的别名。

**上下文：**与 `best_tool` 需要相同的玩家、方块和手部上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 玩家选择器。 |
| `hand` | `MAIN_HAND` | 手部选择器。 |
| `value` | `true` | 期望的结果。 |

### 示例

```yaml
type: preferred_tool
target: SOURCE
value: true
```

### 行为与限制

* 使用与 `best_tool` 相同的实现。

---

## `biome_changed`

**用途：**检查玩家移动事件是否跨越了生物群系边界。

**上下文：**需要 `PlayerMoveEvent` 上下文，`to` 不能为空，且起点和终点必须位于同一世界。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: biome_changed
```

### 行为与限制

* 比较 `from` 和 `to` 所在方块的生物群系。

---

## `block_break_time`

**用途：**计算玩家破坏上下文方块所需的 Tick 数或时间。

**上下文：**默认 `target` 为 `SOURCE`；需要玩家和 `BLOCK`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 玩家选择器。 |
| `unit` | `TICKS` | 可填写 `TICKS`、`SECONDS` 或 `MILLISECONDS`。 |
| `min / max` | 无限制 | 包含边界值的时间范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: block_break_time
target: SOURCE
unit: TICKS
max: 10
```

### 行为与限制

* 使用 Bukkit 的 `Block#getBreakSpeed(player)`，并通过 `ceil(1 / progressPerTick)` 计算。
* 非正数的破坏速度会被视为正无穷时间。

---

## `break_time`

**用途：**`block_break_time` 的别名。

**上下文：**与 `block_break_time` 需要相同的玩家和方块上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 玩家选择器。 |
| `unit` | `TICKS` | 时间单位。 |
| `min / max` | 无限制 | 包含边界值的范围。 |
| `compare / value` | 未设置 | 另一种比较方式。 |

### 示例

```yaml
type: break_time
unit: SECONDS
max: 1.5
```

### 行为与限制

* 使用与 `block_break_time` 完全相同的计算方式。

---

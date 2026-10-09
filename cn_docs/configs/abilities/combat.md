# ⚔️ 战斗与移动能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `explosion`
* `lightning`
* `damage_entity`
* `damage_item`
* `pull_target`
* `pull_location`
* `teleport_near_target`
* `teleport`
* `guardian_beam`
* `sonic_boom`
* `evoker_fangs`
* `arrow_rain`
* `creeper_stats`
* `swap_health`
* `swap_potion_effects`
* `swap_locations`

---

## `explosion`

**用途：**在解析出的位置创建爆炸和爆炸发射器粒子。

**上下文：**默认 `target` 为 `TARGET`；平台支持时，会将选中的目标作为爆炸来源。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `yield` | `2.0` | 爆炸强度。 |
| `set-fire` | `false` | 爆炸是否可以点燃方块。 |
| `break-blocks` | `false` | 爆炸是否可以破坏方块。 |

### 示例

```yaml
type: explosion
target: TARGET
yield: 3
set-fire: false
break-blocks: false
```

### 行为与限制

* 爆炸伤害会被包装为直接能力伤害，避免递归触发同一条能力链。

---

## `lightning`

**用途：**在目标附近生成仅用于显示的闪电，并直接伤害附近的生物实体。

**上下文：**默认 `target` 为 `TARGET`；`source` 默认使用 `SOURCE`，用于伤害归属。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `count` | `2` | 尝试生成闪电的次数，最小值为 1。 |
| `radius` | `3.0` | 水平随机偏移范围，同时也是附近伤害半径。 |
| `damage` | `4.0` | 每次闪电附近施加的伤害。 |

### 示例

```yaml
type: lightning
target: TARGET
source: SOURCE
count: 3
radius: 4
damage: 6
```

### 行为与限制

* 闪电本身只用于显示；配置的伤害会单独施加。
* 多个闪电伤害区域重叠时，同一个实体可能受到多次伤害。

---

## `damage_entity`

**用途：**对一个生物实体造成一次独立的直接伤害。

**上下文：**默认 `target` 为 `TARGET`；`source` 用于选择伤害归属，默认使用 `SOURCE`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `1` | 伤害数量；支持当前和原始触发器伤害占位符。 |

### 示例

```yaml
type: damage_entity
target: TARGET
source: SOURCE
amount: '{original} * 0.5'
```

### 行为与限制

* 数值最小限制为 0。
* 直接能力伤害会受到保护，避免递归触发能力。

---

## `damage_item`

**用途：**损耗选中装备槽位中的物品耐久度。

**上下文：**默认 `target` 为 `TARGET`；需要具有装备的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `1` | 耐久损耗；可以使用触发器伤害占位符。 |
| `slots` | 空 | 可填写 `MAIN_HAND`、`OFF_HAND`、各护甲槽位或 `ARMOR`。 |
| `mode` | `DIRECT` | `DIRECT` 直接修改物品损耗元数据；`PAPER` 使用 Paper 的物品耐久损耗行为。 |

### 示例

```yaml
type: damage_item
target: TARGET
amount: 2
mode: PAPER
slots:
  - ARMOR
  - MAIN_HAND
```

### 行为与限制

* 空气物品、不可破坏物品和不可损耗物品会被跳过。
* Paper 模式允许触发平台的正常耐久机制，例如耐久附魔和物品损坏事件。

---

## `pull_target`

**用途：**将目标拉向来源、推离来源，或随机选择移动方向。

**上下文：**默认 `target` 为 `TARGET`；`source` 默认使用 `SOURCE`，并作为方向原点。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `direction` | `TOWARD` | 可填写 `TOWARD`、`AWAY`/`AWAY_FROM_SOURCE` 或 `RANDOM`。 |
| `speed` | `strength` 或 `1` | 速度向量大小。 |
| `strength` | `1` | 未配置 `speed` 时使用的旧版别名。 |
| `vertical` | 自动 | 精确的 Y 轴速度；未配置时，实现会提供一个较小的向上分量。 |
| `remove-powder-snow` | `false` | 移动目标前，是否替换目标脚下的细雪。 |

### 示例

```yaml
type: pull_target
source: SOURCE
target: TARGET
direction: TOWARD
speed: 1.1
vertical: 0.25
```

### 行为与限制

* 来源和目标位置重叠时，会使用来源的视线方向作为后备方向。

---

## `pull_location`

**用途：**将选中的实体移动至解析出的位置方向。

**上下文：**默认 `target` 为 `TARGET`；目标与目的地必须位于同一世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `speed` | `strength` 或 `1` | 速度向量大小。 |
| `strength` | `1` | 未配置 `speed` 时使用的别名。 |
| `vertical` | 根据方向计算 | 可选的精确 Y 轴速度。 |
| `preserve-momentum` | `false` | 是否将当前 X/Z 速度加入生成的向量。 |
| `location` | `CONTEXT` | 扩展位置选择器及可选偏移。 |

### 示例

```yaml
type: pull_location
target: TARGET
speed: 0.8
vertical: 0.25
preserve-momentum: true
location:
  target: SOURCE
```

### 行为与限制

* 速度为非正数、位置跨世界或两个位置几乎相同时，会跳过执行。
* 未配置 `vertical` 时，Y 轴速度来自归一化后的方向向量。

---

## `teleport_near_target`

**用途：**将一个生物实体随机传送至其当前位置附近的安全位置。

**上下文：**默认 `target` 为 `TARGET`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min-radius` | `3.0` | 最小水平距离，最小值为 1。 |
| `max-radius` | `8.0` | 最大水平距离，不能小于 `min-radius`。 |
| `max-tries` | `12` | 尝试随机候选位置的次数，最小值为 1。 |

### 示例

```yaml
type: teleport_near_target
target: SOURCE
min-radius: 4
max-radius: 10
max-tries: 16
```

### 行为与限制

* 每个候选位置都会围绕其 Y 坐标搜索安全位置，要求脚部和头部方块可通过，且下方为实体方块。
* 会保留偏航角和俯仰角，并使用调度器安全的传送处理。

---

## `teleport`

**用途：**将选中的实体传送至扩展上下文位置或玩家的重要位置。

**上下文：**默认 `target` 为 `SOURCE`；目的地可以使用普通位置选择器或重要位置模式。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `location` | `CONTEXT` | 扁平选择器，或包含 `target`、`owner`、`world` 和 `offset` 的嵌套部分。 |
| `location-owner / location.owner` | 选中的实体 | 用于玩家专属目的地的实体。 |
| `world / location.world` | 所有者或上下文世界 | `WORLD_SPAWN` 使用的世界。 |
| `offset.x/y/z or location.offset.x/y/z` | `0` | 目的地偏移。 |
| `important locations` | — | `BED_SPAWN`、`RESPAWN`、`WORLD_SPAWN`、`MAIN_WORLD_SPAWN`、`LAST_DEATH`、`COMPASS_TARGET` 及文档中说明的别名。 |

### 示例

```yaml
type: teleport
target: SOURCE
location:
  target: WORLD_SPAWN
  world: world
  offset:
    y: 1
```

### 行为与限制

* 对于非玩家所有者，玩家专属目的地不会返回位置。
* 玩家没有个人重生点时，会回退至当前世界的出生点。
* 传送使用 `SchedulerUtil.teleport`。

---

## `guardian_beam`

**用途：**显示虚拟守卫者光束，等待蓄力完成后伤害目标。

**上下文：**需要作为 `SOURCE` 和 `TARGET` 的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `range` | `18.0` | 开始和蓄力完成时允许的最大距离。 |
| `charge-ticks` | `30` | 蓄力时间，单位为 Tick，最小值为 1。 |
| `damage` | `6.0` | 最终伤害，最小值为 0。 |

### 示例

```yaml
type: guardian_beam
target: TARGET
range: 20
charge-ticks: 35
damage: 8
```

### 行为与限制

* 需要 PacketEvents。
* 蓄力完成前，如果实体失效、死亡、切换世界或超出范围，则不会造成伤害。

---

## `sonic_boom`

**用途：**蓄力并发射类似监守者的音波攻击，同时生成粒子和音效，并造成伤害与击退。

**上下文：**需要作为 `SOURCE` 和 `TARGET` 的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `range-xz` | `15` | 水平范围。 |
| `range-y` | `20` | 最大垂直高度差。 |
| `charge-ticks` | `34` | 发射前的延迟。 |
| `charge-volume / charge-pitch` | `3 / 1` | 蓄力音效的音量与音调。 |
| `boom-volume / boom-pitch` | `3 / 1` | 爆发音效的音量与音调。 |
| `particle-extra-steps` | `7` | 额外粒子位置数量。 |
| `damage` | `10` | 直接伤害。 |
| `knockback-horizontal` | `2.5` | 水平击退倍率。 |
| `knockback-vertical` | `0.5` | 垂直方向倍率。 |

### 示例

```yaml
type: sonic_boom
target: TARGET
charge-ticks: 30
damage: 12
knockback-horizontal: 2
knockback-vertical: 0.4
```

### 行为与限制

* 蓄力完成后会再次检查范围和实体有效性。

---

## `evoker_fangs`

**用途：**在来源与目标之间生成原版唤魔者尖牙攻击阵型。

**上下文：**需要作为 `SOURCE` 和 `TARGET` 的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `damage` | `6.0` | 保存到每个尖牙上的伤害，用于直接伤害处理。 |
| `close-range` | `3.0` | 距离低于该值时使用环形阵型，否则使用直线阵型。 |

### 示例

```yaml
type: evoker_fangs
target: TARGET
damage: 8
close-range: 4
```

### 行为与限制

* 尖牙会向下搜索实体地面。
* 选中的生物实体来源会成为尖牙的所有者。

---

## `arrow_rain`

**用途：**在目标位置上方的随机方形区域中生成向下飞行的箭。

**上下文：**默认 `target` 为 `TARGET`；`source` 控制发射者归属。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `count` | `10` | 箭的数量，最小值为 1。 |
| `radius` | `4.0` | 水平散布范围，最小值为 0.1。 |
| `height` | `12.0` | 生成高度，最小值为 2。 |
| `spread` | `0.18` | X/Z 方向的随机速度散布。 |
| `damage` | `-1` | 箭的基础伤害；负数保留 Bukkit 默认值。 |
| `pierce-level` | `0` | 穿透等级，最小值为 0。 |
| `critical` | `false` | 是否为暴击箭。 |

### 示例

```yaml
type: arrow_rain
target: TARGET
source: SOURCE
count: 16
radius: 5
height: 14
damage: 4
```

### 行为与限制

* 生成的箭无法被拾取。

---

## `creeper_stats`

**用途：**修改选中苦力怕的属性。

**上下文：**默认 `target` 为 `SOURCE`；选中的实体必须是苦力怕。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `explosion-radius` | 保持不变 | 新的爆炸半径，最小值为 0。 |
| `fuse-ticks` | 保持不变 | 新的最大引信 Tick，最小值为 1。 |
| `powered` | 保持不变 | 是否为高压苦力怕。 |

### 示例

```yaml
type: creeper_stats
target: SOURCE
explosion-radius: 6
fuse-ticks: 30
powered: true
```

### 行为与限制

* 只会修改配置中明确填写的字段。

---

## `swap_health`

**用途：**交换来源和目标的当前生命值百分比。

**上下文：**需要作为 `SOURCE` 和 `TARGET` 的生物实体；共用实现不会使用可配置选择器。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: swap_health
```

### 行为与限制

* 会根据两个实体各自的最大生命值重新计算绝对生命值。
* 结果会限制为大于 0，因此此能力不会直接杀死任一实体。

---

## `swap_potion_effects`

**用途：**在来源和目标之间转移或交换选中的活动药水效果。

**上下文：**使用通用 `source` 和 `target`；两者必须是不同的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `direction` | `SOURCE_TO_TARGET` | 可填写 `SOURCE_TO_TARGET`、`TARGET_TO_SOURCE` 或 `SWAP` 的别名。 |
| `potion` | 未设置 | 单个效果选择器。 |
| `potions` | `ALL` | 效果选择器列表，包括效果分类。 |
| `max-effects` | `-1` | 最多选择的效果类型数量；负数表示无限制。 |

### 示例

```yaml
type: swap_potion_effects
source: SOURCE
target: TARGET
direction: SWAP
potions:
  - HARMFUL
max-effects: 3
```

### 行为与限制

* 转移模式会从来源实体移除效果，并将其添加到目标实体。
* 交换模式会交换每一种选中的效果类型；只存在于一侧的效果会移动到另一侧。

---

## `swap_locations`

**用途：**交换两个实体的位置。

**上下文：**使用通用 `source` 和 `target` 选择器。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: swap_locations
source: SOURCE
target: TARGET
```

### 行为与限制

* 两个实体必须不同，并位于同一世界。
* 传送使用调度器安全处理。

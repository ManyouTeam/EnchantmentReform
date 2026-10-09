# 🧍 实体与状态条件

本页面会将每一种已注册条件作为独立条目进行说明。所有条目也支持[能力条件](README.md)页面中介绍的通用条件字段。

## 本页面的注册键

* `headshot`
* `height`
* `sneaking`
* `not_sneaking`
* `health`
* `health_percent`
* `has_potion`
* `distance`
* `bounding_box_distance`
* `food_level`
* `attribute_value`
* `skill_level`
* `gliding`
* `on_ground`
* `in_air`
* `falling`
* `fall_distance`
* `match_entity`
* `match_item`
* `catch_match_item`
* `min_attack_cooldown`
* `blocking`
* `sprinting`
* `swimming`
* `flying`
* `climbing`
* `riding`
* `game_mode`
* `potion_effect_type`
* `effect_type`
* `potion_effect_cause`
* `potion_effect_action`
* `ageable`
* `first_attack_against_entity`
* `first_attack_from_monster`
* `first_monster_attack`
* `target_category`
* `fatal_damage`
* `stationary`

---

## `headshot`

**用途：**检查当前投射物在垂直方向上是否接近目标的眼睛位置。

**上下文：**需要作为 `TARGET` 的生物实体，以及作为 `SKILL` 的投射物；这两个角色由实现固定使用。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `tolerance` | `0.45` | 投射物位置与目标眼睛位置之间允许的最大 Y 轴绝对差值。 |

### 示例

```yaml
type: headshot
tolerance: 0.4
```

### 行为与限制

* 只检查垂直位置；这不是精确的碰撞箱相交检测。
* 应在同时提供这两个角色的投射物伤害或命中触发器中使用。

---

## `height`

**用途：**检查解析出的触发位置所在方块的 Y 坐标。

**上下文：**使用 `PowerContext.location()`，而不是实体选择器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `min` | 最小整数 | 包含边界的最小 Y 值。 |
| `max` | 最大整数 | 包含边界的最大 Y 值。 |

### 示例

```yaml
type: height
min: 60
max: 100
```

### 行为与限制

* 此条件只使用 `min` 和 `max`，不支持共用的数值 `compare` 配置形式。

---

## `sneaking`

**用途：**检查选中的玩家是否正在潜行。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的潜行状态。 |

### 示例

```yaml
type: sneaking
target: SOURCE
value: true
```

### 行为与限制

* 非玩家实体不会匹配。

---

## `not_sneaking`

**用途：**当选中的玩家没有潜行时匹配。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |

### 示例

```yaml
type: not_sneaking
target: SOURCE
```

### 行为与限制

* 对玩家目标而言，等同于使用 `sneaking` 并设置 `value: false`。

---

## `health`

**用途：**使用共用数值比较语法检查当前生命值或最大生命值。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `mode` | `CURRENT` | 可填写 `CURRENT`、`MAX` 或 `MAXIMUM`。 |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种比较方式，例如 `>=`、`<` 或 `=`。 |

### 示例

```yaml
type: health
target: SOURCE
mode: CURRENT
compare: '<='
value: 10
```

### 行为与限制

* 解析数值字段时，正在检查的数值会通过 `{original}` 和 `{current}` 提供。

---

## `health_percent`

**用途：**检查当前生命值占最大生命值的百分比。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `min` | `0` | 包含边界的最小百分比。 |
| `max` | `100` | 包含边界的最大百分比。 |
| `min-max-health` | `0` | 最大生命值本身允许的包含边界最小值。 |
| `max-max-health` | 无限制 | 最大生命值本身允许的包含边界最大值。 |

### 示例

```yaml
type: health_percent
target: TARGET
min: 0
max: 25
min-max-health: 20
```

### 行为与限制

* 百分比按照“当前生命值 ÷ 最大生命值 × 100”计算。
* 此条件不使用 `compare` / `value`。

---

## `has_potion`

**用途：**检查生物实体是否拥有指定的当前有效药水效果。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `potion` | 空 | 药水效果的命名空间键，或受支持的枚举风格名称。 |

### 示例

```yaml
type: has_potion
target: TARGET
potion: minecraft:poison
```

### 行为与限制

* 效果键无效或为空时不会匹配。
* 需要实体没有该效果时，可使用通用字段 `not: true`。

---

## `distance`

**用途：**检查两个上下文实体之间的欧几里得距离。

**上下文：**默认 `source` 为 `SOURCE`，默认 `target` 为 `TARGET`；两个实体必须存在于同一世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `source` | `SOURCE` | 第一个实体选择器。 |
| `target` | `TARGET` | 第二个实体选择器。 |
| `min / max` | 无限制 | 包含边界值的距离范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: distance
source: SOURCE
target: TARGET
min: 4
max: 16
```

### 行为与限制

* 位于不同世界的实体不会匹配。

---

## `bounding_box_distance`

**用途：**检查一个实体的位置到另一个实体碰撞箱的最短距离。

**上下文：**默认 `source` 为 `SOURCE`，默认 `target` 为 `TARGET`；两个实体必须存在于同一世界。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `source` | `SOURCE` | 作为测量起点的实体。 |
| `target` | `TARGET` | 被测量碰撞箱的实体。 |
| `source-eye` | `false` | 使用来源生物的眼睛位置，而不是脚部位置。 |
| `min / max` | 无限制 | 包含边界值的最短距离范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: bounding_box_distance
source: PLAYER
target: TARGET
source-eye: true
min: 3
max: 6.5
```

### 行为与限制

* 测量点位于目标碰撞箱内时，距离为 `0`。
* 对非生物来源启用 `source-eye` 时，仍使用其普通位置。
* 位于不同世界的实体不会匹配。

---

## `food_level`

**用途：**检查玩家当前的饥饿值。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `min / max` | 无限制 | 包含边界值的范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: food_level
target: SOURCE
max: 6
```

### 行为与限制

* 饥饿值使用 Bukkit 的整数值，范围为 0 到 20。

---

## `attribute_value`

**用途：**比较当前玩家的自定义属性最终值。

**上下文：**Power 上下文中必须存在玩家；用于属性加点条件时，该玩家就是正在消耗属性点的玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `attribute` | 必填 | 自定义属性 ID，支持裸 ID、`custom_attribute:` 和 `enchantmentreform:` 写法。 |
| `min / max` | 无限制 | 包含边界的最终值范围。 |
| `compare / value` | 未设置 | 数值比较，支持 `>`、`>=`、`<`、`<=`、`=`、`!=` 等。 |

### 示例

```yaml
type: attribute_value
attribute: strength
compare: '>'
value: 10
```

### 行为与限制

* 最终值包含持久、临时和物品 modifier。
* 玩家或属性不存在时不匹配。

---

## `skill_level`

**用途：**比较当前玩家在指定技能中的等级。

**上下文：**必须存在玩家和已加载的技能。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `skill` | 必填 | 技能 ID，例如 `agility`。 |
| `min / max` | 无限制 | 包含边界的技能等级范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式。 |

### 示例

```yaml
type: skill_level
skill: agility
min: 10
```

### 行为与限制

* 玩家不存在或技能 ID 未知时不匹配。

---

## `gliding`

**用途：**检查生物实体的滑翔状态。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的状态。 |

### 示例

```yaml
type: gliding
target: SOURCE
value: true
```

### 行为与限制

* 非生物实体不会匹配。

---

## `on_ground`

**用途：**检查实体是否处于地面上。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的状态。 |

### 示例

```yaml
type: on_ground
target: TARGET
value: true
```

### 行为与限制

* 数值来自 Bukkit 当前的 on-ground 标记。

---

## `in_air`

**用途：**检查实体 on-ground 状态的反向结果。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` | `true` | 期望的滞空状态。 |

### 示例

```yaml
type: in_air
target: TARGET
value: true
```

### 行为与限制

* 选中的实体必须存在。

---

## `falling`

**用途：**检查滞空实体是否正在向下移动。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `maximum-y` | `0` | 实体的 Y 轴速度必须低于此值。 |
| `value` | `true` | 期望的下落状态。 |

### 示例

```yaml
type: falling
target: PLAYER
maximum-y: 0
value: true
```

### 行为与限制

* 站在地面上的实体不会被判定为正在下落。

---

## `fall_distance`

**用途：**比较实体累计的 Bukkit 坠落距离。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `value` / `compare` | 未设置 / `>=` | 可选的直接数值比较。 |
| `min` | 无限制 | 坠落距离的包含性下限。 |
| `max` | 无限制 | 坠落距离的包含性上限。 |

### 示例

```yaml
type: fall_distance
target: PLAYER
min: 3
```

### 行为与限制

* 数值来自 Bukkit 当前的坠落距离计数器，单位为方块。

---

## `match_entity`

**用途：**对选中的生物实体执行普通的 Match Entity 规则引擎。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `match` | 必填 | 嵌套的 MatchEntityFormat 配置。 |

### 示例

```yaml
type: match_entity
target: TARGET
match:
  entity-types:
    - PLAYER
  entity-health:
    min: 1
```

### 行为与限制

* 缺少 `match` 部分时条件失败。
* 嵌套规则与插件普通实体匹配系统使用的规则相同。

---

## `match_item`

**用途：**对一个或多个选中的物品执行 MatchItemFormat。

**上下文：**使用物品选择器和可选的装备持有者；默认物品为 `CONTEXT`，默认持有者为 `TARGET`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `item` | `CONTEXT` | 物品选择器字符串，或包含 `selector` 和 `holder` 的嵌套部分。 |
| `item-holder` | `TARGET` | 使用扁平配置形式时的持有者选择器。 |
| `match` | 当前部分 | 嵌套的 MatchItemFormat 规则；未填写时，使用当前部分中除控制字段外的字段。 |

### 示例

```yaml
type: match_item
item:
  selector: MAIN_HAND
  holder: SOURCE
match:
  material:
    - DIAMOND_SWORD
```

### 行为与限制

* 任意一个选中的物品匹配时，条件通过。
* 当 `SOURCE` 是玩家时，匹配所使用的玩家为 `SOURCE`；否则使用 `context.player()`。

---

## `catch_match_item`

**用途：**对钓鱼事件中钓到的物品实体执行 MatchItemFormat。

**上下文：**需要 `PlayerFishEvent`，并且 `event.getCaught()` 必须返回一个 `Item`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `match` | 当前部分 | 嵌套的 MatchItemFormat 规则；未填写时，使用当前部分中除控制字段外的字段。 |

### 示例

```yaml
type: catch_match_item
match:
  material:
    - COD
    - SALMON
```

### 行为与限制

* 钓到的对象不是物品，或当前处于无关的钓鱼状态时，条件失败。

---

## `min_attack_cooldown`

**用途：**检查玩家当前的攻击冷却强度。

**上下文：**默认 `target` 为 `TARGET`；选择器必须解析为玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 玩家选择器。 |
| `value` | `0` | 包含边界的最小攻击冷却强度。 |

### 示例

```yaml
type: min_attack_cooldown
target: SOURCE
value: 0.9
```

### 行为与限制

* 使用 Bukkit 的 `Player#getAttackCooldown()`。

---

## `blocking`

**用途：**检查选中的生物实体是否正在使用盾牌格挡。

**上下文：**默认 `target` 为 `TARGET`；选择器必须解析为生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 生物实体选择器。 |
| `value` | `true` | 期望的格挡状态。 |

### 示例

```yaml
type: blocking
target: TARGET
value: true
```

### 行为与限制

* 玩家使用 Bukkit 的实时格挡状态。
* 其他生物实体在举手且任意一只手装备盾牌时匹配。
* 目标缺失或不是生物实体时失败；如果这种情况也应通过，请使用通用 `not: true`。

---

## `perfect_guard`

**用途：**检查玩家是否在举盾后的短暂时间窗内完成格挡。

**上下文：**用于 `on-shield-block`。默认 `target` 为 `TARGET`；选择器必须解析为当前正在格挡的玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 玩家选择器。 |
| `window-millis` | `120` | 从举盾开始计算的最大毫秒数，支持占位符和表达式。 |
| `consume` | `true` | 匹配成功后移除本次举盾记录，使每次举盾最多触发一次完美格挡。 |

### 示例

```yaml
type: perfect_guard
target: TARGET
window-millis: '120 + {level} * 60'
consume: true
```

### 行为与限制

* 使用盾牌右键时会以毫秒精度记录举盾时刻。
* 请配合 `on-shield-block` 使用；该触发器保证攻击确实被盾牌格挡且来自有效的盾牌朝向。
* `consume: true` 时，首次匹配成功后，同一次举盾的后续检查将失败。

---

## `sprinting`

**用途：**检查选中的玩家是否正在疾跑。

**上下文：**默认 `target` 为 `TARGET`；选择器必须解析为玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 玩家选择器。 |
| `value` | `true` | 期望的疾跑状态。 |

### 示例

```yaml
type: sprinting
target: SOURCE
value: true
```

### 行为与限制

* 非玩家实体不会匹配。

---

## `swimming`

**用途：**检查选中的玩家是否正在游泳。

**上下文：**默认 `target` 为 `TARGET`；选择器必须解析为玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 玩家选择器。 |
| `value` | `true` | 期望的游泳状态。 |

### 示例

```yaml
type: swimming
target: PLAYER
value: false
```

### 行为与限制

* 非玩家实体不会匹配。

---

## `flying`

**用途：**检查选中的玩家是否正在飞行。

**上下文：**默认 `target` 为 `TARGET`；选择器必须解析为玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 玩家选择器。 |
| `value` | `true` | 期望的飞行状态。 |

### 示例

```yaml
type: flying
target: PLAYER
value: false
```

### 行为与限制

* 此条件使用 Bukkit 的玩家飞行状态；非玩家实体不会匹配。

---

## `climbing`

检查所选生物实体是否正在攀爬。字段为 `target`（默认 `TARGET`）和布尔值 `value`（默认 `true`）。

```yaml
type: climbing
target: PLAYER
value: false
```

---

## `riding`

检查所选实体是否乘坐其他实体。字段为 `target`（默认 `TARGET`）和布尔值 `value`（默认 `true`）。

```yaml
type: riding
target: PLAYER
value: false
```

---

## `game_mode`

检查所选玩家的游戏模式。`mode` 可填写单个模式，`modes` 可填写模式列表；至少需要配置一个。

```yaml
type: game_mode
target: PLAYER
modes:
  - SURVIVAL
  - ADVENTURE
```

---

## `potion_effect_type`

**用途：**匹配效果事件提供的药水效果类型。

**上下文：**需要 Paper 的 `EntityEffectTickEvent` 或 Bukkit 的 `EntityPotionEffectEvent` 上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `selector / selectors` | 空 | 精确效果选择器。 |
| `effect / effects` | 空 | 效果选择器的别名字段。 |
| `type / types` | 空 | 其他选择器别名字段。 |
| `category / categories` | 空 | `PotionEffectSelector` 支持的效果分类。 |

### 示例

```yaml
type: potion_effect_type
selectors:
  - minecraft:poison
  - HARMFUL
```

### 行为与限制

* 对效果 Tick 事件，从 `getType()` 获取当前效果；对药水效果变更事件，从 `getModifiedType()` 获取。
* 选择器集合为空时不会匹配。

---

## `effect_type`

**用途：**`potion_effect_type` 的别名。

**上下文：**与 `potion_effect_type` 需要相同的事件上下文。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `selector / selectors` | 空 | 效果选择器。 |
| `effect / effects` | 空 | 选择器别名字段。 |
| `type / types` | 空 | 选择器别名字段。 |
| `category / categories` | 空 | 效果分类。 |

### 示例

```yaml
type: effect_type
category: HARMFUL
```

### 行为与限制

* 使用与 `potion_effect_type` 完全相同的实现和行为。

---

## `potion_effect_cause`

**用途：**匹配 Bukkit 药水效果变更事件的原因。

**上下文：**需要 `EntityPotionEffectEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `cause / causes` | 空 | 接受的 `EntityPotionEffectEvent.Cause` 名称，例如 `FOOD`、`POTION_DRINK` 或 `ATTACK`。 |

### 示例

```yaml
type: potion_effect_cause
cause: FOOD
```

### 行为与限制

* 必须至少配置一个原因。
* 原因名称不区分大小写，并会规范化为 Bukkit 枚举风格名称。

---

## `potion_effect_action`

**用途：**匹配 Bukkit 药水效果变更事件的操作。

**上下文：**需要 `EntityPotionEffectEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `action / actions` | 空 | 接受的操作，例如 `ADDED`、`CHANGED`、`REMOVED` 或 `CLEARED`。 |

### 示例

```yaml
type: potion_effect_action
actions:
  - ADDED
  - CHANGED
```

### 行为与限制

* 必须至少配置一个操作。
* 操作名称不区分大小写，并会规范化为 Bukkit 枚举风格名称。

---

## `ageable`

**用途：**检查上下文方块的可生长数据是否已经完全成熟。

**上下文：**需要 `BLOCK`，并且其 `BlockData` 必须实现 Bukkit 的 `Ageable`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的完全成熟状态。 |

### 示例

```yaml
type: ageable
value: true
```

### 行为与限制

* 此条件检查方块或农作物，不检查可成长实体。
* 当前 age 大于或等于最大 age 时视为完全成熟。

---

## `first_attack_against_entity`

**用途：**对于当前条件配置路径，只匹配符合要求的来源玩家与目标实体组合的第一次攻击。

**上下文：**需要作为 `SOURCE` 的玩家、作为 `TARGET` 的生物实体，以及未取消的当前结果。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: first_attack_against_entity
```

### 行为与限制

* 状态身份包含攻击者 UUID、能力 ID、配置路径和目标 UUID。
* 第一次成功检查会立即保存状态；状态会由运行时清理机制清除。

---

## `first_attack_from_monster`

**用途：**对于当前条件配置路径，只匹配防守者第一次受到某个特定怪物的已追踪攻击。

**上下文：**需要作为 `SOURCE` 的 `Monster`，以及通过 `PLAYER` 或 `TARGET` 获取的玩家防守者；已取消的结果不会匹配。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: first_attack_from_monster
```

### 行为与限制

* 状态身份包含防守者、能力 ID、条件路径和怪物。
* 状态会持续到运行时清理、插件重载或相关实体卸载。

---

## `first_monster_attack`

**用途：**`first_attack_from_monster` 的别名。

**上下文：**与 `first_attack_from_monster` 需要相同的怪物和玩家上下文。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: first_monster_attack
```

### 行为与限制

* 使用完全相同的状态与匹配实现。

---

## `target_category`

**用途：**匹配 `TARGET` 的固定分类。

**上下文：**需要 `TARGET`；不使用可配置的实体选择器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `category` | `ANIMAL` | 支持 `MONSTER`、`ANIMAL` 或 `UNDEAD`。 |

### 示例

```yaml
type: target_category
category: UNDEAD
```

### 行为与限制

* 当前亡灵集合包含僵尸、尸壳、溺尸、骷髅、流浪者、凋灵骷髅、凋灵、僵尸村民、僵尸猪灵、幻翼和沼骸。
* 玩家和不受支持的分类不会匹配。

---

## `fatal_damage`

**用途：**检查当前伤害事件的最终伤害是否会杀死 `SOURCE`。

**上下文：**需要作为 `SOURCE` 的生物实体和 `EntityDamageEvent`；实体选择器固定。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: fatal_damage
```

### 行为与限制

* 使用 `event.getFinalDamage()` 与来源实体当前生命值进行比较。
* 不读取插件的可修改伤害结果。

---

## `stationary`

**用途：**检查选中的实体是否在指定时间内始终保持在很小的移动容差范围内。

**上下文：**默认 `target` 为 `TARGET`；需要实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 实体选择器。 |
| `seconds` | `3` | 必须保持静止的时间，单位为秒。 |

### 示例

```yaml
type: stationary
target: SOURCE
seconds: 5
```

### 行为与限制

* 移动的距离平方超过 `0.01`，或切换世界时，会重置计时器。
* 实体卸载、插件卸载，或状态连续 30 分钟未被访问后，状态会被清理。

---

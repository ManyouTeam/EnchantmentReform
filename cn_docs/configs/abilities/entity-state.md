# 🧬 实体状态能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `set_attribute`
* `refresh_attribute`
* `set_health`
* `set_absorption`
* `attribute_layer`
* `set_air`
* `set_food`
* `set_item_cooldown`
* `set_velocity`
* `set_invulnerable`
* `potion_effect`
* `remove_potion_effect`
* `extend_potion_effects`
* `potion_cloud`
* `freeze`
* `fire`
* `experience`
* `skill_experience`

---

## `set_attribute`

**用途：**设置某项 Bukkit 属性的基础值，或设置一项 EnchantmentReform 自定义属性值。

**上下文：**默认 `target` 为 `SOURCE`；选中的实体必须提供所请求的属性。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `attribute` | `max_health` | Bukkit 属性键（例如 `minecraft:max_health`）或自定义属性 ID。 |
| `value` | `1` | 新值；支持使用 `{now}` 表示当前值，使用 `{max}` 表示其最大值。 |

### 示例

```yaml
type: set_attribute
target: SOURCE
attribute: minecraft:max_health
value: '{max} * 1.25'
```

### 行为与限制

* 修改最大生命值时，当前生命值会由平台实现进行调整或限制。
* 此能力修改的是基础值，而不是添加临时属性修改器。
* 自定义属性的目标必须是玩家。结果会取整、限制在配置范围内，并保存到玩家 PDC。

---

## `refresh_attribute`

**用途：**添加或刷新一个临时属性修改器，并防止重复叠加相同副本。

**上下文：**默认 `target` 为 `SOURCE`；目标必须是生物实体，并提供所请求的属性。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `attribute` | `minecraft:max_health` | Bukkit 属性的命名空间键或自定义属性 ID。 |
| `operation` | `ADD_NUMBER` | 支持 `ADD_VALUE`、`ADD_MULTIPLIED_BASE`、`ADD_MULTIPLIED_TOTAL`，也兼容旧 Bukkit 名称 `ADD_NUMBER`、`ADD_SCALAR` 和 `MULTIPLY_SCALAR_1`。 |
| `amount` | `0` | 修改器数值。 |
| `duration` | 来源有效期 | 可选的存在时间，单位为 Tick，最小值限制为 2。附魔或自定义物品提前失效时也会立即移除修改器。 |
| `transition.enabled` | `false` | 启用按条件逐渐增加和衰减的动态修改器。 |
| `transition.rise-duration` | `20` | 渐变进度从 0 增至 1 所需的 Tick。 |
| `transition.fall-duration` | `20` | 渐变进度从 1 降至 0 所需的 Tick。 |
| `transition.update-interval` | `1` | 更新进度和属性的 Tick 间隔，限制在 1 到 20。 |
| `transition.curve` | `LINEAR` | 数值曲线：`LINEAR`、`EASE_IN`、`EASE_OUT` 或 `EASE_IN_OUT`。 |
| `transition.active-conditions` | 空 | 满足时增加进度；不满足时衰减。空条件始终满足。 |
| `transition.on-start` | 空 | 每次由衰减状态重新进入充能状态时执行的子能力。 |
| `transition.on-full` | 空 | 进度首次达到 1 时执行的子能力。 |
| `transition.on-decay-start` | 空 | 充能条件由满足变为不满足时执行的子能力。 |
| `transition.on-empty` | 空 | 衰减进度归零时执行的子能力。 |

### 示例

```yaml
type: refresh_attribute
target: SOURCE
attribute: minecraft:movement_speed
operation: ADD_NUMBER
amount: 0.03
duration: 100
```

### 渐变示例

渐变模式通常应放在 `on-tick` 中。激活条件必须配置在 `transition.active-conditions` 内，否则外层条件失败后能力无法继续执行衰减。

```yaml
type: refresh_attribute
target: PLAYER
attribute: minecraft:armor
operation: ADD_NUMBER
amount: 6
transition:
  enabled: true
  rise-duration: 60
  fall-duration: 200
  update-interval: 1
  curve: LINEAR
  active-conditions:
    sprinting:
      type: sprinting
      target: PLAYER
      value: true
```

### 行为与限制

* 修改器身份由实体 UUID、能力 ID、Ability 配置路径、来源槽位和属性共同确定，因此同一来源再次执行会刷新其修改器，允许重复的不同来源则可独立叠加。
* 代次检查可以防止较早安排的移除任务删除已经被刷新的修改器。
* 无论是否配置 `duration`，附魔或自定义物品失效时都会立即移除修改器；未配置 `duration` 时，来源失效就是正常的生命周期终点。
* 实体卸载或插件卸载时也会移除修改器；最大生命值降低时，当前生命值会受到限制。
* 对于自定义属性，目标必须是玩家。Ability 会刷新一个仅存在于运行时的 modifier，并在到期或来源失效时移除，不会持久化到 PDC。
* 渐变模式中的 `amount` 是进度为 1 时的最大修改器数值，实际数值为 `amount × curve(progress)`。
* 渐变进度按目标实体、Power、Ability 路径、来源槽位和属性隔离，并且只保存在内存中。
* 渐变模式由其调用触发器负责持续更新；推荐使用 `on-tick`。`duration` 在渐变模式下不参与定时移除。
* 渐变状态在进度归零、来源失效、实体卸载、死亡或插件卸载时清理。
* 阶段回调只在对应边界发生时执行；持续保持满值或空值不会重复执行回调。

---

## `set_health`

**用途：**设置生物实体的当前生命值。

**上下文：**默认 `target` 为 `SOURCE`；目标必须是仍然存活的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `1` | 新的生命值；支持当前生命值、最大生命值，以及实现中记录的触发器伤害占位符。 |

### 示例

```yaml
type: set_health
target: SOURCE
amount: 'min({max-health}, {health} + 6)'
```

### 行为与限制

* 数值会限制在合法的生命值范围内，并受到实现中的硬性上限约束。
* 设置为零可能会杀死该实体。

---

## `set_absorption`

**用途：**设置可受伤害实体的伤害吸收量。

**上下文：**默认 `target` 为 `SOURCE`；目标必须是仍然存活的可受伤害实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `10` | 新的伤害吸收值；支持使用 `{health}` 表示当前吸收量，使用 `{max-health}` 表示最大吸收量。 |

### 示例

```yaml
type: set_absorption
target: SOURCE
amount: 'min({max-health}, {health} + 4)'
```

### 行为与限制

* 数值首先被硬性限制为不超过 2048，然后再受实体最大吸收量限制。
* 已死亡或生命值为零的实体会被跳过。

---

## `attribute_layer`

**用途：**使用自定义属性作为共享层数容量，或作为子能力的执行等级。

**上下文：**默认 `target` 为 `SOURCE`；目标必须是玩家，且自定义属性必须存在。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `attribute` | `absorption` | 自定义属性 ID；使用玩家的最终属性值作为容量/等级。 |
| `operation` | `ADD` | 可填写 `ADD`、`SYNC` 或 `EXECUTE`。 |
| `amount` | `1` | `ADD` 增加的层数。 |
| `health-per-layer` | `1` | 每层提供的最大伤害吸收量和伤害吸收值（0.5 颗心）。 |
| `duration` | `140` | 开始逐层衰减前的未触发宽限 Tick；每次 `ADD` 都会刷新。 |
| `decay-interval` | `20` | 持续时间结束后，每次移除一层的间隔 Tick。 |
| `abilities` | 空 | 使用 `EXECUTE` 时，以属性最终值作为 `{level}` 执行的子能力。 |
| `on-increase` | 空 | 使用 `ADD` 且层数增加时执行的子能力。 |
| `on-maximum` | 空 | 使用 `ADD` 且已经达到容量时执行的子能力。 |

### 示例

```yaml
type: attribute_layer
target: PLAYER
attribute: dodge
operation: EXECUTE
random: '{attribute:dodge:melee_dodge_chance_decimal}'
abilities:
  cancel:
    type: cancel_event
```

### 行为与限制

* `EXECUTE` 在属性最终值为零时跳过子能力；外层仍可使用 `random` 等通用字段。
* `ADD` 和 `SYNC` 管理按玩家与属性区分的共享伤害吸收层池。
* 每次 `ADD` 都会刷新整个层池的持续时间；时间结束后按 `decay-interval` 的间隔逐层移除。

---

## `set_air`

**用途：**设置实体剩余的空气 Tick。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | 当前空气值 | 新的剩余空气值；支持 `{air}` 和 `{max-air}`。 |

### 示例

```yaml
type: set_air
target: TARGET
amount: 0
```

### 行为与限制

* 数值会限制在实现允许的下限和最大空气值之间。
* 平台支持时，负空气值可以立即继续溺水行为。

---

## `set_food`

**用途：**设置玩家的饥饿值和饱和度。

**上下文：**默认 `target` 为 `SOURCE`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `food` | `1` | 新的饥饿值，限制在 `0..20`；`{original}` 表示当前饥饿值。 |
| `saturation` | `0` | 新的饱和度，限制在 `0..20`；`{original}` 表示当前饱和度。 |
| `cost` | 未设置 | 启用消耗模式：优先扣除饱和度，不足部分向上取整后从饥饿值扣除。设置后忽略 `food` 和 `saturation`。 |

### 示例

```yaml
type: set_food
target: SOURCE
food: 20
saturation: 5
```

### 行为与限制

* 每次执行能力时，两个字段都会被应用。
* 配置 `cost` 时改为消耗模式；消耗值不能低于零。

---

## `set_item_cooldown`

**用途：**设置或清除玩家某种材质的原版冷却。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `TARGET` | 玩家选择器。 |
| `material` | `SHIELD` | 要更改冷却组的 Bukkit 材质或命名空间材质。 |
| `ticks` | `duration`，再回退为 `0` | 冷却 Tick 数；零表示清除冷却。 |
| `duration` | `0` | `ticks` 缺失时使用的别名。 |

### 示例

```yaml
type: set_item_cooldown
target: TARGET
material: minecraft:shield
ticks: 100
```

### 行为与限制

* 使用 Bukkit `Player#setCooldown(Material, int)`，因此遵循该材质的原版冷却组。
* 非玩家目标、无效材质或空气材质会被跳过。
* 负数持续时间会被限制为零。

---

## `set_velocity`

**用途：**设置或修改实体的速度。

**上下文：**默认 `target` 为 `TARGET`；部分方向模式还需要 `SOURCE`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`MULTIPLY` 或 `SCALE`。 |
| `direction` | `VECTOR` | 可填写 `VECTOR`、`LOOK`、`LOOK_HORIZONTAL`、`RANDOM_HORIZONTAL_SIDE`、`SOURCE_TO_TARGET`、`TARGET_TO_SOURCE` 或 `UP`。 |
| `x / y / z` | `0` | `VECTOR` 模式使用的配置向量。 |
| `strength` | `1` | 方向模式使用的强度。 |
| `vertical` | `0` | 添加到生成的 Y 轴速度上的数值。 |
| `multiplier` | `1` | `SCALE` 操作使用的缩放倍率。 |
| `preserve-vertical` | `false` | 使用目标当前的 Y 轴速度代替生成向量的 Y 分量。 |
| `minimum-vertical` | 无限制 | 保留垂直速度时使用的最低 Y 轴速度。 |
| `minimum-x / minimum-y / minimum-z` | 无限制 | 可选的最终速度分量下限。 |
| `maximum-x / maximum-y / maximum-z` | 无限制 | 可选的最终速度分量上限。 |
| `reset-fall-distance` | `false` | 应用速度后将实体摔落距离重置为零。 |

### 示例

```yaml
type: set_velocity
target: TARGET
operation: SET
direction: SOURCE_TO_TARGET
strength: 1.2
vertical: 0.35
```

### 行为与限制

* `SOURCE` 与 `TARGET` 不在同一世界时，基于两者计算的方向向量无法产生有效方向。
* `MULTIPLY` 会分别乘算向量的各个分量。
* `LOOK_HORIZONTAL` 会忽略视线俯仰角，只使用归一化的水平视线方向。
* `RANDOM_HORIZONTAL_SIDE` 会以相同概率选择目标视线方向的水平左侧或右侧；也可以使用别名 `RANDOM_SIDE`。
* 速度分量限制会在所选操作完成后应用。

---

## `set_invulnerable`

**用途：**启用或禁用实体的无敌状态，并可选择临时生效。

**上下文：**默认 `target` 为 `SOURCE`；需要一个实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | `true` | 期望的无敌状态。 |
| `duration` | `0` | 持续时间，单位为 Tick；非正数表示持续生效。 |

### 示例

```yaml
type: set_invulnerable
target: SOURCE
value: true
duration: 60
```

### 行为与限制

* 临时状态的恢复由能力工具安排，并要求实体在恢复时仍然有效。

---

## `potion_effect`

**用途：**向生物实体施加一个药水效果。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `potion` | `SLOWNESS` | 药水效果注册键。 |
| `duration` | `100` | 持续时间，单位为 Tick。 |
| `infinite-duration-threshold` | `999999` | 正数阈值；持续时间达到该值时转换为无限持续。填写零会禁用转换。 |
| `amplifier` | `0` | 效果倍率，最小值为 0。 |
| `ambient` | `false` | 是否为环境效果。 |
| `particles` | `true` | 是否显示粒子。 |
| `icon` | `true` | 是否显示 HUD 图标。 |
| `accumulate` | `false` | 是否累加已有持续时间，并保留更高的效果倍率。 |

### 示例

```yaml
type: potion_effect
target: TARGET
potion: SLOWNESS
duration: 120
amplifier: 1
accumulate: true
```

### 行为与限制

* 有限持续时间最小限制为 1 Tick。
* 累加后的持续时间会在整数最大值处饱和。

---

## `remove_potion_effect`

**用途：**移除生物实体身上选中的活跃药水效果。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `potion` | 未设置 | 单个精确效果或选择器。 |
| `potions` | 空 | 按顺序检查的选择器；支持精确键，以及 `ALL`、`BENEFICIAL`、`HARMFUL` 和 `NEUTRAL` 等分类。 |
| `max-effects` | `-1` | 最多移除的效果数量；负数表示无限制。 |

### 示例

```yaml
type: remove_potion_effect
target: TARGET
potions:
  - HARMFUL
  - minecraft:slowness
max-effects: 2
```

### 行为与限制

* 在每个选择器规则中，效果会按照注册键以确定性顺序选取。

---

## `extend_potion_effects`

**用途：**延长或缩短选中药水效果的剩余持续时间。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `potion` | `HARMFUL` | 单个精确效果或分类选择器。 |
| `potions` | 空 | 按顺序检查的选择器；支持 `ALL`、`BENEFICIAL`、`HARMFUL`、`NEUTRAL` 和精确效果键。 |
| `percentage` | `0` | 根据每个选中效果当前剩余时间增加的百分比；负值表示缩短。 |
| `multiplier` | 未设置 | 直接使用的持续时间倍率；存在时会覆盖 `percentage`。小于 `1` 缩短，大于 `1` 延长。 |
| `max-effects` | `-1` | 最多修改的效果数量；负数表示无限制。 |

### 示例

```yaml
type: extend_potion_effects
target: TARGET
potions:
  - minecraft:poison
  - minecraft:slowness
percentage: 20
```

```yaml
# 玩家获得负面效果时缩短 30% 持续时间。
type: extend_potion_effects
target: PLAYER
potions:
  - HARMFUL
percentage: -30
```

### 行为与限制

* 每个效果的倍率、环境标记、粒子和图标设置都会保留。
* 无限持续效果不会发生变化。
* 有限持续时间会四舍五入到最接近的 Tick，并在整数最大值处饱和。
* 当由 `on-potion-effect` 触发器触发时，会直接修改即将生效的效果；若缩短后持续时间归零，则移除该效果（并取消事件）。
* 在每个选择器规则中，效果会按照注册键以确定性顺序选取。

---

## `potion_cloud`

**用途：**生成一个包含单个自定义药水效果的区域效果云。

**上下文：**默认 `target` 为 `TARGET`；效果云位置使用解析出的触发位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `3.0` | 效果云半径。 |
| `duration` | `120` | 效果云存在时间，单位为 Tick。 |
| `potion` | `POISON` | 药水效果键。 |
| `potion-duration` | `100` | 药水效果持续时间。 |
| `potion-amplifier` | `1` | 药水效果倍率。 |
| `accumulate` | `false` | 是否累加采样到的已有持续时间，并保留更高的效果倍率。 |

### 示例

```yaml
type: potion_cloud
potion: POISON
radius: 4
duration: 160
potion-duration: 100
potion-amplifier: 0
```

### 行为与限制

* 启用累加时，只会在创建效果云时采样附近实体已有的效果状态；之后不会针对每个未来进入的实体独立重新计算。

---

## `freeze`

**用途：**修改生物实体的冰冻 Tick。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `freeze-ticks` | `60` | 配置的冰冻 Tick，最小值限制为 0。 |
| `accumulate` | `false` | 当前实现中：`false` 会加上已有 Tick，`true` 会替换已有 Tick。 |

### 示例

```yaml
type: freeze
target: TARGET
freeze-ticks: 80
accumulate: false
```

### 行为与限制

* 历史配置项名称 `accumulate` 与当前实现的实际行为相反。

---

## `fire`

**用途：**修改生物实体的燃烧 Tick。

**上下文：**默认 `target` 为 `TARGET`；需要生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `fire-ticks` | `60` | 配置的燃烧 Tick，最小值限制为 0。 |
| `accumulate` | `false` | 当前实现中：`false` 会加上已有 Tick，`true` 会替换已有 Tick。 |

### 示例

```yaml
type: fire
target: TARGET
fire-ticks: 100
accumulate: false
```

### 行为与限制

* 历史配置项名称 `accumulate` 与当前实现的实际行为相反。

---

## `experience`

**用途：**给予玩家原始经验点数或经验等级。

**上下文：**默认 `target` 为 `SOURCE`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `1` | 给予的经验数量；负数遵循平台行为。 |
| `mode` | `POINTS` | `POINTS` 给予原始经验点数；`LEVELS` 给予固定经验等级。 |

### 示例

```yaml
type: experience
target: SOURCE
amount: 5
mode: POINTS
```

### 行为与限制

* `POINTS` 调用 Bukkit `Player#giveExp`；`LEVELS` 调用 `Player#giveExpLevels`。

---

## `skill_experience`

**用途：**为一个已配置的 EnchantmentReform 技能增加经验。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家，并且技能 ID 必须存在。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `skill` | 必填 | 获得经验的技能 ID。 |
| `source` | 无 | 可选的技能来源 ID；该来源必须设置 `manual: true`，设置后由来源控制经验公式与反滥用行为。 |
| `amount` | `0`；设置 `source` 时为 `1` | 直接增加的正数经验，或传给手动来源 `{amount}` 的输入值；支持表达式与能力变量。 |

### 示例

```yaml
type: skill_experience
target: PLAYER
skill: agility
source: projectile_dodge
```

### 行为与限制

* 直接发放会正常应用升级奖励，但不经过特定来源的反滥用限制。
* 被引用的来源必须设置 `manual: true`；它会显示在技能来源菜单中并使用正常的经验提示与反滥用流程，但不会由声明的触发器自动发放。

---

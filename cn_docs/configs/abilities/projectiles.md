# 🏹 投射物能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `launch_projectile`
* `shulker_bullet`
* `reflect_projectile`
* `ricochet_projectile`
* `homing_projectile`

---

## `launch_projectile`

**用途：**由一个生物实体发射者生成并配置一个或多个投射物实体。

**上下文：**配置了 `source` 时，它用于选择发射者；否则使用 `target` 选择发射者。`TARGET` 仍然作为瞄准目标。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `entity-type` | `ARROW` | 可生成且实现 `Projectile` 的 Bukkit 实体类型。 |
| `speed` | `1.5` | 发射速度。 |
| `extra-y` | `0` | 额外添加的 Y 轴分量。 |
| `count` | `1` | 投射物数量，最小值为 1。 |
| `spread-degrees` | `0` | 扇形散布的最大角度；多个投射物会均匀分布。 |
| `spawn-offset` | `0` | 从发射者眼睛位置沿前方移动的生成偏移量。 |
| `damage` | 未设置 | 保存到投射物上的能力伤害覆盖值。 |
| `inherit-powers` | `false` | 是否追踪投射物，以继续执行投射物 Tick 和命中触发器。 |
| `fireball-yield` | `1.0` | 爆炸型投射物的爆炸威力。 |
| `fireball-incendiary` | `true` | 火球是否能够引燃方块。 |
| `potion-type` | 未设置 | 投掷药水使用的基础药水类型。 |
| `potion` | 未设置 | 未配置 `potion-effects` 时使用的单个自定义效果。 |
| `duration` | `100` | 单个自定义效果的持续时间。 |
| `amplifier` | `0` | 单个自定义效果的倍率。 |
| `potion-effects` | 未设置 | 自定义效果映射；每项可以配置 potion、duration 和 amplifier。 |

### 示例

```yaml
type: launch_projectile
source: SOURCE
entity-type: SPLASH_POTION
speed: 1.3
count: 3
spread-degrees: 12
inherit-powers: true
potion-effects:
  poison:
    potion: POISON
    duration: 100
    amplifier: 0
```

### 行为与限制

* 存在位于同一世界的生物实体目标时会瞄准该目标；否则使用发射者的视线方向。
* 如果生成结果不是投射物，该实体会被移除；潜影贝导弹会接收当前目标。
* `inherit-powers` 会使用当前玩家、触发物品和装备槽位追踪投射物。

---

## `shulker_bullet`

**用途：**`launch_projectile` 的兼容性别名。

**上下文：**使用与 `launch_projectile` 相同的发射者选择、瞄准目标和字段。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `entity-type` | `ARROW` | 通常设置为 `SHULKER_BULLET`。 |
| `speed` | `1.5` | 发射速度。 |
| `extra-y` | `0` | 额外添加的 Y 轴分量。 |
| `count` | `1` | 投射物数量，最小值为 1。 |
| `spread-degrees` | `0` | 扇形散布的最大角度；多个投射物会均匀分布。 |
| `spawn-offset` | `0` | 从发射者眼睛位置沿前方移动的生成偏移量。 |
| `damage` | 未设置 | 保存到投射物上的能力伤害覆盖值。 |
| `inherit-powers` | `false` | 是否追踪投射物，以继续执行投射物 Tick 和命中触发器。 |
| `fireball-yield` | `1.0` | 爆炸型投射物的爆炸威力。 |
| `fireball-incendiary` | `true` | 火球是否能够引燃方块。 |
| `potion-type` | 未设置 | 投掷药水使用的基础药水类型。 |
| `potion` | 未设置 | 未配置 `potion-effects` 时使用的单个自定义效果。 |
| `duration` | `100` | 单个自定义效果的持续时间。 |
| `amplifier` | `0` | 单个自定义效果的倍率。 |
| `potion-effects` | 未设置 | 自定义效果映射；每项可以配置 potion、duration 和 amplifier。 |

### 示例

```yaml
type: shulker_bullet
entity-type: SHULKER_BULLET
target: SOURCE
speed: 1.2
```

### 行为与限制

* 存在当前上下文目标时，生成的潜影贝导弹会自动将其设为目标。
* 其他所有行为均与本仓库中的 `launch_projectile` 实现相同。

---

## `reflect_projectile`

**用途：**将当前上下文中的投射物反射回原攻击者。

**上下文：**`target` 默认为 `SKILL`，且必须解析为投射物；`shooter` 默认为 `PLAYER`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SKILL` | 要反射的投射物实体选择器。 |
| `shooter` | `PLAYER` | 反射后的新发射者。 |
| `velocity-multiplier` | `1.0` | 反转来袭速度后应用的速度倍率。 |
| `damage-multiplier` | `1.0` | 反射投射物之后造成伤害时应用的倍率。 |
| `reflection-cooldown` | `0` | 每名玩家独立计算的反射冷却，单位为秒。 |
| `minimum-velocity-squared` | `0.08` | 速度长度平方低于此值时改用备用方向。 |
| `fallback-speed` | `0.95` | 投射物近乎静止时，朝原攻击者发射所使用的速度。 |
| `origin-distance` | `0.55` | 将投射物放到玩家前方时使用的距离。 |
| `abilities` | 未设置 | 仅在成功反射后执行的嵌套能力。 |

### 示例

```yaml
type: reflect_projectile
target: SKILL
shooter: PLAYER
random: 0.5
reflection-cooldown: 1.2
velocity-multiplier: 0.85
damage-multiplier: 0.75
abilities:
  effect:
    type: sound
    location: PLAYER
    sound: ITEM_SHIELD_BLOCK
```

### 行为与限制

* 成功反射后会取消当前命中事件。
* 箭矢会替换为全新的反射副本，避免原箭已完成命中后直接下坠；其他投射物仍原地重定向。
* 实体类型及负载数据会保留，发射者、位置、速度与伤害倍率则被更新。
* 原有投射物能力追踪会停止，避免所有权改变后继续执行原发射者的投射物能力。
* 已反射的投射物会被标记，不能再次被此能力反射。
* 可在嵌套能力中使用标准 `particle` 和 `sound`，无需重复实现粒子与声音逻辑。

---

## `ricochet_projectile`

**用途：**箭矢撞击方块后，生成一支沿碰撞表面反射方向继续飞行的同类箭矢。

**上下文：**需要 `on-projectile-hit`、方块碰撞，且 `SKILL` 必须解析为 `AbstractArrow`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `maximum-ricochets` | `1` | 最大连续反弹次数，限制在 `0..12`。 |
| `speed-bonus-per-ricochet` | `0.1` | 每次反弹应用的速度增幅比例。 |
| `damage-bonus-per-ricochet` | `0` | 每次反弹后累积的固定额外伤害。 |
| `minimum-velocity-squared` | `0.09` | 允许反弹的最小解析入射速度平方。 |
| `minimum-live-velocity-squared` | `0.0004` | 实时速度低于该值时，改用箭矢朝向作为入射方向。 |
| `minimum-post-ricochet-speed` | `0.45` | 应用单次速度增幅前的最低速度。 |
| `maximum-post-ricochet-speed` | `1.5` | 应用单次速度增幅后的最终速度上限。 |
| `maximum-ricochet-distance` | `24` | 每次反弹后的最大累计飞行距离；非正数表示不限制。 |
| `surface-offset` | `0.22` | 生成点远离碰撞表面的距离。 |
| `direction-offset` | `0.14` | 沿反射方向追加的生成偏移。 |
| `abilities` | 未设置 | 仅在成功反弹后执行的嵌套能力。 |

### 示例

```yaml
type: ricochet_projectile
maximum-ricochets: 3
speed-bonus-per-ricochet: 0.2
damage-bonus-per-ricochet: 1.5
abilities:
  sparks:
    type: particle
    particle: ELECTRIC_SPARK
    location: CONTEXT
```

### 行为与限制

* 根据碰撞方块表面的法线计算反射方向。
* 替代箭会保留箭矢类型、药水载荷、发射者、拾取状态、持久化数据和相关战斗属性。
* 新箭会继承投射物能力追踪，因此后续撞击方块时仍可继续反弹。
* 固定额外伤害会随反弹次数累积，并在箭矢伤害实体时追加。
* 撞击特效请通过嵌套的标准 `particle` 和 `sound` ability 配置。

---

## `homing_projectile`

**用途：**引导当前投射物飞向已经锁定或新选中的生物实体目标。

**上下文：**要求 `SKILL` 是有效投射物；通常用于投射物 Tick 触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `16` | 搜索或锁定目标的最大半径。 |
| `strength` | `0.2` | 转向混合强度，数值限制在 `0.01..1`。 |
| `require-line-of-sight` | `false` | 是否要求目标处于视线范围内。 |
| `max-angle` | `360` | 以当前飞行方向为中心的最大锥形半角。 |
| `lead` | `0` | 目标运动预测系数。 |
| `max-ticks` | `100` | 最大执行次数；填写非正数时禁用过期。 |
| `remove-on-expire` | `true` | 过期时是否移除投射物。 |
| `disable-gravity` | `false` | 自动追踪期间是否禁用重力。 |

### 示例

```yaml
type: homing_projectile
radius: 24
strength: 0.18
max-angle: 90
lead: 0.5
max-ticks: 120
```

### 行为与限制

* 投射物会保留之前仍然有效的目标，只在需要时重新搜索。
* 会忽略创造模式和旁观模式玩家。
* 目标优先级依次为：现有锁定目标、来源怪物当前目标、附近最佳候选目标。
* 过期时会返回取消请求，使投射物追踪可以停止。

---

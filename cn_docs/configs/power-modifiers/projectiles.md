# 🏹 投射物修改器

本页面会分别说明每一种修改器。所有条目也支持[能力修改器](README.md)页面中介绍的通用修改器字段。

## 本页面的注册键

* `modify_projectile`
* `replace_projectile`

---

## `modify_projectile`

**用途：**直接修改当前投射物。

**上下文：**优先使用作为 `SKILL` 的投射物，不存在时尝试使用作为 `TARGET` 的投射物；通常用于射击、发射或投射物 Tick 触发器。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `speed-multiplier` | `1` | 乘算当前速度。 |
| `draw-speed-multiplier` | 未设置 | 可获取弓的蓄力程度时，根据记录的弓蓄力缩放速度。 |
| `spread-degrees` | 未设置 | 在 X/Y 方向添加随机角度散布。 |
| `accuracy` | 未设置 | 将投射物方向混合调整至来源生物实体的视线方向；数值限制在 `0..1`。 |
| `gravity` | 保持不变 | 启用或禁用重力。 |
| `fire-ticks` | 保持不变 | 设置投射物的燃烧 Tick，最小值为 0。 |
| `damage-multiplier` | `1` | 乘算 `AbstractArrow` 的基础伤害。 |
| `critical` | 当前值 | 设置箭的暴击状态。 |
| `pierce-level` | 当前值 | 设置穿透等级，数值限制在 `0..127`。 |

### 示例

```yaml
type: modify_projectile
speed-multiplier: 1.25
spread-degrees: 3
gravity: false
damage-multiplier: 1.5
pierce-level: 2
```

### 行为与限制

* 未填写的选项不会改变对应属性；中性的速度和伤害倍率除外。
* `accuracy` 会朝发射者的视线方向校准，而不是直接瞄准 `TARGET`。

---

## `replace_projectile`

**用途：**移除当前投射物，并替换为 TNT 或其他投射物类型，同时保留核心运动状态和所有权。

**上下文：**从 `SKILL` 或已记录的投射物上下文中读取旧投射物，并将替换实体写入为新的技能实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `projectile` | 必填 | `TNT` 别名或可生成的 Bukkit 投射物类型。 |
| `fuse` | `40` | TNT 引信 Tick，最小值为 1。 |
| `fireball-yield` | `1` | 火球替换实体的爆炸威力。 |
| `fireball-incendiary` | `true` | 火球是否能够引燃方块。 |
| `potion-type` | 未设置 | 喷溅药水等投掷药水使用的基础药水类型。 |
| `potion-effects` | 未设置 | 自定义药水效果；`duration` 默认值为 100，`amplifier` 默认值为 0。 |

### 示例

```yaml
type: replace_projectile
projectile: SPLASH_POTION
potion-type: HARMING
potion-effects:
  slowness:
    duration: 80
    amplifier: 1
```

### 行为与限制

* 替换为其他投射物时，会复制原投射物的速度和重力状态。
* 已记录的生物来源会成为新投射物的发射者。
* 替换结果仍为投射物时会转移追踪数据；替换为 TNT 或非投射物实体时，投射物后续追踪结束。
* TNT 的速度会乘以 1.2。

---
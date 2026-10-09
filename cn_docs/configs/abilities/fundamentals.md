# ✨ 基础与视觉能力

本页面会将每一种已注册能力作为完整的参考条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `mark`
* `cancel_event`
* `remove`
* `place_block`
* `place_temp_block`
* `particle`
* `sound`
* `vanilla_animation`

---

## `mark`

**用途：**将选中的实体标记为属于当前能力链。

**上下文：**默认 `target` 为 `SKILL`；选中的实体必须存在。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: mark
target: SKILL
```

### 行为与限制

* 该标记仅供插件内部使用，不会添加可见的计分板标签。
* 通常用于必须保留能力上下文的投射物或辅助实体。

---

## `cancel_event`

**用途：**请求取消当前触发器事件。

**上下文：**不需要实体目标；触发器必须提供可取消的事件。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: cancel_event
```

### 行为与限制

* 取消信号会通过父级流程控制能力继续传播。
* 如果取消的是较晚触发的通知类事件，可能无法撤销服务器已经完成的操作。

---

## `remove`

**用途：**将选中的实体从世界中移除。

**上下文：**默认 `target` 为 `SKILL`；需要一个实体。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: remove
target: SKILL
```

### 行为与限制

* 此清理能力不应将玩家作为目标。
* 适用于投射物、临时辅助实体或被替换的技能实体。

---

## `place_block`

**用途：**放置配置的方块，并可选择执行子能力。

**上下文：**使用扩展位置选择器；存在可解析玩家时，会使用该玩家执行保护检查。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `block` | `STONE` | 由 `BlockPriceUtil` 解析的方块规格，包括受支持的自定义方块语法。 |
| `replace-existing` | `false` | 是否允许替换目标位置已有的非空气方块。 |
| `abilities` | 空 | 成功放置后执行的子能力，并将已放置方块作为上下文。 |
| `failure-abilities` | 空 | 位置解析、替换、保护检查或方块放置失败时执行的子能力。 |
| `else-abilities` | 空 | 未配置 `failure-abilities` 时使用的后备别名。 |

### 示例

```yaml
type: place_block
block: COBWEB
replace-existing: false
location:
  target: TARGET
abilities:
  sound:
    type: sound
    sound: BLOCK_WOOL_PLACE
failure-abilities:
  notice:
    type: send_message
    message: '<red>The block could not be placed.'
```

### 行为与限制

* 存在玩家上下文时会检查保护插件挂钩。
* 成功放置方块会增加已更改方块结果计数器。
* 此能力会永久放置方块；需要自动恢复时请使用 `place_temp_block`。

---

## `place_temp_block`

**用途：**放置一个临时方块，并在持续时间结束后恢复原方块。

**上下文：**使用解析后的扩展位置；目标位置当前必须是空气。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `block` | `COBWEB` | 要放置的 Bukkit 材质。 |
| `duration` | `40` | 存在时间，单位为 Tick，最小值限制为 1。 |

### 示例

```yaml
type: place_temp_block
block: PACKED_ICE
duration: 100
location:
  target: TARGET
```

### 行为与限制

* 材质无效、世界不存在或目标位置不是空气时，会跳过执行。
* 方块恢复由 `TempBlockManager` 管理。

---

## `particle`

**用途：**在解析出的位置生成 Bukkit 粒子。

**上下文：**默认目标由具体能力实现决定；粒子位置使用触发器位置或解析位置，以及通用偏移量。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `particle` | `FLAME` | Bukkit `Particle` 枚举名称。 |
| `count` | `1` | 粒子数量。 |
| `offset-x / offset-y / offset-z` | `0` | 粒子散布范围。 |
| `extra` | `0` | 粒子专用的速度或额外数值。 |
| `block` | `STONE` | 方块数据粒子使用的方块材质。 |
| `item` | `STONE` | `ITEM` 粒子使用的物品材质。 |
| `color` | 由粒子决定 | RGB 数值，例如 `255,0,0`。 |
| `size` | `1` | 尘埃大小，或其他粒子专用的大小或持续时间。 |
| `from / to` | 由粒子决定 | 过渡颜色、振动目标位置或其他结构化粒子数据。 |
| `value` | `1.0` | `DRAGON_BREATH` 或 `SCULK_CHARGE` 等粒子使用的浮点数据。 |
| `delay` | `20` | `SHRIEK` 使用的延迟数据。 |
| `duration` | `20` | `VIBRATION` 使用的移动持续时间。 |

### 示例

```yaml
type: particle
particle: DUST
count: 20
offset-x: 0.4
offset-y: 0.8
offset-z: 0.4
color: 255,80,20
size: 1.5
```

### 行为与限制

* 粒子名称无效或缺少兼容的必需数据时，能力不会执行任何操作。
* 在高频触发器中应使用较少的粒子数量。

---

## `sound`

**用途：**在解析出的位置播放 Bukkit 音效。

**上下文：**使用触发器位置或解析位置，以及通用偏移量。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `sound` | `ENTITY_PLAYER_LEVELUP` | Bukkit `Sound` 枚举名称。 |
| `volume` | `1` | 音量及可听范围。 |
| `pitch` | `1` | 音调。 |

### 示例

```yaml
type: sound
sound: ENTITY_WARDEN_SONIC_BOOM
volume: 2
pitch: 1
```

### 行为与限制

* 无效的音效名称会被忽略。
* 非常大的音量值会扩大音效的可听范围。

---

## `vanilla_animation`

**用途：**在选中的实体上播放 Bukkit `EntityEffect` 动画。

**上下文：**默认 `target` 为 `SOURCE`；需要与所选效果兼容的实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `animation` | `HURT` | Bukkit `EntityEffect` 枚举名称。 |
| `entity-effect` | `HURT` | 未配置 `animation` 时使用的别名。 |

### 示例

```yaml
type: vanilla_animation
target: SOURCE
animation: TOTEM_RESURRECT
```

### 行为与限制

* 无效的动画或效果名称会被忽略。
* 该动画仅在客户端可见，本身不会造成伤害或应用其他游戏机制。

---
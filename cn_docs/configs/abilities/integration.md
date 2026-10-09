# 🔌 集成与目标选择能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `nearby_entities`
* `summon`
* `mythic_skill`
* `send_message`
* `execute_command`
* `execute_action`
* `auto_fishing`
* `disable_enchantments`

---

## `nearby_entities`

**用途：**寻找附近实体，并将每个符合要求的实体设为新的 `TARGET` 后执行子能力。

**上下文：**默认基础 `target` 为 `SOURCE`；实体不可用时会回退至解析出的位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `radius` | `5` | 三个轴共同使用的默认半径。 |
| `radius-x / radius-y / radius-z` | `radius` | 各坐标轴分别使用的半径。 |
| `max-targets` | `0` | 最多接受的实体数量；零表示无限制。 |
| `include-source` | `false` | 是否允许原始来源实体。 |
| `exclude-target` | `false` | 是否从候选项中排除原始上下文目标。 |
| `include-items` | `false` | 是否允许掉落物实体。 |
| `include-non-living` | `false` | 是否允许其他非生物实体。 |
| `include-living` | `true` | 是否允许生物实体。 |
| `include-armor-stands` | `false` | 启用生物实体时是否允许盔甲架。 |
| `direction-source` | `SOURCE` | `max-angle` 所使用的朝向实体。 |
| `max-angle` | 未设置 | 锥形范围的最大半角（度）；未设置时不进行锥形筛选。 |
| `match-entity` | 匹配全部 | 用于筛选生物实体候选项的 Match Entity 规则。 |
| `abilities` | 必填 | 嵌套能力。 |
| `after-abilities` | 未设置 | 至少接受一个候选项后执行一次的能力，可使用 `{target_count}`。 |

### 示例

```yaml
type: nearby_entities
target: SOURCE
radius: 8
max-targets: 3
include-source: false
exclude-target: true
max-angle: 45
match-entity:
  entity-types:
    - PLAYER
abilities:
  damage:
    type: damage_entity
    amount: 4
after-abilities:
  feedback:
    type: send_message
    message: '<green>命中了 {target_count} 个附近目标。'
```

### 行为与限制

* 候选实体按照距离由近到远排序。
* 子能力上下文的位置会更新为每个选中的目标位置。
* `max-angle` 表示半角：填写 `45` 时会接受正前方 90 度的锥形范围。
* `after-abilities` 仅在至少接受一个候选项时执行，并保留原始目标上下文。

---

## `summon`

**用途：**生成一个 Bukkit 生物实体，并配置其基础属性。

**上下文：**生成位置使用该能力解析出的位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `entity` | 空 | 单个 Bukkit 实体类型。 |
| `entity-type` | 空 | `entity` 的别名。 |
| `entities` | 空 | 等概率实体列表；存在时优先使用。 |
| `max-health` | `-1` | 为正数时设置最大生命值。 |
| `health` | `-1` | 为正数时设置当前生命值。 |
| `attack-damage` | `-1` | 为非负数时设置攻击伤害基础值。 |
| `set-target` | 未设置 | 将指定上下文实体选择器解析出的实体设为生成怪物的目标。 |
| `set-none-drops` | `false` | 清除战利品表、禁用拾取，并将装备掉落概率设置为零。 |
| `creeper.explosion-radius` | `-1` | 苦力怕爆炸半径。 |
| `creeper.fuse-ticks` | `-1` | 苦力怕最大引信 Tick。 |
| `creeper.powered` | 未设置 | 苦力怕充能状态。 |

### 示例

```yaml
type: summon
entities:
  - ZOMBIE
  - SKELETON
set-target: TARGET
set-none-drops: true
max-health: 30
attack-damage: 6
```

### 行为与限制

* 选中的实体类型必须能够生成，并且生成结果必须是生物实体。
* 与 EnchantedMobs 不同，此实现不会分配 EnchantedMobs 能力 ID。

---

## `mythic_skill`

**用途：**施放一个或多个 MythicMobs 技能。

**上下文：**施法者为 `SOURCE`；默认 `target` 为 `TARGET`，并且目标必须存在。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `skill` | 空 | `skills` 为空时使用的单个技能。 |
| `skills` | 空 | 按顺序执行的技能名称列表。 |
| `power` | `1.0` | MythicMobs 技能威力倍率。 |

### 示例

```yaml
type: mythic_skill
target: TARGET
skills:
  - Fireball
  - GroundSlash
power: 1.5
```

### 行为与限制

* 需要 MythicMobs。
* 目标实体和解析出的位置都会传递给集成。

---

## `send_message`

**用途：**向 `context.player()` 或附近玩家发送一条或多条格式化消息。

**上下文：**目标玩家模式使用当前玩家上下文；附近模式会在 `SOURCE` 周围搜索玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `mode` | `target-player` | 可填写 `target-player` 或 `nearby`。 |
| `radius` | `16` | 附近玩家搜索半径。 |
| `message` | 空 | `messages` 为空时使用的单条消息。 |
| `messages` | 空 | 消息列表。 |
| `placeholders` | 空 | 自定义消息占位符映射；值会解析上下文变量，并在可行时作为数学表达式计算。 |

### 示例

```yaml
type: send_message
mode: target-player
messages:
  - '<green>Activated at level {level}'
  - '<gray>Target: {target}'
placeholders:
  scaled_amount: '{original_experience} * 0.5'
```

### 行为与限制

* 根据接收者解析消息时，支持 `{player}`、`{target}`、等级与上下文变量、语言解析，以及可用时的 PlaceholderAPI。
* `placeholders` 的键可在 `message` 或 `messages` 中以 `{键}` 使用。数值表达式会自动计算，并移除无意义的末尾零。
* 附近玩家离线或死亡时会被跳过。

---

## `execute_command`

**用途：**针对来源怪物当前锁定的玩家目标或附近玩家执行命令。

**上下文：**目标玩家模式会解析怪物 `SOURCE` 当前锁定的玩家目标；附近模式会在 `SOURCE` 周围搜索玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `mode` | `target-player` | 可填写 `target-player` 或 `nearby`。 |
| `radius` | `16` | 附近玩家搜索半径。 |
| `as-console` | `true` | 是否由控制台执行；设置为 false 时由每个接收玩家执行命令。 |
| `command` | 空 | `commands` 为空时使用的单条命令。 |
| `commands` | 空 | 命令列表。 |

### 示例

```yaml
type: execute_command
mode: target-player
as-console: true
commands:
  - 'effect give {player} minecraft:slowness 5 1'
```

### 行为与限制

* 命令开头的斜杠会被移除。
* 每个被选中且在线、存活的玩家都会执行一次命令，并支持玩家、目标和上下文占位符。

---

## `execute_action`

**用途：**为选中的玩家执行共享 ActionManager 动作格式。

**上下文：**默认 `target` 为 `SOURCE`；选中的实体必须是玩家，并且必须存在 `actions`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `SOURCE` | 玩家选择器。 |
| `actions` | 必填 | 嵌套的共享动作条目。 |

### 示例

```yaml
type: execute_action
target: SOURCE
actions:
  message:
    type: message
    message: '<green>Action executed.'
```

### 行为与限制

* 可用的动作类型和字段由共享动作管理器提供。
* 子动作上下文会使用选中的玩家。

---

## `auto_fishing`

**用途：**鱼上钩后自动收杆并重新抛出鱼竿。

**上下文：**需要 `PlayerFishEvent.State.BITE`，以及捕获到的主手或副手鱼竿槽位。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `retrieve-delay-ticks` | `5` | 收杆前的延迟，最小值为 1。 |
| `recast-delay-ticks` | `10` | 重新抛竿前的延迟，最小值为 1。 |
| `method` | `LEGACY` | `LEGACY` 使用 PacketEvents 交互；`NMS` 使用钓鱼 NMS 桥接。 |

### 示例

```yaml
type: auto_fishing
retrieve-delay-ticks: 5
recast-delay-ticks: 10
method: NMS
```

### 行为与限制

* 每次操作前都会重新检查玩家、鱼钩、手部和鱼竿是否仍然有效。
* 旧版模式需要 PacketEvents；NMS 模式需要钓鱼竿能力桥接。

---

## `disable_enchantments`

**用途：**阻止同一物品上匹配的附魔进入活跃状态。

**上下文：**这是仅用于激活阶段的能力，由活跃附魔管理器在普通触发器执行前检查。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `enchantments` | 空 | 要禁用的不区分大小写 Glob 模式字符串或列表。 |
| `exclude-enchantments` | 空 | 不受禁用影响的排除模式。 |

### 示例

```yaml
type: disable_enchantments
enchantments:
  - 'enchantmentreform:*_curse'
exclude-enchantments:
  - enchantmentreform:allowed_curse
```

### 行为与限制

* 模式会与逻辑文件名、键值和完整命名空间键进行匹配。
* `*` 匹配任意文本，`?` 匹配一个字符。
* 将此能力作为普通触发器能力执行不会产生效果；必须将其放在激活能力中。

---

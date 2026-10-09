# 🧱 方块与交互触发器

这些触发器通常会让所有实体选择器都指向玩家，并通过 `BLOCK`、`LOCATION` 或 `TRIGGER_ITEM` 提供真正有意义的目标。

下方每个触发器部分都会说明当前实现所创建的运行时上下文。

| 上下文 | 含义 |
| --- | --- |
| `PLAYER` | 正在检查其激活附魔的玩家。 |
| `SOURCE` | 对事件负责的行为主体或所有者。 |
| `SKILL` | 直接载体或中间实体，例如投射物、鱼钩、烟花或监守者。 |
| `TARGET` | 最终受到影响或被检查的实体。部分事件中可能不存在。 |
| `BLOCK` | 触发器提供的事件方块。 |
| `LOCATION` | 供位置相关条件和能力使用的主要事件位置。 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 选中当前激活附魔的物品和装备槽位。 |
| `EVENT` | 底层 Bukkit/Paper 事件。未复制到其他字段的触发器专用状态，仍可由事件感知型实现通过该事件访问。 |

当某个角色标记为**玩家**时，它会解析为与 `PLAYER` 相同的实体。缺失的角色会解析为 `null`；需要该角色的条件或能力会安全失败或被跳过。

## `on-block-break`

当玩家破坏方块时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 被破坏的方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手工具 / `HAND` |
| 额外上下文 | `original_experience`、可选的 `internal_block_break`；可修改的经验值结果 |

## `on-block-damage`

当玩家开始挖掘或破坏一个方块时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 正在受到破坏的方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 事件物品 / `HAND` |

## `on-block-break-progress-update`

当 Paper 报告由玩家产生的方块破坏进度更新时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手工具 / `HAND` |
| 额外上下文 | `block_break_progress` |

## `on-block-drop-item`

当被破坏方块生成物品实体时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 方块位置及其中心 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手工具 / `HAND` |
| 额外上下文 | `broken_block_state` 会保留方块破坏前的状态；掉落物实体仍保留在事件中 |

## `on-block-place`

当玩家放置方块时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 已放置的方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 用于放置的物品及事件使用的手 |
| 额外上下文 | 被替换的方块和放置详情仍保留在事件中 |

## `on-inside-block`

当 Paper 的实体处于方块内部事件中的实体是玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 碰撞箱包含或正在处理该玩家的方块，以及该方块的中心位置 |

## `on-interact`

在 `PlayerInteractEvent` 中执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` | 被点击的方块；与空气交互时不存在 |
| `LOCATION` | 存在被点击方块时，为该方块的中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 事件物品及事件使用的手 |
| 额外上下文 | 动作、点击面、交互结果及其他详情仍保留在事件中 |

## `on-consume`

当玩家食用或消耗一个物品时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 被消耗的物品及事件使用的手 |

## `on-name-entity`

在 Paper 的玩家为实体命名事件中执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | 玩家 |
| `TARGET` | 正在被命名的实体 |
| `LOCATION` | 目标实体所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 能够找到时，为主手或副手中的命名牌 |

## `on-vibration-receive`

当幽匿传感器即将接收到由玩家产生的振动（游戏事件）时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | 产生振动的玩家 |
| `BLOCK` / `LOCATION` | 幽匿传感器方块及其位置 |
| 额外上下文 | `GameEvent` 仍保留在 `BlockReceiveGameEvent` 上；取消事件可阻止传感器激活，从而阻止振动传播到幽匿尖啸器 |

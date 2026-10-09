# 🕹️ 移动与输入触发器

这些触发器会提供玩家的移动或输入状态。高频执行的能力应配合严格条件，并尽量使用开销较小的能力。

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

## `on-move`

仅当 `PlayerMoveEvent.hasChangedBlock()` 为 `true` 时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 正在移动的玩家 |
| `FROM` / `TO` | 移动前的位置和目标位置 |
| `LOCATION` | 目标位置 |
| `BLOCK` | 目标位置处的方块 |

## `on-input`

当 Paper 报告玩家输入状态发生更新时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | 前进、后退、左移、右移、跳跃、疾跑和潜行输入状态仍保留在 `PlayerInputEvent` 中 |

## `on-jump`

在 Paper 的玩家跳跃事件中执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 正在跳跃的玩家 |
| `FROM` / `TO` | 起跳位置和目标位置 |
| `LOCATION` | 目标位置 |
| `BLOCK` | 目标位置处的方块 |

## `on-toggle-flight`

当玩家切换飞行状态时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | 新的飞行状态仍保留在 `PlayerToggleFlightEvent` 中；对于可取消事件，取消状态会应用到事件 |

## `on-toggle-sneak`

当玩家切换潜行状态时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | 新的潜行状态仍保留在 `PlayerToggleSneakEvent` 中 |
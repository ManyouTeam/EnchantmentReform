# 🎒 物品、附魔与装备触发器

这些触发器主要处理物品或装备状态变化。在假设存在实体目标前，应先检查 `TRIGGER_ITEM` 和 `TRIGGER_SLOT`。

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

## `on-item-damage`

当玩家的某个物品受到耐久度损耗时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` | 受到耐久度损耗的物品 |
| `TRIGGER_SLOT` | 找到该物品的槽位 |
| 额外上下文 | `original_item_damage`；可修改的物品耐久损耗结果 |

## `on-item-group-cooldown`

在 Paper 的物品冷却组事件中执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `LOCATION` | 玩家位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 当事件为 `PlayerItemCooldownEvent` 时，为主手或副手中与冷却物品材质匹配的物品；其他情况下可能不存在 |
| 额外上下文 | 冷却组和持续时间仍保留在 Paper 事件中 |

## `on-item-held`

当玩家选中的快捷栏槽位发生变化时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 新快捷栏槽位中的物品 / `HAND` |
| 额外上下文 | 原槽位索引和新槽位索引仍保留在事件中 |

## `on-swap-hand`

当主手和副手物品互换时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 事件中的主手物品 / `HAND` |
| 额外上下文 | 交换后两只手中的物品均保留在事件中 |

## `on-enchant-item`

当玩家通过 `EnchantItemEvent` 对物品进行附魔时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 进行附魔的玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 正在附魔的物品 / `HAND` |
| 额外上下文 | `original_experience` 为附魔选项显示的等级门槛；`enchantment_level_cost` 为实际扣除的 1/2/3 级；`original_lapis` 为所选档位的青金石花费；可修改的经验结果；附魔台提供的附魔仍保留在事件中 |

## `on-elytra-boost`

当玩家使用烟花火箭加速鞘翅飞行时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | 玩家 |
| `SKILL` | 烟花实体 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 烟花物品和事件使用的手 |

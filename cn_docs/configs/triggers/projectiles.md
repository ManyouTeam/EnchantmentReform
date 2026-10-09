# 🏹 投射物与远程触发器

这些触发器会将发射者（`SOURCE`）与投射物或其他直接载体（`SKILL`）区分开。

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

## `on-shoot`

当 `EntityShootBowEvent` 的发射者是玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` | 发射者 |
| `SKILL` / `TARGET` | 生成的投射物实体 |
| `LOCATION` | 投射物所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 弓或弩，以及事件使用的手 |
| 额外上下文 | `bow_force`、`bow`、`consumable`、`hand`、`projectile` |
| 区别 | 不会启动 EnchantmentReform 的投射物后续追踪 |

## `on-shoot-bow`

初始上下文与 `on-shoot` 相同；能力执行完成后，如果最终的 `SKILL` 实体是投射物，则开始追踪该投射物。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` | 发射者 |
| `SKILL` / `TARGET` | 原始投射物，或能力返回的替代实体 |
| `LOCATION` | 投射物所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 弓或弩，以及事件使用的手 |
| 额外上下文 | `bow_force`、`bow`、`consumable`、`hand`、`projectile` |
| 区别 | 启用 `on-projectile-tick` 和带追踪数据的 `on-projectile-hit` 后续处理 |

## `on-load-crossbow`

当玩家为弩装填弹药时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 弩以及事件使用的手 |
| 额外行为 | 在支持的情况下，取消能力可以取消或阻止底层 Paper 事件 |

## `on-projectile-launch`

当投射物被发射，并且其发射者是玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` | 发射者 |
| `SKILL` / `TARGET` | 发射出的投射物 |
| `LOCATION` | 投射物所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 当前主手物品 / `HAND` |
| 额外上下文 | `projectile` |
| 说明 | 此通用发射触发器本身不会创建投射物后续追踪数据 |

## `on-projectile-tick`

对于由 `on-shoot-bow` 追踪的投射物，每 Tick 执行一次。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` | 发射投射物或捕获该能力的玩家 |
| `SOURCE` | 当前的发射者实体 |
| `SKILL` / `TARGET` | 被追踪的投射物 |
| `LOCATION` / `BLOCK` | 投射物所在位置，以及该位置的方块 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 发射时捕获的物品和槽位 |
| `TICK` | 当前运行时 Tick |
| 额外上下文 | `projectile`；支持 `interval` |

## `on-projectile-hit`

当投射物命中后，且能够解析到追踪数据时执行；对于由玩家直接发射且可直接解析的投射物，也会执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` | 追踪时捕获的所有者，或直接发射投射物的玩家 |
| `SOURCE` | 解析出的发射者 |
| `SKILL` | 投射物 |
| `TARGET` | 被命中的实体；命中方块时不存在 |
| `BLOCK` | 被命中的方块；命中实体时不存在 |
| `LOCATION` | 被命中方块的中心、被命中实体的位置，或投射物当前位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 存在追踪数据时，为发射时捕获的物品和槽位 |
| 额外上下文 | `projectile`；处理此事件后会结束追踪 |

## `on-riptide`

当玩家使用激流附魔时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 使用激流的物品 / `HAND` |
| 额外上下文 | 仍可访问 `PlayerRiptideEvent` |

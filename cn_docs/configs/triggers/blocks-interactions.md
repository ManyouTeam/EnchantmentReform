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

支持的 `count`：`events`。

当玩家破坏方块时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 被破坏的方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手工具 / `HAND` |
| 额外上下文 | `original_experience`、可选的 `internal_block_break`；可修改的经验值结果 |

## `on-block-damage`

支持的 `count`：`events`。

当玩家开始挖掘或破坏一个方块时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 正在受到破坏的方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 事件物品 / `HAND` |

## `on-block-break-progress-update`

支持的 `count`：`events`。

当 Paper 报告由玩家产生的方块破坏进度更新时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手工具 / `HAND` |
| 额外上下文 | `block_break_progress` |

## `on-block-drop-item`

支持的 `count`：`events`、`items`。

当被破坏方块生成物品实体时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 方块位置及其中心 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手工具 / `HAND` |
| 额外上下文 | `broken_block_state` 会保留方块破坏前的状态；掉落物实体仍保留在事件中 |

## `on-block-place`

支持的 `count`：`events`、`blocks`。

当玩家放置方块时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 已放置的方块及其中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 用于放置的物品及事件使用的手 |
| 额外上下文 | 被替换的方块和放置详情仍保留在事件中 |

## `on-inside-block`

支持的 `count`：`events`。

当 Paper 的实体处于方块内部事件中的实体是玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` / `LOCATION` | 碰撞箱包含或正在处理该玩家的方块，以及该方块的中心位置 |

## `on-interact`

支持的 `count`：`events`。

在 `PlayerInteractEvent` 中执行。

支持左键和右键空气交互。右键空气事件即使因没有方块目标而被 Bukkit 默认标记为取消，仍会触发；若事件明确禁止使用手持物品（`useItemInHand: DENY`），则不会触发右键空气交互。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` | 被点击的方块；与空气交互时不存在 |
| `LOCATION` | 存在被点击方块时，为该方块的中心位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 事件物品及事件使用的手 |
| 额外上下文 | 动作、点击面、交互结果及其他详情仍保留在事件中 |

## `on-consume`

支持的 `count`：`events`。

当玩家食用或消耗一个物品时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 被消耗的物品及事件使用的手 |

## `on-name-entity`

支持的 `count`：`events`。

在 Paper 的玩家为实体命名事件中执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | 玩家 |
| `TARGET` | 正在被命名的实体 |
| `LOCATION` | 目标实体所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 能够找到时，为主手或副手中的命名牌 |

## `on-vibration-receive`

支持的 `count`：`events`。

当幽匿传感器即将接收到由玩家产生的振动（游戏事件）时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | 产生振动的玩家 |
| `BLOCK` / `LOCATION` | 幽匿传感器方块及其位置 |
| 额外上下文 | `GameEvent` 仍保留在 `BlockReceiveGameEvent` 上；取消事件可阻止传感器激活，从而阻止振动传播到幽匿尖啸器 |

## `on-brew`

支持 `count: events` 和 `count: items`。在酿造完成时触发，使用酿造后的药水结果；未改变的瓶子不计入结果。`events` 每批计 1，`items` 按符合条件的药水数量计数。使用 `count: items` 和 `COUNTED_ITEM` 物品匹配，可筛选指定药水；`TRIGGER_ITEM` 为第一份已改变的产物，槽位留空。

## `on-cook`

支持 `count: events` 和 `count: items`。在熔炉烧制完成时触发，包含普通熔炉、烟熏炉和高炉，使用烧制后的产物。`events` 每次完成计 1，`items` 按符合条件的产物数量计数。可用 `COUNTED_ITEM` 物品匹配限制为 `COOKED_CHICKEN` 等熟食；不配置筛选时也会统计其他烧制产物。`TRIGGER_ITEM` 为烧制产物，槽位留空。取出已有成品不会再次触发；营火不包含在此事件中。

### 酿造和烹饪的玩家归属

这两个完成事件本身没有玩家。插件会在点击、Shift 点击或拖拽后检查原料槽，实际放入原料的玩家成为设备当前操作者。仅打开界面、放燃料和取出成品不会接管归属。酿造原料槽为瓶子及材料槽；熔炉为输入槽。

玩家 UUID 保存在设备 PDC 中，跨重启保留；不保留长期玩家对象缓存。原料槽全部清空时清除记录，漏斗等库存自动输入也会清除记录。未记录操作者、操作者离线或事件被取消时不计数。多人合用设备时，完成事件归属最后实际放入原料的玩家，而非逐份追踪物品所有权。`PLAYER` / `SOURCE` 为操作者，`BLOCK` / `LOCATION` 为设备，支持 Paper 和 Spigot。

## `on-craft-item`

支持 `count`: `events`, `items`. 合成物品并成功取出成品时触发。`events` 每次合成点击计 1，`items` 按产物数量计数，支持 Shift 批量合成并考虑背包容量；创造模式克隆不计入。

## `on-fertilize-block`

支持 `count`: `events`, `blocks`. 玩家用骨粉成功施肥且产生方块变化时触发。`events` 每次使用计 1；`blocks` 按实际变化方块计数。`BLOCK` 为施肥目标，触发物品和手来自原始交互。发射器等无玩家事件不计数。

## `on-forge-item`

支持 `count`: `events`, `items`. 在锻造台成功取出成品时触发，包括装备升级和纹饰。仅查看预览不会触发。

## `on-grindstone-item`

支持 `count`: `events`, `items`. 在砂轮成功取出处理后的成品时触发，包括修补和祛除附魔。仅放入或查看物品不触发。

## `on-milk-mob`

支持 `count`: `events`, `items`. 成功获得奶桶时触发，并关联本次交互的牛、蘑菇牛或山羊为 `TARGET`。空桶点击幼年动物、失败或取消的操作不计数。

## `on-honey-extract`

支持 `count`: `events`, `items`. 使用玻璃瓶从满蜜蜂巢或蜂箱采集蜂蜜时触发，在下一 tick 确认蜜量下降和玻璃瓶使用结果。触发物品为玻璃瓶，产物为蜂蜜瓶。

## `on-honey-comb-extract`

支持 `count`: `events`, `items`. 使用剪刀从蜂巢或蜂箱剪取蜜脾时触发，产物取自 Paper 剪切方块事件的掉落物。仅支持具备该事件的 Paper。

## `on-anvil-repair`

支持 `count`: `events`, `items`. 在铁砧成功取出耐久损伤减少的物品时触发。仅改名不会触发此项。

## `on-anvil-enchant`

支持 `count`: `events`, `items`. 在铁砧成功取出新增附魔或附魔等级提高的物品时触发，包括附魔书合并；保留原有且不提升的附魔不计入。

## `on-anvil-rename`

支持 `count`: `events`, `items`. 在铁砧成功取出自定义名称发生变化的物品时触发，包含添加、修改和清除名称；仅修补或附魔不会误计改名。

物品数量模式使用 `COUNTED_ITEM` 筛选产物；锻造、砂轮及铁砧的触发物品为成品。一次铁砧操作可分别触发修补、附魔和改名，取决于实际发生的变化。库存成品事件会在下一 tick 确认输入物品被消耗，取消或未取出成品不会计数。蜂蜜触发发生在操作完成后，不能用来取消原始采集。除蜜脾触发器外，以上均支持 Paper 和 Spigot。

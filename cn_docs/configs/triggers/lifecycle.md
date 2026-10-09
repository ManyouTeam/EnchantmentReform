# ⏱️ Tick 与生命周期触发器

这些触发器会周期性执行，或在玩家的激活附魔集合发生变化时执行。

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

## `on-tick`

对每个拥有激活附魔的在线玩家周期性执行。可在触发器部分配置执行间隔：

```yaml
powers:
  on-tick:
    interval: 20
    abilities: {}
```

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` | 玩家当前位置的方块 |
| `TICK` | 当前运行时 Tick |
| 额外行为 | `interval` 的最小值会被限制为 1 Tick |

## `on-target-tick`

当玩家视线通过 32 格实体射线追踪命中某个实体时，周期性执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | 玩家 |
| `TARGET` | 当前被射线追踪命中的实体 |
| `LOCATION` / `BLOCK` | 目标位置以及该位置的方块 |
| `TICK` | 当前运行时 Tick |
| 额外行为 | 未找到实体时不会执行；支持 `interval` |

## `on-spawn`

玩家加入服务器后的 1 Tick 执行一次，玩家重生后的 1 Tick 也会执行一次。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `LOCATION` | 重生路径中为重生位置；加入服务器时为玩家正常位置 |
| `BLOCK` | 解析后位置处的方块 |
| 额外行为 | 这是玩家生命周期触发器，并不是通用实体生成触发器 |

## `on-respawn`

在 `PlayerRespawnEvent` 后的 1 Tick 执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 正在重生的玩家 |
| `LOCATION` | 重生位置 |
| `BLOCK` | 重生位置处的方块 |

## `on-activate`

当某个附魔在符合要求的装备槽位中变为激活状态时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` | 玩家位置处的方块 |
| `TRIGGER_ITEM` | 包含刚刚激活附魔的物品 |
| `TRIGGER_SLOT` | 该附魔变为激活状态的槽位 |
| 额外行为 | 仅针对正在发生状态变化的那个附魔执行 |

## `on-deactivate`

当某个附魔不再处于激活状态时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| `BLOCK` | 玩家位置处的方块 |
| `TRIGGER_ITEM` | 之前包含该附魔的物品 |
| `TRIGGER_SLOT` | 该附魔变为非激活状态的槽位 |
| 额外行为 | 仅针对正在发生状态变化的那个附魔执行 |
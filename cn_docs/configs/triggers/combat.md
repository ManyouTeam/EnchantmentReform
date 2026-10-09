# ⚔️ 战斗、伤害与死亡触发器

这些触发器适用于造成伤害、受到伤害、恢复生命、死亡、盾牌以及 Paper 提供的战斗动作。

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

## `on-attack`

当附魔玩家需要为某次实体伤害负责时执行，包括可追溯到该玩家的投射物或三叉戟伤害。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` | 发起攻击的玩家 |
| `SKILL` | 直接伤害来源：玩家、投射物或三叉戟 |
| `TARGET` | 受到伤害的实体 |
| `LOCATION` | 目标所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手武器；对于已追踪投射物，则为恢复出的发射武器和槽位 |
| 额外上下文 | `original_damage`、`damage_cause`；可修改的 `damage` 结果 |

## `on-melee-attack`

仅当直接伤害来源就是玩家本人时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | 发起攻击的玩家 |
| `TARGET` | 受到伤害的实体 |
| `LOCATION` | 目标所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 主手物品 / `HAND` |
| 额外上下文 | `original_damage`、`damage_cause`；可修改的 `damage` 结果 |

## `on-damage`

当附魔玩家受到任意伤害时执行，包括环境伤害。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 受到伤害的玩家 |
| `SOURCE` | 解析出的攻击者或所有者；环境伤害时不存在 |
| `SKILL` | 直接伤害来源；不存在直接伤害来源时为玩家本人 |
| `LOCATION` | 受伤玩家所在位置 |
| 额外上下文 | `original_damage`、`damage_cause`、`damage_by_entity`、`damage_by_block`；可修改的 `damage` 结果 |

## `on-damage-by-entity`

仅当附魔玩家受到实体造成的伤害时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 受到伤害的玩家 |
| `SOURCE` | 解析出的攻击者或投射物所有者 |
| `SKILL` | 直接伤害来源 |
| `LOCATION` | 受伤玩家所在位置 |
| 额外上下文 | `original_damage`、`damage_cause`、`damage_by_entity=true`；可修改的 `damage` 结果 |

## `on-kill`

当某个玩家被记录为实体击杀者时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | 击杀者 |
| `TARGET` | 被击杀的实体 |
| `LOCATION` | 被击杀实体所在位置 |
| 额外上下文 | `original_experience`；可修改的掉落经验值 |

## `on-death`

当附魔玩家死亡时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 死亡的玩家 |
| `SOURCE` / `SKILL` | 击杀者；不存在玩家击杀者时为空 |
| `LOCATION` | 死亡玩家所在位置 |
| 额外上下文 | `original_experience`、死亡掉落物；支持清空掉落物和 Paper 的复活生命值行为 |

## `on-regain`

当附魔玩家恢复生命值时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 恢复生命值的玩家 |
| 额外上下文 | `original_amount`；可修改的生命恢复量；恢复原因仍保留在事件中 |

## `on-combust`

当附魔玩家被点燃时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 正在燃烧的玩家 |
| 额外上下文 | `original_duration`；可修改的燃烧持续时间；事件子类型用于标识点燃来源 |

## `on-knockback`

当 Paper 的实体击退事件中，被击退实体是附魔玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 被击退的玩家 |
| `SOURCE` / `SKILL` | 攻击击退时为推动实体；其他情况为玩家本人 |
| 额外上下文 | 击退原因和向量仍可通过 Paper 事件访问 |

## `on-attempt-smash-attack`

当玩家尝试执行 Paper 的重击攻击时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` | 发起攻击的玩家 |
| `TARGET` | 预定的重击目标 |
| `LOCATION` | 目标所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 事件武器 / `HAND` |
| 额外行为 | 取消该能力会阻止此次重击尝试 |

## `on-lunge`

当 Paper 的实体突进事件中的实体是玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 正在突进的玩家 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 当前正在使用的物品及其手，或当前选中手中的物品 |
| 额外上下文 | 突进专用数值仍保留在 Paper 事件中 |

## `on-shield-block`

当受到的实体伤害被作为盾牌格挡处理时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 正在格挡的玩家 |
| `SOURCE` | 解析出的攻击者或所有者 |
| `SKILL` | 直接伤害来源 |
| `LOCATION` | 格挡玩家所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 正在使用的盾牌及其手 |
| 额外上下文 | `original_damage` 为被格挡的伤害量；`damage_cause`；可修改的 `damage` 结果 |

## `on-shield-disable`

当 Paper 报告玩家的盾牌被禁用时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 盾牌被禁用的玩家 |
| `SOURCE` / `SKILL` | 导致盾牌被禁用的伤害来源 |
| `LOCATION` | 玩家所在位置 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 正在使用的盾牌及其手 |

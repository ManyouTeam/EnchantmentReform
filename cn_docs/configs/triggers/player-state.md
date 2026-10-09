# ❤️ 效果、空气、食物与经验触发器

这些触发器用于监听或修改玩家状态。大多数触发器使用默认的玩家选择器，并通过触发器结果数据提供可修改的数值。

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

触发器页面中记录的数字型额外上下文名称，在当前触发器提供对应数据时，也可以作为表达式占位符使用。例如，`original_air` 对应 `{original_air}`。完整的内置列表和解析规则请参阅[数学计算格式](../../shared-formats/math-calculate-format.md#numeric-trigger-context-placeholders)。

## `on-effect-tick`

当 Paper 的瞬时或周期性效果 Tick 事件作用于玩家时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 受到效果影响的玩家 |
| 额外上下文 | 效果类型和效果实例仍保留在 `EntityEffectTickEvent` 中 |

## `on-potion-effect`

当玩家身上的药水效果被添加、改变或移除时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `TARGET` | 受到效果影响的玩家 |
| `SOURCE` / `SKILL` | 效果来源；不存在来源时为玩家自身 |
| 额外上下文 | `original_duration` 为新效果的持续 Tick 数；旧效果、新效果、操作和原因仍保留在 `EntityPotionEffectEvent` 中 |

## `on-exhaustion`

当 Paper 报告玩家产生消耗度时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | 消耗度数值和原因仍保留在 `EntityExhaustionEvent` 中 |

## `on-food-level-change`

当玩家的饥饿值发生变化时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | `original_food_level`；可修改的饥饿值结果 |

`{original_food_level}` 可用于支持上下文的字符串和数字表达式。

## `on-air-change`

当玩家的剩余空气值发生变化时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | `original_air`、`previous_air`；可修改的空气值结果 |

`{original_air}` 是事件最初请求的新空气值；`{previous_air}` 是事件发生前玩家已有的空气值。两者都可用于支持上下文的字符串和数字表达式。

## `on-exp-change`

当玩家的经验数量发生变化时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `SKILL` / `TARGET` | 玩家 |
| 额外上下文 | `original_experience`；可修改的经验值结果 |

`{original_experience}` 会解析为能力修改器应用前的经验数量。

## `on-pickup-experience`

当玩家拾取 Paper 经验球时执行。

| 上下文 | 值 |
| --- | --- |
| `PLAYER` / `SOURCE` / `TARGET` | 玩家 |
| `SKILL` | 经验球 |
| `LOCATION` | 经验球所在位置 |
| 额外上下文 | `original_experience` 为经验球中储存的经验值 |

可以使用 [`state`](../abilities/orchestration.md#state) 能力累计多次拾取的经验球数值。每达到一个完整阈值组，就可以修改一个嵌套能力的数字结果；不足一个完整组的剩余数值则会保存在状态池中，供之后拾取经验球时继续累计：

```yaml
abilities:
  accumulate-experience:
    type: state
    key: experience-lifeblood
    operation: ADD
    amount: '{original_experience}'
    trigger-at: 3
    consume-trigger-value: true
    trigger-abilities:
      restore-health:
        type: set_health
        target: PLAYER
        amount: 'min({health} + {state_trigger_count}, {max-health} * 0.5)'

  consume-experience-orb:
    type: remove
    target: SKILL

  prevent-normal-experience:
    type: cancel_event
```

假设状态池中已经储存了 `1`，随后拾取一个价值为 `7` 的经验球，总数便为 `8`：其中两个完整的阈值组会恢复 2 点生命值，剩余的 `2` 点经验则继续保存在状态池中。经验球会被移除，原始拾取事件也会被取消，防止同一份经验再次按照原版机制发放。

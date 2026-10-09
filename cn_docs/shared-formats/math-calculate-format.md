# ➗ 数学计算格式

当 `config.yml -> math.enabled` 设置为 true 时，受支持的数值字段可以使用表达式代替固定数字。

```yaml
amount: '2 + level * 0.5'
random: '0.1 + level * 0.02'
```

## 可用数值

根据接收表达式的字段不同，可以使用以下数值：

* `level` / `{level}`；
* 根配置中的 `variables` 数值；
* `{distance}` 等运行时上下文；
* 来源或目标的生命值、最大生命值、生命值百分比、水平速度和坠落距离；
* 下方列出的数值触发器上下文占位符；
* `{original}`、`{now}`、`{max}`、`{air}` 或 `{max-air}` 等字段专用数值；
* 存在玩家上下文时，PlaceholderAPI 返回的数值结果。
* EnchantmentReform 提供的**数学计算格式**与 UltimateShop 中的格式几乎相同，因此本 Wiki 不再重复介绍所有基础语法。详细说明请参阅 UltimateShop Wiki，点击[此处](https://ultimateshop.superiormc.cn/format)查看。

## 数值触发器上下文占位符

每个数值类型的 `BuiltinContextKeys` 条目都会自动使用花括号中的键名公开为占位符。只有当前触发器或父能力提供了对应上下文值时，占位符才会被替换；不可用的数值会保持未解析状态，而不会自动变为 `0`。

| 占位符 | 数值类型 | 上下文数值 |
| --- | --- | --- |
| `{original_damage}` | `Double` | 兼容伤害触发器捕获的原始伤害。 |
| `{original_amount}` | `Double` | 触发器提供的原始通用可修改数量。 |
| `{original_duration}` | `Float` | 支持持续时间的触发器提供的原始持续时间。 |
| `{original_yield}` | `Float` | 触发器提供的原始爆炸威力或掉落率。 |
| `{original_radius}` | `Float` | 触发器提供的原始半径。 |
| `{block_break_progress}` | `Float` | 当前方块破坏进度。 |
| `{original_air}` | `Integer` | 原始剩余空气值。 |
| `{previous_air}` | `Integer` | 空气变化事件发生前玩家已有的剩余空气值。 |
| `{original_food_level}` | `Integer` | 原始饥饿值。 |
| `{original_experience}` | `Integer` | 原始经验数量，或拾取的经验球中保存的经验数量。 |
| `{enchantment_level_cost}` | `Integer` | 附魔台实际扣除的经验等级数，即所选档位的 1、2 或 3。 |
| `{original_lapis}` | `Integer` | 附魔台所选档位消耗的青金石数量。 |
| `{original_item_damage}` | `Integer` | 原始物品耐久损耗。 |
| `{state_previous}` | `Double` | 当前 `state` 能力执行操作前的状态值。可供其触发的子能力使用。 |
| `{state_current}` | `Double` | 分组消耗阈值前的更新后状态值。可供其触发的子能力使用。 |
| `{state_trigger_count}` | `Long` | 当前 `state` 能力计算出的完整阈值分组数量。可供其触发的子能力使用。 |
| `{state_remainder}` | `Double` | 消耗完整阈值分组后剩余的状态值。可供其触发的子能力使用。 |
| `{bow_force}` | `Float` | 兼容投射物触发器提供的弓蓄力强度。 |
| `{target_count}` | `Integer` | 兼容目标选择上下文提供的目标数量。 |

这些占位符同样可以用于带上下文的字符串字段，以及通过 `getDouble(...)` 或 `getInt(...)` 解析的数值字段。

```yaml
amount: 'min({source_health} + {original_experience} / 3, {source_max_health} * 0.5)'
value: '{original_damage} * 0.25'
```

状态结果占位符只会添加到为 `state.trigger-abilities` 创建的子上下文中。可以使用完整阈值分组的数量缩放一次嵌套执行的结果：

```yaml
type: state
key: stored-experience
operation: ADD
amount: '{original_experience}'
trigger-at: 3
consume-trigger-value: true
trigger-abilities:
  heal:
    type: set_health
    target: PLAYER
    amount: 'min({health} + {state_trigger_count}, {max-health} * 0.5)'
```

根能力变量会优先解析。因此，变量可以安全引用数值触发器上下文：

```yaml
variables:
  converted-damage: '{original_damage} * 0.25'

powers:
  on-attack:
    abilities:
      example:
        type: set_health
        amount: '{source_health} + {converted-damage}'
```

通过内置键工厂添加新的数值 `ContextKey` 时，会自动添加使用相同键名的占位符，不需要另外注册解析器。

## 随机范围

```yaml
amount: 2~5
```

使用该字段所采用的解析器，在两个端点之间选择一个数值。

## 等级选择器

```yaml
amount:
  '==1': 2
  '>=2;;<=4': '2 + level'
  '>=5': 10
```

条目会从上到下进行检查，并使用第一个匹配的选择器。YAML 中的选择器键必须使用引号包裹。

## 变量

```yaml
variables:
  bonus: '1 + level * 0.25'
  label:
    '==1': I
    '==2': II
    '>=3': III+
```

数值使用方会计算数值变量的结果。文本使用方只会替换字符串值，不会将其作为公式计算。

## 函数

`math.enable-function` 用于启用 EnchantmentReform 注册的函数，例如当前实现支持的常见聚合函数和辅助函数。只有在排查兼容性问题，或确定不需要函数时，才建议将其禁用。

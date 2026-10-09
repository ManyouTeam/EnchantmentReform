# 💥 能力配置

一个附魔的能力配置用于定义：

* 是否启用运行时能力；
* 各触发器共用的激活限制；
* 能力在什么情况下触发；
* 每个触发器需要执行哪些条件、修改器和能力。

与 EnchantedMobs 不同，EnchantmentReform 不会将每个能力单独保存为一个文件。能力配置就是附魔文件中的 `powers` 部分，附魔文件位于：

```
plugins/EnchantmentReform/enchantments/
```

附魔文件的其他部分负责定义支持物品、等级、稀有度、有效槽位等注册表信息。本页面只说明运行时使用的 `variables` 和 `powers` 部分。

## 快速开始

```yaml
powers:
  on-melee-attack:
    abilities:
      ignite-target:
        type: fire
        target: TARGET
        fire-ticks: 100
```

当附魔物品处于激活状态，且物品所有者直接攻击某个实体时，目标会燃烧 100 Tick。

## 能力结构

```yaml
# 可复用的值应声明在附魔根节点。
variables:
  damage-multiplier: '1 + {level} * 0.1'

powers:
  # 启用或禁用此附魔的全部运行时行为。
  enabled: true

  # 每个触发器执行前都会检查的共用限制。
  limit:
    random: 1
    cooldown: 0
    times: 0

  # 触发器部分。
  on-attack:
    conditions: {}
    modifiers: {}
    abilities: {}
```

| 部分 | 用途 |
| --- | --- |
| `variables` | 定义附魔可重复使用、按等级变化或经过计算的值。此部分位于 `powers` 外部。 |
| `powers.enabled` | 启用或禁用附魔的运行时能力，但不会修改其注册表条目。 |
| `powers.limit` | 控制整个能力共用的触发概率、冷却和使用次数。 |
| `powers.on-...` | 定义一个触发器对应的条件、修改器和能力。 |
| `powers.activation-abilities` | 定义判断附魔是否激活时执行的特殊能力。 |

`powers` 下的键通常是内置触发器，例如 `on-attack`、`on-damage`、`on-block-break` 或 `on-fish`。例外是上方列出的通用选项，以及特殊的 `activation-abilities` 部分。

## 执行流程

当匹配的事件发生时，EnchantmentReform 会按照以下顺序处理一个激活附魔：

```
查找激活附魔和触发器
  → 检查触发器专属条件，例如手部或 Tick 间隔
  → powers.limit
  → 触发器条件
  → 触发器修改器
  → 触发器能力
  → 最终事件结果
```

* 同一级别中的所有条件都必须通过。
* 修改器按照 YAML 中的顺序执行，并更新触发器支持修改的事件结果数据。
* 能力会在修改器之后按照 YAML 中的顺序执行。
* 如果某个触发器或子项所需的上下文不可用，该项会被跳过。
* 对于已追踪的弹射物后续触发，插件可能会复用发射时捕获的能力状态，而不是重新判定能力整体限制。

{% hint style="warning" %}
`powers.limit` 会在触发器条件之前检查。因此，即使后续条件导致该触发器被跳过，已配置的冷却或使用次数仍可能被消耗。
{% endhint %}

有关各触发器提供的上下文，以及 `SOURCE`、`SKILL` 和 `TARGET` 的含义，请参阅[能力触发器](../triggers.md)。

## 变量和动态值

变量可以避免在同一个附魔中重复编写相同公式：

```yaml
variables:
  damage-multiplier:
    '==1': 1.1
    '==2': 1.2
    '>=3': 1.35

powers:
  on-attack:
    modifiers:
      scale-damage:
        type: damage
        operation: MULTIPLY
        value: '{damage-multiplier}'
```

根据字段的实现，数值可能支持：

* `{level}` 或 `level`；
* `{damage-multiplier}` 等附魔变量；
* `4 + {level} * 0.5` 等公式；
* `1~3` 或 `0.5~1.5` 等范围；
* 等级选择器映射；
* `{source_health}`、`{target_health_percent}` 和 `{distance}` 等运行时占位符；
* 在存在玩家上下文时使用 PlaceholderAPI。

```yaml
value:
  '==1': 2
  '>=2;;<5': '{level} * 0.5'
  '>=5': 4
```

并非每个触发器或能力都能提供所有运行时占位符。有关表达式语法，请参阅[数学计算格式](../../shared-formats/math-calculate-format.md)。

## 能力整体限制

```yaml
powers:
  limit:
    random: 0.5
    cooldown: 8
    times: 3

  on-attack:
    abilities:
      effect:
        type: particle
        target: TARGET
        particle: CRIT
```

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `random` | `1` | 激活概率，`1` 表示 100%。 |
| `cooldown` | `0` | 整个能力共用的冷却时间，单位为秒。 |
| `times` | `0` | 此能力最多成功通过顶层限制检查的次数，`0` 表示无限。 |

这些选项必须写在 `powers.limit` 下，而不是某个单独的 `on-...` 触发器部分中。

支持数值表达式的字段可以使用公式、变量、范围或等级选择器映射。

## 触发器部分

所有普通触发器都使用相同的基本结构：

```yaml
powers:
  on-damage:
    conditions:
      fire-damage:
        type: damage_cause
        values:
          - FIRE
          - FIRE_TICK

    modifiers:
      reduce-fire-damage:
        type: damage
        operation: MULTIPLY
        value: 0.5

    abilities:
      smoke:
        type: particle
        target: TARGET
        particle: SMOKE
        count: 10
```

`fire-damage`、`reduce-fire-damage` 和 `smoke` 等名称都是用户自定义的条目 ID，只需在各自所在部分中保持唯一。`type` 的值用于选择已注册的条件、修改器或能力实现。

### `conditions`

条件用于决定触发器是否继续执行。同一部分中的条目默认使用逻辑 AND，即全部条件都必须满足。

```yaml
conditions:
  in-water:
    type: in_water
    target: SOURCE
  low-health:
    type: health_percent
    target: SOURCE
    max: 35
```

以上两个条件必须同时通过。如果多个分支中只需要任意一个通过，请使用 `any` 条件。

请参阅[能力条件参考](../power-conditions/)。

### `modifiers`

修改器会在能力执行前，修改当前触发器提供的可变数据。

示例包括：

* 在攻击或受伤触发器中使用 `damage`；
* 在饥饿值变化时使用 `food`；
* 在物品耐久损耗时使用 `item_damage`；
* 在兼容的 `on-fish` 状态中使用钓鱼修改器；
* 在商人交易触发器中使用交易修改器；
* 在监守者愤怒值变化时使用 `warden_anger`。

如果将一个有效修改器用于不相关的触发器，当前上下文中将没有可供它修改的兼容结果。配置键必须使用复数形式 `modifiers`。

请参阅[能力修改器参考](../power-modifiers/)。

### `abilities`

能力会按照 YAML 中的顺序执行。能力可以使用触发器提供的实体、物品、方块、位置、弹射物或其他数据。

```yaml
powers:
  on-block-break:
    abilities:
      vein:
        type: break_blocks
        shape: VEIN
        center: EVENT_BLOCK
        breaker: SOURCE
        same-type: true
        max-blocks: '8 + {level} * 8'
```

只有在所选触发器提供了方块时，`EVENT_BLOCK` 才能使用。根据具体能力的文档，单个能力还可能支持自己的条件、概率、冷却、使用次数限制或子能力。

请参阅[能力参考](../abilities/)。

## 触发器专属选项

某些触发器除了 `conditions`、`modifiers` 和 `abilities` 外，还支持专属选项。例如，Tick 触发器支持 `interval`：

```yaml
powers:
  on-tick:
    interval: 20
    abilities:
      effect:
        type: particle
        target: SOURCE
        particle: HAPPY_VILLAGER
```

触发器专属选项记录在[能力触发器](../triggers.md)页面中。

## 激活能力

`activation-abilities` 不是事件触发器。EnchantmentReform 扫描物品并判断哪些附魔处于激活状态时，会执行此部分。

内置的 `disable_enchantments` 能力可以禁用同一物品上匹配的附魔：

```yaml
powers:
  activation-abilities:
    disable-curses:
      type: disable_enchantments
      enchantments:
        - '*_curse'
      exclude-enchantments:
        - enchantmentreform:allowed_curse
```

声明此规则的附魔不会禁用自身。普通触发器中的条件、修改器和 `powers.limit` 不适用于这个特殊部分。

## 多个触发器

一个附魔可以定义多个触发器部分：

```yaml
powers:
  on-shoot-bow:
    abilities:
      capture-power:
        type: mark

  on-projectile-tick:
    abilities:
      trail:
        type: particle
        target: SKILL
        particle: WHITE_ASH
        count: 6

  on-projectile-hit:
    abilities:
      remove-projectile:
        type: remove
        target: SKILL
```

发射触发器会捕获弹射物后续触发所需的能力状态，Tick 触发器生成轨迹，命中触发器处理最终撞击。

## 完整示例

```yaml
variables:
  bonus-damage: '1.5 * {level}'

powers:
  enabled: true
  limit:
    random: 1
    cooldown: 0
    times: 0

  on-attack:
    conditions:
      wounded-target:
        type: health_percent
        target: TARGET
        max: 30

    modifiers:
      finishing-damage:
        type: damage
        operation: ADD
        value: '{bonus-damage}'

    abilities:
      sound:
        type: sound
        target: TARGET
        sound: ENTITY_PLAYER_ATTACK_CRIT
        volume: 1
        pitch: 1.2

      particles:
        type: particle
        target: TARGET
        particle: CRIT
        amount: '5 + {level} * 3'
```

当目标生命值低于 30% 时，该附魔会增加伤害，然后播放音效和粒子反馈。

## 相关参考

* [附魔配置](../enchantment-configuration.md)：注册表字段和完整附魔文件结构。
* [能力触发器](../triggers.md)：触发器上下文、事件差异、结果兼容性和弹射物后续触发。
* [能力条件参考](../power-conditions/)：全部内置条件。
* [能力修改器参考](../power-modifiers/)：全部内置修改器。
* [能力参考](../abilities/)：全部内置能力。

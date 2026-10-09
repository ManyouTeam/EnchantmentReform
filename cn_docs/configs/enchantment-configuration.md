# 📄 附魔配置

{% hint style="info" %}
## 想在物品 Lore 中显示附魔描述？

请使用 [EnchantmentSlots](https://www.spigotmc.org/resources/enchantmentslots-add-enchantment-slot-feature-to-your-server-1-20-5.113048/)。
{% endhint %}

{% hint style="info" %}
## 想让生物也能使用附魔？

由于同一套附魔同时作用于生物和玩家时可能不够可靠，此功能已拆分至 [EnchantedMobs](https://www.spigotmc.org/resources/enchantedmobs-dynamic-mob-abilities-and-player-scaling-difficulty-61-built-in-power-1-21-3.133242/)。
{% endhint %}

自定义附魔会从以下目录中递归读取：

```
plugins/EnchantmentReform/enchantments/
```

你可以使用子目录整理盔甲、近战、远程、工具、钓鱼、诅咒或其他自定义分类。YAML 中的 `key` 是实际注册到 Minecraft 注册表中的键；文件路径只是用于管理的标识，但也应保持唯一。

指定的原版附魔定义可在以下目录中进行自定义：

```
plugins/EnchantmentReform/vanilla_enchantments/
```

原版附魔覆盖同样属于服务端启动阶段的数据，修改后必须重启服务器。有关部分覆盖规则、可用字段、`effects` 行为、禁用原版附魔以及完整示例，请参阅[原版附魔覆盖](vanilla-enchantment-overrides.md)。

## 注册表层与运行时层

一个附魔由两个不同层级组成：

1. **注册表数据**在服务器启动阶段创建，包括命名空间键、支持物品、主要物品、最高等级、权重、附魔费用、铁砧费用、有效槽位、互斥关系、诅咒状态、获取来源以及原生 `effects` 组件。
2. **运行时能力数据**在服务器运行期间执行，包括变量、触发器、条件、修改器、能力、冷却、概率和状态。

注册表数据会在服务端启动完成后冻结。当前重载实现支持重新读取部分运行时数据，但重载无法新增或删除注册表条目，也无法重建原生 `effects`。

## 完整结构

```yaml
enabled: true
key: enchantmentreform:example

name: Example
description: '&8Deals additional damage at high target health.'

supported-items: enchantmentreform:sword
primary-items: enchantmentreform:sword
exclusive-with:
  - minecraft:sharpness

max-level: 3
rarity: RARE
weight: 8
anvil-cost: 4

minimum-cost:
  base: 8
  per-level: 10
maximum-cost:
  base: 28
  per-level: 10

active-slots:
  - HAND
obtaining-sources:
  - ENCHANTING_TABLE
  - VILLAGER_TRADE
  - RANDOM_LOOT

execution-priority: 0

conditions:
  permission:
    type: permission
    permission: enchantmentreform.example.use

variables:
  multiplier:
    '==1': 1.15
    '==2': 1.3
    '>=3': 1.5

effects:
  minecraft:attributes:
    - id: enchantmentreform:example_attack_speed
      attribute: minecraft:attack_speed
      amount:
        type: minecraft:linear
        base: 0.1
        per_level_above_first: 0.05
      operation: add_value

powers:
  enabled: true
  limit:
    random: 1
    cooldown: 0
    times: 0

  on-attack:
    conditions:
      high-health-target:
        type: health_percent
        target: TARGET
        min: 80

    modifiers:
      damage:
        type: damage
        operation: MULTIPLY
        value: '{multiplier}'

    abilities:
      effect:
        type: particle
        target: TARGET
        particle: CRIT
```

## 原版附魔覆盖

`vanilla_enchantments/` 中的文件用于修改已有的 `minecraft:` 附魔，而不是注册一个新的键：

```yaml
enabled: true
key: minecraft:unbreaking
max-level: 5
```

原版附魔覆盖采用**部分覆盖**规则：

* 未填写的注册表字段会保留原版值；
* 已填写的字段会在 Paper 注册表启动阶段替换对应值；
* 省略 `effects` 会保留原版机制；
* `effects: {}` 会移除所有原版原生效果组件；
* 填写了内容的 `effects` 会完整替换原来的效果映射；
* `variables` 和 `powers` 会独立添加 EnchantmentReform 的运行时行为，不依赖原生注册表效果。

文件名会作为默认键，因此 `protection.yml` 对应 `minecraft:protection`。如果显式填写覆盖用的 `key`，必须使用 `minecraft` 命名空间。

修改已有原版附魔前，请先阅读[原版附魔覆盖](vanilla-enchantment-overrides.md)。该页面说明了平台支持、全部可用字段、继承行为、物品集合、互斥关系、有效槽位、禁用方式、重载要求及完整示例。

## 注册表字段

| 字段 | 用途 |
| --- | --- |
| `enabled` | 设置为 `false` 时不加载该附魔。 |
| `key` | 必填的 Minecraft 附魔命名空间键，必须唯一且保持稳定。 |
| `name` | 显示文本或语言占位符。 |
| `description` | 供兼容的界面或 Lore 集成显示的描述。 |
| `supported-items` | 此附魔可应用到的物品标签或支持物品定义。 |
| `primary-items` | 原版附魔逻辑支持时使用的首选物品子集。 |
| `exclusive-with` | 不能与该附魔共存的附魔键或互斥定义。 |
| `max-level` | 注册到游戏中的最高等级。 |
| `rarity` | 从 `config.yml -> rarity` 中读取默认配置。 |
| `weight` | 候选附魔的选择权重，可覆盖稀有度默认值。 |
| `anvil-cost` | 基础铁砧费用，可覆盖稀有度默认值。 |
| `minimum-cost` / `maximum-cost` | 各等级对应的附魔强度公式。 |
| `active-slots` | 能力和依赖装备的原生效果生效时所处的装备槽位。 |
| `obtaining-sources` | 允许该附魔自然生成的获取途径。 |
| `execution-priority` | 多个附魔处理同一事件时的运行顺序，数值越大越先执行。 |
| `effects` | 由当前服务端版本解码的 Minecraft 原生附魔效果组件。 |

修改注册表字段后必须完整重启服务器。请确保这些字段有效，否则服务端可能在加载附魔注册表时启动失败。

## 运行时字段

| 字段 | 用途 |
| --- | --- |
| `variables` | 可在描述和能力中重复使用、按等级变化或经过计算的值。 |
| `allow-duplicate` | 默认为 `false`。设为 `true` 后，同一种附魔在多个有效装备槽位中会分别执行能力。 |
| `conditions` | 使用附魔能力前必须满足的条件。扁平的命名条件是整条附魔的开关；使用数字等级键时，插件从物品附魔等级向下检查能力条件，使用第一个满足条件的等级，全部不满足时能力不生效。两种格式不能混用。 |
| `powers` | 运行时触发器、能力整体限制、条件、修改器、能力和激活能力。 |

完整的 `powers` 语法在[能力配置](powers/)页面中单独说明，以免将注册表字段与事件执行规则混在一起。

## 支持物品与主要物品

建议优先使用 `config.yml -> supported-items.tags` 中声明的可复用标签：

```yaml
supported-items: enchantmentreform:tools
primary-items: enchantmentreform:pickaxe
```

根据具体字段的实现，也可能支持列表。请统一使用命名空间键，并在附魔目录界面中确认最终匹配到的物品集合。

## 激活附魔

只有附魔物品位于配置的 `active-slots` 槽位中时，该附魔才会激活。常用值包括：

* `HAND`
* `MAINHAND`
* `OFFHAND`
* `HEAD`
* `CHEST`
* `LEGS`
* `FEET`
* `BODY`
* `ARMOR`
* `ANY`

Paper 的装备生命周期变化可触发：

* `on-activate`：附魔刚刚变为激活状态；
* `on-deactivate`：附魔不再处于激活状态。

处理事件触发器时，会根据当前激活物品和槽位决定哪些附魔参与执行。Minecraft 也会使用注册的有效槽位处理依赖装备的原生 `effects`，例如属性修改器。

## 获取来源

内置附魔常用的获取来源包括：

* `ENCHANTING_TABLE`
* `VILLAGER_TRADE`
* `RANDOM_LOOT`
* `TRADED_EQUIPMENT`

只应填写希望该附魔自然参与的获取途径。命令或其他插件仍可以直接将已注册的附魔添加到物品上。

## 稀有度继承

如果省略 `weight`、附魔费用或 `anvil-cost`，EnchantmentReform 可以从 `config.yml` 中所选的稀有度配置继承这些值。

这样可以让多个附魔共用同一套成长模型，同时仍允许某个附魔单独覆盖默认值。

## 执行优先级

多个激活附魔处理同一事件时，`execution-priority` 决定执行顺序。数值越大越先执行；数值相同时会按照附魔键以确定性的顺序排列。

当一个附魔需要为另一个附魔准备上下文时，应设置不同的优先级。例如，第一个附魔先修改方块，第二个附魔再检查修改后的方块。

## 原生效果

根节点的 `effects` 部分是 Minecraft 原生附魔效果组件数据的 YAML 表示：

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:example_health
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 2.0
        per_level_above_first: 1.0
      operation: add_value
```

`effects` 和 `powers` 是两套不同的系统：

* `effects` 由 Minecraft 原生附魔引擎处理，内容必须符合当前服务端版本的原生 Codec；
* `powers` 由 EnchantmentReform 处理，支持插件触发器、条件、修改器、能力、冷却、概率和状态；
* 同一个附魔可以同时使用这两个部分。

`{level}` 等插件表达式和根节点变量不会在 `effects` 中展开。原生等级缩放必须使用 `minecraft:linear` 等 Minecraft 数据结构。

有关 JSON 转 YAML、字段规则、属性效果、条件伤害免疫、位置效果、校验错误和完整示例，请参阅[原生附魔效果](native-enchantment-effects.md)。

## 变量

根节点变量可以在描述和 `powers` 中重复使用：

```yaml
variables:
  chance: '0.1 + {level} * 0.05'
  roman:
    '==1': I
    '==2': II
    '>=3': III+
```

字符串变量会直接替换为文本。对于支持数学值的字段，数字变量可作为表达式计算。

变量应声明在附魔根节点，而不是 `powers` 内部。变量不会在原生 `effects` 中展开。

## 能力

`powers` 部分采用与 EnchantedMobs 相同的“触发器 → 条件 → 修改器 → 能力”模型，但它必须写在附魔文件中：

```yaml
powers:
  limit:
    random: 1
    cooldown: 0
    times: 0

  on-attack:
    conditions: {}
    modifiers: {}
    abilities: {}
```

能力整体限制应写在 `powers.limit` 下。每个触发器部分包含该触发器的专属选项，以及 `conditions`、`modifiers` 和 `abilities`。

有关执行顺序、变量、限制、触发器部分、激活能力、弹射物后续触发和完整示例，请参阅[能力配置](powers/)。

## 相关页面

* [原版附魔覆盖](vanilla-enchantment-overrides.md)
* [原生附魔效果](native-enchantment-effects.md)
* [能力配置](powers/)
* [能力触发器](triggers.md)
* [能力条件](power-conditions/)
* [能力修改器](power-modifiers.md)
* [能力](abilities/)

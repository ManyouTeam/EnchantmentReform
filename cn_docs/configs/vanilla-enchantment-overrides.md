# 🧩 原版附魔覆盖

原版附魔覆盖允许 EnchantmentReform 修改指定的现有 `minecraft:` 附魔，并为它们添加 EnchantmentReform 的运行时能力。

覆盖文件从以下目录加载：

```
plugins/EnchantmentReform/vanilla_enchantments/
```

例如：

```
plugins/EnchantmentReform/vanilla_enchantments/protection.yml
```

覆盖文件**不会**注册第二个附魔，而是直接修改现有的原版注册表条目，例如 `minecraft:protection`。

{% hint style="warning" %}
注册表层面的原版附魔覆盖字段会在 Paper 初始化附魔注册表时应用。修改后必须完整重启服务器。`/enchantmentreform reload` 无法重建原版附魔注册表。
{% endhint %}

{% hint style="info" %}
运行时的 `variables` 和 `powers` 层与注册表替换相互独立。本页面介绍的注册表字段依赖 Paper 的注册表实现；不要假设普通 Spigot 可以重写现有的原版注册表条目。
{% endhint %}

## 最小覆盖配置

最精简且有实际作用的覆盖配置，可以在不修改原有注册表字段和原生机制的情况下，为现有原版附魔添加一个能力：

```yaml
enabled: true
key: minecraft:sharpness

variables:
  bonus-chance: '0.05 * {level}'

powers:
  on-attack:
    abilities:
      particles:
        type: particle
        target: TARGET
        particle: CRIT
        random: '{bonus-chance}'
```

由于配置中没有出现 `effects`、`max-level`、物品集合、附魔费用以及其他注册表字段，这些字段会继续使用原版值。

## 完整结构

```yaml
enabled: true
key: minecraft:protection

name: Protection Plus
description: '&8Protection rewritten by EnchantmentReform.'

supported-items: minecraft:enchantable/armor
primary-items: minecraft:enchantable/armor
exclusive-with:
  - '#minecraft:exclusive_set/armor'

max-level: 6
rarity: COMMON
weight: 10
anvil-cost: 1

minimum-cost:
  base: 1
  per-level: 11
maximum-cost:
  base: 12
  per-level: 11

active-slots:
  - ARMOR

execution-priority: 0

# Present and empty: remove every original native effect component.
effects: {}

variables:
  reduction: '{level}'

powers:
  on-damage:
    modifiers:
      protection:
        type: damage
        operation: SUBTRACT
        value: 'min({reduction}, {original} * 0.5)'
```

只有文件中明确声明的字段才会替换对应的原版注册表值，其他原版值保持不变。

## 文件名与 `key`

省略 `key` 时，插件会自动使用文件名：

```
protection.yml -> minecraft:protection
sharpness.yml  -> minecraft:sharpness
```

也可以明确填写键：

```yaml
key: minecraft:protection
```

键必须使用 `minecraft` 命名空间。`enchantmentreform:protection` 之类的自定义命名空间不属于原版附魔覆盖；这种情况应使用普通的自定义附魔文件。

每个原版附魔键只应对应一个覆盖文件。

## 部分覆盖规则

原版附魔覆盖以字段为单位。省略某个注册表字段表示**保留当前的原版值**。

| 配置方式 | 结果 |
| --- | --- |
| 未声明字段 | 保留原版注册表值。 |
| 声明字段 | 使用配置值替换对应的注册表值。 |
| 未声明 `effects` | 保留全部原版原生附魔效果。 |
| `effects: {}` | 使用空映射替换原生效果映射，从而移除原有机制。 |
| 填写了 `effects` | 使用配置的组件完整替换原生效果映射。 |
| `enabled: false` | 在 Paper 初始化阶段清空支持物品、主要物品和原生效果，从而禁用该附魔覆盖条目。 |

`effects` 会被完整替换，而不是逐个组件合并。需要保留原版附魔的部分原生机制时，必须把所有需要保留的原生效果组件复制到新的 `effects` 部分中。

## 支持的字段

### 启动阶段注册表字段

| 字段 | 应用行为 |
| --- | --- |
| `enabled` | 默认为 `true`。设为 `false` 时会禁用运行时能力，并由 Paper 清空该原版条目的支持物品、主要物品和原生效果。 |
| `key` | 目标原版附魔键。默认值为 `minecraft:<file-name>`，并且必须使用 `minecraft` 命名空间。 |
| `name` | 声明后替换附魔注册表中的显示描述。必须是非空字符串。 |
| `supported-items` | 替换允许应用此附魔的物品集合。 |
| `primary-items` | 替换原版附魔逻辑使用的优先物品子集。 |
| `exclusive-with` | 替换附魔不兼容集合。 |
| `weight` | 替换原版选择权重。有效配置范围为 `1..1024`。 |
| `max-level` | 替换原版最高等级。省略时，EnchantmentReform 会读取原注册条目的最高等级。 |
| `minimum-cost` | 替换最低附魔费用公式，必须包含 `base` 和 `per-level`。 |
| `maximum-cost` | 替换最高附魔费用公式，必须包含 `base` 和 `per-level`。 |
| `anvil-cost` | 替换基础铁砧费用。 |
| `active-slots` | 替换原生有效装备槽位组，同时控制 EnchantmentReform 能力是否激活。 |
| `effects` | 完整替换 Minecraft 原生附魔效果组件映射。 |

### 插件元数据与运行时字段

| 字段 | 用途 |
| --- | --- |
| `description` | 供兼容 EnchantmentReform 的 UI 或 Lore 集成使用的文本；不会替换原生注册表中的描述。 |
| `rarity` | EnchantmentReform 用于显示、排序和默认元数据的稀有度信息。它本身不会替换原版注册表权重；需要明确设置 `weight` 才能修改注册表权重。 |
| `execution-priority` | 多个已激活的 EnchantmentReform 能力处理同一事件时的执行顺序。数值越高越先执行。 |
| `variables` | 可在描述和 `powers` 中复用的值。 |
| `allow-duplicate` | 默认为 `false`。设为 `true` 后，同一种附魔在多个有效装备槽位中会分别执行能力。 |
| `powers` | EnchantmentReform 的触发器、条件、修改器、能力、限制、冷却、概率和状态。 |

## 物品集合

单个字符串会被视为物品标签：

```yaml
supported-items: minecraft:enchantable/armor
primary-items: minecraft:enchantable/armor
```

列表会被视为明确指定的物品键：

```yaml
supported-items:
  - minecraft:diamond_sword
  - minecraft:netherite_sword
```

不要在同一个列表中混用以 `#` 开头的标签条目和直接物品键。

## 互斥关系

可以使用一个附魔标签：

```yaml
exclusive-with: '#minecraft:exclusive_set/armor'
```

也可以使用直接附魔键：

```yaml
exclusive-with:
  - minecraft:fire_protection
  - minecraft:blast_protection
  - minecraft:projectile_protection
```

不要在同一个 `exclusive-with` 列表中混用附魔标签和直接附魔键。

## 有效槽位

省略 `active-slots` 时，只要含有该附魔的物品位于任意装备槽位中，EnchantmentReform 的运行时能力就会认为此覆盖附魔处于激活状态。

常用值包括：

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

`MAIN_HAND` 和 `MAINHAND` 会被标准化为 `HAND`；`OFFHAND` 会被标准化为 `OFF_HAND`。

声明该字段后，Paper 还会替换原版附魔的有效槽位组。这会影响 `minecraft:attributes` 等依赖装备槽位的原生组件。

## 原生 `effects` 行为

### 保留原版机制

不要声明该字段：

```yaml
# No effects section.
```

当你只想添加 EnchantmentReform 能力，或只修改 `max-level` 等元数据时，应使用这种方式。

### 移除原版机制

使用空映射：

```yaml
effects: {}
```

插件内置的 `protection.yml` 使用了这种方式：移除原版保护附魔的效果组件，然后改用 `powers` 实现伤害减免。

### 替换原版机制

提供完整的新原生效果映射：

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:vanilla_override_speed
      attribute: minecraft:movement_speed
      amount:
        type: minecraft:linear
        base: 0.01
        per_level_above_first: 0.01
      operation: add_value
```

有关组件结构、随等级变化的值、谓词、JSON 转 YAML 以及验证规则，请参阅[原生附魔效果](native-enchantment-effects.md)。

## 示例：只提高原版最高等级

```yaml
enabled: true
key: minecraft:unbreaking
max-level: 5
```

原版名称、支持物品、有效槽位、费用、互斥关系和原生效果均保持不变。

## 示例：保留原版机制并添加能力

```yaml
enabled: true
key: minecraft:sharpness

variables:
  ignite-chance: '0.03 * {level}'

powers:
  on-attack:
    abilities:
      ignite:
        type: fire
        target: TARGET
        fire-ticks: 60
        random: '{ignite-chance}'
```

由于没有声明 `effects` 字段，锋利会继续保留其原版原生伤害效果。

## 示例：完全重写保护附魔

```yaml
enabled: true
key: minecraft:protection
name: '{lang:vanilla-enchantment-protection-name}'
description: '{lang:vanilla-enchantment-protection-description}'
max-level: 6
rarity: COMMON

# Remove the original vanilla protection components.
effects: {}

variables:
  reduction: '{level}'

powers:
  on-damage:
    modifiers:
      reduction:
        type: damage
        operation: SUBTRACT
        value: 'min({reduction}, {original} * 0.5)'
```

## `enabled: false`

```yaml
enabled: false
key: minecraft:binding_curse
```

在 Paper 初始化阶段，此配置会保留注册表键，但清空支持物品、主要物品和原生效果。因此，该附魔无法再通过正常方式使用，EnchantmentReform 也不会初始化它的运行时能力。

这比仅仅省略 `powers` 更彻底。

## 重载与验证

注册表字段会在注册表冻结前处理。修改下列任意内容后，都必须完整重启服务器：

* `enabled`；
* `key`；
* `name`；
* 物品集合；
* 互斥关系；
* 权重和等级；
* 附魔费用和铁砧费用；
* 有效槽位；
* 原生 `effects`。

常见启动错误包括：

* 使用了非 `minecraft` 命名空间的键；
* 使用了不存在的原版附魔键；
* 权重超出 `1..1024`；
* 在列表中混用标签和直接键；
* 物品、附魔、属性、效果组件或谓词键无效；
* 原生 `effects` 复制自其他 Minecraft 版本。

验证消息中会包含文件路径，例如：

```
vanilla_enchantments/protection.yml: key must use the minecraft namespace
```

## 相关页面

* [附魔配置](enchantment-configuration.md)
* [原生附魔效果](native-enchantment-effects.md)
* [能力配置](powers/)

# 自定义物品

`plugins/EnchantmentReform/items` 中的每个 `.yml` 文件定义一个自定义物品。去掉 `.yml` 后的文件名就是物品 ID。不支持旧的根级 ItemFormat 和 `{ item: ... }` 定义。

```yaml
base-item:
  material: GOLDEN_HELMET
  name: '{lang:custom-item-night_vision_helmet-name}'
  lore:
    - '{lang:custom-item-night_vision_helmet-lore}'
    - '<gray>护甲：<white>{attribute:minecraft:armor}'
    - '<gray>耐力：<white>{attribute:custom_attribute:endurance}'

variables:
  armor-roll: 'random(1, 3)'

base-attributes:
  minecraft:armor: '{armor-roll} + 1'
  custom_attribute:endurance: '1 + random(0, 1)'

change-item:
  add-lore-last:
    - '<gray>最终护甲：<white>{attribute:minecraft:armor}'

active-slots:
  - HEAD

allow-duplicate: false

powers:
  on-tick:
    interval: 10
    abilities:
      night-vision:
        type: potion_effect
        target: SOURCE
        potion: minecraft:night_vision
        duration: 30
        particles: false
```

## 配置项

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `enabled` | `true` | 是否加载此自定义物品定义。 |
| `base-item` | 必填 | 用于构建物品的 [ItemFormat™](../shared-formats/itemformat-tm.md)。 |
| `base-attributes` | 空 | 物品提供的原版及自定义属性 modifier，格式见下文。 |
| `use-base-attack-modifier-keys` | `false` | 让 `minecraft:attack_damage` 和 `minecraft:attack_speed` 分别使用原版 `minecraft:base_attack_damage` 与 `minecraft:base_attack_speed` Modifier Key。 |
| `change-item` | 空 | 在物品构建的最后阶段应用[物品修改规则](../shared-formats/change-item-rules.md)。 |
| `active-slots` | 必填 | 可以激活物品 Power 的装备槽位。 |
| `allow-duplicate` | `false` | 是否允许每个已装备副本分别执行 Power。 |
| `execution-priority` | `0` | 相对于其他活跃 Power 来源的执行顺序，数值越高越先执行。 |
| `variables` | 空 | 供该物品 Power 配置共用的变量。 |
| `powers` | 必填 | 与附魔和自定义属性使用相同的触发器、条件、修改器及 ability 格式。 |

有效槽位包括 `HAND`、`OFF_HAND`、`HEAD`、`CHEST`、`LEGS`、`FEET`、`ARMOR` 和 `ANY`。`ARMOR` 包含四个盔甲槽位，`ANY` 包含所有受支持的装备槽位。

## 基础属性

`base-attributes` 下的每个键都是属性 ID，值可以是数值或数学表达式。原版属性使用 `minecraft:<ID>`；自定义属性支持裸 ID、`custom_attribute:<ID>` 或 `enchantmentreform:<ID>`。名称可能产生歧义时，建议明确使用 `custom_attribute:`。所有属性固定使用 `ADD_NUMBER`，装备槽位根据物品材质自动选择。

原版属性默认使用自动生成的 Modifier Key，例如 `enchantmentreform:base_attribute/example/minecraft/attack_damage`。在物品根节点设置 `use-base-attack-modifier-keys: true` 后，`minecraft:attack_damage` 会使用 `minecraft:base_attack_damage`，`minecraft:attack_speed` 会使用 `minecraft:base_attack_speed`；其他属性仍使用自动生成的 Key。

属性值使用与 Power 相同的数值解析管线：支持 `{level}`（自定义物品固定为 `1`）、根级 `variables`、生成物品时传入的参数、存在玩家时的 PlaceholderAPI 数值、数学表达式、`2~5` 随机范围以及 `random(1, 5)` 等函数。每个表达式只在生成物品时计算一次。

每个原版及自定义属性的计算结果都会以 `DOUBLE` 写入物品 PDC。复制或移动物品不会改变随机结果；重载或修改配置也不会重新随机已有物品，需要重新生成物品才能应用新公式。

可以在 `base-item.name`、`base-item.item-name` 或 `base-item.lore` 中使用 `{attribute:<属性 ID>}` 显示生成结果。占位符中的 ID 必须与 `base-attributes` 下的键一致，例如 `{attribute:minecraft:armor}`。显示值使用与描述相同的紧凑数字格式。

每个配置的原版基础属性都会覆盖该属性已有的全部 Modifier，包括物品材质的默认 Modifier。插件随后使用配置结果写入一个 `ADD_NUMBER` Modifier；未在 `base-attributes` 中配置的其他属性仍保留原版默认 Modifier。计算结果也会写入物品 PDC。

自定义属性 modifier 会在带有正确自定义物品 ID 的物品实际处于自动槽位时从物品 PDC 读取计算结果。它会影响最终属性值及描述、显示在属性 GUI 中，并在移走物品后立即消失；它不会写入玩家 PDC。

## 最终物品修改

可选的根节点 `change-item` 使用标准[物品修改格式](../shared-formats/change-item-rules.md)。它会在 `base-item`、原版基础属性 Modifier 和属性 PDC 写入完成后执行，所有规则按照 YAML 顺序进一步修改已完成的物品。

这些规则支持根节点 `variables`、物品生成参数、普通 Change Item 占位符以及 `{attribute:<属性 ID>}` 占位符。即使使用 `replace-item`，生成结果仍属于当前自定义物品定义。

## 物品身份与 Power 激活

通过定义构建的物品会获得持久化的自定义物品 ID。仅仅复制相同的材质、名称和 Lore，并不会让另一个物品成为自定义物品。

玩家在配置的有效槽位中手持或穿戴该物品时，它的 Power 会加入玩家的活跃 Power 来源。`allow-duplicate: false` 时，同一定义只有一个副本会执行；设为 `true` 后，每个处于有效槽位的副本都会分别执行。

传递给 Power 的 `{level}` 值为 `1`。

## 默认自定义物品

`items` 目录为空时，EnchantmentReform 会自动释放 `night_vision_helmet.yml`。玩家把这顶金头盔戴在头部槽位后，会持续获得夜视效果。名称和 Lore 使用语言文件中的 `{lang:...}` 变量符。

## 创建和给予自定义物品

以下指令操作的是新格式的自定义物品定义：

```text
/enchantmentreform saveitem <ID> [bukkit|itemformat]
/enchantmentreform givesaveitem <ID> [玩家] [数量]
/enchantmentreform generateitemformat
```

`saveitem` 根据玩家主手物品创建 `items/<ID>.yml`，并生成 `active-slots: [HAND]` 和空的 `powers` 配置，之后可以继续编辑。`givesaveitem` 会根据配置构建物品，写入持久化的自定义物品 ID，然后给予玩家。

`generateitemformat` 会把主手物品转换为独立的 ItemFormat，并写入 `plugins/EnchantmentReform/generated-item-format.yml`。它不会创建自定义物品定义。

手动修改定义后，需要重载 EnchantmentReform 再进行测试。

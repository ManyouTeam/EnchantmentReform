# ✨ 附魔与 ItemFormat 规则

本页面介绍 MatchItemFormat 中用于匹配直接附魔、存储附魔、附魔等级与数量、可附魔性，以及序列化 ItemFormat 的规则。

## 本页面的规则

* `has-enchants`
* `has-stored-enchants`
* `contains-enchants`
* `contains-enchants-amount`
* `enchantable`
* `item-format`

## 直接附魔与存储附魔

Minecraft 会将物品上的直接附魔，与附魔书中保存的附魔分开存储。应根据实际检查的组件选择对应规则。

## `has-enchants`

要求存在一个或多个直接附魔键。

```yaml
match-item:
  has-enchants:
    - minecraft:sharpness
    - enchantmentreform:full_health_strike
```

列表的具体行为由规则实现决定。需要明确表示任意一个附魔存在即可时，请使用单独的 `any` 组。

请使用命名空间键。显示名称会被翻译，并不是稳定标识符。

## `has-stored-enchants`

要求附魔书或其他兼容的存储附魔组件中存在指定附魔。

```yaml
match-item:
  material:
    - ENCHANTED_BOOK
  has-stored-enchants:
    - enchantmentreform:auto_smelt
```

剑上的直接附魔不会满足存储附魔规则，附魔书中的条目也不一定满足直接附魔规则。

## `contains-enchants`

同时检查附魔键与等级要求。

```yaml
match-item:
  contains-enchants:
    minecraft:sharpness: 3
```

根据解析器语法，数值可能表示精确等级或最低阈值。需要精确上下限时，请使用当前内置示例中展示的范围或比较结构。

配置多个附魔键的示例：

```yaml
match-item:
  contains-enchants:
    minecraft:unbreaking: 2
    enchantmentreform:vein_mining: 1
```

同一层级的规则通常使用 AND，因此除非使用嵌套逻辑组，否则物品必须满足每个配置的键。

## `contains-enchants-amount`

检查物品中存在多少个符合要求的附魔。

```yaml
match-item:
  contains-enchants-amount:
    min: 2
    max: 5
```

此规则适用于“至少带有两个附魔的物品”等通用要求。需要确认具体附魔身份时，不能用数量规则代替附魔键检查。

根据规则实现，存储附魔和直接附魔可能会分别计数。请单独测试附魔书和普通装备。

## `enchantable`

检查物品是否符合配置的可附魔规则。

```yaml
match-item:
  enchantable: true
```

可附魔性可能取决于材质、组件数据和当前 Minecraft 版本。它并不表示每一个自定义附魔都支持该物品。需要确认某个 EnchantmentReform 附魔是否接受该物品时，请检查该附魔的支持物品集合，或使用等价的匹配标签。

物品已经带有附魔时，仍然可能被视为可附魔。

## `item-format`

匹配选中的序列化 ItemFormat 字段。

```yaml
match-item:
  item-format:
    material: DIAMOND_SWORD
    name: '&bExample Sword'
```

已经使用相同 ItemFormat 语法创建物品时，此规则非常实用。与简单的材质、名称或 Lore 规则相比，它可以比较更有结构的元数据。

只有当前 ItemFormat 匹配器能够识别的字段才会参与比较。只需要两个稳定字段时，不要复制完整的序列化物品；过于严格的比较会在无关元数据发生变化时失效。

## 自定义附魔键

EnchantmentReform 附魔使用命名空间键注册：

```yaml
key: enchantmentreform:example
```

在 MatchItemFormat 中应使用相同的键：

```yaml
match-item:
  has-enchants:
    - enchantmentreform:example
```

修改附魔键会创建不同的注册表身份，并使旧键的物品无法匹配。更新时应保持键稳定。

## 直接附魔与活跃附魔

这些规则检查的是物品组件，并不一定表示该附魔当前处于活跃状态。

附魔可能存在于物品上，但由于以下原因不处于活跃状态：

* 物品不在 `active-slots` 指定的槽位中；
* 激活能力抑制了该附魔；
* 附魔文件已禁用或注册失败；
* 配置修改后，该物品不再属于支持物品集合。

需要判断运行时是否活跃，而不是判断物品是否保存附魔数据时，应使用能力运行时或槽位逻辑。

## 示例：高等级自定义附魔

```yaml
match-item:
  material-tag:
    - minecraft:pickaxes
  contains-enchants:
    enchantmentreform:vein_mining: 3
```

## 示例：原版保护或自定义保护

```yaml
match-item:
  any:
    vanilla:
      has-enchants:
        - minecraft:protection
    custom:
      has-enchants:
        - enchantmentreform:magic_guard
```

## 示例：附魔书筛选器

```yaml
match-item:
  material:
    - ENCHANTED_BOOK
  has-stored-enchants:
    - enchantmentreform:full_health_strike
```

## 示例：灵活的 ItemFormat 比较

```yaml
match-item:
  item-format:
    material: NETHERITE_SWORD
  not:
    contains-lore:
      - Disabled
```

## 获取方式与匹配

`obtaining-sources` 控制附魔如何自然出现。附魔已经存在于物品上后，它不会影响 MatchItemFormat 能否检测到该附魔。

同样，`exclusive-with` 控制附魔操作期间的兼容性；匹配时不会隐藏任意一个附魔键。

## 常见错误

* 使用 `has-enchants` 匹配附魔书中的存储附魔；
* 使用显示名称代替命名空间附魔键；
* 将物品可附魔性与特定附魔是否支持该物品混淆；
* 认为物品上的附魔在当前槽位中一定处于活跃状态；
* 修改自定义附魔键后，仍期望旧物品能够匹配；
* 使用不稳定元数据对 `item-format` 施加过多限制；
* 认为附魔数量规则会以完全相同的方式计算直接附魔和存储附魔。

另请参阅[附魔配置](../../configs/enchantment-configuration.md)、[ItemFormat™](../itemformat-tm.md)和[逻辑与 NBT 规则](logic-nbt.md)。

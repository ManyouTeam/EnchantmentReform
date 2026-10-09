# 🧟 实体身份与状态规则

本页面介绍 MatchEntityFormat 中用于匹配 Bukkit 实体类型、名称、生命值、标签、PersistentDataContainer 数值、远程分类、怪物和动物的规则。

## 本页面的规则

- `entity-types`
- `entity-contains-name`
- `entity-health`
- `entity-tag`
- `entity-pdc`
- `ranged`
- `monster`
- `animal`

MatchEntityFormat 接受生物实体。同一层级的规则使用逻辑 AND；一条规则内部的列表值通常使用逻辑 OR。

## `entity-types`

匹配 Bukkit 实体类型。

```yaml
match-entity:
  entity-types:
    - ZOMBIE
    - HUSK
    - DROWNED
```

请使用目标服务器版本中存在的枚举名称。自定义 MythicMobs 通常仍会保留一个 Bukkit 基础实体类型；需要精确匹配其内部怪物 ID 时，请使用 MythicMobs 集成规则。

## `entity-contains-name`

检查实体的自定义名称或显示名称是否包含配置文本。

```yaml
match-entity:
  entity-contains-name:
    - Boss
    - Elite
```

名称匹配使用方便，但并不稳定，原因包括：

- 名称可能被翻译或添加颜色格式；
- 其他插件可以动态修改名称；
- 普通怪物可能使用相同文本；
- 未命名实体可能公开原版翻译后的显示组件，而不是自定义名称。

需要稳定识别时，应优先使用标签、PDC 或集成 ID。

## `entity-health`

检查当前生命值是否符合配置的数值或范围。

```yaml
match-entity:
  entity-health:
    min: 1
    max: 20
```

具体接受的标量或范围语法由该规则的实现决定。当前生命值并不等于最大生命值，并且可能在目标选择和延迟执行之间发生变化。

在能力配置中进行百分比检查时，使用 `health_percent` 能力条件通常更清晰。

## `entity-tag`

匹配计分板或实体标签。

```yaml
match-entity:
  entity-tag:
    - elite
    - summoned_by_plugin
```

当服务器能够控制标签分配时，实体标签是一种稳定且轻量的标识符。如果 Bukkit 标签存储区分大小写，标签匹配也会区分大小写。

不要将实体标签与 Minecraft 实体类型注册表标签混淆。

## `entity-pdc`

匹配实体 PersistentDataContainer 中保存的数值。

```yaml
match-entity:
  entity-pdc:
    namespace:key: expected-value
```

具体配置结构取决于 PDC 匹配器和保存的数据类型。命名空间、键和数据类型都必须与写入这些数据的插件一致。

当实体由你自己的插件或数据包管理时，PDC 比显示名称更适合作为稳定标识符。不要在未经版本测试的情况下依赖其他插件未公开说明的键。

## `ranged`

检查平台实现是否将该实体归类为远程攻击实体。

```yaml
match-entity:
  ranged: true
```

Paper 可能提供比 Spigot 更丰富的远程实体分类。即使实体当前没有手持弓，只要其 AI 会使用投射物，也可能被归入远程实体。

实际手持物品很重要时，请使用 `equip`。

## `monster`

要求或排除 Bukkit 的 `Monster` 分类。

```yaml
match-entity:
  monster: true
```

```yaml
match-entity:
  monster: false
```

此规则依据 Bukkit 接口，而不是自定义的敌对实体表。在不同版本中，某些敌对或特殊实体可能没有实现预期的精确类别。

## `animal`

要求或排除 Bukkit 动物分类。

```yaml
match-entity:
  animal: true
```

驯服状态、所有者、年龄和是否能够繁殖属于不同条件，此规则不会隐含检查这些状态。

## 组合身份规则

```yaml
match-entity:
  monster: true
  entity-types:
    - SKELETON
    - STRAY
  ranged: true
```

实体必须同时满足这三条规则。

需要允许“带标签的僵尸”或“具有指定名称的骷髅”时，请使用 `any`：

```yaml
match-entity:
  any:
    tagged-zombie:
      entity-types:
        - ZOMBIE
      entity-tag:
        - elite
    named-skeleton:
      entity-types:
        - SKELETON
      entity-contains-name:
        - Archer
```

## 生命值规则与能力条件的区别

当生命值属于可复用 MatchEntityFormat 筛选器的一部分时，尤其是在 `nearby_entities` 内部，请使用 `entity-health`。

以下情况更适合使用 `health` 或 `health_percent` 能力条件：

- 已经知道要使用的目标选择器；
- 需要基于最大生命值百分比判断；
- 希望使用普通条件的反转或嵌套功能；
- 生命值检查直接属于某一个能力。

## 示例：附近低生命值怪物

```yaml
abilities:
  nearby:
    type: nearby_entities
    radius: 8
    amount: 5
    match-entity:
      monster: true
      entity-health:
        max: 10
    abilities:
      mark:
        type: particle
        target: TARGET
        particle: CRIT
        amount: 5
```

## 示例：排除类似玩家的实体

```yaml
match-entity:
  monster: true
  not:
    entity-types:
      - PLAYER
      - ARMOR_STAND
```

MatchEntityFormat 面向生物实体；非生物实体类型可能在单独规则执行前就已经匹配失败。

## 常见错误

- 在存在稳定标签、PDC 或集成 ID 时仍使用名称进行匹配；
- 混淆当前生命值和最大生命值；
- 认为 `monster` 或 `animal` 分类一定与自定义语义分类完全相同；
- 认为远程实体一定正在手持弓；
- 使用 MatchEntityFormat 匹配非生物实体；
- 使用目标服务器版本中不存在的实体类型枚举；
- 认为 PDC 数值会在不同数据类型之间自动转换。

另请参阅[装备与集成规则](equipment-integrations.md)和[逻辑规则](logic.md)。
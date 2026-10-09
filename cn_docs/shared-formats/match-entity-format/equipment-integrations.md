# 🛡️ 装备与集成规则

本页面介绍 MatchEntityFormat 中用于匹配装备和可选怪物插件集成的规则。

## 本页面的规则

- `equip`
- `mythicmobs`
- `levelled-mobs`

只有在 EnchantmentReform 启用时，对应插件和兼容挂钩均可用，集成规则才会被注册。

## `equip`

使用嵌套的 MatchItemFormat 配置部分匹配一个或多个装备槽位。

```yaml
match-entity:
  equip:
    main-hand:
      material-tag:
        - minecraft:swords
    helmet:
      material:
        - NETHERITE_HELMET
```

除非装备匹配器明确说明支持其他组合模式，否则每个已配置槽位都必须通过其嵌套物品规则。

常见槽位键包括：

- `main-hand`
- `off-hand`
- `helmet`
- `chestplate`
- `leggings`
- `boots`

请使用当前实现接受的准确槽位名称。配置解析器可能提供面向用户的别名，因此这些名称不一定与 Bukkit 枚举名称相同。

## 匹配空槽位

缺少物品通常会表现为空气或空 `ItemStack`。请使用装备匹配器实际支持的 MatchItemFormat 形式，不要直接假设 `material: AIR` 在所有情况下都表示空槽位。

空槽位条件很重要时，请使用准确的实体类型和服务器版本单独测试。

## 装备示例：远程骷髅

```yaml
match-entity:
  entity-types:
    - SKELETON
    - STRAY
  equip:
    main-hand:
      material:
        - BOW
        - CROSSBOW
```

此规则比 `ranged: true` 更严格，因为它会检查实际主手物品。

## 装备示例：带附魔武器

```yaml
match-entity:
  equip:
    main-hand:
      material-tag:
        - minecraft:swords
      has-enchants:
        - minecraft:sharpness
```

所有 MatchItemFormat 规则都可以嵌套在槽位下，包括逻辑组，以及可选的提供器或 NBT 规则。

## `mythicmobs`

MythicMobs 挂钩已加载时，匹配 MythicMobs 内部怪物 ID。

```yaml
match-entity:
  mythicmobs:
    - SkeletonKing
    - EliteZombie
```

请使用 MythicMobs 内部 ID，而不是显示名称。仍然可以通过 `entity-types` 检查 MythicMob 的 Bukkit 实体类型，但内部 ID 更加精确。

### 组合基础类型与 MythicMobs ID

```yaml
match-entity:
  entity-types:
    - SKELETON
  mythicmobs:
    - SkeletonKing
```

两条规则都必须通过。这样可以防止意外的 ID 或挂钩不匹配，但仅使用 MythicMobs ID 已经足够时无需额外配置基础类型。

## `levelled-mobs`

LevelledMobs 挂钩已加载时，匹配 LevelledMobs 数据。

```yaml
match-entity:
  levelled-mobs:
    # level/rule fields supported by the hook
```

具体配置结构取决于插件附带的集成实现。请从当前示例或挂钩实现开始，并针对已安装的 LevelledMobs 版本进行验证。

常见用途包括根据生成等级、受管理状态，或挂钩公开的其他 LevelledMobs 元数据进行筛选。

## 可选规则注册

MythicMobs 或 LevelledMobs 缺失或不兼容时，对应的 MatchEntityFormat 规则可能不会被注册。

MatchEntityFormat 会忽略未知键。这意味着不可用的可选规则可能导致匹配范围比预期更广。

{% hint style="warning" %}
将 `mythicmobs` 或 `levelled-mobs` 用于安全控制、奖励或破坏性能力前，务必确认启动日志显示所需的集成挂钩已经注册。
{% endhint %}

## 安全集成后备限制

某项能力必须在集成存在时才执行时，应尽可能将集成规则与另一个稳定限制组合使用：

```yaml
match-entity:
  entity-types:
    - WITHER_SKELETON
  mythicmobs:
    - InfernalGuard
```

这不能替代挂钩检查，但在可选键被忽略时，可以防止匹配器变为完全不受限制。

对于高风险操作，缺少必要依赖时应直接禁用整个附魔或配置。

## 延迟执行期间的装备变化

实体被选中后仍然可以更换装备。`nearby_entities` 能力可能先匹配实体，随后延迟执行子能力。

必须确保准确装备仍然存在时：

- 避免过长延迟；
- 可以时，在延迟分支中再次执行 `match_entity` 条件；
- 不要认为之前捕获的 `ItemStack` 引用始终代表当前物品；
- 考虑怪物拾取或丢弃装备的情况。

## 自定义物品装备

嵌套 MatchItemFormat 可以使用提供器 ID：

```yaml
match-entity:
  equip:
    main-hand:
      items:
        - namespace:boss_sword
```

对应的物品提供器挂钩也必须完成注册。MythicMobs 实体匹配和 MythicMobs 物品匹配属于两个独立集成。

## 示例：选择带装备的 MythicMob

```yaml
match-entity:
  mythicmobs:
    - SkeletonKing
  equip:
    main-hand:
      material:
        - BOW
    helmet:
      has-name: true
```

## 示例：排除穿戴护甲的目标

```yaml
match-entity:
  monster: true
  not:
    equip:
      chestplate:
        material-tag:
          - minecraft:chest_armor
```

请确认该标签确实存在，并验证空装备槽位的行为是否符合预期。

## 常见错误

- 使用显示名称代替 MythicMobs 内部 ID；
- 未检查挂钩注册就依赖可选键；
- 认为未知集成键会以关闭方式失败；
- 配置解析器要求别名时却使用 Bukkit 槽位名称；
- 认为 `ranged: true` 与匹配弓类装备完全相同；
- 只检查一次装备，却在物品改变后执行延迟操作；
- 混淆实体提供器集成与物品提供器集成。

另请参阅 [MatchItemFormat](../match-item-format/README.md)、[实体身份与状态规则](identity-state.md)和[逻辑规则](logic.md)。
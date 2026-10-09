# 🔍 实体匹配格式

当 EnchantmentReform 需要检查一个生物实体时，会使用共享的 `MatchEntityFormat` 格式。

`match_entity` 能力条件，以及 `nearby_entities` 等实体选择能力都会使用此格式。

```yaml
match-entity:
  monster: true
  entity-types:
    - ZOMBIE
    - SKELETON
  entity-health: 20
```

## 检查规则

MatchEntityFormat 配置部分不使用 `type` 字段。每个可识别的键都会启用一条规则，同一层级的规则使用逻辑 **AND（与）**。同一条规则内部的列表项通常使用逻辑 **OR（或）**。

## 缺失或无效输入

* 缺少 MatchEntityFormat 配置部分时，视为匹配所有实体。
* 缺少实体时不会匹配。
* 匹配器接受 `LivingEntity`；非生物实体必须由明确支持它们的能力进行筛选，而不能使用此格式。
* 未知键会被忽略。
* `none: true` 会强制匹配失败。

## 逻辑组合

使用 `any` 表示多个候选条件：

```yaml
match-entity:
  any:
    skeleton:
      entity-types:
        - SKELETON
      ranged: true
    mythic:
      mythicmobs:
        - SkeletonKing
```

使用 `not` 排除嵌套匹配结果：

```yaml
match-entity:
  monster: true
  not:
    entity-types:
      - CREEPER
```

## 装备匹配

`equip` 可以针对一个或多个装备槽位包含 MatchItemFormat 配置部分：

```yaml
match-entity:
  equip:
    main-hand:
      material-tag:
        - minecraft:swords
    helmet:
      material:
        - CARVED_PUMPKIN
```

## 内置规则

| 规则键 | 用途 |
| --- | --- |
| `entity-types` | 匹配 Bukkit 实体类型。 |
| `none` | 设置为 true 时强制失败。 |
| `entity-contains-name` | 匹配实体自定义名称或显示名称中的文本。 |
| `entity-health` | 检查实体当前生命值是否符合配置的阈值或范围。 |
| `entity-tag` | 匹配 Bukkit/Minecraft 实体标签。 |
| `entity-pdc` | 匹配配置的 PersistentDataContainer 数值。 |
| `ranged` | 检查平台实现是否将该实体归类为远程攻击实体。 |
| `monster` | 要求或排除 Bukkit `Monster`。 |
| `animal` | 要求或排除 Bukkit 动物分类。 |
| `equip` | 使用嵌套 MatchItemFormat 配置匹配装备。 |
| `mythicmobs` | 挂钩可用时匹配 MythicMobs 内部 ID。 |
| `levelled-mobs` | 插件可用时匹配 LevelledMobs 数据。 |
| `any` | 至少一个嵌套组通过时通过。 |
| `not` | 排除一个嵌套匹配结果。 |

## 可选规则

| 规则 | 要求 |
| --- | --- |
| `mythicmobs` | MythicMobs 必须在 EnchantmentReform 注册匹配规则前加载。 |
| `levelled-mobs` | 必须加载 LevelledMobs。 |
| Paper 远程实体分类 | Paper 可能提供比 Spigot 更丰富的远程实体分类。 |

不可用的可选规则不会被注册，因此对应键会被忽略。请在启动日志中确认挂钩已经注册。

## 完整示例

```yaml
match-entity:
  monster: true
  entity-health: 20
  any:
    ranged:
      ranged: true
      equip:
        main-hand:
          material:
            - BOW
            - CROSSBOW
    custom:
      mythicmobs:
        - SkeletonKing
  not:
    entity-types:
      - CREEPER
```

## 相关页面

* [MatchItemFormat](../match-item-format/)
* [能力条件](../../configs/power-conditions/)
* [能力](../../configs/abilities/)
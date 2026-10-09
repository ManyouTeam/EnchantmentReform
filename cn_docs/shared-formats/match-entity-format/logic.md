# 🧠 逻辑规则

本页面介绍 MatchEntityFormat 的组合方式。同一层级的规则使用逻辑 AND；使用 `any` 表示多个候选条件，使用 `not` 表示排除条件。

## 本页面的规则

- `none`
- `any`
- `not`

## 默认 AND 行为

```yaml
match-entity:
  monster: true
  entity-types:
    - ZOMBIE
    - HUSK
  entity-health:
    max: 20
```

实体必须属于 Bukkit `Monster`，必须是僵尸或尸壳，并且必须满足生命值规则。

## `none`

启用时强制匹配器失败。

```yaml
match-entity:
  none: true
```

这适合用于明确禁用或自动生成的配置，但通常直接移除不使用的匹配器会更清晰。

## `any`

至少一个嵌套的 MatchEntityFormat 组匹配时通过。

```yaml
match-entity:
  any:
    zombie:
      entity-types:
        - ZOMBIE
        - HUSK
    skeleton:
      entity-types:
        - SKELETON
        - STRAY
```

`zombie`、`skeleton` 等每个子项都是完整的匹配器。子项内部的规则仍然使用 AND。

### 按集成提供候选条件

```yaml
match-entity:
  any:
    vanilla-boss:
      entity-types:
        - WITHER
        - ENDER_DRAGON
    mythic-boss:
      mythicmobs:
        - SkeletonKing
        - InfernalDragon
```

请注意，不可用的可选集成键会被忽略，而不会自动导致匹配失败。对于具有破坏性或影响奖励的逻辑，必须先确认挂钩已经启用，才能使用这种配置。

## `not`

排除一个嵌套的 MatchEntityFormat 组。

```yaml
match-entity:
  monster: true
  not:
    entity-types:
      - CREEPER
```

此配置会匹配除苦力怕外的怪物。

### 排除多个类别

```yaml
match-entity:
  not:
    any:
      players:
        entity-types:
          - PLAYER
      named-boss:
        entity-contains-name:
          - Boss
      protected-tag:
        entity-tag:
          - protected
```

## 组合 AND、OR 和 NOT

下方配置表达的逻辑为：

```text
monster AND (zombie OR skeleton) AND NOT protected
```

```yaml
match-entity:
  monster: true
  any:
    zombie:
      entity-types:
        - ZOMBIE
        - HUSK
        - DROWNED
    skeleton:
      entity-types:
        - SKELETON
        - STRAY
        - WITHER_SKELETON
  not:
    entity-tag:
      - protected
```

## 一个候选条件中的多项要求

```yaml
match-entity:
  any:
    armed-zombie:
      entity-types:
        - ZOMBIE
      equip:
        main-hand:
          material-tag:
            - minecraft:swords
    ranged-skeleton:
      entity-types:
        - SKELETON
      ranged: true
```

每一个候选组都可以包含任意数量的 AND 规则。

## 反转单条规则

应尽量使用 `not` 包裹最小范围的匹配器：

```yaml
match-entity:
  not:
    animal: true
```

避免反转过大的配置组，否则可能意外匹配缺少数据或不支持对应数据的实体。

## 缺失实体与生物实体限制

缺少实体时不会匹配。MatchEntityFormat 专门面向 `LivingEntity`；非生物实体应由明确支持它们的能力或触发器处理。

逻辑包装器不会使缺失或非生物实体变为有效目标。例如，不应把 `not: { monster: true }` 当作安全匹配所有非怪物对象的方式。

## 可选键与开放失败行为

未知的 MatchEntityFormat 键会被忽略，因为没有已注册规则负责处理它们。以下情况尤其需要注意：

- 未加载 MythicMobs 挂钩时使用 `mythicmobs`；
- 未加载 LevelledMobs 挂钩时使用 `levelled-mobs`；
- 使用平台专属分类规则；
- 键名拼写错误。

只包含被忽略键的匹配器，实际效果可能变为不受任何限制。

对于重要筛选器：

1. 检查启动日志，确认挂钩已经注册；
2. 尽可能同时加入一个稳定的内置限制；
3. 分别使用一个确定会匹配和一个确定不会匹配的实体进行测试；
4. 缺少必要集成时，禁用依赖该集成的附魔。

## 示例：选择附近敌对实体

```yaml
abilities:
  nearby:
    type: nearby_entities
    radius: 8
    amount: 6
    match-entity:
      monster: true
      not:
        any:
          creeper:
            entity-types:
              - CREEPER
          protected:
            entity-tag:
              - no_enchantment_target
    abilities:
      glow:
        type: particle
        target: TARGET
        particle: ENCHANT
        amount: 5
```

## 示例：自定义 Boss 或高生命值原版怪物

```yaml
match-entity:
  any:
    custom:
      mythicmobs:
        - SkeletonKing
    vanilla:
      monster: true
      entity-health:
        min: 100
```

## 调试嵌套匹配器

1. 将每个子项单独作为完整的 `match-entity` 配置部分进行测试。
2. 先移除 `not`，确认正向匹配组能够正常工作。
3. 确认选中的目标是生物实体。
4. 检查准确的实体类型、标签、PDC、装备和集成 ID。
5. 确认可选规则已经注册。
6. 每次只添加一个候选组。
7. 尽量减少嵌套层级，并为各组使用具有说明性的名称。

## 常见错误

- 认为顶层规则键使用 OR；
- 直接在 `any` 下放置多个互不相关的规则键，而没有建立完整子组；
- 反转的配置范围大于实际需要；
- 认为未知集成键会以关闭方式失败；
- 使用 `not` 匹配缺失或非生物实体；
- 忘记每个候选组的内部规则仍使用 AND；
- 匹配范围过宽，导致 `nearby_entities` 选中玩家或受保护怪物。

另请参阅[实体身份与状态规则](identity-state.md)和[装备与集成规则](equipment-integrations.md)。
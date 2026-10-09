# 🧱 基础与元数据规则

本页面介绍 MatchItemFormat 中用于匹配提供器 ID、材质、标签、稀有度、显示名称和 Lore 的规则。同一层级的规则使用逻辑 AND；一条规则内部的列表项通常使用 OR。

## 本页面的规则

- `items`
- `material`
- `material-tag`
- `rarity`
- `has-name`
- `contains-name`
- `has-lore`
- `contains-lore`

## `items`

匹配已注册物品提供器挂钩能够识别的物品 ID。

```yaml
match-item:
  items:
    - namespace:custom_sword
    - another_provider_item
```

可接受的 ID 语法取决于提供器集成。应优先使用稳定的提供器命名空间和内部 ID，而不是匹配显示文本。

提供器插件或挂钩不可用时，无法可靠解析该 ID。请检查启动日志和兼容性页面。

## `material`

匹配 Bukkit 材质或带命名空间的材质标识符。

```yaml
match-item:
  material:
    - DIAMOND_SWORD
    - NETHERITE_SWORD
```

请使用目标 Minecraft 版本支持的材质枚举名称。被新版 API 移除或重命名的材质不会匹配。

根据解析器行为，可能同时接受单个值和列表；允许多个材质时，使用列表更清晰。

## `material-tag`

匹配 Minecraft 或物品标签。

```yaml
match-item:
  material-tag:
    - minecraft:swords
    - minecraft:axes
```

标签通常比很长的材质列表更合适，因为它们会跟随服务端注册表，并且在支持时可以包含模组或插件定义的标签内容。

常见原版标签示例包括：

- `minecraft:swords`
- `minecraft:axes`
- `minecraft:pickaxes`
- `minecraft:shovels`
- `minecraft:hoes`
- `minecraft:damageable`

请根据目标服务端版本确认标签名称。EnchantmentReform 在附魔注册中使用的支持物品标签与此有关，但需要在 `config.yml -> supported-items.tags` 中单独配置。

## 组合材质和标签

同一层级的规则使用 AND，因此下方配置通常无法匹配，除非物品同时满足两项要求：

```yaml
match-item:
  material:
    - DIAMOND_SWORD
  material-tag:
    - minecraft:pickaxes
```

任意一组通过即可时，请使用 `any`：

```yaml
match-item:
  any:
    sword:
      material-tag:
        - minecraft:swords
    special-pickaxe:
      material:
        - DIAMOND_PICKAXE
```

## `rarity`

在服务端 API 支持时匹配现代物品稀有度。

```yaml
match-item:
  rarity:
    - RARE
    - EPIC
```

此规则需要服务端版本支持物品稀有度。不要将物品的客户端稀有度或组件，与自定义附魔配置中的 `rarity` 字段混淆。

当前服务端无法使用此规则时，它可能不会被注册。未知的 MatchItemFormat 键会被忽略，因此必须在日志中确认可选规则已经注册。

## `has-name`

要求或排除自定义显示名称。

```yaml
match-item:
  has-name: true
```

```yaml
match-item:
  has-name: false
```

原版物品经过翻译后的名称不一定属于自定义名称。此规则会按照匹配器的实现检查物品元数据或组件状态。

## `contains-name`

检查显示名称或自定义名称是否包含配置文本。

```yaml
match-item:
  contains-name:
    - Legendary
    - 传说
```

列表项通常使用 OR。文本比较可能会经过共享匹配器实现的颜色或组件规范化，但不要认为格式代码一定会被移除。

名称匹配不如提供器 ID 或持久元数据稳定，因为玩家和其他插件可以重命名物品。

## `has-lore`

要求或排除 Lore。

```yaml
match-item:
  has-lore: true
```

根据服务端 API 和物品序列化方式，空 Lore 组件与完全没有 Lore 的行为可能不同。

## `contains-lore`

检查一行或多行 Lore 是否包含配置文本。

```yaml
match-item:
  contains-lore:
    - Soulbound
    - Cannot be traded
```

只有不存在更稳定的提供器 ID、PDC 或组件规则、附魔规则时，才建议用此规则匹配便于人类阅读的标记。

## 完整示例

### 带名称的剑

```yaml
match-item:
  material-tag:
    - minecraft:swords
  has-name: true
  contains-name:
    - Hunter
```

该物品必须是剑、具有自定义名称，并且名称中包含 `Hunter`。

### 提供器物品或原版后备项

```yaml
match-item:
  any:
    provider:
      items:
        - namespace:miner_pickaxe
    vanilla:
      material:
        - NETHERITE_PICKAXE
      contains-lore:
        - Mining Tool
```

### 排除已禁用物品

```yaml
match-item:
  material-tag:
    - minecraft:damageable
  not:
    contains-lore:
      - Disabled
```

## 匹配翻译文本或组件文本

现代 Minecraft 会将名称和 Lore 保存为组件。插件可能使用 MiniMessage、旧版颜色代码、翻译组件或原始 JSON 组件。为了实现可靠的自动处理，建议按照以下顺序选择匹配依据：

1. 优先使用提供器或物品 ID；
2. 其次使用材质或标签；
3. 再使用附魔或稳定的组件、PDC 数据；
4. 最后才使用名称或 Lore 文本。

## 常见错误

- 认为同一层级的不同规则键会像列表项一样使用 OR；
- 使用目标服务端版本中无效的材质或标签；
- 使用物品稀有度规则匹配自定义附魔的稀有度；
- 在没有对应挂钩时依赖提供器 ID；
- 将原版翻译名称视为自定义名称；
- 未确认规范化行为就匹配带颜色格式的文本；
- 认为未知的可选键会导致匹配失败——未注册时它们会被忽略。

`any`、`not` 和可选 NBT 规则请参阅[逻辑与 NBT 规则](logic-nbt.md)，附魔相关匹配请参阅[附魔与 ItemFormat 规则](enchantments-format.md)。

# 🧠 逻辑与 NBT 规则

本页面介绍 MatchItemFormat 的逻辑组合方式，以及可选的旧版 NBT 匹配规则。

## 本页面的规则

- `none`
- `any`
- `not`
- `contains-nbt`
- `nbt-string`
- `nbt-byte`
- `nbt-int`
- `nbt-double`

只有 EnchantmentReform 启用时 NBTAPI 已经可用，NBT 规则才会被注册。

## 默认 AND 行为

同一层级中，每一条可识别规则都必须通过：

```yaml
match-item:
  material-tag:
    - minecraft:swords
  has-name: true
  contains-name:
    - Legendary
```

该物品必须同时满足这三个规则组。

## `none`

启用时强制整个 MatchItemFormat 配置部分匹配失败。

```yaml
match-item:
  none: true
```

它适合作为明确禁用的占位符，或由程序生成的配置值。可以时，直接移除不再使用的可选筛选器通常更加清晰。

## `any`

至少一个嵌套组匹配时通过。

```yaml
match-item:
  any:
    sword:
      material-tag:
        - minecraft:swords
    axe:
      material-tag:
        - minecraft:axes
```

`any` 下的每个子项都是一个完整的 MatchItemFormat 组。一个子项内部的规则仍然使用 AND。

### 混合提供器物品与原版物品候选项

```yaml
match-item:
  any:
    provider:
      items:
        - namespace:custom_pickaxe
    vanilla:
      material:
        - NETHERITE_PICKAXE
      has-enchants:
        - minecraft:efficiency
```

提供器物品，或带有效率附魔的下界合金镐，都可以通过此匹配。

## `not`

排除一个嵌套匹配结果。

```yaml
match-item:
  material-tag:
    - minecraft:damageable
  not:
    contains-lore:
      - Disabled
```

该物品必须可损坏，并且 Lore 中不能包含 `Disabled`。

### 排除多个候选项

```yaml
match-item:
  not:
    any:
      cursed:
        has-enchants:
          - minecraft:binding_curse
      blocked-name:
        contains-name:
          - Admin Item
```

## 嵌套逻辑

以下表达式表示：

```text
(sword OR axe) AND NOT disabled
```

```yaml
match-item:
  any:
    sword:
      material-tag:
        - minecraft:swords
    axe:
      material-tag:
        - minecraft:axes
  not:
    contains-lore:
      - Disabled
```

尽可能减少嵌套层级。过深的逻辑更难调试，并且通常可以改用可复用的物品 ID 或标签简化。

## 可选 NBT 规则

现代 Minecraft 的物品数据越来越多地使用数据组件，而不是旧版任意 NBT。下方规则通过 NBTAPI 提供兼容支持，不应作为新配置的首选方案。

{% hint style="warning" %}
如果 EnchantmentReform 注册匹配规则时没有加载 NBTAPI，这些键会成为未知键并被忽略。可选键被忽略后，匹配范围可能比预期更广。依赖 NBT 规则前，请检查启动日志。
{% endhint %}

## `contains-nbt`

检查物品是否包含配置的 NBT 结构，或路径和值的组合。

```yaml
match-item:
  contains-nbt:
    custom-key: custom-value
```

具体序列化语法取决于由 NBTAPI 实现的匹配器。建议从已经能够工作的内置示例或提供器示例开始。

## `nbt-string`

检查字符串类型的 NBT 值。

```yaml
match-item:
  nbt-string:
    path: custom.id
    value: example_sword
```

底层 NBT 区分大小写时，路径和字段名称也会区分大小写。

## `nbt-byte`

检查字节值，通常用于类似布尔标记的数据。

```yaml
match-item:
  nbt-byte:
    path: custom.bound
    value: 1
```

不要认为现代版本中的每个布尔组件都会表示为旧版字节值。

## `nbt-int`

检查整数值。

```yaml
match-item:
  nbt-int:
    path: custom.tier
    value: 3
```

只有保存的标签类型确实为整数时，才应使用整数规则。不同数值 NBT 类型不一定会自动转换。

## `nbt-double`

检查双精度浮点数值。

```yaml
match-item:
  nbt-double:
    path: custom.power
    value: 1.5
```

精确比较浮点数可能并不稳定。可以时，应优先使用稳定的整数等级或提供器 ID。

## NBT、PDC 与提供器 ID

建议按照以下优先级选择物品标识方式：

1. 已注册的自定义物品提供器 ID；
2. 命名空间附魔键；
3. 稳定的材质、标签和组件元数据；
4. 支持时使用由插件自己管理的 PersistentDataContainer 或组件规则；
5. 只有确实必要时才匹配旧版 NBT。

提供器插件可能会在不同版本之间重写内部 NBT，同时保持公开的物品 ID 不变。

## 调试逻辑

1. 将每个嵌套组单独作为完整的 `match-item` 配置部分进行测试。
2. 确认每个规则键都已经注册。
3. 暂时使用材质或提供器 ID 检查替换可选 NBT 检查。
4. 使用可信的检查工具确认实际 NBT 类型、路径和值。
5. 只有正向规则正常工作后，才添加 `not`。
6. 记录或显示触发器实际提供的物品；延迟事件或装备事件提供的物品可能与预期不同。

## 常见错误

- 认为顶层规则使用 OR；
- 没有正确建立子组，就将多个独立规则键放在 `any` 下；
- 意外反转了比预期更大的配置组；
- NBTAPI 没有注册时仍依赖 NBT 规则；
- 使用 `nbt-double` 比较整数标签，或反过来使用整数规则比较双精度值；
- 匹配提供器私有 NBT，而不是公开的物品 ID；
- 认为未知键会使匹配失败。

另请参阅[基础与元数据规则](basic-metadata.md)和[附魔与 ItemFormat 规则](enchantments-format.md)。

# ✨ 原生附魔效果

根节点的 `effects` 部分用于定义 **Minecraft 原生附魔效果组件**。这些效果会作为附魔本身的一部分注册，并由 Minecraft 的常规附魔引擎处理。

对于原版附魔系统已经支持的机制，例如属性修改器、原生伤害调整、伤害免疫、攻击后效果或基于位置的方块效果，应使用 `effects`。需要 EnchantmentReform 的能力触发器、能力条件、能力修改器、能力、随机概率、冷却或状态时，应使用 [`powers`](powers/README.md)。

一个附魔可以同时使用这两个系统。

{% hint style="warning" %}
`effects` 属于注册表数据。修改后必须**完整重启服务器**。`/enchantmentreform reload` 无法重建已经冻结的附魔注册表条目。
{% endhint %}

## `effects` 与 `powers` 对比

| 功能 | `effects` | `powers` |
| --- | --- | --- |
| 执行引擎 | Minecraft 原生附魔引擎 | EnchantmentReform 运行时能力引擎 |
| 语法 | 使用 YAML 编写的原版附魔效果组件数据 | 触发器 → 条件 → 修改器 → 能力 |
| 等级缩放 | `minecraft:linear` 等原版等级值结构 | `{level}`、变量、选择器和数学表达式 |
| 条件 | `requirements` 中的原版战利品条件谓词 | 能力条件 |
| 概率与冷却 | 仅在对应原生组件或效果结构支持时可用 | 通用的 `random`、`cooldown` 和 `times` 字段 |
| 重载行为 | 必须完整重启 | 在重载功能支持的范围内，可以重新读取运行时部分 |

## 基础语法

```yaml
effects:
  minecraft:<effect_component>:
    <value required by that Minecraft component>
```

`effects` 正下方的键是 Minecraft 附魔效果组件键。对应值的结构取决于该组件，可能是列表、对象、数字或其他原生数据结构。

EnchantmentReform 不会为这一部分再设计一层额外结构。插件会将 YAML 映射和列表转换为原生数据，然后传递给当前服务端版本的 `EnchantmentEffectComponents` Codec。

顶层组件键未填写命名空间时，会自动按照 `minecraft:<key>` 处理，但仍建议明确填写完整的命名空间键：

```yaml
effects:
  minecraft:attributes: []
```

嵌套标识符应始终填写完整命名空间，例如 `minecraft:max_health` 或 `enchantmentreform:my_modifier`。

## 按 Minecraft 游戏版本选择效果

自定义附魔和原版附魔覆盖都可以使用 `effects-by-version`，为不同游戏版本提供不同的原生效果格式：

```yaml
# 旧版本的默认效果。
effects:
  minecraft:damage_immunity:
    - effect: {}
      requirements:
        condition: minecraft:damage_source_properties
        predicate:
          tags:
            - id: minecraft:burn_from_stepping
              expected: true

effects-by-version:
  - min-version: '26.3'
    effects:
      minecraft:damage_immunity:
        - effect: {}
          requirements:
            type: minecraft:damage_source_properties
            predicate:
              tags:
                - id: '#minecraft:burn_from_stepping'
                  expected: true
```

按照列表顺序选择**第一个匹配项，完整替换默认 `effects` 映射**，不会合并组件。只有选中的效果会传入服务端原生 Codec；所有分支的版本选择条件都会进行格式检查。

| 字段 | 说明 |
| --- | --- |
| `versions` | 单个带引号的版本或版本列表，例如 `'26.3'`、`['1.21.11', '26.2']`；列表中任一版本匹配即可。 |
| `min-version` | 最低版本，包含边界。 |
| `max-version` | 最高版本，包含边界。 |
| `effects` | 该版本的完整原生效果映射，必须填写；`{}` 可以清空所有原生效果组件。 |

每项至少填写一个版本条件，多个字段组合时必须全部满足。版本按数值逐段比较：`1.21.10` 大于 `1.21.9`，`26.3` 与 `26.3.0` 相等。配置与服务端版本均须为两段或三段正式版本号，配置值必须加引号，避免 YAML 将其解析成数字。

没有匹配项时使用根节点的 `effects`；如果根节点也没有填写，则视为未配置原生效果（原版附魔覆盖会保留原版效果）。匹配到空映射仍属于显式替换。Paper 的引导注册阶段和 Spigot 均支持版本选择，修改后必须完整重启服务器。

[Minecraft 26.3 的格式变更](https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3)包括谓词 `condition` 改为 `type`，方块状态 `Name` 改为 `id`、`Properties` 改为 `properties`，以及提供器 `minecraft:simple_state_provider` 改为 `minecraft:simple`。插件在选择分支后默认执行下述已知格式迁移；未覆盖的格式差异仍需在分支中明确配置。

26.3 的伤害来源谓词中，`tags[].id` 的标签引用必须使用带引号的 `'#minecraft:标签名'`，例如 `'#minecraft:burn_from_stepping'`。不带 `#` 的值表示具体伤害类型 ID；将标签名写成类型 ID 可能导致注册表冻结时出现 `Unbound values`。旧版本默认分支仍使用原来的标签格式。

## 自动迁移旧版原生效果

默认启用加载时迁移。插件先选择 `effects-by-version`（未匹配则选择根节点 `effects`），再根据当前游戏版本对选中的数据副本执行迁移，自定义附魔和原版附魔覆盖均适用。迁移结果供注册和运行时读取使用，原始 YAML 文件不被改写。

当服务端为 26.3 或更新正式版本时，已支持：

* 将 `requirements` 及其 `terms` / `term` 中的旧谓词类型字段 `condition` 转换为 `type`。
* 将隐式谓词列表转换为显式 `minecraft:all_of`。
* 将方块状态中的 `Name` / `Properties` 转换为 `id` / `properties`。
* 转换已知提供器名称：`dual_noise_provider`、`noise_provider`、`noise_threshold_provider`、`randomized_int_state_provider`、`rule_based_state_provider`、`simple_state_provider`、`weighted_state_provider`。
* 为旧 `condition: minecraft:damage_source_properties` 中的伤害标签 ID 添加 `#`，保留自定义命名空间和 `expected`。
* 修复已经改为 `type`，但仍将 `burn_from_stepping` 或 `bypasses_invulnerability` 写为普通 ID 的混合格式配置。

已经符合新格式的数据保持相同含义，迁移可重复执行。现代伤害谓词中的其他普通 ID 会保留为具体伤害类型，不会猜测自定义 ID 是否代表标签。NBT 和组件负载不参与字段转换。26.3 之前的服务端保留原格式，不执行反向迁移。

可在单个附魔配置的根节点关闭：

```yaml
auto-migrate-effects: false
```

这是已知格式的兼容转换，不涵盖所有 Minecraft 数据格式变更；例如噪声提供器仅转换类型名称，不重建其参数结构。其他差异仍可使用 `effects-by-version`。同一方块状态的旧、新字段内容冲突时，会报告配置错误，不会静默选择其中一个。修改配置或更新迁移代码后必须完整重启服务端。

## 将原版 JSON 转换为 YAML

原版附魔定义可能包含类似下面的 JSON：

```json
{
  "effects": {
    "minecraft:attributes": [
      {
        "id": "enchantmentreform:example_health",
        "attribute": "minecraft:max_health",
        "amount": {
          "type": "minecraft:linear",
          "base": 2.0,
          "per_level_above_first": 1.0
        },
        "operation": "add_value"
      }
    ]
  }
}
```

在 YAML 中写入相同结构，并移除最外层 JSON 对象：

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

转换规则如下：

* JSON 对象转换为 YAML 节点；
* JSON 数组转换为以 `-` 开头的 YAML 列表；
* 字符串、布尔值和数字保持原值；
* 空 JSON 对象转换为 `{}`；
* 带命名空间的标识符应保持不变。

## 属性效果示例

下面的附魔在 I 级时提供 `+2` 最大生命值，并且每高于 I 级一级再增加 1 点：

```yaml
active-slots:
  - ARMOR

max-level: 5

effects:
  minecraft:attributes:
    - id: enchantmentreform:vitality
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 2.0
        per_level_above_first: 1.0
      operation: add_value
```

### 属性字段

| 字段 | 用途 |
| --- | --- |
| `id` | 原生属性修改器的稳定命名空间标识符。不要为互不相关的效果重复使用同一个 ID。 |
| `attribute` | Minecraft 属性注册表键。 |
| `amount` | 原生的随等级变化数值。 |
| `operation` | 原生属性运算方式。当前服务端版本支持时，可使用 `add_value`、`add_multiplied_base` 或 `add_multiplied_total` 等值。 |

附魔定义中的 `active-slots` 字段决定物品必须装备在哪些槽位，依赖装备的原生效果才会生效。

## 原生等级缩放

插件变量和数学占位符**不会在 `effects` 内展开**。不要这样填写：

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:wrong_example
      attribute: minecraft:max_health
      amount: '{level} * 2' # Not a Power expression here.
      operation: add_value
```

应改用 Minecraft 原生的等级值结构：

```yaml
amount:
  type: minecraft:linear
  base: 2.0
  per_level_above_first: 2.0
```

对于这个线性数值：

* I 级 = `base`；
* II 级 = `base + per_level_above_first`；
* III 级 = `base + per_level_above_first × 2`。

当前服务端版本的 Codec 可能还接受其他原生等级值类型。类型名称和字段必须与对应 Minecraft 版本的原版格式一致。

## 带条件的原生效果示例

许多原生效果组件使用包含 `effect` 和可选原版战利品条件 `requirements` 的条目。

下面的示例会阻止踩在燃烧方块上造成的伤害，但不会阻止带有“绕过无敌”标签的伤害来源：

```yaml
effects:
  minecraft:damage_immunity:
    - effect: {}
      requirements:
        condition: minecraft:damage_source_properties
        predicate:
          tags:
            - expected: true
              id: minecraft:burn_from_stepping
            - expected: false
              id: minecraft:bypasses_invulnerability
```

`requirements` 使用 Minecraft 原生战利品条件和谓词语法。它不是能力条件部分，因此不能在其中使用 `type: health_percent` 等字段。

并非每个效果组件都使用 `{ effect, requirements }` 包装结构。例如，`minecraft:attributes` 会直接使用属性条目。必须始终遵循所选组件的原版结构。

## 位置效果示例

下面的原生位置效果会将穿戴者脚下附近的熔岩替换为岩浆块：

```yaml
active-slots:
  - FEET

effects:
  minecraft:location_changed:
    - effect:
        type: minecraft:replace_disk
        block_state:
          type: minecraft:simple_state_provider
          state:
            Name: minecraft:magma_block
        height: 1.0
        offset:
          - 0
          - -1
          - 0
        predicate:
          type: minecraft:all_of
          predicates:
            - type: minecraft:matching_block_tag
              offset:
                - 0
                - 1
                - 0
              tag: minecraft:air
            - type: minecraft:matching_blocks
              blocks: minecraft:lava
            - type: minecraft:matching_fluids
              fluids: minecraft:lava
        radius:
          type: minecraft:linear
          base: 3.0
          per_level_above_first: 1.0
        trigger_game_event: minecraft:block_place
      requirements:
        condition: minecraft:entity_properties
        entity: this
        predicate:
          flags:
            is_on_ground: true
```

`offset` 必须是按照 X、Y、Z 顺序排列的三个数字坐标列表。

## 常见原生组件分类

实际可用的组件键和嵌套字段由当前 Minecraft 服务端版本决定。常见分类包括：

| 组件分类 | 常见用途 |
| --- | --- |
| `minecraft:attributes` | 附魔激活时添加原生属性修改器。 |
| `minecraft:damage` / `minecraft:damage_protection` | 修改造成伤害或保护计算。 |
| `minecraft:damage_immunity` | 使符合条件的伤害来源无法造成伤害。 |
| `minecraft:item_damage` | 修改耐久消耗。 |
| `minecraft:post_attack` | 攻击后执行原生实体效果。 |
| `minecraft:tick` | 附魔激活期间持续执行原生实体效果。 |
| `minecraft:location_changed` | 移动或位置更新后执行原生位置效果。 |
| 与弹射物相关的组件 | 修改弹射物数量、散布、弹药消耗、蓄力时间或其他相关行为。 |

此表并非完整的结构列表。只要当前服务端的原生附魔效果 Codec 接受，EnchantmentReform 就可以接受对应数据。

## 同时使用 `effects` 和 `powers`

原生属性效果可以与插件驱动的能力组合使用：

```yaml
max-level: 3
active-slots:
  - HAND

variables:
  kill-heal: '1 + {level}'

effects:
  minecraft:attributes:
    - id: enchantmentreform:hybrid_attack_speed
      attribute: minecraft:attack_speed
      amount:
        type: minecraft:linear
        base: 0.1
        per_level_above_first: 0.05
      operation: add_value

powers:
  on-kill:
    abilities:
      heal:
        type: set_health
        target: SOURCE
        amount: '{health} + {kill-heal}'
```

原生属性由 `effects` 注册，击杀后的行为则由 `powers` 独立处理。

## 验证与常见错误

### 原生结构无效

如果组件键、效果类型、谓词、注册表键或必填字段无效，服务器会在启动阶段失败，并显示类似下面的错误：

```text
example.yml: invalid native enchantment effects: ...
```

继续阅读 Codec 错误的剩余部分，其中通常会指出无效的字段或值。

### 在原生效果中使用插件语法

以下系统不会在 `effects` 内生效：

* 能力条件；
* 能力修改器；
* 能力；
* 根节点 `variables` 和 `{level}` 数学表达式；
* `random`、`cooldown` 和 `times` 等通用能力字段。

需要这些功能时应使用 `powers`。

### 从其他 Minecraft 版本复制数据

不同 Minecraft 版本之间的原生效果组件结构可能发生变化。应从相同服务端版本的原版附魔数据或示例中复制定义。

### 缩进错误

每个组件都必须直接放在根节点 `effects` 下：

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:example
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 1.0
        per_level_above_first: 1.0
      operation: add_value
```

不要把原生效果放在 `powers`、某个触发器或 `abilities` 下。

## 内置示例

默认配置在以下位置提供了实用的原生效果示例：

```text
plugins/EnchantmentReform/enchantments/armor/vitality.yml
plugins/EnchantmentReform/enchantments/armor/jump_boost.yml
plugins/EnchantmentReform/enchantments/tools/entity_reach.yml
plugins/EnchantmentReform/enchantments/armor/lava_walker.yml
```

可以将这些文件作为模板，然后把组件专用字段替换为适用于当前服务端版本的值。

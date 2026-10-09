# 🔗 兼容性

EnchantmentReform 只会在对应插件存在时加载可选集成。缺少可选插件不会阻止核心插件启动。

## 自定义物品提供插件

内置挂钩可以解析以下插件中的物品：

* ItemsAdder
* Oraxen
* MMOItems
* EcoItems 和 EcoArmor
* eco
* NeigeItems
* ExecutableItems
* Nexo
* CraftEngine
* MythicMobs 物品

ItemFormat、MatchItemFormat、物品费用、物品替换或修改能力、掉落物，以及配置中的提供插件物品 ID 都会使用这些挂钩。

## 经济插件

安装对应插件后，`cost_price` 等依赖经济的能力可以使用受支持的经济插件，包括：

* 兼容 Vault 的经济插件
* PlayerPoints
* ExcellentEconomy
* UltraEconomy
* EcoBits
* PEconomy
* RedisEconomy
* RoyaleEconomy
* VotingPlugin。

在受支持的功能中，可以使用 `enchantmentreform.bypass.economy` 绕过配置的经济费用。

## 保护插件

除非玩家拥有 `enchantmentreform.bypass.protection` 权限，否则修改方块和破坏方块的能力会检查受支持的保护插件挂钩。

已声明的集成包括：

* WorldGuard
* Residence
* GriefPrevention
* Lands
* HuskTowns
* HuskClaims
* PlotSquared
* Towny
* BentoBox
* Dominion
* SuperiorSkyblock2。

## MythicMobs

安装 MythicMobs 后，EnchantmentReform 可以：

* 解析 MythicMobs 物品；
* 在受支持的位置使用兼容 MythicMobs 的实体匹配；
* 通过 `mythic_skill` 能力执行配置的 MythicMobs 技能；
* 通过受支持的召唤选项召唤配置的 MythicMobs 实体。

## PlaceholderAPI

在存在玩家上下文时，受支持的文本和数值表达式可以使用 PlaceholderAPI 变量。EnchantmentReform 的正常运行不强制依赖 PlaceholderAPI。

## NBTAPI

NBTAPI 用于支持旧版、面向 NBT 的 MatchItemFormat 规则。在当前 Minecraft 版本中，应尽可能优先使用现代物品组件和普通的 ItemFormat 匹配。

## PacketEvents 和基于 NMS 的功能

模拟物品使用、钓鱼交互、客户端效果或其他与版本相关行为的功能会使用 PacketEvents 和特定版本的 NMS 桥接。如果某项功能所需的能力不可用，它应当安全失效，而不是导致其他无关附魔无法工作。

### 内置附魔介绍显示

安装 PacketEvents 后，EnchantmentReform 可以在不修改服务端真实物品的情况下，为客户端物品提示添加附魔介绍。它会转换发出的窗口物品、单槽位、光标物品和村民交易数据包。为避免客户端背包同步问题，创造模式数据包不会被修改。

```yaml
enchantment-description:
  wrap-length: 30
  item-display:
    enabled: true
    force-enabled: false
    lore-prefix: "§y"
    remove-lore-first: true
    black-creative: true
    black-item: []
    packet-listener-priority: LOWEST
    at-first-or-last: false
    display-value:
      - '{enchants}'
    placeholder:
      auto-parse: true
      enchants:
        format: '&a{enchant_name}{enchant_level_roman}'
        auto-add-space: true
        level-hide-one: true
        description:
          format: '&7  {enchant_description}'
```

布局沿用 EnchantmentSlots 的物品 Lore 配置方式，但不包含任何槽位字段。`{enchants}` 会为每个受支持附魔展开一个格式化区块。附魔格式支持 `{enchant_name}`、`{enchant_raw_name}`、`{enchant_level}`、`{enchant_level_roman}` 和 `{enchant_description}`。附魔配置了介绍时一定显示，不提供介绍开关、权限、玩家切换命令、局部 `sort` 选项或可自定义的 `enchant-level` 映射；数字等级和罗马数字由代码直接生成。

检测到 EnchantmentSlots 时，Item Display 默认会停用，因为两个功能都可能添加附魔 Lore。EnchantmentReform 只会在控制台提示一次；只有明确需要同时显示时，才将 `force-enabled: true`。

Item Display 始终按照 EnchantmentReform 的稀有度、权重和 NamespacedKey 比较器排列受支持附魔。`lore-prefix` 标记客户端生成的 Lore，`remove-lore-first` 据此避免数据包被重复转换。`{enchants}` 生成内容后，客户端副本会隐藏原版附魔提示。`black-item` 支持普通 MatchItemFormat 规则，`at-first-or-last: true` 会把配置区块放到原 Lore 前方。重载配置会更新显示设置和数据包监听优先级。如果 MythicChanger 也通过规则执行 Fake Change，请关闭其中一种显示途径，避免重复介绍。

## MythicChanger

安装 MythicChanger 后，EnchantmentReform 会注册 `er-enchantment-description` 物品修改规则。它会根据物品上的实际附魔等级，添加 ER 中配置的本地化附魔名称、等级和等级相关介绍。

使用默认布局：

```yaml
changes:
  er-enchantment-description: true
```

也可以自定义生成的 Lore：

```yaml
changes:
  er-enchantment-description:
    position: LAST
    format:
      - '{name} {level-roman}'
      - '&7{description}'
    separator:
      - ''
    wrap-length: 30
```

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `position` | `LAST` | 使用 `FIRST` 或 `LAST`，将生成区块放在原 Lore 的开头或末尾。 |
| `format` | 如上方示例 | 每个受支持附魔生成的 Lore 行。 |
| `separator` | 一个空行 | 插入两个附魔区块之间的 Lore；使用 `separator: []` 可取消分隔行。 |
| `wrap-length` | `enchantment-description.wrap-length` | 每行介绍的最大可见字符数；小于或等于 `0` 时不自动换行。 |

`format` 支持 `{key}`、`{name}`、`{level}`、`{level-roman}` 和 `{description}`。包含 `{description}` 的格式行会为自动换行后的每行介绍重复生成。普通 MythicChanger 物品占位符和 PlaceholderAPI 占位符仍然可用。

规则会读取普通物品附魔，以及附魔书中保存的附魔。只有在 ER 中启用且配置了介绍的附魔才会显示，排列顺序沿用 ER 的 `tooltip-order` 设置。MythicChanger 会按照 YAML 顺序执行修改，因此需要介绍反映附魔修改结果时，应将本规则放在修改附魔的规则之后。

本规则与 EnchantmentReform 内置 Item Display 共用附魔解析、本地化、换行、附魔书处理和排序逻辑，但分别使用各自的布局配置。

Fake change 只向展示副本添加介绍，不会写入追踪数据。Real change 会先替换本规则上次生成的区块，再写入当前结果，因此重复执行不会累积介绍。在 real change 中将规则设为 `false`，可以移除之前由本规则生成的区块。

## EnchantmentSlots

安装 EnchantmentSlots 后，EnchantmentReform 可以提供附魔信息，并为兼容的 Lore 显示使用配置的稀有度和提示文本排序。

{% hint style="info" %}
声明软依赖只会控制插件加载顺序。请始终检查启动日志，确认你需要的具体挂钩和功能是否已成功注册。
{% endhint %}

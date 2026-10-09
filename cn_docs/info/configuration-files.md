# 🛠️ 配置文件

EnchantmentReform 的运行时文件存放在 `plugins/EnchantmentReform/` 中。

## 主要文件和目录

| 路径 | 用途 | 重载还是重启？ |
| --- | --- | --- |
| `config.yml` | 语言、数学计算、物品插件挂钩、附魔能力值覆盖、稀有度默认值、适用物品标签和能力安全设置。 | 取决于具体字段；参与注册表注册的值需要重启。 |
| `enchantments/` | 自定义附魔定义。支持使用子目录。 | 修改注册表相关内容后需要完整重启。 |
| `enchantments/index.txt` | 复制默认附魔时使用的内置文件索引。 | 修改附魔文件集合后需要完整重启。 |
| `vanilla_enchantments/` | 覆盖指定的原版附魔。 | 完整重启。 |
| `languages/` | 本地化的插件文本和附魔文本。 | 通常运行时重载即可。 |
| `menus/` | 每种 GUI 使用一个独立文件，公共菜单设置位于 `settings.yml`。 | 通常运行时重载即可。 |

插件更新时不会覆盖已有文件。请将自定义文件与新版 JAR 中包含的资源进行比较。

## 重要的 `config.yml` 配置节

### `config-files`

```yaml
config-files:
  language: en_US
  per-player-language: true
  force-parse-mini-message: false
```

控制默认语言、是否根据玩家客户端语言选择语言文件，以及 MiniMessage 的解析行为。

### `math`

```yaml
math:
  enabled: true
  enable-function: true
```

启用附魔能力中使用的等级表达式、范围、变量和已注册的数学函数。

### `hook-item-method`

```yaml
hook-item-method: DEFAULT
```

选择物品提供插件的解析策略。除非已安装的集成明确要求使用其他模式，否则请保持为 `DEFAULT`。

### `item-enchantability-overrides`

```yaml
item-enchantability-overrides:
  enabled: true
  ignore-equipment-when-open-other-invenotry: true
  materials:
    SHIELD: 10
```

覆盖进入玩家背包的匹配物品所具有的 `minecraft:enchantable` 组件。配置值必须为正整数。

### `enchant-randomly-overrides`

```yaml
enchant-randomly-overrides:
  enabled: true
  minimum-cost: 10
  maximum-cost: 30
```

将 `minecraft:enchant_randomly` 的行为替换为 `minecraft:enchant_with_levels` 使用的基于等级的附魔算法。这里的数值表示附魔费用或附魔强度，而不是最终生成的附魔等级。

最终构建的插件分别包含适用于 Minecraft `1.21.11` ABI 和 `26.1+` ABI 的命名 NMS Advice 模块。启动时，EnchantmentReform 会检查 `EnchantRandomlyFunction` 是否包含 `includeAdditionalCostComponent`，只加载匹配的 Advice 类，并在安装 Byte Buddy 前执行该模块完整的 ABI 探测。如果两个 ABI 都不匹配，只会禁用此项可选覆盖功能。

### `anvil`

```yaml
anvil:
  bypass-enchantment-level-limit: false
```

在 Paper 上，将 `bypass-enchantment-level-limit` 设置为 `true` 后，铁砧可以应用超过 `Enchantment#getMaxLevel()` 的附魔等级。这包括保留附魔书中的超等级附魔，以及合并两个等级相同的超等级附魔。此选项不会修改铁砧消耗的经验等级；如需修改费用，请使用 `modify_repair_cost` 激活能力。

### `tooltip-order`

控制原版附魔提示文本的排序，以及兼容的 EnchantmentSlots Lore 排序。不同稀有度按照 `rarity-sort-rule` 排序；同一稀有度中的附魔按照权重和键排序。

### `rarity`

每个稀有度都可以提供以下默认值：

- 显示名称和颜色前缀、后缀；
- 选取权重；
- 铁砧费用；
- 最低和最高附魔费用公式；
- 是否为诅咒；
- 此稀有度对应的交易行为。

单个附魔文件可以在本地覆盖受支持的注册表字段。

### `supported-items`

`tags` 用于定义可复用的物品标签，例如剑、工具、弓、盾牌、各个盔甲槽位、钓鱼竿、三叉戟和长矛。`filters` 用于定义根据这些标签构建的附魔图鉴 GUI 筛选器。

Paper 可以在启动阶段注册配置中的标签。Spigot 无法直接修改注册表时，会使用安全的兼容解析方式进行显示和匹配。

### `powers`

重要的安全和运行时设置包括：

```yaml
powers:
  active-enchantment-mode: SCAN
  strict-active-slot-check: true
  break-block:
    max-blocks-per-activation: 512
  location-changed-effect:
    auto-remove:
      enabled: true
      duration: 80~120
  temp-block-crack-animation:
    enabled: true
    start-at: 0.5
    update-interval: 5
    view-distance: 64
  cost-price:
    shulker-box-check: false
    check-method: Bukkit
```

- `active-enchantment-mode`：`SCAN` 会在触发能力时检查装备；`CACHE` 使用 Paper 的装备变化事件，并在必要时回退为扫描。
- `strict-active-slot-check`：要求附魔物品位于该附魔配置的有效槽位之一。
- `max-blocks-per-activation`：所有 `break_blocks` 能力共用的方块数量硬限制。
- 临时方块相关设置控制方块恢复和客户端裂纹动画。
- `cost-price` 控制物品或经济费用的匹配行为。

### `menus/`

GUI 配置拆分为 `main.yml`、`enchantment-info.yml`、`attribute-info.yml`、`attribute-detail.yml`、`skill-info.yml`、`skill-detail.yml` 和 `attribute-allocation.yml`；也可以新增自由菜单。GUI 防复制检查等公共设置位于 `settings.yml`。首次创建 `menus` 目录时，会自动迁移旧版 `config.yml` 中的菜单配置。各文件选项、占位符和动作见[菜单文档](../menus/README.md)。

## 注册表安全操作流程

1. 关闭服务器。
2. 编辑附魔、稀有度、物品标签或原版附魔覆盖文件。
3. 检查 YAML 缩进，并确认命名空间键没有重复。
4. 启动服务器并检查启动日志。
5. 使用 `/enchantmentreform menu enchantment-info` 验证注册结果。

不要期望 `/enchantmentreform reload` 能在服务端启动完成后添加、删除或重新注册附魔。

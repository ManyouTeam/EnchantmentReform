# 内置菜单选项

本页按照 `plugins/EnchantmentReform/menus/` 中的文件说明每个选项。物品节点支持 [ItemFormat™](../shared-formats/itemformat-tm.md) 的材质、名称、描述、CustomModelData 等通用物品选项。

表中列出的是菜单代码提供的动态占位符。`{lang:键}` 从玩家当前语言文件读取文本；安装 PlaceholderAPI 后，还可在受支持的文本中使用其占位符。某些占位符仅在指定物品模板中有效，请按各表标题使用。

布局中的每个字符都代表一种内容或按钮。`.` 和空格表示不放置物品；同一个字符可以重复出现。布局不得超过 6 行，每行应为 9 个字符。

## `main.yml`：主菜单

主菜单属于自由菜单，只包含静态入口按钮。

| 选项 | 作用 |
| --- | --- |
| `title` | 背包界面标题。支持语言与颜色占位符。 |
| `layout` | 定义按钮位置及菜单行数。默认的 `S`、`A`、`U`、`E`、`C` 分别是技能、属性、属性加点、附魔图鉴和关闭按钮。 |
| `items.<字符>` | 定义该字符对应的静态按钮。字符必须与 `layout` 中的字符一致。 |
| `items.<字符>.item` | 按钮显示的物品。也可将物品选项直接写在 `items.<字符>` 下。 |
| `items.<字符>.actions` | 点击按钮时依次执行的动作。主菜单默认通过 `open_menu` 打开其他菜单，或通过 `close` 关闭界面。 |
| `actions.<名称>.type` | 动作类型。`open_menu` 还需要 `menu` 指定目标菜单 ID。动作名称仅用于区分同一按钮下的多个动作。 |

### 主菜单及自由菜单通用占位符

这些占位符可用于自由菜单的 `title`、静态物品及动态内容物品：

| 占位符 | 显示内容 |
| --- | --- |
| `{player}` | 正在查看菜单的玩家名称。 |
| `{menu}` | 当前菜单 ID，即 YAML 文件名去掉 `.yml`。 |
| `{page}` | 当前页码，从 1 开始。 |
| `{pages}` | 当前菜单总页数；多个内容区时取页数最多的内容区。 |
| `{points}` | 玩家当前可用的属性点数。 |
| `{attribute_points}` | 同 `{points}`。 |

## `skill-info.yml`：技能总览

| 选项 | 作用 |
| --- | --- |
| `title` | 菜单标题。通常使用 `{page}` 和 `{pages}` 显示页码。 |
| `layout` | `S` 为动态技能槽；其他字符从 `items` 读取静态按钮。动态槽数量决定每页显示的技能数量。 |
| `contents.S.type` | 内容类型。技能总览必须使用 `skills`。 |
| `contents.S.use-skill-icon` | 为 `true` 时优先使用各 `skills/*.yml` 中配置的技能图标；模板仍会覆盖名称和描述。 |
| `contents.S.include` | 可选技能 ID 白名单。留空时包含全部技能。 |
| `contents.S.exclude` | 可选技能 ID 黑名单，在 `include` 之后排除。 |
| `contents.S.item` | 每个技能物品的显示模板。 |
| `contents.S.actions` | 任意点击都执行的动作。 |
| `contents.S.click-actions.<点击类型>` | 只响应指定 Bukkit 点击类型，例如 `LEFT`。它存在时会代替通用 `actions`。 |
| `items` | 翻页、说明、返回等静态按钮。`hide-on-first-page`、`hide-on-last-page` 可在边界页隐藏按钮，`hidden-item` 决定隐藏后显示的占位物品。 |

### 技能物品占位符

以下占位符用于 `contents.S.item`，也继承上面的自由菜单通用占位符。`open_skill` 会打开被点击技能的详情页。

| 占位符 | 显示内容 |
| --- | --- |
| `{id}` | 技能 ID。 |
| `{name}` | 当前玩家语言下的技能名称。 |
| `{description}` | 当前玩家语言下的技能描述。 |
| `{level}` | 玩家当前技能等级。 |
| `{maximum_level}` | 该技能的最高等级。 |
| `{experience}` | 当前等级已积累的经验值。 |
| `{required_experience}` | 升级所需的经验值；已达最高等级时由技能系统决定其值。 |
| `{remaining_experience}` | `required_experience - experience`，最小为 0。 |
| `{progress}` | 当前经验进度百分比数值，不含 `%`；无升级需求时显示 100。 |
| `{progress_bar}` | 20 格的彩色经验进度条。 |
| `{source_count}` | 此技能配置的经验来源数量。 |

## `attribute-info.yml`：属性总览

| 选项 | 作用 |
| --- | --- |
| `title` | 属性总览标题。 |
| `layout` | `A` 为动态属性槽；其他字符对应 `items` 中的静态按钮。 |
| `contents.A.type` | 内容类型，必须使用 `attributes`。 |
| `contents.A.mode` | 属性筛选模式。`information` 只显示启用了 `show-in-attribute-gui` 的属性。 |
| `contents.A.include` / `exclude` | 按属性 ID 设置白名单或黑名单。 |
| `contents.A.item` | 属性物品显示模板。 |
| `contents.A.actions` / `click-actions` | 配置属性物品的通用或指定点击动作。默认左键使用 `open_attribute`。 |
| `items` | 翻页、说明和返回主菜单等静态按钮。 |

### 属性列表物品占位符

这些占位符用于 `attribute-info.yml` 和 `attribute-allocation.yml` 的 `contents.A.item`，也可用于其他自由菜单的属性内容区。

| 占位符 | 显示内容 |
| --- | --- |
| `{id}` | 属性 ID。 |
| `{name}` | 当前玩家语言下的属性名称。 |
| `{description}` | 属性当前效果的本地化描述；同 `{current_description}`。 |
| `{current_description}` | 按玩家当前最终值生成的效果描述。 |
| `{next_description}` | 按下一基础等级预计最终值生成的效果描述。 |
| `{base_value}` | 玩家已解锁的属性基础等级。 |
| `{value}` | 玩家当前最终属性值，包含有效等级及修改器等影响。 |
| `{minimum_value}` | 属性配置的最低值。 |
| `{maximum_value}` | 此列表中表示属性的可加点上限。 |
| `{next_base}` | 下一基础等级，最多为可加点上限。 |
| `{next_value}` | 升至 `{next_base}` 后预计的最终值。 |
| `{next_price}` | 从当前基础等级提升 1 级所需的属性点；同 `{price}`。 |
| `{price}` | 同 `{next_price}`。 |
| `{requirement}` | 下一基础等级的加点条件说明。 |
| `{modifier_count}` | 玩家当前作用于该属性的修改器数量。 |

## `attribute-allocation.yml`：属性加点

此菜单同样使用自由菜单格式，与 `attribute-info.yml` 的主要区别如下：

| 选项 | 作用 |
| --- | --- |
| `contents.A.mode` | 使用 `allocation`，只显示启用了 `allocation.show-in-menu` 的属性。 |
| `contents.A.item` | 通常显示当前等级、升级后的数值、`{next_price}` 与 `{requirement}`。这里只负责显示，不决定真实价格或条件。 |
| `contents.A.actions` / `click-actions` | 默认通过 `open_attribute` 进入详情页后升级；也可使用 `allocate_attribute` 直接加点，并用 `add` 指定增加等级。 |
| `items.I` | 默认显示玩家剩余属性点，支持 `{points}` 或 `{attribute_points}`。 |

真实价格、最大可加点等级及条件来自对应 `attributes/*.yml` 的 `allocation` 节点。

## `enchantment-info.yml`：附魔图鉴

附魔图鉴使用专用格式，不使用 `contents` 和 `items`。

| 选项 | 作用 |
| --- | --- |
| `title` | 图鉴标题，支持 `{page}` 与 `{pages}`。 |
| `size` | 背包格数，默认 `54`。应与布局行数对应，并为 9 的倍数。 |
| `enchantment-symbol` | `layout` 中用于放置附魔条目的字符，默认 `E`。 |
| `rarity-sort-rule` | 稀有度排序顺序。未列出的稀有度排在已列出项之后，再按名称排序。 |
| `layout` | 定义附魔槽和按钮位置。`.` 与空格为空槽，其他字符从 `buttons` 读取。 |
| `enchantment-item` | 每个附魔条目的物品模板。 |
| `buttons.<字符>` | 静态按钮定义，字符与 `layout` 对应。物品选项直接写在此节点下。 |
| `buttons.<字符>.filter-type` | 将按钮设为筛选器。支持 `RARITY` 和 `SUPPORTED_ITEM`；左键切换到下一项，右键切换到上一项。 |
| `hide-on-first-page` / `hide-on-last-page` | 在第一页或最后一页隐藏按钮。 |
| `hidden-item` | 按钮隐藏时显示的替代物品；省略时该格为空。 |
| `actions` | 普通按钮动作，例如 `previous_page`、`next_page`、`refresh` 和 `close`。 |

### 附魔条目占位符

用于 `enchantment-item`。其中 `name` 字段最终只会替换 `{name}` 和 `{key}`；下表其余占位符应放在 `lore` 等物品字段。原版附魔若没有自定义描述，`{description}` 和 `{level-descriptions}` 可能为空。

| 占位符 | 显示内容 |
| --- | --- |
| `{name}` | 附魔显示名称。 |
| `{description}` | 自定义附魔最高等级的本地化描述。 |
| `{level-descriptions}` | 自定义附魔从 1 级到最高等级的逐级描述，按语言文件中的等级描述格式拼接。 |
| `{max_level}` | 附魔最高等级。 |
| `{rarity}` | 本地化后的稀有度名称。 |
| `{weight}` | 附魔出现权重。 |
| `{supported_items}` | 适用物品类别名称；无法解析时可能显示标签键或 `-`。 |
| `{key}` | 附魔的完整键，例如 `minecraft:sharpness`。 |

### 附魔图鉴按钮占位符

用于 `buttons.<字符>`；隐藏按钮的 `hidden-item` 仅会收到 `{page}`、`{pages}`、`{amount}`。

| 占位符 | 显示内容 |
| --- | --- |
| `{page}` | 当前页码，从 1 开始。 |
| `{pages}` | 当前筛选结果的总页数。 |
| `{amount}` | 当前筛选条件下的附魔总数。 |
| `{rarity_filter}` | 当前稀有度筛选项的显示名称，未筛选时为“全部”。 |
| `{supported_item_filter}` | 当前适用物品筛选项的显示名称，未筛选时为“全部”。 |

## `skill-detail.yml`：技能详情

技能详情包含“经验来源”和“等级奖励”两个视图，使用专用格式。

| 选项 | 作用 |
| --- | --- |
| `source-title` / `reward-title` | 两个视图各自的标题。若省略则回退到 `title`。 |
| `size` | 菜单格数，默认 `54`。 |
| `layout` | 经验来源视图布局。 |
| `reward-layout` | 等级奖励视图布局；省略时复用 `layout`。奖励槽会按行蛇形排序，用于绘制成长路线。 |
| `source-symbol` | 经验来源槽字符，默认 `S`。 |
| `reward-symbol` | 等级奖励槽字符，默认 `R`。 |
| `attribute-symbol` | 来源视图中的属性槽字符，默认 `A`。 |
| `back-symbol` | 返回按钮字符，默认 `B`。点击后回到打开详情页的菜单。 |
| `view-symbol` | 来源/奖励视图切换按钮字符，默认 `V`。 |
| `source-item` | 经验来源默认模板。来源自身配置了 `icon` 时使用其材质，但名称与描述仍由此模板格式化。 |
| `attribute-item` | 技能详情顶部属性条目的模板。点击条目会打开属性详情。 |
| `reward-item` | 所有等级奖励的基础名称和描述模板。 |
| `reward-item-claimed` | 已达到等级的奖励材质样式。名称和描述仍取自 `reward-item`。 |
| `reward-item-next` | 下一个待解锁等级的奖励材质样式。 |
| `buttons.<字符>` | 布局中其他静态按钮。 |
| `buttons.<视图字符>.reward-view-item` | 当前位于奖励视图时，视图切换按钮使用的替代外观。 |
| `hide-on-first-page` / `hide-on-last-page` / `hidden-item` | 控制翻页按钮在边界页的外观。 |
| `actions` | 普通按钮动作。返回和切换视图由对应字符自动处理。 |

### 技能详情通用占位符

这些占位符用于标题和静态按钮，也可用于来源、属性和奖励物品。

| 占位符 | 显示内容 |
| --- | --- |
| `{id}` | 当前技能 ID。 |
| `{name}` | 当前玩家语言下的技能名称。 |
| `{description}` | 当前玩家语言下的技能描述。 |
| `{level}` | 玩家当前技能等级。 |
| `{maximum_level}` | 技能最高等级。 |
| `{experience}` | 当前技能经验。 |
| `{required_experience}` | 当前等级升级所需经验。 |
| `{remaining_experience}` | 尚需经验，最小为 0。 |
| `{progress}` | 当前经验进度百分比数值，不含 `%`。 |
| `{progress_bar}` | 20 格的彩色经验进度条。 |
| `{source_count}` | 此技能配置的经验来源数量。 |
| `{view}` | 当前视图：`sources` 或 `rewards`。 |
| `{page}` | 当前视图页码，从 1 开始。 |
| `{pages}` | 当前视图总页数。 |

### 来源物品占位符

用于 `source-item`，同时支持上表中的技能占位符。

| 占位符 | 显示内容 |
| --- | --- |
| `{source_id}` | 经验来源 ID。 |
| `{source_name}` | 来源的本地化名称。 |
| `{source_description}` | 来源的本地化描述。 |
| `{source_xp}` | 来源配置的经验值或经验公式原文，实际获得值可能受条件和倍率影响。 |
| `{source_unit}` | 来源配置的单位；非空时前面自动加一个空格。 |
| `{source_index}` | 来源在列表中的序号，从 1 开始。 |

### 技能详情中的属性物品占位符

用于 `attribute-item`，同时支持技能详情通用占位符。

| 占位符 | 显示内容 |
| --- | --- |
| `{attribute_id}` | 属性 ID。 |
| `{attribute_name}` | 属性的本地化名称。 |
| `{attribute_description}` | 按当前最终值生成的属性效果描述。 |
| `{base_value}` | 玩家已解锁的属性基础等级。 |
| `{value}` | 属性当前最终值。 |
| `{maximum_value}` | 属性可加点上限。 |
| `{price}` | 再升 1 级所需的属性点。 |
| `{next_cost}` | 下一等级的本地化费用提示；达到上限时显示已满级提示。 |
| `{points}` | 玩家当前可用的属性点数。 |
| `{requirement}` | 下一基础等级的加点条件说明。 |

### 等级奖励物品占位符

用于 `reward-item`，同时支持技能详情通用占位符。

| 占位符 | 显示内容 |
| --- | --- |
| `{reward_level}` | 此奖励对应的技能等级。 |
| `{reward_status}` | 本地化状态：已获得、下一级或未解锁。 |
| `{reward_count}` | 此等级配置的奖励条目总数。 |
| `{reward_lines}` | 按配置顺序生成的所有奖励名称行；无奖励时显示对应语言提示。 |
| `{reward_attribute_points}` | 此等级奖励的属性点总数。 |
| `{reward_attributes}` | 此等级属性奖励的格式化摘要。 |
| `{reward_command_count}` | 此等级命令奖励数量。 |
| `{reward_commands}` | 此等级命令奖励的格式化摘要。 |
| `{reward_item_count}` | 此等级物品奖励数量。 |
| `{reward_items}` | 此等级物品奖励的格式化摘要。 |

## `attribute-detail.yml`：属性详情

| 选项 | 作用 |
| --- | --- |
| `title` | 详情标题，通常使用 `{attribute_name}`。 |
| `size` | 菜单格数，默认 `54`。 |
| `layout` | 属性信息、修改器、控制按钮的位置。 |
| `modifier-symbol` | 动态修改器槽字符，默认 `M`。修改器超过槽数时分页。 |
| `back-symbol` | 返回按钮字符，默认 `B`。会返回原技能菜单或属性列表。 |
| `upgrade-symbol` | 升级按钮字符，默认 `U`。 |
| `enable-symbol` | 玩家个人启用/停用属性效果的按钮字符，默认 `E`。 |
| `effective-level-symbol` | 玩家选择已解锁有效等级的按钮字符，默认 `L`。 |
| `upgrade-clicks.<点击类型>.add` | 该点击一次提升的基础等级数量。价格和条件仍按属性配置计算。 |
| `effective-level-clicks.<点击类型>.add` | 在当前有效等级基础上增加或减少指定数值。 |
| `effective-level-clicks.<点击类型>.set-to` | 直接选择 `minimum` 或 `maximum`；`maximum` 表示玩家已解锁的基础等级。 |
| `modifier-item` | 每个属性修改器的显示模板。 |
| `no-modifiers-item` | 没有修改器时在第一个修改器槽显示的物品。 |
| `buttons.<字符>` | 静态按钮定义。 |
| `buttons.U.maximum-item` | 属性达到 `allocation.maximum-value` 后替换升级按钮的外观。 |
| `buttons.E.enabled-item` / `disabled-item` | 属性当前启用或停用时显示的两种外观。 |
| `hide-on-first-page` / `hide-on-last-page` / `hidden-item` | 控制分页按钮在边界页的显示。 |
| `actions` | 除返回、升级、启停和有效等级控制外，其他按钮执行的动作。 |

### 属性详情通用占位符

用于 `title`、`buttons`、`modifier-item` 和 `no-modifiers-item`。

| 占位符 | 显示内容 |
| --- | --- |
| `{attribute_id}` | 当前属性 ID。 |
| `{attribute_name}` | 属性的本地化名称。 |
| `{attribute_description}` | 当前最终值对应的属性效果描述；同 `{current_description}`。 |
| `{current_description}` | 当前最终值对应的属性效果描述。 |
| `{next_description}` | 下一基础等级预计最终值对应的效果描述。 |
| `{base_value}` | 玩家已解锁的基础等级。 |
| `{unlocked_value}` | 同 `{base_value}`。 |
| `{effective_base_value}` | 玩家选用的有效基础等级，不能高于已解锁等级。 |
| `{effective_value}` | 当前最终属性值；同 `{value}`。 |
| `{attribute_enabled}` | 玩家是否启用此属性，输出 `true` 或 `false`。 |
| `{attribute_status}` | 根据启停状态生成的本地化文字。 |
| `{value}` | 当前最终属性值，包含有效等级及修改器等影响。 |
| `{minimum_value}` | 属性配置的最低值。 |
| `{maximum_value}` | 属性配置的最高值；与属性列表中的可加点上限不同。 |
| `{allocation_maximum_value}` | 属性可通过加点达到的最高基础等级。 |
| `{default_value}` | 属性配置的默认值。 |
| `{modifier_count}` | 当前作用于该属性的修改器数量。 |
| `{next_base}` | 下一基础等级，最多为可加点上限。 |
| `{next_value}` | 升至 `{next_base}` 后预计的最终值。 |
| `{price}` | 从当前基础等级提升 1 级所需属性点。 |
| `{points}` | 玩家当前可用的属性点数。 |
| `{requirement}` | 下一基础等级的加点条件说明。 |
| `{page}` | 当前修改器列表页码，从 1 开始。 |
| `{pages}` | 修改器列表总页数。 |

### 修改器物品专用占位符

仅用于 `modifier-item`，并可同时使用上表中的属性详情占位符。

| 占位符 | 显示内容 |
| --- | --- |
| `{modifier}` | 修改器 ID。 |
| `{operation}` | 运算类型的内部枚举名，如 `ADD_VALUE`。 |
| `{operation_display}` | 运算类型的本地化名称。 |
| `{amount}` | 修改器配置的原始数值。 |
| `{amount_display}` | 面向玩家的数值：加法带正负号，乘法换算成百分比。 |

## `settings.yml`：公共设置

| 选项 | 默认值 | 作用 |
| --- | --- | --- |
| `anti-dupe-checker` | `true` | 在 GUI 交互期间启用防复制保护。除非正在定位其他背包插件的兼容问题，否则建议保持开启。 |

## 通用按钮动作

专用菜单按钮支持翻页、关闭、消息、命令、效果等多种动作；自由菜单支持打开其他菜单、加点等动作。两套完整类型、参数与限制见[菜单动作](actions.md)。

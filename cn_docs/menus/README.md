# 菜单类型

插件内置菜单均位于 `plugins/EnchantmentReform/menus/`。除 `settings.yml` 外，YAML 文件名就是菜单 ID，可使用以下命令打开：

```text
/enchantmentreform menu <菜单ID>
```

控制台必须指定在线玩家：`enchantmentreform menu <菜单ID> <玩家名> [技能ID|属性ID]`。例如 `enchantmentreform menu main Steve` 或 `enchantmentreform menu skill-detail Steve mining`。玩家原有命令格式保持不变。菜单启用开关和权限检查仍对目标玩家生效；控制台 Tab 可补全已启用菜单、在线玩家及详情 ID。

菜单中的名称、描述、材质、布局和按钮均可修改。编辑后执行 `/enchantmentreform reload` 即可重载。

查阅顺序：[内置菜单选项与占位符](builtin-options.md)说明每个 YAML 选项和动态文本的含义；[自由菜单配置](../configs/menus.md)给出可复用的布局示例；[菜单动作](actions.md)列出两套动作格式、每种动作的参数和使用范围。

## 内置菜单

| 菜单 ID | 配置文件 | 用途 | 打开方式 |
| --- | --- | --- | --- |
| `main` | `main.yml` | 默认主菜单，集中提供技能、属性、属性加点和附魔图鉴入口。 | `/enchantmentreform menu main` |
| `enchantment-info` | `enchantment-info.yml` | 分页展示全部附魔、各等级描述、稀有度、权重与适用物品，并支持按稀有度和物品类型筛选。 | `/enchantmentreform menu enchantment-info` |
| `skill-info` | `skill-info.yml` | 技能总览，显示每项技能的等级、经验、进度和经验来源数量。 | `/enchantmentreform menu skill-info` |
| `skill-detail` | `skill-detail.yml` | 单项技能详情，可在经验来源和等级奖励路线之间切换，并显示可用属性。 | 从技能总览点击进入，或使用 `/enchantmentreform menu skill-detail <技能ID>` |
| `attribute-info` | `attribute-info.yml` | 只读属性总览，显示基础值、最终值、效果与修改器数量。 | `/enchantmentreform menu attribute-info` |
| `attribute-detail` | `attribute-detail.yml` | 单项属性详情，显示全部修改器、有效等级控制、升级条件与升级按钮。 | 从属性菜单点击进入，或使用 `/enchantmentreform menu attribute-detail <属性ID>` |
| `attribute-allocation` | `attribute-allocation.yml` | 全局属性加点入口，预览升级前后效果、价格和条件。 | `/enchantmentreform menu attribute-allocation` |

## 自由菜单

除内置菜单外，可以在 `menus/` 下新增任意名称的 `.yml` 文件。文件名会自动注册为菜单 ID，例如 `profile.yml` 可通过 `/enchantmentreform menu profile` 打开。

自由菜单可以包含静态按钮，也可以使用 `skills` 或 `attributes` 内容区动态列出技能和属性。按钮动作可以打开其他菜单、打开技能或属性详情、翻页、刷新、关闭菜单、分配属性点或执行命令。完整的布局、内容区、占位符和动作格式见[自由菜单配置](../configs/menus.md)。

`enchantment-info`、`skill-detail`、`attribute-detail` 使用各自的专用 YAML 格式，其 `buttons` 动作也与自由菜单的 `items` 动作不同，详见[菜单动作](actions.md)。

## 公共设置

`settings.yml` 不是可打开的菜单，它保存所有菜单共用的设置：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `anti-dupe-checker` | `true` | 启用 GUI 防复制检查。除非正在排查兼容性问题，否则建议保持开启。 |

## 数据来源

- 技能菜单读取 `skills/*.yml`；完整技能模块还必须在 `config.yml -> modules.skills` 中启用。
- 属性菜单读取 `attributes/*.yml`。`show-in-attribute-gui`、`skill-menu.show` 和 `allocation.show-in-menu` 分别控制属性是否出现在对应菜单中。
- 附魔图鉴读取已注册的原版和自定义附魔。
- 菜单文本支持语言占位符、颜色代码及该菜单提供的动态占位符。

# 🖱️ 菜单动作

所有菜单共用同一套动作类型与执行入口。`main.yml`、技能/属性总览、自定义菜单、附魔图鉴及详情页均可使用下列公共动作；需要技能或属性对象的动作仍受当前菜单上下文限制。

## 配置位置与点击方式

| 菜单中的位置 | 动作节点 | 物品外观节点 |
| --- | --- | --- |
| 主菜单、总览、自定义菜单的静态按钮 | `items.<字符>.actions` | `items.<字符>.item` |
| 动态技能或属性条目 | `contents.<字符>.actions` | `contents.<字符>.item` |
| 附魔图鉴、技能详情、属性详情的静态按钮 | `buttons.<字符>.actions` | `buttons.<字符>` |

`items` 与 `buttons` 的区别是布局配置结构，动作类型和参数相同。每个动作节点下可以按任意名称配置多个子动作，按 YAML 顺序执行；只有一项动作时，也可以直接在 `actions` 下写 `type`。

所有上述位置均支持以 `click-actions.LEFT`、`click-actions.RIGHT`、`click-actions.SHIFT_LEFT` 等 Bukkit 点击类型替代 `actions`。匹配的 `click-actions` 会完全替代该次点击的通用动作，空节点可以禁止该点击执行通用动作。

```yaml
items:
  H:
    item:
      material: COMPASS
      name: '&b返回主菜单'
    actions:
      open:
        type: open_menu
        menu: main
    click-actions:
      RIGHT:
        type: message
        message: '&e左键返回主菜单。'
```

相同动作也可放在详情页的 `buttons.H.actions` 或 `buttons.H.click-actions.RIGHT` 下，无需改写动作类型。

## 公共动作

| `type` | 作用 | 参数 |
| --- | --- | --- |
| `previous_page` | 当前视图上一页，到第一页后停止。 | 无。 |
| `next_page` | 当前视图下一页，到最后一页后停止。 | 无。 |
| `refresh` | 重新打开当前页。 | 无。 |
| `close` | 关闭背包界面。 | 无。 |
| `open_menu` | 打开其他已注册菜单，保留当前菜单的权限检查方式。 | `menu`：菜单 ID，默认 `main`。附魔图鉴使用 `enchantment-info`。需要选中对象的详情页使用下方的 `open_skill` 或 `open_attribute`。 |
| `message` | 向点击者发送聊天消息。 | `message`。 |
| `title` | 显示标题与副标题。 | `main-title`、`sub-title`、`fade-in`、`stay`、`fade-out`。 |
| `action_bar` | 显示快捷栏上方的消息。 | `message`。 |
| `announcement` | 向所有在线玩家广播聊天消息。 | `message`。 |
| `sound` | 在点击者位置播放声音。 | `sound`；可选 `volume`、`pitch`，默认均为 `1`。 |
| `particle` | 在点击者附近生成粒子。 | `particle`、`count`、`offset-x`、`offset-y`、`offset-z`、`speed`。 |
| `effect` | 给点击者施加药水效果。 | `potion`、`duration`（tick）、`level`（效果放大值）；可选 `ambient`、`particles`、`icon`，默认均为 `true`。 |
| `console_command` | 由控制台执行命令。 | `command`，不带 `/`。 |
| `player_command` | 由点击者执行命令。 | `command`，不带 `/`。 |
| `op_command` | 以临时 OP 权限执行命令。 | `command`，不带 `/`。 |
| `command` | 保留旧配置的命令写法。 | `command`；`as-console` 默认 `true`，设为 `false` 时由玩家执行。新配置可直接使用 `console_command` 或 `player_command`。 |
| `teleport` | 传送点击者。 | `world`、`x`、`y`、`z`；可选 `yaw`、`pitch`。 |
| `entity_spawn` | 在指定位置生成实体。 | `entity`；可用 `world`、`x`、`y`、`z` 指定位置。 |
| `chance` | 按百分比概率执行子动作。 | `rate`（0–100）、`actions`。 |
| `any` | 随机选择指定数量的子动作执行。 | `actions`；可选 `amount`，默认 `1`。 |
| `delay` | 延迟执行子动作。 | `time`（服务器 tick 数）、`actions`；执行时玩家必须仍在线。 |

`chance`、`any`、`delay` 的子 `actions` 使用同一套动作格式，也可以打开菜单或执行带对象上下文的动作。

## 技能与属性上下文

| `type` | 作用 | 可用位置与参数 |
| --- | --- | --- |
| `open_skill` | 打开当前技能的详情页。 | 动态技能条目、技能详情页，以及从技能详情进入的属性详情页。 |
| `open_attribute` | 打开当前属性的详情页。 | 动态属性条目或属性详情页。 |
| `allocate_attribute` | 给当前属性加点，然后刷新当前页。 | 动态属性条目或属性详情页。`add` 默认 `1`，至少为 `1`；仍检查属性点、上限和条件。 |

没有对应对象上下文时，上述动作不执行。普通静态按钮不会自动选中技能或属性。

## 文本占位符

所有菜单的文本动作均支持 `{player}`（玩家名）、`{menu}`（当前菜单 ID）、`{page}`（从 1 开始）、`{pages}`（总页数）、`{slot}`（从 0 开始的背包格位）、`{click}`（Bukkit 点击类型）。有对象上下文时，`{skill}` 和 `{attribute}` 分别为对应 ID，否则为空。已安装 PlaceholderAPI 时，也支持其占位符。

## 菜单自身处理的点击

`skill-detail.yml` 的 `back-symbol`、`view-symbol`，以及 `attribute-detail.yml` 的 `back-symbol`、`upgrade-symbol`、`enable-symbol`、`effective-level-symbol` 由菜单本身处理点击；这些位置的主要行为不由 `actions` 或 `click-actions` 替换。技能详情的动态属性条目直接打开属性详情；附魔图鉴中带 `filter-type` 的按钮优先切换筛选项。

# 菜单动作

菜单有两套动作格式。`main.yml`、`skill-info.yml`、`attribute-info.yml`、`attribute-allocation.yml` 及自行新增的菜单使用[自由菜单动作](#自由菜单动作)；`enchantment-info.yml`、`skill-detail.yml`、`attribute-detail.yml` 使用[专用菜单按钮动作](#专用菜单按钮动作)。两套格式的动作类型不能互换。

## 自由菜单动作

静态按钮放在 `items.<字符>.actions`；技能或属性内容物品放在 `contents.<字符>.actions`。需要区分点击方式时，改用 `click-actions.LEFT`、`click-actions.RIGHT`、`click-actions.SHIFT_LEFT` 等 Bukkit 点击类型。若某个点击类型有专用配置，就使用它而不执行通用 `actions`。

`actions` 下每个任意名称的子节点是一项动作，按 YAML 中的顺序执行；也可以直接把 `type` 写在 `actions` 下，表示只有一项动作。

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
```

| `type` | 作用 | 其他参数与限制 |
| --- | --- | --- |
| `open_menu` | 打开另一个已注册菜单。 | `menu`: 目标菜单 ID，省略时为 `main`。`enchantment-info` 会打开专用附魔图鉴。不要用它打开需要选中对象的详情菜单。 |
| `open_skill` | 打开所点击技能的详情页。 | 只能用于 `type: skills` 的动态内容物品；静态按钮或属性物品没有技能上下文。 |
| `open_attribute` | 打开所点击属性的详情页。 | 只能用于 `type: attributes` 的动态内容物品。 |
| `allocate_attribute` | 给所点击属性加点，并刷新菜单。 | 只能用于属性内容物品；`add` 为增加的等级数，省略时为 `1`，至少为 `1`。仍会检查属性点、上限和条件。 |
| `previous_page` | 打开上一页。 | 到第一页后不会继续向前。 |
| `next_page` | 打开下一页。 | 到最后一页后不会继续向后。 |
| `refresh` | 按当前页重新构建菜单。 | 无额外参数。 |
| `close` | 关闭玩家当前背包界面。 | 无额外参数。 |
| `command` | 执行一条命令。 | `command`: 命令文本，不带 `/`；`as-console` 默认 `true`，设为 `false` 时由玩家执行。命令文本支持 `{player}` 和 `{menu}`。 |

## 专用菜单按钮动作

专用菜单的静态按钮使用 `buttons.<字符>.actions.<动作名>`。`<动作名>` 可自定；`type` 决定行为。按钮物品选项直接写在 `buttons.<字符>` 下，而非 `item` 子节点。

```yaml
buttons:
  P:
    material: ARROW
    name: '&a上一页'
    actions:
      page:
        type: previous_page
```

专用按钮支持以下动作类型：

| `type` | 作用 | 主要参数 |
| --- | --- | --- |
| `previous_page` | 当前视图上一页。 | 无。 |
| `next_page` | 当前视图下一页。 | 无。 |
| `refresh` | 重新显示当前页。 | 无。 |
| `close` | 关闭背包界面。 | 无。 |
| `message` | 向点击者发送聊天消息。 | `message`。 |
| `title` | 显示屏幕标题与副标题。 | `main-title`、`sub-title`、`fade-in`、`stay`、`fade-out`。 |
| `action_bar` | 显示快捷栏上方的消息。 | `message`。 |
| `announcement` | 向所有在线玩家广播聊天消息。 | `message`。 |
| `sound` | 在点击者位置播放声音。 | `sound`；可选 `volume`、`pitch`，默认均为 `1`。 |
| `particle` | 在点击者附近生成粒子。 | `particle`、`count`、`offset-x`、`offset-y`、`offset-z`、`speed`。 |
| `effect` | 给点击者施加药水效果。 | `potion`、`duration`（tick）、`level`（效果放大值）；可选 `ambient`、`particles`、`icon`，默认均为 `true`。 |
| `console_command` | 由控制台执行命令。 | `command`，不带 `/`。 |
| `op_command` | 以临时 OP 权限执行命令。 | `command`，不带 `/`。 |
| `player_command` | 由点击者执行命令。 | `command`，不带 `/`。 |
| `teleport` | 传送点击者。 | `world`、`x`、`y`、`z`；可选 `yaw`、`pitch`。 |
| `entity_spawn` | 在指定位置生成实体。 | `entity` 必填；可用 `world`、`x`、`y`、`z` 指定位置。 |
| `chance` | 按百分比概率执行子动作。 | `rate` 为 0–100，`actions` 为子动作节点。 |
| `any` | 从子动作中随机执行指定数量。 | `actions`；可选 `amount`，默认为 `1`。 |
| `delay` | 延迟执行子动作。 | `time` 为服务器 tick 数，`actions` 为子动作节点；玩家届时必须仍在线。 |

专用按钮的文本动作可使用 `{page}`（从 1 开始）、`{pages}`（总页数）、`{slot}`（从 0 开始的背包格位）和 `{click}`（Bukkit 点击类型），并支持 PlaceholderAPI 占位符（若已安装）。

`conditional` 虽在通用动作管理器中注册，但菜单上下文没有条件检查器，因此在专用菜单中条件始终不成立。`mythicmobs_spawn` 未注册到菜单动作管理器。

`skill-detail.yml` 的 `back-symbol`、`view-symbol`，以及 `attribute-detail.yml` 的 `back-symbol`、`upgrade-symbol`、`enable-symbol`、`effective-level-symbol` 由菜单本身处理点击；这些位置不能靠 `actions` 改写其主要行为。附魔图鉴按钮设置 `filter-type` 后，点击优先切换筛选项。

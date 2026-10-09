# 自由菜单配置

本页说明 `main.yml`、`skill-info.yml`、`attribute-info.yml`、`attribute-allocation.yml` 及自行新增菜单使用的自由菜单格式。菜单类型和打开方式见[菜单类型](../menus/README.md)；各内置选项及每个占位符的含义见[内置菜单选项](../menus/builtin-options.md)；全部动作类型与参数见[菜单动作](../menus/actions.md)。

自由菜单的 YAML 文件名就是菜单 ID。入口菜单统一使用以下指令打开：

```text
/enchantmentreform menu <菜单ID>
```

`skill-detail` 和 `attribute-detail` 需要已选中的对象，可从列表使用 `open_skill`、`open_attribute` 进入，也可在菜单命令后追加对应 ID。

## 启用开关与打开权限

所有菜单（包括附魔图鉴、详情菜单及自定义菜单）均支持顶层 `enabled` 与 `permission`。`enabled` 默认为 `true`，设为 `false` 后无法打开；`permission` 默认为空字符串，不额外限制权限，填写权限节点后，命令、按钮跳转和返回菜单都会检查该权限。菜单命令本身仍需要 `enchantmentreform.menu`。默认 main 菜单通过 `{lang:main-menu-*}` 引用语言文件中 `override-lang` 下的文本。

```yaml
enabled: true
permission: ''
```

## 布局与内容区

自由菜单使用每行 9 个字符的 `layout`，允许 1–6 行，行数直接决定背包大小。字符就是 `contents` 内容区或 `items` 按钮的 ID；重复字符代表该区域占用多个位置。因此这两个节点下的 ID 必须是单个字符，不提供 `slot` 或 `slots`。附魔图鉴和两种详情菜单使用各自的专用布局选项。

```yaml
title: '&8角色菜单 &7{page}/{pages}'
layout:
  - 'FFFFFFFFF'
  - 'FAAAAAAAF'
  - 'FAAAAAAAF'
  - 'PFFFFFFFN'

contents:
  A:
    type: attributes
    mode: information
    item:
      material: AMETHYST_SHARD
      name: '&d&l{name}'
      lore:
        - '&8属性'
        - ' '
        - '&f当前效果'
        - '&7{current_description}'
        - ' '
        - '&f升级后效果'
        - '&7{next_description}'
        - ' '
        - '&e点击查看详情'
    click-actions:
      LEFT:
        open:
          type: open_attribute
```

内容类型为 `skills` 或 `attributes`。属性模式支持 `all`、`information`、`allocation` 和 `skill`；`skill` 模式显示所有启用了 `skill-menu.show` 的属性，不再按技能 ID 过滤。两种内容均支持 `include` 与 `exclude` ID 列表。多个内容区共用当前页码，页数由最大的内容集合决定。

通用、技能与属性内容区的占位符逐项解释见[内置菜单选项与占位符](../menus/builtin-options.md)。

## 静态物品与动作

静态物品通过自己的单字符键出现在 `layout` 对应位置。只有布局中实际存在翻页字符时，菜单才会提供对应按钮。

```yaml
items:
  P:
    hide-on-first-page: true
    item:
      material: ARROW
      name: '&a上一页'
    hidden-item:
      material: GRAY_STAINED_GLASS_PANE
      name: ' '
    actions:
      page:
        type: previous_page
  F:
    item:
      material: BLACK_STAINED_GLASS_PANE
      name: ' '
```

静态物品和内容物品都可以使用 `actions` 响应所有点击，或使用 `click-actions.<点击类型>` 区分 Bukkit 点击类型。

自由菜单支持的九种动作、各自参数及适用的按钮类型见[菜单动作](../menus/actions.md)。

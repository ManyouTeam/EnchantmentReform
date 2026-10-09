# 技能系统

完整技能模块由 `config.yml -> modules.skills` 控制。设为 `false` 后，不加载技能文件，也不会运行技能进度、经验、奖励、技能菜单内容及技能指令；所有 `skill_level` 能力条件及属性加点条件都会直接视为满足。

每个技能是 `plugins/EnchantmentReform/skills` 文件夹中的一个 YAML 文件，文件名就是技能 ID。插件会补齐缺少的内置技能文件，但绝不会覆盖已有文件。默认提供耕作、伐木、采矿、挖掘、钓鱼、战斗、箭术、防御、敏捷、炼金和附魔 11 项技能；它们只定义进度与经验来源，不自带技能能力。

## 基础配置

```yaml
enabled: true
order: 3
name: '&b采矿'
description: '&7挖掘天然方块获得经验。'
maximum-level: 100
experience-formula: '100 * {level} * {level}'
icon:
  material: IRON_PICKAXE
```

`experience-formula` 计算升到下一级需要的经验。`{level}` 是将要达到的等级，`{current_level}` 是当前等级，并支持插件的数学计算格式。

## 升级奖励

```yaml
rewards:
  every-level:
    rewards:
      - type: attribute-points
        display-name: '&e+{amount} 属性点'
        amount: 1
  levels:
    '10':
      rewards:
        - type: attribute-points
          display-name: '&e额外 +{amount} 属性点'
          amount: 2
        - type: attributes
          display-name: '&a属性提升：{attributes}'
          attributes:
            endurance: 1
        - type: command
          display-name: '&6VIP 7 天'
          command: 'grantvip {player} 7d'
        - type: item
          display-name: '&b钻石'
          material: DIAMOND
          amount: 3
```

`every-level` 每级执行一次；`levels` 是指定等级的额外奖励。列表中的每个条目都是一项独立命名的奖励，可直接使用 `type: attribute-points`、`type: attribute`、`type: attributes`、`type: command` 或 `type: item`。单属性 `attribute` 使用 `attribute` 和 `amount`；复数 `attributes` 必须提供一个 `attributes` 映射，并把本地化后的汇总内容提供为 `{attributes}`。属性奖励会增加自定义属性基础值；物品条目本身就是 ItemFormat，无需嵌套 `item`。S 型奖励 GUI 会参考 AuraSkills/EcoSkills 的形式，按照 YAML 顺序逐条显示 `display-name`，不再使用固定分类标题。奖励名称按类型支持 `{amount}`、`{attribute}`、`{attributes}`、`{player}`、`{skill}` 和 `{level}`。奖励只读取带 `type` 的 `rewards` 条目，不接受无类型的旧版奖励段。

## 经验来源

经验来源直接使用 [TriggerManager 的触发器](triggers.md) 和相同的 [能力条件](power-conditions/README.md)：

```yaml
sources:
  diamond_ore:
    name: '&b钻石矿石'
    description: '&7开采普通或深层钻石矿石。'
    trigger: block_break
    xp: 12
    unit: '&7/方块'
    icon:
      material: DEEPSLATE_DIAMOND_ORE
    conditions:
      block:
        type: block_type
        types:
          - DIAMOND_ORE
          - DEEPSLATE_DIAMOND_ORE
    anti-abuse:
      ignore-player-placed: true
      ignore-internal-block-breaks: true
      cooldown-ms: 50
      maximum-uses-per-minute: 240
      maximum-xp-per-minute: 500
      pressure:
        threshold: 100
        reset-after-seconds: 120
        multiplier: 0.25
```

`name`、`description`、`unit` 和 `icon` 用于该技能的独立来源 GUI。`trigger` 可写内置 ID（如 `block_break`、`melee_attack`、`attack`、`damage_by_entity`、`fish`、`enchant_item`、`consume`、`move`），也可写已注册触发器的完整命名空间 ID。`xp` 支持触发上下文数值占位符以及 `{amount}`；例如伤害触发可使用 `{original_damage}`，附魔可使用 `{original_experience}`。

## 经验倍率

权限倍率在 `config.yml` 中配置。使用 `*` 或 `all` 表示全部技能，也可以列出一个或多个技能 ID。玩家同时匹配多个配置时，所有倍率相乘。

```yaml
skills:
  experience-multipliers:
    permissions:
      vip:
        permission: enchantmentreform.skill-multiplier.vip
        skills: ['*']
        multiplier: 1.5
      mining-event:
        permission: enchantmentreform.skill-multiplier.mining-event
        skills: [mining]
        multiplier: 2.0
```

管理员还可以设置持久化的玩家倍率。`all` 倍率和指定技能倍率都会生效，并与权限倍率相乘。设为 `1` 会删除对应覆盖值；设为 `0` 会停止对应范围的经验获取。

```text
/enchantmentreform setskillmultiplier <玩家> <技能ID|all> <倍率>
```

权限为 `enchantmentreform.setskillmultiplier`，默认仅管理员拥有。该指令只会在 `modules.skills` 开启时注册。倍率在 `maximum-xp-per-minute` 之前应用，因此经验来源上限仍会限制最终获得量。

如果一个来源需要显示在技能菜单中，但必须由 `skill_experience` 能力在特定成功结果后发放，请设置 `manual: true`。能力通过 `skill` 和 `source` 引用它，并复用该来源的经验公式、反滥用配置、速率限制、压力衰减和正常经验提示，同时避免配置的触发器再次自动发放。

## 共用防刷配置

以下 `anti-abuse` 选项同时可写在技能来源，以及附魔、物品、属性的任意触发器段中：

- `ignore-player-placed`：玩家放置、骨粉催熟或由已标记方块蔓延出的方块不触发。标记保存在区块 PDC 中并跟随活塞移动。
- `ignore-internal-block-breaks`：忽略插件连锁破坏等内部方块破坏。
- `ignore-spawner-mobs`：忽略刷怪笼与试炼刷怪笼生成的生物。
- `cooldown-ms`：同一玩家、同一来源/能力的触发冷却。
- `target-cooldown-seconds`：同一目标的重复触发冷却。
- `player-victim-cooldown-seconds`：仅玩家目标的重复触发冷却。
- `maximum-uses-per-minute`：每分钟最大有效触发次数。

技能来源默认开启前三项安全检查，并默认对同一玩家受害者设置 300 秒冷却。`maximum-xp-per-minute` 与 `pressure` 是技能经验专用：前者限制每分钟经验，后者在连续重复同一来源超过阈值后降低经验，切换活动或等待重置时间可恢复。

附魔、物品或属性中的示例：

```yaml
powers:
  on-block-break:
    anti-abuse:
      ignore-player-placed: true
      cooldown-ms: 100
    abilities:
      # ...
```

## 菜单与占位符

- `/enchantmentreform menu skill-info`：打开技能总览；点击任意技能可进入详情页。每个技能详情菜单都会显示所有启用了 `skill-menu.show` 的属性；点击属性会进入 `menus/attribute-detail.yml` 配置的独立详情页，分区显示当前值、基础值、全部修改器、要求和明确的升级按钮。技能菜单还可在经验来源与 S 型逐级奖励路线之间切换。
- `menus/skill-info.yml` 与 `menus/skill-detail.yml`：分别配置总览和技能详情 GUI。进度条使用 `{progress_bar}`；来源物品支持 `{source_name}`、`{source_description}`、`{source_xp}` 与 `{source_unit}`；奖励物品使用 `{reward_lines}` 显示按配置顺序排列的奖励名称。
- `skills.feedback.experience`：配置获得经验时的提示、间隔和音效。`cooldown-ticks` 以 Tick 为单位控制提示间隔；`display` 支持 `BOSS_BAR`、`ACTION_BAR` 和 `BOTH`，BossBar 会按照当前经验/升级所需经验显示进度。`skills.feedback.level-up` 配置升级标题与音效。将音效 `name` 留空即可关闭音效。
- `/enchantmentreform menu attribute-allocation`：打开属性加点菜单。显示开关、每级价格、通用条件和逐级条件都配置在对应属性文件的 `allocation` 中。
- PlaceholderAPI：`%enchantmentreform_skill_level_<技能ID>%`、`%enchantmentreform_skill_xp_<技能ID>%`、`%enchantmentreform_skill_required_xp_<技能ID>%`、`%enchantmentreform_attribute_points%`。

# 🔢 计数方式

`count` 选择计量单位，`conditions` 筛选参与计数的事件、方块或产物。技能经验来源和附魔能力使用相同的计数规则。默认 `count: events`，每次符合条件的事件计 1；伤害来源要按伤害量计算经验时，须明确设置 `count: damage`。

## 支持范围

所有内置触发器均支持 `events`。以下是额外支持的方式；没有列出的触发器只支持 `events`，每个触发器参考条目也会标明可选值。

| `count` | 支持的触发器 | 累计内容 |
| --- | --- | --- |
| `items` | `fish`、`block_drop_item`、`piglin_barter`、`trade`、`purchase`、`kill` | 钓获物、掉落物或交易产物的堆叠数量之和。 |
| `blocks` | `block_place` | 本次实际涉及的放置方块数，逐块检查条件。 |
| `damage` | `attack`、`melee_attack`、`damage`、`damage_by_entity` | 事件最终伤害，计入护甲等修正；不按目标剩余生命截断。 |
| `blocked_damage` | `shield_block` | 盾牌实际格挡的伤害量。 |
| `healing` | `regain` | 正数恢复量，最多计入玩家缺失的生命值。 |
| `durability` | `item_damage` | 事件损耗量，最多计入物品剩余耐久。 |
| `experience` | `exp_change`、`pickup_experience` | 正数 Minecraft 经验点数，不是等级。 |
| `distance` | `move` | 相邻事件坐标之间的三维直线距离，单位为方块。 |
| `exhaustion` | `exhaustion` | 正数消耗度数值。 |

`death`、`block_break`、`air_change`、`food_level_change`、`warden_anger_change` 本次仍只支持 `events`。事件中即使存在经验或状态变化值，也不会自动成为可选的计数方式。例如 `block_break + items` 会报错；统计挖掘产物应使用 `block_drop_item + items`。

## 批次结算

省略 `count-every` 时，每次按本次符合条件的数量结算。配置正数 `count-every` 后，累计满指定数量才结算完整批次，余量继续保留。例如阈值为 10，本次累计到 25，结算 2 批，余下 5。最多支持 6 位小数；未满一批不会发经验或执行能力。

技能公式中的 `{amount}`：未配置批次时表示本次数量；配置批次后表示本次结算的完整批次数。要让经验随数量变化，应写 `xp: '单价 * {amount}'`。固定的 `xp: 5` 每次结算仍只发 5，不会自动乘数量或批次。

能力每次结算执行一次修改器和能力，使用 `{count_amount}` 读取本次数量或批次数；多个批次不会自动重复执行整组能力。`{count_quantity}` 是本次通过条件的原始数量，`{count_remainder}` 是结算后的余量。

UQ 的 `amount` 是完成目标所需的累计数量；ER 的 `count-every` 是可以反复结算的批次大小。UQ 累计到要求后目标完成，ER 则继续积累下一批。

## 移动与重复动作

下面是技能文件中的 `sources` 片段。每疾跑 10 格获得 3.5 XP，每游泳 10 格获得 2.5 XP，每跳跃 10 次获得 5 XP：

```yaml
sources:
  sprinting:
    trigger: move
    count: distance
    count-every: 10
    xp: '3.5 * {amount}'
    display-xp: 3.5
    conditions:
      sprint:
        type: sprinting
        target: PLAYER
  swimming:
    trigger: move
    count: distance
    count-every: 10
    xp: '2.5 * {amount}'
    display-xp: 2.5
    conditions:
      swim:
        type: swimming
        target: PLAYER
  jumping:
    trigger: jump
    count: events
    count-every: 10
    xp: '5 * {amount}'
    display-xp: 5
```

移动类型由已有的 `sprinting`、`swimming`、`flying`、`gliding`、`riding` 等能力条件筛选，记得指定 `target: PLAYER`。距离使用满足条件的移动事件的三维直线位移，同一方块内的移动也计入；转头、传送和跨世界跳转不计距离。不读取原版累计统计，也不使用 `count-statistic`。

## 物品条件

下面的片段按挖矿掉落的钻石数量发放经验，工具可另外使用 `TRIGGER_ITEM` 条件筛选：

```yaml
sources:
  diamonds:
    trigger: block_drop_item
    count: items
    xp: '2 * {amount}'
    conditions:
      output:
        type: match_item
        item: COUNTED_ITEM
        match:
          material: [DIAMOND]
```

`COUNTED_ITEM` 只在 `count: items` 逐项检查条件时存在。每个产物堆分别检查 `conditions`，只累计通过条件的数量。没有物品条件时累计全部产物；`CONTEXT` 仍指当前附魔物品，`TRIGGER_ITEM` 仍指事件物品，不会被产物替换。方块数量模式也会逐块检查条件。

## 附魔能力

同样的字段放在 `powers.on-...` 中。例如，每累计疾跑 10 格触发一次能力：

```yaml
powers:
  on-move:
    count: distance
    count-every: 10
    conditions:
      sprint:
        type: sprinting
        target: PLAYER
    abilities:
      feedback:
        type: message
        target: PLAYER
        message: 'Completed batches: {count_amount}'
```

## 计数状态与执行顺序

先检查条件，再进行一次事件级防刷判定，通过后累计数量并结算批次。能力结算还要通过原有的 `powers.limit`；概率、冷却或次数限制阻止执行时，不退回已结算批次。技能经验继续应用倍率、速率上限和压力衰减。

余量按技能来源或附魔触发器独立保存，附魔计数还按装备槽位隔离。切换疾跑、游泳状态只会停止相应来源增加数量，不清空余量。退出、重载或停用插件会清空尚未结算的余量。

# 🎒 库存与装备能力

本页面会将每一种已注册能力作为完整条目进行说明。所有条目也支持[能力](README.md)页面中介绍的通用能力字段。

## 本页面的注册键

* `consume_food`
* `auto_feed`
* `enhance_equipment`
* `enhance_helditem`
* `replace_item`
* `shuffle_inventory`
* `change_item`
* `cost_price`
* `drop_item`
* `give_item`
* `give_loot_table_item`
* `preserve_inventory`
* `preserve_item`
* `preserve_experience`
* `use_on`

---

## `consume_food`

**用途：**消耗玩家的饱和度和/或饥饿值，并支持累计小数形式的饥饿值消耗。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `0.25` | 合并消耗数量；支持 `{food}` 和 `{saturation}`。 |
| `saturation-first` | `true` | 使用合并模式时，是否先消耗饱和度再消耗饥饿值。 |
| `saturation-amount` | 未设置 | 明确指定要消耗的饱和度。 |
| `food-amount` | 未设置 | 明确指定要消耗的饥饿值。 |

### 示例

```yaml
type: consume_food
target: TARGET
amount: 1.5
saturation-first: true
```

### 行为与限制

* 小数形式的饥饿值消耗会按玩家分别保存，累计达到一个完整点数后才会实际扣除。
* 只要配置了任意一个明确数量字段，实际扣减就会由明确模式控制。

---

## `auto_feed`

**用途：**通过 NMS 桥接在玩家的存储栏中寻找并食用第一个符合条件的食物物品。

**上下文：**默认 `target` 为 `SOURCE`；目标必须是在线且存活的玩家，并且饥饿值低于 20。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `threshold` | `10` | 允许自动进食的最大饥饿值。 |
| `health-not-full-threshold` | `threshold` | 当前生命值低于最大生命值时使用的阈值。 |
| `success-abilities` | 空 | 成功食用物品后执行的子能力。 |

### 示例

```yaml
type: auto_feed
target: SOURCE
threshold: 10
health-not-full-threshold: 16
success-abilities:
  sound:
    type: sound
    sound: ENTITY_GENERIC_EAT
```

### 行为与限制

* 按顺序扫描存储栏，并使用第一个可食用物品或带食物组件的物品。
* 需要 NMS `FINISH_USING_ITEM`；容器类剩余物会放入库存，无法放入时自然掉落。
* NMS 使用失败时，已经移除的食物物品会被返还。

---

## `enhance_equipment`

**用途：**使用新创建的盔甲替换选中的盔甲部件，并随机添加兼容附魔。

**上下文：**默认 `target` 为 `SOURCE`；需要具有装备的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `armor-material` | `NETHERITE` | 与盔甲部件名称组合使用的材质前缀。 |
| `pieces` | 全部盔甲 | 可填写 `HELMET`、`CHESTPLATE`、`LEGGINGS` 和 `BOOTS`。 |
| `enchant.min-amount` | `1` | 不同附魔的最小数量。 |
| `enchant.max-amount` | `3` | 不同附魔的最大数量。 |
| `enchant.min-level` | `1` | 最小附魔等级。 |
| `enchant.max-level` | `4` | 最大附魔等级，不会超过该附魔的最高等级。 |
| `enchant.enchantments` | 全部兼容附魔 | 可选的 Bukkit 附魔允许列表。 |

### 示例

```yaml
type: enhance_equipment
target: SOURCE
armor-material: DIAMOND
pieces:
  - HELMET
  - CHESTPLATE
enchant:
  min-amount: 1
  max-amount: 2
  min-level: 1
  max-level: 3
  enchantments:
    - PROTECTION
    - UNBREAKING
```

### 行为与限制

* 选中的现有装备会被替换。
* 对于非玩家实体，被替换部件的装备掉落概率会设置为零。

---

## `enhance_helditem`

**用途：**为手持物品随机添加兼容附魔。

**上下文：**默认 `target` 为 `SOURCE`；需要具有装备的生物实体，并且所选手部物品不能是空气。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `hand` | `MAIN_HAND` | 可填写 `MAIN_HAND` 或 `OFF_HAND`。 |
| `enchant.min-amount` | `1` | 不同附魔的最小数量。 |
| `enchant.max-amount` | `3` | 不同附魔的最大数量。 |
| `enchant.min-level` | `1` | 最小附魔等级。 |
| `enchant.max-level` | `4` | 最大附魔等级，不会超过附魔自身的最高等级。 |

### 示例

```yaml
type: enhance_helditem
target: SOURCE
hand: MAIN_HAND
enchant:
  min-amount: 1
  max-amount: 3
  min-level: 1
  max-level: 4
```

### 行为与限制

* 兼容附魔会在不重复的情况下选择。
* 对于非玩家实体，主手和副手的装备掉落概率都会设置为零。

---

## `replace_item`

**用途：**使用嵌套 ItemFormat 构建物品，并将其放入指定装备槽位。

**上下文：**默认 `target` 为 `SOURCE`；需要具有装备的生物实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `slot` | `MAIN_HAND` | 可填写 `MAIN_HAND`、`OFF_HAND`、`HELMET`、`CHESTPLATE`、`LEGGINGS` 或 `BOOTS`。 |
| `item` | 必填 | 用于构建替换物品的嵌套 ItemFormat。 |

### 示例

```yaml
type: replace_item
target: SOURCE
slot: MAIN_HAND
item:
  material: DIAMOND_SWORD
  name: '<red>Elite Blade'
```

### 行为与限制

* 对于非玩家实体，被替换槽位的装备掉落概率会设置为零。
* 不受支持的槽位值不会改变装备。

---

## `shuffle_inventory`

**用途：**在配置的槽位范围内，随机重新排列玩家库存中已占用的槽位。

**上下文：**默认 `target` 为 `TARGET`；选中的实体必须是玩家。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `start-slot` | `0` | 第一个玩家库存索引，最小值为 0。 |
| `end-slot` | `35` | 最后一个索引，不会超过库存大小。 |

### 示例

```yaml
type: shuffle_inventory
target: TARGET
start-slot: 0
end-slot: 35
```

### 行为与限制

* 只有已占用的槽位参与重新排列，因此原本为空的槽位仍然为空。
* 已占用槽位少于两个时不会发生变化。

---

## `change_item`

**用途：**对每个被选中且匹配的物品应用 EnchantmentReform `ChangesManager` 修改。

**上下文：**使用通用能力物品字段提供的扩展物品选择器和可选装备持有者。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `item / item.selector` | `CONTEXT` | 物品选择器；可以选中一个或多个装备物品。 |
| `item-holder / item.holder` | 默认目标 | 装备物品使用的持有者选择器。 |
| `match-item` | 匹配全部 | MatchItemFormat 过滤器。 |
| `changes` | 空 | ChangesManager 修改配置。 |

### 示例

```yaml
type: change_item
item:
  selector: MAIN_HAND
  holder: SOURCE
match-item:
  material-tag:
    - minecraft:damageable
changes:
  damage:
    operation: SUBTRACT
    value: 2
```

### 行为与限制

* 每个选中的物品都会单独检查。
* 执行后会清除活跃附魔缓存，使物品变化能够被重新检查。

---

## `cost_price`

**用途：**检查并可选择扣除配置的价格，然后执行成功或失败子能力。

**上下文：**`payer` 必须解析为玩家；由提供器实现的价格需要对应集成可用。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `payer` | `PLAYER` | 用于检查能否支付和执行付款的玩家选择器。 |
| `cost / amount / price.cost` | `0` | 所需数量；按照最先出现的配置位置读取。 |
| `take / price.take` | `true` | 是否实际扣除价格。 |
| `cost-every` | `1` | 每 N 次激活才收取一次价格；其他激活会免费执行成功子能力。 |
| `price` | 必填 | 价格定义：插件物品、原版物品、MatchItemFormat、插件经济、原版经济、占位符、储备或免费。 |
| `abilities` | 必填 | 成功时执行的子能力。 |
| `else-abilities` | 空 | 失败分支；推荐使用的后备字段名称。 |
| `failure-abilities / fail-abilities` | 空 | 其他失败分支别名。 |

### 示例

```yaml
type: cost_price
payer: PLAYER
cost: 10
take: true
price:
  economy-plugin: Vault
  economy-type: default
abilities:
  reward:
    type: potion_effect
    target: SOURCE
    potion: HASTE
    duration: 100
failure-abilities:
  message:
    type: send_message
    message: '<red>You cannot afford this ability.'
```

### 行为与限制

* 价格为负数时失败。
* 无法解析付款玩家时会执行失败分支。
* 价格类型根据 `price` 中存在的字段识别；无效配置会记录错误。
* 执行返回值会从实际选中的子能力分支向上传播。

---

## `drop_item`

**用途：**从一个被选中的现有物品堆中移除指定数量，并将其生成成掉落物实体。

**上下文：**需要选中的物品，以及能够解析出世界的位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `item / item.selector` | `CONTEXT` | 通用物品选择系统提供的物品选择器。 |
| `item-holder / item.holder` | 默认目标 | 装备持有者选择器。 |
| `amount` | 整个物品堆 | 要移除并掉落的数量，限制在 `1..available`。 |
| `naturally` | `true` | 是否使用带随机运动的自然掉落，而不是精确掉落。 |
| `pickup-delay` | 未设置 | 可选的拾取延迟，最小值为 0。 |

### 示例

```yaml
type: drop_item
item:
  selector: MAIN_HAND
  holder: SOURCE
amount: 1
naturally: true
pickup-delay: 20
```

### 行为与限制

* 此能力不会通过 ItemFormat 构建新物品，而是从选中的物品堆中实际扣除。
* 选中的物品无效或为空时会跳过执行。

---

## `give_item`

**用途：**按 ItemFormat 构建物品，并将其给予玩家或掉落在世界中。

**上下文：**`INVENTORY` 发放模式要求目标为玩家；`DROP` 发放模式要求能够解析掉落位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `target` | `PLAYER` | 接收物品的玩家。 |
| `item` | 必填 | 用于构建物品的 ItemFormat 配置段。 |
| `amount` | 构建出的物品数量 | 覆盖物品数量，最小为 1。 |
| `delivery` | `INVENTORY` | `INVENTORY` 将物品给予 `target`；`DROP` 在 `location` 生成掉落物。 |
| `location` | `CONTEXT` | 使用 `delivery: DROP` 时的掉落位置选择器。 |
| `naturally` | `true` | 是否让掉落物获得自然的随机抛出速度。 |
| `pickup-delay` | 未设置 | 掉落物的可选拾取延迟，单位为 tick。 |
| `drop-overflow` | `true` | 背包放不下时，在玩家位置自然掉落剩余物品。 |

### 示例

```yaml
type: give_item
target: PLAYER
amount: 1
random: 0.25
item:
  material: ARROW
```

```yaml
type: give_item
delivery: DROP
location: SOURCE
naturally: true
item:
  material: ARROW
  amount: 1
```

### 行为与限制

* 缺少 `item` 或构建结果无效时会跳过执行。
* `drop-overflow` 为 `false` 时，不会把背包剩余物生成到世界中。

---

## `give_loot_table_item`

**用途：**抽取战利品表，并将其产出的物品给予玩家或掉落在世界中。与 `give_item` 不同，本能力不从配置构建具体物品，而是直接从指定战利品表随机抽取物品。

**上下文：**`INVENTORY` 发放模式要求目标为玩家；`DROP` 发放模式要求能够解析掉落位置。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `loot-table` | 必填 | 要抽取的战利品表的命名空间键，例如 `minecraft:gameplay/piglin_bartering`。 |
| `target` | `PLAYER` | 接收抽取物品的玩家。 |
| `amount` | `1` | 抽取战利品表的次数。 |
| `luck` | `0` | 传入战利品上下文的幸运值，会影响部分战利品条目。 |
| `delivery` | `INVENTORY` | `INVENTORY` 将物品给予 `target`；`DROP` 在 `location` 生成掉落物。 |
| `location` | `CONTEXT` | 使用 `delivery: DROP` 时的掉落位置选择器。 |
| `naturally` | `true` | 是否让掉落物获得自然的随机抛出速度。 |
| `pickup-delay` | 未设置 | 掉落物的可选拾取延迟，单位为 tick。 |
| `drop-overflow` | `true` | 背包放不下时，在玩家位置自然掉落剩余物品。 |

### 示例

```yaml
type: give_loot_table_item
target: PLAYER
loot-table: minecraft:gameplay/piglin_bartering
amount: 1
random: 0.25
```

```yaml
type: give_loot_table_item
delivery: DROP
location: SOURCE
loot-table: minecraft:gameplay/piglin_bartering
amount: 1
```

### 行为与限制

* 缺少 `loot-table` 或键无效时会跳过执行。
* 战利品上下文中的 `lootedEntity` 会在可用时取自触发器的目标实体。
* `drop-overflow` 为 `false` 时，不会把背包剩余物生成到世界中。

---

## `preserve_inventory`

**用途：**使玩家在死亡事件中保留库存和/或经验。

**上下文：**需要 `PlayerDeathEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `inventory` | `true` | 启用保留库存并清除事件掉落物。 |
| `experience` | `true` | 启用保留等级，并将掉落经验结果设置为零。 |

### 示例

```yaml
type: preserve_inventory
inventory: true
experience: true
```

### 行为与限制

* 此能力会直接修改死亡事件，应与游戏规则和其他保留库存插件共同测试。

---

## `preserve_item`

**用途：**使当前上下文物品在玩家死亡后得到保留。

**上下文：**需要作为 `SOURCE` 的玩家、`PlayerDeathEvent`，以及非空的 `context.item()`。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: preserve_item
```

### 行为与限制

* 会根据相似性从死亡掉落物中移除上下文物品的副本，并在一个 Tick 后返还。
* 玩家库存放不下的溢出物品会自然掉落在玩家所在位置。

---

## `preserve_experience`

**用途：**使玩家在死亡后保留一定比例的总经验点数。

**上下文：**需要作为 `SOURCE` 的玩家和 `PlayerDeathEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `ratio` | `0.5` | 保留比例，数值限制在 `0..1`。 |

### 示例

```yaml
type: preserve_experience
ratio: 0.75
```

### 行为与限制

* 保留的经验会从可修改的掉落经验结果中扣除。
* 保留的经验点数会在一个 Tick 后返还给玩家。

---

## `use_on`

**用途：**通过 NMS 对方块使用桥接，让手中物品或匹配的库存物品对方块执行使用操作。

**上下文：**需要玩家、受支持的手部、可解析的方块、有效方块面和 NMS 实现。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `player` | `SOURCE` | 玩家选择器。 |
| `hand` | `CONTEXT` | 可填写 `CONTEXT`、触发物品别名、`MAIN_HAND` 或 `OFF_HAND`。 |
| `block-offset.x/y/z` | `0` | 相对于上下文或解析方块的偏移。 |
| `face` | `UP` | 基本方向方块面；支持 `TOP` 和 `BOTTOM` 别名。 |
| `hit.x/y/z` | `0.5` | 方块内部的命中坐标，数值限制在 `0..1`。 |
| `inside` | `false` | 是否将命中视为发生在方块内部。 |
| `inventory-items / inventory-item` | 未设置 | 用于寻找临时使用库存物品的 MatchItemFormat。 |
| `success-abilities` | 空 | NMS 使用成功后执行的子能力。 |
| `failure-abilities` | 空 | 使用失败后执行的子能力。 |

### 示例

```yaml
type: use_on
player: SOURCE
hand: CONTEXT
face: UP
block-offset:
  y: -1
hit:
  x: 0.5
  y: 1
  z: 0.5
inventory-items:
  material:
    - BONE_MEAL
success-abilities:
  sound:
    type: sound
    sound: ITEM_BONE_MEAL_USE
```

### 行为与限制

* 匹配库存物品时，该物品会被临时移动到选中的手部，使用后剩余的物品堆会恢复到原槽位。
* 只接受主手和副手。
* 选中的成功或失败分支会使用解析出的方块上下文执行。
* 成功表示原版交互返回成功，不保证放置的方块能在后续破坏或物理更新后保留下来。补种时，可在 `success-abilities` 中显式执行 [`prevent_block_break`](blocks-world.md#prevent_block_break)，短暂保护新苗。

---

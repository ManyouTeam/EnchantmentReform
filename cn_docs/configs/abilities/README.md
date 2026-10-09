# 📂 能力

能力会在触发器条件和能力修改器之后执行。每个分类页面都会提供用途、确切上下文、带默认值的完整专用字段、可直接运行的 YAML 示例，以及行为与限制说明。

EnchantmentReform 注册了 **89 个内置能力键**。

## 参考页面

* [基础与视觉能力](fundamentals.md)
* [实体状态能力](entity-state.md)
* [战斗与移动能力](combat.md)
* [投射物能力](projectiles.md)
* [库存与装备能力](inventory-equipment.md)
* [铁砧激活能力](anvil.md)
* [流程控制能力](orchestration.md)
* [集成与目标选择能力](integration.md)
* [方块与世界能力](blocks-world.md)

## 通用格式

```yaml
abilities:
  unique-entry-id:
    type: damage_entity
    target: TARGET
    amount: '2 + level'
    conditions: {}
    random: 1
    cooldown: 0
    times: 0
```

## 通用字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `type` | 必填 | 已注册的能力键。键不区分大小写，且 `-` 会被规范化为 `_`。 |
| `target` | 由能力决定 | 选择 `SOURCE`、`SKILL` 或 `TARGET` 等实体角色。 |
| `source` | 使用时默认为 `SOURCE` | 用于归属、方向、发射者、所有者或其他来源角色。 |
| `conditions` | 空 | 带类型的能力条件；每个子条件都必须通过。 |
| `random` | `1` | 执行概率。 |
| `cooldown` | `0` | 按来源、能力和配置路径分别计算的冷却时间，单位为秒。 |
| `times` | `0` | 此配置路径允许的最大成功尝试次数；零表示无限制。 |
| `cooldown-message` | 空 | 可选的冷却提示消息，可使用 `{remaining}` 和 `{power_id}`。 |
| `location` | `CONTEXT` | 扁平或嵌套的扩展位置选择器。 |
| `location.offset.x/y/z` | `0` | 解析位置后应用的偏移量。 |
| `item` | `CONTEXT` | 物品相关能力使用的扁平或嵌套物品选择器。 |
| `item-holder` | 由能力决定 | 物品选择器使用的装备持有者。 |

## 通用执行顺序

1. 能力自身的带类型条件。
2. 使用次数检查。
3. 已存在的冷却检查。
4. 随机概率判定。
5. 获取冷却。
6. 增加使用次数。
7. 执行该类型的专用实现。

由于通用冷却和次数状态会在能力的专用实现之前获取，因此当所需的事件、实体、物品、方块或集成上下文不存在时，能力可能已经消耗一次尝试，随后才跳过执行。

## 动态数值

受支持的字段可以解析 `level`、能力变量、等级选择映射、数字范围、运行时占位符，以及存在玩家上下文时的 PlaceholderAPI 占位符。每个能力条目都会注明重要的显式占位符。

## 类型索引

| 类型 | 参考页面 |
| --- | --- |
| `mark` | [基础与视觉能力](fundamentals.md#mark) |
| `cancel_event` | [基础与视觉能力](fundamentals.md#cancel-event) |
| `remove` | [基础与视觉能力](fundamentals.md#remove) |
| `place_block` | [基础与视觉能力](fundamentals.md#place-block) |
| `place_temp_block` | [基础与视觉能力](fundamentals.md#place-temp-block) |
| `particle` | [基础与视觉能力](fundamentals.md#particle) |
| `sound` | [基础与视觉能力](fundamentals.md#sound) |
| `vanilla_animation` | [基础与视觉能力](fundamentals.md#vanilla-animation) |
| `set_attribute` | [实体状态能力](entity-state.md#set-attribute) |
| `refresh_attribute` | [实体状态能力](entity-state.md#refresh-attribute) |
| `set_health` | [实体状态能力](entity-state.md#set-health) |
| `set_absorption` | [实体状态能力](entity-state.md#set-absorption) |
| `attribute_layer` | [实体状态能力](entity-state.md#attribute-layer) |
| `set_air` | [实体状态能力](entity-state.md#set-air) |
| `set_food` | [实体状态能力](entity-state.md#set-food) |
| `set_item_cooldown` | [实体状态能力](entity-state.md#set-item-cooldown) |
| `set_velocity` | [实体状态能力](entity-state.md#set-velocity) |
| `set_invulnerable` | [实体状态能力](entity-state.md#set-invulnerable) |
| `potion_effect` | [实体状态能力](entity-state.md#potion-effect) |
| `remove_potion_effect` | [实体状态能力](entity-state.md#remove-potion-effect) |
| `extend_potion_effects` | [实体状态能力](entity-state.md#extend-potion-effects) |
| `potion_cloud` | [实体状态能力](entity-state.md#potion-cloud) |
| `freeze` | [实体状态能力](entity-state.md#freeze) |
| `fire` | [实体状态能力](entity-state.md#fire) |
| `experience` | [实体状态能力](entity-state.md#experience) |
| `skill_experience` | [实体状态能力](entity-state.md#skill-experience) |
| `explosion` | [战斗与移动能力](combat.md#explosion) |
| `lightning` | [战斗与移动能力](combat.md#lightning) |
| `damage_entity` | [战斗与移动能力](combat.md#damage-entity) |
| `damage_item` | [战斗与移动能力](combat.md#damage-item) |
| `pull_target` | [战斗与移动能力](combat.md#pull-target) |
| `pull_location` | [战斗与移动能力](combat.md#pull-location) |
| `teleport_near_target` | [战斗与移动能力](combat.md#teleport-near-target) |
| `teleport` | [战斗与移动能力](combat.md#teleport) |
| `guardian_beam` | [战斗与移动能力](combat.md#guardian-beam) |
| `sonic_boom` | [战斗与移动能力](combat.md#sonic-boom) |
| `evoker_fangs` | [战斗与移动能力](combat.md#evoker-fangs) |
| `arrow_rain` | [战斗与移动能力](combat.md#arrow-rain) |
| `creeper_stats` | [战斗与移动能力](combat.md#creeper-stats) |
| `swap_health` | [战斗与移动能力](combat.md#swap-health) |
| `swap_potion_effects` | [战斗与移动能力](combat.md#swap-potion-effects) |
| `swap_locations` | [战斗与移动能力](combat.md#swap-locations) |
| `launch_projectile` | [投射物能力](projectiles.md#launch-projectile) |
| `ricochet_projectile` | [投射物能力](projectiles.md#ricochet-projectile) |
| `shulker_bullet` | [投射物能力](projectiles.md#shulker-bullet) |
| `homing_projectile` | [投射物能力](projectiles.md#homing-projectile) |
| `consume_food` | [库存与装备能力](inventory-equipment.md#consume-food) |
| `auto_feed` | [库存与装备能力](inventory-equipment.md#auto-feed) |
| `enhance_equipment` | [库存与装备能力](inventory-equipment.md#enhance-equipment) |
| `enhance_helditem` | [库存与装备能力](inventory-equipment.md#enhance-helditem) |
| `replace_item` | [库存与装备能力](inventory-equipment.md#replace-item) |
| `shuffle_inventory` | [库存与装备能力](inventory-equipment.md#shuffle-inventory) |
| `change_item` | [库存与装备能力](inventory-equipment.md#change-item) |
| `modify_repair_cost` | [铁砧激活能力](anvil.md#modify-repair-cost) |
| `cost_price` | [库存与装备能力](inventory-equipment.md#cost-price) |
| `drop_item` | [库存与装备能力](inventory-equipment.md#drop-item) |
| `give_item` | [库存与装备能力](inventory-equipment.md#give-item) |
| `give_loot_table_item` | [库存与装备能力](inventory-equipment.md#give-loot-table-item) |
| `preserve_inventory` | [库存与装备能力](inventory-equipment.md#preserve-inventory) |
| `preserve_item` | [库存与装备能力](inventory-equipment.md#preserve-item) |
| `preserve_experience` | [库存与装备能力](inventory-equipment.md#preserve-experience) |
| `use_on` | [库存与装备能力](inventory-equipment.md#use-on) |
| `delay` | [流程控制能力](orchestration.md#delay) |
| `conditional` | [流程控制能力](orchestration.md#conditional) |
| `any_of` | [流程控制能力](orchestration.md#any-of) |
| `limit` | [流程控制能力](orchestration.md#limit) |
| `repeat` | [流程控制能力](orchestration.md#repeat) |
| `nearby_entities` | [集成与目标选择能力](integration.md#nearby-entities) |
| `nearby_block` | [方块与世界能力](blocks-world.md#nearby-block) |
| `summon` | [集成与目标选择能力](integration.md#summon) |
| `mythic_skill` | [集成与目标选择能力](integration.md#mythic-skill) |
| `send_message` | [集成与目标选择能力](integration.md#send-message) |
| `execute_command` | [集成与目标选择能力](integration.md#execute-command) |
| `execute_action` | [集成与目标选择能力](integration.md#execute-action) |
| `break_blocks` | [方块与世界能力](blocks-world.md#break-blocks) |
| `break_block` | [方块与世界能力](blocks-world.md#break-block) |
| `replace_block` | [方块与世界能力](blocks-world.md#replace-block) |
| `change_block_face` | [方块与世界能力](blocks-world.md#change-block-face) |
| `grow_crop` | [方块与世界能力](blocks-world.md#grow-crop) |
| `scan_blocks` | [方块与世界能力](blocks-world.md#scan-blocks) |
| `locate_structure` | [方块与世界能力](blocks-world.md#locate-structure) |
| `locate_biome` | [方块与世界能力](blocks-world.md#locate-biome) |
| `state` | [流程控制能力](orchestration.md#state) |
| `auto_fishing` | [集成与目标选择能力](integration.md#auto-fishing) |
| `disable_enchantments` | [集成与目标选择能力](integration.md#disable-enchantments) |

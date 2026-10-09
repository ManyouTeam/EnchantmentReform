# ✅ 能力条件

能力条件用于决定触发器、能力修改器或能力能否继续执行。普通的 `conditions` 映射使用逻辑 **AND（与）**：其中每个具名子条件都必须匹配。

EnchantmentReform 注册了 **79 个内置条件键**，本参考文档会对它们进行说明。

## 参考页面

* [逻辑与随机条件](logical-random.md)
* [实体与状态条件](entity-state.md)
* [物品与耐久度条件](item-durability.md)
* [环境与位置条件](environment.md)
* [触发器数据条件](trigger-data.md)

## 通用格式

```yaml
conditions:
  unique-entry-id:
    type: health_percent
    target: TARGET
    max: 50
    not: false
```

## 通用字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `type` | 必填 | 已注册的条件键。键不区分大小写，且 `-` 会被规范化为 `_`。 |
| `not` | `false` | 反转该条件最终返回的结果。 |
| `target` | 由类型决定 | 当条件实现提供实体选择器时，可使用 `SOURCE`、`SKILL` 或 `TARGET` 等选择器。 |
| `meet-message` | 空 | 条件匹配时，可选择向解析出的玩家发送消息。 |
| `not-meet-message` | 空 | 条件不匹配时，可选择发送消息。 |

当条件所需的实体、物品、方块、位置、事件或可修改结果不存在时，该条件会返回 `false`。各分类页面会说明每种条件所需的确切上下文，以及其支持的所有专用字段。

## 检查时机

触发器层级的条件会在能力修改器和能力之前执行。附加在某个能力修改器或能力上的条件，则会在执行到该条目时检查。因此，读取结果的条件可以观察同一触发器部分中更早执行的能力修改器所产生的变化。

## 类型索引

| 类型 | 参考页面 |
| --- | --- |
| `any` | [逻辑与随机条件](logical-random.md#any) |
| `not` | [逻辑与随机条件](logical-random.md#not) |
| `random` | [逻辑与随机条件](logical-random.md#random) |
| `player_conditions` | [逻辑与随机条件](logical-random.md#player-conditions) |
| `headshot` | [实体与状态条件](entity-state.md#headshot) |
| `height` | [实体与状态条件](entity-state.md#height) |
| `sneaking` | [实体与状态条件](entity-state.md#sneaking) |
| `not_sneaking` | [实体与状态条件](entity-state.md#not-sneaking) |
| `health` | [实体与状态条件](entity-state.md#health) |
| `health_percent` | [实体与状态条件](entity-state.md#health-percent) |
| `has_potion` | [实体与状态条件](entity-state.md#has-potion) |
| `distance` | [实体与状态条件](entity-state.md#distance) |
| `bounding_box_distance` | [实体与状态条件](entity-state.md#bounding-box-distance) |
| `food_level` | [实体与状态条件](entity-state.md#food-level) |
| `attribute_value` | [实体与状态条件](entity-state.md#attribute-value) |
| `skill_level` | [实体与状态条件](entity-state.md#skill-level) |
| `gliding` | [实体与状态条件](entity-state.md#gliding) |
| `on_ground` | [实体与状态条件](entity-state.md#on-ground) |
| `in_air` | [实体与状态条件](entity-state.md#in-air) |
| `falling` | [实体与状态条件](entity-state.md#falling) |
| `fall_distance` | [实体与状态条件](entity-state.md#fall-distance) |
| `match_entity` | [实体与状态条件](entity-state.md#match-entity) |
| `match_item` | [实体与状态条件](entity-state.md#match-item) |
| `item_damage` | [物品与耐久度条件](item-durability.md#item-damage) |
| `catch_match_item` | [实体与状态条件](entity-state.md#catch-match-item) |
| `min_attack_cooldown` | [实体与状态条件](entity-state.md#min-attack-cooldown) |
| `blocking` | [实体与状态条件](entity-state.md#blocking) |
| `perfect_guard` | [实体与状态条件](entity-state.md#perfect-guard) |
| `sprinting` | [实体与状态条件](entity-state.md#sprinting) |
| `potion_effect_type` | [实体与状态条件](entity-state.md#potion-effect-type) |
| `effect_type` | [实体与状态条件](entity-state.md#effect-type) |
| `potion_effect_cause` | [实体与状态条件](entity-state.md#potion-effect-cause) |
| `potion_effect_action` | [实体与状态条件](entity-state.md#potion-effect-action) |
| `ageable` | [实体与状态条件](entity-state.md#ageable) |
| `first_attack_against_entity` | [实体与状态条件](entity-state.md#first-attack-against-entity) |
| `first_attack_from_monster` | [实体与状态条件](entity-state.md#first-attack-from-monster) |
| `first_monster_attack` | [实体与状态条件](entity-state.md#first-monster-attack) |
| `target_category` | [实体与状态条件](entity-state.md#target-category) |
| `fatal_damage` | [实体与状态条件](entity-state.md#fatal-damage) |
| `stationary` | [实体与状态条件](entity-state.md#stationary) |
| `in_water` | [环境与位置条件](environment.md#in-water) |
| `in_lava` | [环境与位置条件](environment.md#in-lava) |
| `liquid_surface_distance` | [环境与位置条件](environment.md#liquid-surface-distance) |
| `liquid_transition` | [环境与位置条件](environment.md#liquid-transition) |
| `in_rain` | [环境与位置条件](environment.md#in-rain) |
| `in_sunlight` | [环境与位置条件](environment.md#in-sunlight) |
| `in_structure` | [环境与位置条件](environment.md#in-structure) |
| `light_level` | [环境与位置条件](environment.md#light-level) |
| `clear_weather` | [环境与位置条件](environment.md#clear-weather) |
| `storm` | [环境与位置条件](environment.md#storm) |
| `thunder` | [环境与位置条件](environment.md#thunder) |
| `world` | [环境与位置条件](environment.md#world) |
| `night` | [环境与位置条件](environment.md#night) |
| `environment` | [环境与位置条件](environment.md#environment) |
| `block_type` | [环境与位置条件](environment.md#block-type) |
| `block_type_offset` | [环境与位置条件](environment.md#block-type-offset) |
| `best_tool` | [环境与位置条件](environment.md#best-tool) |
| `preferred_tool` | [环境与位置条件](environment.md#preferred-tool) |
| `biome_changed` | [环境与位置条件](environment.md#biome-changed) |
| `block_break_time` | [环境与位置条件](environment.md#block-break-time) |
| `break_time` | [环境与位置条件](environment.md#break-time) |
| `damage_value` | [触发器数据条件](trigger-data.md#damage-value) |
| `damage_cause` | [触发器数据条件](trigger-data.md#damage-cause) |
| `damage_origin` | [触发器数据条件](trigger-data.md#damage-origin) |
| `spawn_reason` | [触发器数据条件](trigger-data.md#spawn-reason) |
| `combust_duration` | [触发器数据条件](trigger-data.md#combust-duration) |
| `regain_amount` | [触发器数据条件](trigger-data.md#regain-amount) |
| `explosion_yield` | [触发器数据条件](trigger-data.md#explosion-yield) |
| `explosion_radius` | [触发器数据条件](trigger-data.md#explosion-radius) |
| `target_reason` | [触发器数据条件](trigger-data.md#target-reason) |
| `interaction_action` | [触发器数据条件](trigger-data.md#interaction-action) |
| `input_type` | [触发器数据条件](trigger-data.md#input-type) |
| `event_state` | [触发器数据条件](trigger-data.md#event-state) |
| `smash_attack_lands` | [触发器数据条件](trigger-data.md#smash-attack-lands) |
| `food_change` | [触发器数据条件](trigger-data.md#food-change) |
| `air_change` | [触发器数据条件](trigger-data.md#air-change) |
| `state_value` | [触发器数据条件](trigger-data.md#state-value) |
| `combust_origin` | [触发器数据条件](trigger-data.md#combust-origin) |
| `knockback_cause` | [触发器数据条件](trigger-data.md#knockback-cause) |
| `knockback_reason` | [触发器数据条件](trigger-data.md#knockback-reason) |
| `exhaustion_reason` | [触发器数据条件](trigger-data.md#exhaustion-reason) |
| `villager_trade` | [触发器数据条件](trigger-data.md#villager-trade) |
| `cooldown` | [触发器数据条件](trigger-data.md#cooldown) |
| `cooldown_material` | [触发器数据条件](trigger-data.md#cooldown-material) |

# ✅ Power Conditions

Power Conditions decide whether a trigger, modifier, or ability may continue. A normal `conditions` mapping is logical **AND**: every named child must match.

EnchantmentReform registers **79 built-in condition keys** documented in this reference.

## Reference pages

* [Logical and Random Conditions](logical-random.md)
* [Entity and State Conditions](entity-state.md)
* [Item and Durability Conditions](item-durability.md)
* [Environment and Location Conditions](environment.md)
* [Trigger-data Conditions](trigger-data.md)

## Common format

```yaml
conditions:
  unique-entry-id:
    type: health_percent
    target: TARGET
    max: 50
    not: false
```

## Common fields

| Field | Default | Description |
| --- | --- | --- |
| `type` | required | Registered condition key. Keys are case-insensitive and `-` is normalized to `_`. |
| `not` | `false` | Inverts the final result returned by the condition. |
| `target` | type-specific | Entity selector such as `SOURCE`, `SKILL`, or `TARGET` when the implementation exposes one. |
| `meet-message` | empty | Optional message sent to the resolved player when the condition matches. |
| `not-meet-message` | empty | Optional message sent when the condition does not match. |

A condition returns `false` when its required entity, item, block, location, event, or mutable result is missing. The category pages state the exact required context and every supported type-specific field.

## Evaluation timing

Trigger-level conditions run before modifiers and abilities. Conditions attached to a modifier or ability run when that entry is reached. A result-reading condition can therefore observe changes made by earlier modifiers in the same trigger section.

## Type index

| Type | Reference |
| --- | --- |
| `any` | [Logical and Random Conditions](logical-random.md#any) |
| `not` | [Logical and Random Conditions](logical-random.md#not) |
| `random` | [Logical and Random Conditions](logical-random.md#random) |
| `player_conditions` | [Logical and Random Conditions](logical-random.md#player-conditions) |
| `headshot` | [Entity and State Conditions](entity-state.md#headshot) |
| `height` | [Entity and State Conditions](entity-state.md#height) |
| `sneaking` | [Entity and State Conditions](entity-state.md#sneaking) |
| `not_sneaking` | [Entity and State Conditions](entity-state.md#not-sneaking) |
| `health` | [Entity and State Conditions](entity-state.md#health) |
| `health_percent` | [Entity and State Conditions](entity-state.md#health-percent) |
| `has_potion` | [Entity and State Conditions](entity-state.md#has-potion) |
| `distance` | [Entity and State Conditions](entity-state.md#distance) |
| `bounding_box_distance` | [Entity and State Conditions](entity-state.md#bounding-box-distance) |
| `food_level` | [Entity and State Conditions](entity-state.md#food-level) |
| `attribute_value` | [Entity and State Conditions](entity-state.md#attribute-value) |
| `skill_level` | [Entity and State Conditions](entity-state.md#skill-level) |
| `gliding` | [Entity and State Conditions](entity-state.md#gliding) |
| `on_ground` | [Entity and State Conditions](entity-state.md#on-ground) |
| `in_air` | [Entity and State Conditions](entity-state.md#in-air) |
| `falling` | [Entity and State Conditions](entity-state.md#falling) |
| `fall_distance` | [Entity and State Conditions](entity-state.md#fall-distance) |
| `match_entity` | [Entity and State Conditions](entity-state.md#match-entity) |
| `match_item` | [Entity and State Conditions](entity-state.md#match-item) |
| `item_damage` | [Item and Durability Conditions](item-durability.md#item-damage) |
| `catch_match_item` | [Entity and State Conditions](entity-state.md#catch-match-item) |
| `min_attack_cooldown` | [Entity and State Conditions](entity-state.md#min-attack-cooldown) |
| `blocking` | [Entity and State Conditions](entity-state.md#blocking) |
| `perfect_guard` | [Entity and State Conditions](entity-state.md#perfect-guard) |
| `sprinting` | [Entity and State Conditions](entity-state.md#sprinting) |
| `potion_effect_type` | [Entity and State Conditions](entity-state.md#potion-effect-type) |
| `effect_type` | [Entity and State Conditions](entity-state.md#effect-type) |
| `potion_effect_cause` | [Entity and State Conditions](entity-state.md#potion-effect-cause) |
| `potion_effect_action` | [Entity and State Conditions](entity-state.md#potion-effect-action) |
| `ageable` | [Entity and State Conditions](entity-state.md#ageable) |
| `first_attack_against_entity` | [Entity and State Conditions](entity-state.md#first-attack-against-entity) |
| `first_attack_from_monster` | [Entity and State Conditions](entity-state.md#first-attack-from-monster) |
| `first_monster_attack` | [Entity and State Conditions](entity-state.md#first-monster-attack) |
| `target_category` | [Entity and State Conditions](entity-state.md#target-category) |
| `fatal_damage` | [Entity and State Conditions](entity-state.md#fatal-damage) |
| `stationary` | [Entity and State Conditions](entity-state.md#stationary) |
| `in_water` | [Environment and Location Conditions](environment.md#in-water) |
| `in_lava` | [Environment and Location Conditions](environment.md#in-lava) |
| `liquid_surface_distance` | [Environment and Location Conditions](environment.md#liquid-surface-distance) |
| `liquid_transition` | [Environment and Location Conditions](environment.md#liquid-transition) |
| `in_rain` | [Environment and Location Conditions](environment.md#in-rain) |
| `in_sunlight` | [Environment and Location Conditions](environment.md#in-sunlight) |
| `in_structure` | [Environment and Location Conditions](environment.md#in-structure) |
| `light_level` | [Environment and Location Conditions](environment.md#light-level) |
| `clear_weather` | [Environment and Location Conditions](environment.md#clear-weather) |
| `storm` | [Environment and Location Conditions](environment.md#storm) |
| `thunder` | [Environment and Location Conditions](environment.md#thunder) |
| `world` | [Environment and Location Conditions](environment.md#world) |
| `night` | [Environment and Location Conditions](environment.md#night) |
| `environment` | [Environment and Location Conditions](environment.md#environment) |
| `block_type` | [Environment and Location Conditions](environment.md#block-type) |
| `block_type_offset` | [Environment and Location Conditions](environment.md#block-type-offset) |
| `best_tool` | [Environment and Location Conditions](environment.md#best-tool) |
| `preferred_tool` | [Environment and Location Conditions](environment.md#preferred-tool) |
| `biome_changed` | [Environment and Location Conditions](environment.md#biome-changed) |
| `block_break_time` | [Environment and Location Conditions](environment.md#block-break-time) |
| `break_time` | [Environment and Location Conditions](environment.md#break-time) |
| `damage_value` | [Trigger-data Conditions](trigger-data.md#damage-value) |
| `damage_cause` | [Trigger-data Conditions](trigger-data.md#damage-cause) |
| `damage_origin` | [Trigger-data Conditions](trigger-data.md#damage-origin) |
| `spawn_reason` | [Trigger-data Conditions](trigger-data.md#spawn-reason) |
| `combust_duration` | [Trigger-data Conditions](trigger-data.md#combust-duration) |
| `regain_amount` | [Trigger-data Conditions](trigger-data.md#regain-amount) |
| `explosion_yield` | [Trigger-data Conditions](trigger-data.md#explosion-yield) |
| `explosion_radius` | [Trigger-data Conditions](trigger-data.md#explosion-radius) |
| `target_reason` | [Trigger-data Conditions](trigger-data.md#target-reason) |
| `interaction_action` | [Trigger-data Conditions](trigger-data.md#interaction-action) |
| `input_type` | [Trigger-data Conditions](trigger-data.md#input-type) |
| `event_state` | [Trigger-data Conditions](trigger-data.md#event-state) |
| `smash_attack_lands` | [Trigger-data Conditions](trigger-data.md#smash-attack-lands) |
| `food_change` | [Trigger-data Conditions](trigger-data.md#food-change) |
| `air_change` | [Trigger-data Conditions](trigger-data.md#air-change) |
| `state_value` | [Trigger-data Conditions](trigger-data.md#state-value) |
| `combust_origin` | [Trigger-data Conditions](trigger-data.md#combust-origin) |
| `knockback_cause` | [Trigger-data Conditions](trigger-data.md#knockback-cause) |
| `knockback_reason` | [Trigger-data Conditions](trigger-data.md#knockback-reason) |
| `exhaustion_reason` | [Trigger-data Conditions](trigger-data.md#exhaustion-reason) |
| `villager_trade` | [Trigger-data Conditions](trigger-data.md#villager-trade) |
| `cooldown` | [Trigger-data Conditions](trigger-data.md#cooldown) |
| `cooldown_material` | [Trigger-data Conditions](trigger-data.md#cooldown-material) |

# 📂 Abilities

Abilities run after trigger conditions and Power Modifiers. Each category page provides purpose, exact context, complete type-specific fields with defaults, a runnable YAML example, and behavior/limits.

EnchantmentReform registers **89 built-in ability keys**.

## Reference pages

* [Fundamental and Visual Abilities](fundamentals.md)
* [Entity State Abilities](entity-state.md)
* [Combat and Movement Abilities](combat.md)
* [Projectile Abilities](projectiles.md)
* [Inventory and Equipment Abilities](inventory-equipment.md)
* [Anvil Activation Abilities](anvil.md)
* [Control-flow Abilities](orchestration.md)
* [Integration and Targeting Abilities](integration.md)
* [Block and World Abilities](blocks-world.md)

## Common format

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

## Common fields

| Field | Default | Description |
| --- | --- | --- |
| `type` | required | Registered ability key. Keys are case-insensitive and `-` is normalized to `_`. |
| `target` | ability-specific | Selects an entity role such as `SOURCE`, `SKILL`, or `TARGET`. |
| `source` | `SOURCE` where used | Attribution, direction, shooter, owner, or another source role. |
| `conditions` | empty | Typed Power Conditions; every child must pass. |
| `random` | `1` | Execution probability. |
| `cooldown` | `0` | Per-source, per-power, per-config-path cooldown in seconds. |
| `times` | `0` | Maximum successful attempts for this configured path; zero is unlimited. |
| `cooldown-message` | empty | Optional cooldown message with `{remaining}` and `{power_id}`. |
| `location` | `CONTEXT` | Flat or nested extended location selector. |
| `location.offset.x/y/z` | `0` | Offset applied after resolving the location. |
| `item` | `CONTEXT` | Flat or nested item selector used by item-aware abilities. |
| `item-holder` | ability-specific | Equipment holder for item selectors. |

## Common execution order

1. Ability-local typed conditions.
2. Usage-count check.
3. Existing cooldown check.
4. Random roll.
5. Cooldown acquisition.
6. Usage-count increment.
7. Type-specific implementation.

Because common cooldown/count state is acquired before type-specific execution, an ability can consume an attempt and then skip when required event/entity/item/block/integration context is unavailable.

## Dynamic values

Supported fields may resolve `level`, power variables, level selector maps, numeric ranges, runtime placeholders, and PlaceholderAPI where a player context exists. Each entry documents important explicit placeholders.

## Type index

| Type | Reference |
| --- | --- |
| `mark` | [Fundamental and Visual Abilities](fundamentals.md#mark) |
| `cancel_event` | [Fundamental and Visual Abilities](fundamentals.md#cancel-event) |
| `remove` | [Fundamental and Visual Abilities](fundamentals.md#remove) |
| `place_block` | [Fundamental and Visual Abilities](fundamentals.md#place-block) |
| `place_temp_block` | [Fundamental and Visual Abilities](fundamentals.md#place-temp-block) |
| `particle` | [Fundamental and Visual Abilities](fundamentals.md#particle) |
| `sound` | [Fundamental and Visual Abilities](fundamentals.md#sound) |
| `vanilla_animation` | [Fundamental and Visual Abilities](fundamentals.md#vanilla-animation) |
| `set_attribute` | [Entity State Abilities](entity-state.md#set-attribute) |
| `refresh_attribute` | [Entity State Abilities](entity-state.md#refresh-attribute) |
| `set_health` | [Entity State Abilities](entity-state.md#set-health) |
| `set_absorption` | [Entity State Abilities](entity-state.md#set-absorption) |
| `attribute_layer` | [Entity State Abilities](entity-state.md#attribute-layer) |
| `set_air` | [Entity State Abilities](entity-state.md#set-air) |
| `set_food` | [Entity State Abilities](entity-state.md#set-food) |
| `set_item_cooldown` | [Entity State Abilities](entity-state.md#set-item-cooldown) |
| `set_velocity` | [Entity State Abilities](entity-state.md#set-velocity) |
| `set_invulnerable` | [Entity State Abilities](entity-state.md#set-invulnerable) |
| `potion_effect` | [Entity State Abilities](entity-state.md#potion-effect) |
| `remove_potion_effect` | [Entity State Abilities](entity-state.md#remove-potion-effect) |
| `extend_potion_effects` | [Entity State Abilities](entity-state.md#extend-potion-effects) |
| `potion_cloud` | [Entity State Abilities](entity-state.md#potion-cloud) |
| `freeze` | [Entity State Abilities](entity-state.md#freeze) |
| `fire` | [Entity State Abilities](entity-state.md#fire) |
| `experience` | [Entity State Abilities](entity-state.md#experience) |
| `skill_experience` | [Entity State Abilities](entity-state.md#skill-experience) |
| `explosion` | [Combat and Movement Abilities](combat.md#explosion) |
| `lightning` | [Combat and Movement Abilities](combat.md#lightning) |
| `damage_entity` | [Combat and Movement Abilities](combat.md#damage-entity) |
| `damage_item` | [Combat and Movement Abilities](combat.md#damage-item) |
| `pull_target` | [Combat and Movement Abilities](combat.md#pull-target) |
| `pull_location` | [Combat and Movement Abilities](combat.md#pull-location) |
| `teleport_near_target` | [Combat and Movement Abilities](combat.md#teleport-near-target) |
| `teleport` | [Combat and Movement Abilities](combat.md#teleport) |
| `guardian_beam` | [Combat and Movement Abilities](combat.md#guardian-beam) |
| `sonic_boom` | [Combat and Movement Abilities](combat.md#sonic-boom) |
| `evoker_fangs` | [Combat and Movement Abilities](combat.md#evoker-fangs) |
| `arrow_rain` | [Combat and Movement Abilities](combat.md#arrow-rain) |
| `creeper_stats` | [Combat and Movement Abilities](combat.md#creeper-stats) |
| `swap_health` | [Combat and Movement Abilities](combat.md#swap-health) |
| `swap_potion_effects` | [Combat and Movement Abilities](combat.md#swap-potion-effects) |
| `swap_locations` | [Combat and Movement Abilities](combat.md#swap-locations) |
| `launch_projectile` | [Projectile Abilities](projectiles.md#launch-projectile) |
| `ricochet_projectile` | [Projectile Abilities](projectiles.md#ricochet-projectile) |
| `shulker_bullet` | [Projectile Abilities](projectiles.md#shulker-bullet) |
| `homing_projectile` | [Projectile Abilities](projectiles.md#homing-projectile) |
| `consume_food` | [Inventory and Equipment Abilities](inventory-equipment.md#consume-food) |
| `auto_feed` | [Inventory and Equipment Abilities](inventory-equipment.md#auto-feed) |
| `enhance_equipment` | [Inventory and Equipment Abilities](inventory-equipment.md#enhance-equipment) |
| `enhance_helditem` | [Inventory and Equipment Abilities](inventory-equipment.md#enhance-helditem) |
| `replace_item` | [Inventory and Equipment Abilities](inventory-equipment.md#replace-item) |
| `shuffle_inventory` | [Inventory and Equipment Abilities](inventory-equipment.md#shuffle-inventory) |
| `change_item` | [Inventory and Equipment Abilities](inventory-equipment.md#change-item) |
| `modify_repair_cost` | [Anvil Activation Abilities](anvil.md#modify-repair-cost) |
| `cost_price` | [Inventory and Equipment Abilities](inventory-equipment.md#cost-price) |
| `drop_item` | [Inventory and Equipment Abilities](inventory-equipment.md#drop-item) |
| `give_item` | [Inventory and Equipment Abilities](inventory-equipment.md#give-item) |
| `give_loot_table_item` | [Inventory and Equipment Abilities](inventory-equipment.md#give-loot-table-item) |
| `preserve_inventory` | [Inventory and Equipment Abilities](inventory-equipment.md#preserve-inventory) |
| `preserve_item` | [Inventory and Equipment Abilities](inventory-equipment.md#preserve-item) |
| `preserve_experience` | [Inventory and Equipment Abilities](inventory-equipment.md#preserve-experience) |
| `use_on` | [Inventory and Equipment Abilities](inventory-equipment.md#use-on) |
| `delay` | [Control-flow Abilities](orchestration.md#delay) |
| `conditional` | [Control-flow Abilities](orchestration.md#conditional) |
| `any_of` | [Control-flow Abilities](orchestration.md#any-of) |
| `limit` | [Control-flow Abilities](orchestration.md#limit) |
| `repeat` | [Control-flow Abilities](orchestration.md#repeat) |
| `nearby_entities` | [Integration and Targeting Abilities](integration.md#nearby-entities) |
| `nearby_block` | [Block and World Abilities](blocks-world.md#nearby-block) |
| `summon` | [Integration and Targeting Abilities](integration.md#summon) |
| `mythic_skill` | [Integration and Targeting Abilities](integration.md#mythic-skill) |
| `send_message` | [Integration and Targeting Abilities](integration.md#send-message) |
| `execute_command` | [Integration and Targeting Abilities](integration.md#execute-command) |
| `execute_action` | [Integration and Targeting Abilities](integration.md#execute-action) |
| `break_blocks` | [Block and World Abilities](blocks-world.md#break-blocks) |
| `break_block` | [Block and World Abilities](blocks-world.md#break-block) |
| `replace_block` | [Block and World Abilities](blocks-world.md#replace-block) |
| `change_block_face` | [Block and World Abilities](blocks-world.md#change-block-face) |
| `grow_crop` | [Block and World Abilities](blocks-world.md#grow-crop) |
| `scan_blocks` | [Block and World Abilities](blocks-world.md#scan-blocks) |
| `locate_structure` | [Block and World Abilities](blocks-world.md#locate-structure) |
| `locate_biome` | [Block and World Abilities](blocks-world.md#locate-biome) |
| `state` | [Control-flow Abilities](orchestration.md#state) |
| `auto_fishing` | [Integration and Targeting Abilities](integration.md#auto-fishing) |
| `disable_enchantments` | [Integration and Targeting Abilities](integration.md#disable-enchantments) |

# 🔢 Counting modes

`count` selects a unit, and `conditions` filter contributing events, blocks, or outputs. Skill sources and enchantment powers use the same measurement rules. The default `count: events` contributes one per matching event; set `count: damage` explicitly when XP should scale with damage.

## Supported modes

All built-in triggers support `events`. The table lists additional modes. Unlisted triggers support only `events`; each trigger reference also lists its allowed values.

| `count` | Supported triggers | Quantity |
| --- | --- | --- |
| `items` | `fish`, `block_drop_item`, `piglin_barter`, `trade`, `purchase`, `kill` | Sum of caught, dropped, or output item stack quantities. |
| `blocks` | `block_place` | Blocks involved in the placement, with conditions evaluated per block. |
| `damage` | `attack`, `melee_attack`, `damage`, `damage_by_entity` | Final event damage after modifiers such as armor; not capped by remaining health. |
| `blocked_damage` | `shield_block` | Damage actually blocked by the shield. |
| `healing` | `regain` | Positive healing capped by missing health. |
| `durability` | `item_damage` | Event wear capped by remaining item durability. |
| `experience` | `exp_change`, `pickup_experience` | Positive Minecraft XP points, not levels. |
| `distance` | `move` | Three-dimensional straight-line distance between event positions, in blocks. |
| `exhaustion` | `exhaustion` | Positive exhaustion amounts. |

`death`, `block_break`, `air_change`, `food_level_change`, and `warden_anger_change` still support only `events`. An extra event value does not automatically make it a supported counting mode. For example, `block_break + items` is rejected; use `block_drop_item + items` for mining outputs.

## Batch settlement

Without `count-every`, each event settles its matching quantity. A positive `count-every` accumulates full batches and carries the remainder. With a threshold of 10, a total of 25 settles two batches and retains five. Up to six decimal places are supported. Incomplete batches neither award XP nor execute powers.

In skill formulas, `{amount}` is the current quantity without batching, or the number of complete batches with batching. Use `xp: 'unit_price * {amount}'` to scale XP. A fixed `xp: 5` still gives five per settlement; it is not multiplied automatically.

Powers execute their modifiers and abilities once per settlement. `{count_amount}` exposes the quantity or batch count; multiple batches do not repeat the entire ability group automatically. `{count_quantity}` is the current matching raw quantity; `{count_remainder}` is the remaining quantity after settlement.

UQ `amount` is an objective completion requirement. ER `count-every` is a repeatable batch size. UQ completes after reaching its requirement, while ER keeps accumulating further batches.

## Movement and repeated actions

This skill `sources` fragment gives 3.5 XP per ten sprinted blocks, 2.5 XP per ten swum blocks, and 5 XP per ten jumps:

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

Use existing `sprinting`, `swimming`, `flying`, `gliding`, or `riding` power conditions to select movement types, with `target: PLAYER`. Distance is the three-dimensional displacement of matching move events, including movement within one block. Rotation, teleports, and cross-world jumps contribute no distance. It does not read vanilla cumulative statistics or use `count-statistic`.

## Item conditions

This fragment awards XP for diamond mining outputs. A separate `TRIGGER_ITEM` condition can select the tool:

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

`COUNTED_ITEM` exists only during per-item condition evaluation for `count: items`. Each output stack is checked separately; only matching quantities contribute. Without item conditions, all outputs contribute. `CONTEXT` remains the active enchantment item and `TRIGGER_ITEM` remains the event item. Block quantity mode also evaluates conditions per block.

## Enchantment powers

The same fields work under `powers.on-...`. For example, trigger an ability after every ten matching sprinted blocks:

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

## State and execution order

Conditions run first, followed by one anti-abuse check per event, then accumulation and batch settlement. Powers still pass through `powers.limit`; batches already settled are not refunded when chance, cooldown, or usage limits prevent execution. Skill multipliers, XP rate limits, and pressure reductions still apply.

Remainders are independent per skill source or power trigger, with power counters also isolated by equipment slot. Changing movement state pauses contribution to a source without clearing its remainder. Logout, reload, and shutdown clear unsettled remainders.

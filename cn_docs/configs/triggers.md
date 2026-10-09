# ⚡ 能力触发器

触发器是附魔 `powers` 下的一个配置部分，用于决定能力何时执行，并创建供能力条件、能力修改器和能力使用的上下文。

```yaml
powers:
  on-attack:
    conditions: {}
    modifiers: {}
    abilities: {}
```

EnchantmentReform 目前注册了 **63 个内置触发器**。

## 参考页面

* [Tick 与生命周期触发器](triggers/lifecycle.md)
* [战斗、伤害与死亡触发器](triggers/combat.md)
* [投射物与远程触发器](triggers/projectiles.md)
* [方块与交互触发器](triggers/blocks-interactions.md)
* [物品、附魔与装备触发器](triggers/items-equipment.md)
* [移动与输入触发器](triggers/movement-input.md)
* [效果、空气、食物与经验触发器](triggers/player-state.md)
* [钓鱼与交易触发器](triggers/fishing-trading.md)
* [目标选择与特殊实体触发器](triggers/targeting-special.md)

## 通用上下文

每个触发器都会创建一个 `TriggerData` 上下文。各角色的实际含义取决于所使用的触发器。

| 上下文 | 含义 |
| --- | --- |
| `PLAYER` | 提供当前激活附魔物品的玩家。此上下文始终存在。 |
| `SOURCE` | 对事件负责的行为主体或所有者。 |
| `SKILL` | 直接载体或中间实体，例如投射物、鱼钩、烟花或监守者。 |
| `TARGET` | 最终受到影响或被检查的实体。 |
| `BLOCK` | 触发器提供的事件方块。 |
| `LOCATION` | 供位置相关条件和能力使用的主要事件位置。 |
| `FROM` / `TO` | 触发器提供的移动起点和终点。 |
| `TRIGGER_ITEM` / `TRIGGER_SLOT` | 选中当前激活附魔的物品和槽位。 |
| `EVENT` | 底层 Bukkit/Paper 事件。 |
| `TICK` | 周期性触发器使用的运行时 Tick。 |

`SOURCE`、`SKILL`、`TARGET`、`BLOCK`、移动位置和触发物品数据均为可选上下文。需要某个缺失上下文的条件或能力会安全失败或被跳过。

{% hint style="info" %}
不要假设同一个选择器在所有触发器中都具有相同含义。例如，`on-attack` 使用 `SOURCE` 表示发起攻击的玩家，使用 `SKILL` 表示直接造成伤害的投射物或伤害来源；而 `on-damage` 使用 `TARGET` 表示正在受到伤害的附魔玩家。
{% endhint %}

## 触发器列表

### Tick 与生命周期触发器

* [`on-tick`](triggers/lifecycle.md#on-tick)
* [`on-target-tick`](triggers/lifecycle.md#on-target-tick)
* [`on-spawn`](triggers/lifecycle.md#on-spawn)
* [`on-respawn`](triggers/lifecycle.md#on-respawn)
* [`on-activate`](triggers/lifecycle.md#on-activate)
* [`on-deactivate`](triggers/lifecycle.md#on-deactivate)

### 战斗、伤害与死亡触发器

* [`on-attack`](triggers/combat.md#on-attack)
* [`on-melee-attack`](triggers/combat.md#on-melee-attack)
* [`on-damage`](triggers/combat.md#on-damage)
* [`on-damage-by-entity`](triggers/combat.md#on-damage-by-entity)
* [`on-kill`](triggers/combat.md#on-kill)
* [`on-death`](triggers/combat.md#on-death)
* [`on-regain`](triggers/combat.md#on-regain)
* [`on-combust`](triggers/combat.md#on-combust)
* [`on-knockback`](triggers/combat.md#on-knockback)
* [`on-attempt-smash-attack`](triggers/combat.md#on-attempt-smash-attack)
* [`on-lunge`](triggers/combat.md#on-lunge)
* [`on-shield-block`](triggers/combat.md#on-shield-block)
* [`on-shield-disable`](triggers/combat.md#on-shield-disable)

### 投射物与远程触发器

* [`on-shoot`](triggers/projectiles.md#on-shoot)
* [`on-shoot-bow`](triggers/projectiles.md#on-shoot-bow)
* [`on-load-crossbow`](triggers/projectiles.md#on-load-crossbow)
* [`on-projectile-launch`](triggers/projectiles.md#on-projectile-launch)
* [`on-projectile-tick`](triggers/projectiles.md#on-projectile-tick)
* [`on-projectile-hit`](triggers/projectiles.md#on-projectile-hit)
* [`on-riptide`](triggers/projectiles.md#on-riptide)

### 方块与交互触发器

* [`on-block-break`](triggers/blocks-interactions.md#on-block-break)
* [`on-block-damage`](triggers/blocks-interactions.md#on-block-damage)
* [`on-block-break-progress-update`](triggers/blocks-interactions.md#on-block-break-progress-update)
* [`on-block-drop-item`](triggers/blocks-interactions.md#on-block-drop-item)
* [`on-block-place`](triggers/blocks-interactions.md#on-block-place)
* [`on-inside-block`](triggers/blocks-interactions.md#on-inside-block)
* [`on-interact`](triggers/blocks-interactions.md#on-interact)
* [`on-consume`](triggers/blocks-interactions.md#on-consume)
* [`on-name-entity`](triggers/blocks-interactions.md#on-name-entity)
* [`on-vibration-receive`](triggers/blocks-interactions.md#on-vibration-receive)

### 物品、附魔与装备触发器

* [`on-item-damage`](triggers/items-equipment.md#on-item-damage)
* [`on-item-group-cooldown`](triggers/items-equipment.md#on-item-group-cooldown)
* [`on-item-held`](triggers/items-equipment.md#on-item-held)
* [`on-swap-hand`](triggers/items-equipment.md#on-swap-hand)
* [`on-enchant-item`](triggers/items-equipment.md#on-enchant-item)
* [`on-elytra-boost`](triggers/items-equipment.md#on-elytra-boost)

### 移动与输入触发器

* [`on-move`](triggers/movement-input.md#on-move)
* [`on-input`](triggers/movement-input.md#on-input)
* [`on-jump`](triggers/movement-input.md#on-jump)
* [`on-toggle-flight`](triggers/movement-input.md#on-toggle-flight)
* [`on-toggle-sneak`](triggers/movement-input.md#on-toggle-sneak)

### 效果、空气、食物与经验触发器

* [`on-effect-tick`](triggers/player-state.md#on-effect-tick)
* [`on-potion-effect`](triggers/player-state.md#on-potion-effect)
* [`on-exhaustion`](triggers/player-state.md#on-exhaustion)
* [`on-food-level-change`](triggers/player-state.md#on-food-level-change)
* [`on-air-change`](triggers/player-state.md#on-air-change)
* [`on-exp-change`](triggers/player-state.md#on-exp-change)
* [`on-pickup-experience`](triggers/player-state.md#on-pickup-experience)

### 钓鱼与交易触发器

* [`on-fish`](triggers/fishing-trading.md#on-fish)
* [`on-purchase`](triggers/fishing-trading.md#on-purchase)
* [`on-trade`](triggers/fishing-trading.md#on-trade)

### 目标选择与特殊实体触发器

* [`on-target`](triggers/targeting-special.md#on-target)
* [`on-untag`](triggers/targeting-special.md#on-untag)
* [`on-creeper-explode`](triggers/targeting-special.md#on-creeper-explode)
* [`on-enderman-attack-player`](triggers/targeting-special.md#on-enderman-attack-player)
* [`on-phantom-pre-spawn`](triggers/targeting-special.md#on-phantom-pre-spawn)
* [`on-warden-anger-change`](triggers/targeting-special.md#on-warden-anger-change)

## 选择兼容的条件和修改器

应先选择触发器，再打开对应的分类页面确认它提供哪些上下文。

例如：

* `damage` 修改器需要触发器提供可修改的伤害数据；
* `item_damage` 需要使用 `on-item-damage`；
* 钓鱼结果修改器需要使用 `on-fish`，并处于对应的钓鱼状态；
* 交易修改器需要使用 `on-purchase` 或 `on-trade`；
* `warden_anger` 需要使用 `on-warden-anger-change`；
* 方块条件需要触发器提供 `BLOCK`；
* 以实体为目标的能力必须使用该触发器实际提供的选择器。

## 扩展触发器

其他插件可以通过 EnchantmentReform API 注册自定义触发器。自定义触发器的键及其上下文由扩展插件定义，因此不会列入本页面的内置触发器清单。

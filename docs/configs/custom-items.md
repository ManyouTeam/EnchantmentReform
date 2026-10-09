# Custom items

Every `.yml` file in `plugins/EnchantmentReform/items` defines one custom item. The file name without `.yml` is its ID. Legacy root-level ItemFormat and `{ item: ... }` definitions are not supported.

```yaml
base-item:
  material: GOLDEN_HELMET
  name: '{lang:custom-item-night_vision_helmet-name}'
  lore:
    - '{lang:custom-item-night_vision_helmet-lore}'
    - '<gray>Armor: <white>{attribute:minecraft:armor}'
    - '<gray>Endurance: <white>{attribute:custom_attribute:endurance}'

variables:
  armor-roll: 'random(1, 3)'

base-attributes:
  minecraft:armor: '{armor-roll} + 1'
  custom_attribute:endurance: '1 + random(0, 1)'

change-item:
  add-lore-last:
    - '<gray>Final armor: <white>{attribute:minecraft:armor}'

active-slots:
  - HEAD

allow-duplicate: false

powers:
  on-tick:
    interval: 10
    abilities:
      night-vision:
        type: potion_effect
        target: SOURCE
        potion: minecraft:night_vision
        duration: 30
        particles: false
```

## Fields

| Field | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Whether this custom item definition is loaded. |
| `base-item` | required | The [ItemFormat™](../shared-formats/itemformat-tm.md) used to build the item. |
| `base-attributes` | empty | Vanilla and custom attribute modifiers supplied by the item. Uses the format described below. |
| `active-slots` | required | Equipment slots in which the item activates its powers. |
| `allow-duplicate` | `false` | Allows each equipped copy to execute its powers separately. |
| `execution-priority` | `0` | Execution order relative to other active power sources. Higher values execute first. |
| `variables` | empty | Variables shared by the item's power configuration. |
| `powers` | required | Uses the same trigger, condition, modifier, and ability format as enchantments and custom attributes. |

Valid active slots are `HAND`, `OFF_HAND`, `HEAD`, `CHEST`, `LEGS`, `FEET`, `ARMOR`, and `ANY`. `ARMOR` covers all four armor slots. `ANY` covers every supported equipment slot.

## Base attributes

Each key below `base-attributes` is an attribute ID and its value is a number or mathematical expression. Use `minecraft:<id>` for a vanilla attribute. Custom attributes accept a bare ID, `custom_attribute:<id>`, or `enchantmentreform:<id>`; explicit `custom_attribute:` is recommended when a name could be ambiguous. Every entry uses `ADD_NUMBER`, and its equipment slot is selected automatically from the item material.

By default, vanilla entries use an automatically generated modifier key such as `enchantmentreform:base_attribute/example/minecraft/attack_damage`. Set `use-base-attack-modifier-keys: true` at the item root to use `minecraft:base_attack_damage` for `minecraft:attack_damage` and `minecraft:base_attack_speed` for `minecraft:attack_speed`. Other attributes continue to use generated keys.

Values use the same numeric pipeline as powers: `{level}` (always `1` for a custom item), root `variables`, build arguments, PlaceholderAPI values when a player is available, mathematical expressions, `2~5` ranges, and functions such as `random(1, 5)` are supported. Each expression is evaluated exactly once when the item is generated.

The calculated result for every vanilla and custom attribute is stored as a `DOUBLE` in the item's PDC. Cloning or moving the item preserves the roll. Reloading or changing the configuration does not reroll existing items; generate a new item to apply new formulas.

Use `{attribute:<attribute ID>}` in `base-item.name`, `base-item.item-name`, or `base-item.lore` to display the generated value. The ID in the placeholder must match the key under `base-attributes`, for example `{attribute:minecraft:armor}`. Display values use the same compact number formatting as descriptions.

Each configured vanilla base attribute replaces every existing modifier for that same attribute, including the material's default modifier. The configured value is then written as one `ADD_NUMBER` modifier; default modifiers for attributes not listed under `base-attributes` remain unchanged. Calculated values are also stored in PDC.

Custom-attribute modifiers read their calculated values from item PDC while an identified custom item is actually held or equipped in its automatic slot. They affect the final attribute value and description, appear in the attribute GUI, disappear immediately when the item is removed, and are never written to player PDC.

## Final item changes

The optional root `change-item` section uses the standard [Change Item format](../shared-formats/change-item-rules.md). It runs after `base-item`, vanilla base-attribute modifiers, and attribute PDC values have been written, so its rules can further change the completed item in YAML order.

Root `variables`, build arguments, normal Change Item placeholders, and `{attribute:<attribute ID>}` placeholders are available inside these rules. The result remains associated with its custom-item definition even when `replace-item` is used.

## Identity and power activation

Items built from a definition receive a persistent custom-item ID. Merely recreating the same material, name, and lore does not make another item a custom item.

When a player holds or wears the item in a configured active slot, its powers join that player's active power sources. With `allow-duplicate: false`, only one copy of the same definition executes. With `allow-duplicate: true`, every copy in a matching slot executes separately.

The value passed to the power as `{level}` is `1`.

## Default custom item

When the `items` directory is empty, EnchantmentReform extracts `night_vision_helmet.yml`. Wearing this Golden Helmet in the head slot continuously provides Night Vision. Its name and lore use `{lang:...}` placeholders from the language files.

## Creating and giving custom items

The following commands operate on the new custom-item definitions:

```text
/enchantmentreform saveitem <id> [bukkit|itemformat]
/enchantmentreform givesaveitem <id> [player] [amount]
/enchantmentreform generateitemformat
```

`saveitem` creates an `items/<id>.yml` definition from the item in the player's main hand. It creates `active-slots: [HAND]` and an empty `powers` section, which can then be edited. `givesaveitem` builds the configured item and writes its persistent custom-item ID before giving it.

`generateitemformat` converts the main-hand item to a standalone ItemFormat and writes it to `plugins/EnchantmentReform/generated-item-format.yml`. It does not create a custom-item definition.

After manually changing a definition, reload EnchantmentReform before testing it.

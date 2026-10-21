# ⌨️ Commands & Permissions

Main command: `/enchantmentreform`

Aliases: `/er`, `/enchants`

## Menus

```text
/enchantmentreform menu [menu-id]
/enchantmentreform menu skill-detail <skill-id>
/enchantmentreform menu attribute-detail <attribute-id>
```

Opens the YAML menu whose file name matches `menu-id`. For example, use `menu main`, `menu enchantment-info`, `menu skill-info`, `menu attribute-info`, or `menu attribute-allocation`.

Omit the menu ID to open `main`. The console can open menus for online players:

```text
er menu Steve
er menu main Steve
er menu main Steve -b
er menu skill-detail Steve mining -b
er menu attribute-detail Steve strength -b
```

Console syntax: `er menu [menu-id] <player> [skill-id|attribute-id] [-b]`. Without `-b`, the target player's menu permission is checked. With `-b`, the current menu session skips menu `permission` checks, including navigation, detail pages and back buttons. Disabled menus remain disabled, and reward claims and attribute upgrades keep their own checks. Player commands cannot enable this bypass. Menu IDs, online players, context IDs and `-b` support tab completion.

Permission:

```text
enchantmentreform.menu
```

The permission defaults to all players. Separate legacy GUI subcommands are not registered.

## Manage custom attributes

```text
/enchantmentreform setattribute <player> <attribute> <value>
/enchantmentreform setattribute <player> <attribute> <value> -ignore
/enchantmentreform addattribute <player> <attribute> <delta>
/enchantmentreform addattributemodifier <player> <attribute> <modifier-id> <amount> <operation>
/enchantmentreform setattributemodifier <player> <attribute> <modifier-id> <amount> <operation>
```

The first two commands change the base value. The optional `-ignore` suffix on `setattribute` bypasses the attribute's configured minimum and maximum. Modifier operations are `ADD_VALUE`, `ADD_MULTIPLIED_BASE`, and `ADD_MULTIPLIED_TOTAL`. All four management permissions default to operators and use the command name after `enchantmentreform.`.

## Reload runtime files

```text
/enchantmentreform reload
```

Reloads runtime-safe configuration, language, menu, and power state handled by the current implementation.

Permission:

```text
enchantmentreform.reload
```

The permission defaults to operators.

## Manage player skill progress

```text
/enchantmentreform setskillxp <player> <skill-id> <experience>
/enchantmentreform addskillxp <player> <skill-id> <experience>
/enchantmentreform setskilllevel <player> <skill-id> <level>
/enchantmentreform addskilllevel <player> <skill-id> <delta>
```

`setskillxp` changes progress within the current level (and clamps it to that level's requirement), while `addskillxp` uses the normal level-up and reward flow. Direct level changes are clamped to the configured range and do not grant level rewards. Each command has a matching `enchantmentreform.<command>` permission, operator-only by default.

## Set a player's skill XP multiplier

```text
/enchantmentreform setskillmultiplier <player> <skill-id|all> <multiplier>
```

Sets a persistent multiplier for one skill or every skill. The `all` value and a skill-specific value multiply together; use `1` to remove an override and `0` to stop XP gain for that scope. This command is registered only when `config.yml -> modules.skills` is enabled.

Permission: `enchantmentreform.setskillmultiplier` (operator by default).

{% hint style="warning" %}
Reload cannot rebuild the Minecraft enchantment registry. Adding/removing enchantments or changing registry fields still requires a full server restart.
{% endhint %}

## Generate ItemFormat

```text
/enchantmentreform generateitemformat
```

Converts the item in the player's main hand to ItemFormat and writes it to `plugins/EnchantmentReform/generated-item-format.yml`. Running the command again overwrites that file.

Permission:

```text
enchantmentreform.generateitemformat
```

The permission defaults to operators, and the command must be run by a player.

## Bypass permissions

| Permission | Purpose | Default |
| --- | --- | --- |
| `enchantmentreform.bypass.protection` | Allows supported block abilities to bypass protection-plugin checks. | OP |
| `enchantmentreform.bypass.economy` | Bypasses supported economy/item costs such as `cost_price`. | OP |

## Examples

```text
/er gui
/enchants gui
/er reload
/er generateitemformat
```

The console may run `reload`; the GUI command requires a player.

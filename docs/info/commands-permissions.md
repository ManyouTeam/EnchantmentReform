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

Players with `enchantmentreform.enchantmentinfo.givebook` can Shift-click an enchantment in the catalogue to receive one book at its maximum level. This permission defaults to operators. If the inventory is full, the book drops at the player’s feet.

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

## Manage temporary powers

```text
/er temppower set <player> <id> [level]
/er temppower remove <player> <id>
/er temppower clear <player>
/er temppower list <player>
```

The target must be online. An omitted level uses `default-level` from `temp_powers/<id>.yml`; explicit levels must be positive integers. Permission: `enchantmentreform.temppower`, default `op`. Players, the console and other plugins can dispatch these commands.

`remember: false` survives plugin reload or re-enable, but clears on reconnect or server restart. `true` retains grants and levels across both. See [Temporary Powers](../configs/temp-powers.md) for configuration and lifecycle details.

## Give random enchanted books

```text
/er randombook [player] all [amount] [--level <level|range>] [--target <supported-item tag>] [--rarity <rarity ID>]
/er randombook [player] levels <power> [amount] [--target <supported-item tag>] [--rarity <rarity ID>]
```

Players may omit the target player to give themselves books. The console must specify an online player. The amount defaults to 1 and accepts 1–64. Each book is rolled independently. Overflow drops at the target player's location.

`all` rolls one registered enchantment per book. Use `--level 3` to restrict the pool to enchantments that support level III and give books at that level. Use `--level 5~10` to roll a level from 5 through 10 within each selected enchantment’s supported range. `levels` (also `level`) uses vanilla enchanting power and can generate multiple compatible enchantments per book. If a roll fails, the batch is withheld.

Use `--target enchantmentreform:sword` to select enchantments applicable to items in that `supported-items.tags` entry in `config.yml`. Enter the configured key without a `#` prefix. Use `--rarity common` as the final option to select a configured rarity. Both filters can be combined, as in `/er randombook levels 30 5 --target enchantmentreform:sword --rarity common`.

The permission is `enchantmentreform.randombook`, granted to operators by default.

## Give a specific enchanted book

```text
/er givebook [player] <enchantment> [level]
```

Players may omit the target. Console commands require an online target. For example, `/er givebook minecraft:sharpness 5` gives the sender a Sharpness V book. Omitting the level gives a book at the enchantment’s maximum level. You can also specify a level from 1 to 255. Bare IDs look up Minecraft first and then EnchantmentReform. Use a namespaced ID for other plugins.

The permission is `enchantmentreform.givebook`, granted to operators by default.

## Randomly enchant a held item

```text
/er randomenchant [player] all [rolls] [--level <level|range>] [--rarity <rarity ID>]
/er randomenchant [player] levels <power> [rolls] [--rarity <rarity ID>]
```

Players may omit the target to enchant their own main-hand item. Console commands require an online target.

For example, `/er randomenchant Steve all 2 --level 3 --rarity common` adds two compatible level III common enchantments to Steve’s main-hand stack. In `all` mode, `--level` also accepts an inclusive range such as `5~10`. Each roll excludes existing and conflicting enchantments. In levels mode the native algorithm uses the item and the given power. The entire batch is applied only when every roll succeeds.

The permission is `enchantmentreform.randomenchant`, granted to operators by default.

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

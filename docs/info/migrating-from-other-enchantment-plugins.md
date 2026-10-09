# 🔄 Migrating from Another Enchantment Plugin

The safest migration strategy is to preserve every old enchantment's complete namespaced key in its corresponding EnchantmentReform definition.

For example, if the old plugin registered Life Steal as:

```text
oldenchants:lifesteal
```

then the replacement EnchantmentReform file should use the same key:

```yaml
enabled: true
key: oldenchants:lifesteal

name: Life Steal
description: Steals health from the target.

supported-items: enchantmentreform:melee_weapon
primary-items: enchantmentreform:melee_weapon
max-level: 3

# Configure rarity, slots, obtaining sources, powers, and other fields here.
```

The file name and directory do not need to match the old plugin. The root `key` is the authoritative identity stored by Minecraft.

{% hint style="danger" %}
**Back up the server before migrating.** A bad registry migration can affect items in player inventories, ender chests, containers, shulker boxes, saved-item files, trades, and offline player data. Do not treat an untested production world as the first migration test.
{% endhint %}

## What to back up

Stop the server cleanly, then create a restorable backup of:

* every world folder, including player data and region files;
* the old enchantment plugin JAR and its complete data folder;
* `plugins/EnchantmentReform/` if it already exists;
* the server's `plugins/` directory and configuration files;
* external databases used by inventory, economy, mailbox, auction, or storage plugins;
* any proxy, cross-server inventory, or synchronization data that can contain items.

Copy the backup to a separate location and verify that it can be restored. Keeping a backup inside the live server directory is not sufficient protection.

## Build a one-to-one key map

Before removing the old plugin, list every enchantment that it registers. Obtain the keys from its configuration, documentation, commands, API, or registry output.

Create a worksheet similar to this:

| Old enchantment | Old key | EnchantmentReform file | New `key` | Levels checked | Ready |
|---|---|---|---|---|---|
| Life Steal | `oldenchants:lifesteal` | `melee/life_steal.yml` | `oldenchants:lifesteal` | 1–3 | Yes |
| Telepathy | `oldenchants:telepathy` | `tools/telepathy.yml` | `oldenchants:telepathy` | 1 | Yes |
| Heavy Curse | `oldenchants:heavy_curse` | `curse/heavy_curse.yml` | `oldenchants:heavy_curse` | 1–2 | Pending |

Map **every old key to exactly one new definition**:

```text
old key A -> new definition A using old key A
old key B -> new definition B using old key B
old key C -> new definition C using old key C
```

Keep the namespace, spelling, underscores, and letter case exactly as registered by the old plugin. Do not map two old keys to one new key, and do not reuse a key for an unrelated effect.

## Why preserving the key matters

Items using native Minecraft enchantment components store a registry reference, not the old plugin's display name or configuration file name. When EnchantmentReform registers the replacement enchantment under the same key, those items can resolve the reference to the new definition without rewriting each item individually.

Changing the namespace breaks that identity:

```text
oldenchants:lifesteal != enchantmentreform:lifesteal
```

If the new definition uses `enchantmentreform:lifesteal`, existing items still point to `oldenchants:lifesteal`. A separate item-conversion process would then be required across every place that may store an item.

{% hint style="warning" %}
Key preservation works for enchantments represented through Minecraft's enchantment registry and item components. Some plugins emulate enchantments using lore, Persistent Data Container values, custom NBT, or a private database. Those formats are owned by the old plugin and may require its migration command, API, or a dedicated conversion tool.
{% endhint %}

## Create the replacement definitions

For each row in the key map:

1. Create one YAML file under `plugins/EnchantmentReform/enchantments/`.
2. Set its root `key` to the old plugin's complete key.
3. Recreate the intended effect with EnchantmentReform triggers, conditions, modifiers, and abilities.
4. Match the old maximum level or document how out-of-range levels will be handled.
5. Configure supported items, primary items, active slots, exclusivity, rarity, costs, and obtaining sources.
6. Check whether the replacement requires Paper, a newer Minecraft version, or an optional integration.
7. Mark the mapping row ready only after testing every supported level.

The effects do not need to be implemented in the same way as the old plugin, but the resulting behavior should be intentionally equivalent. Pay particular attention to:

* whether percentages are represented as `10`, `0.10`, or a multiplier such as `1.10`;
* cooldown units and whether cooldowns are shared;
* damage source and attacker/target direction;
* equipment slots in which the power is active;
* maximum levels and items carrying levels above the new maximum;
* conflicts with vanilla or other custom enchantments;
* enchanted books versus enchantments directly applied to equipment.

## Perform the migration

Use a test copy of the production server first:

1. Stop the server.
2. Confirm that the backup is complete.
3. Install EnchantmentReform and place all finished replacement definitions in its `enchantments/` folder.
4. Remove or disable the old enchantment plugin before startup. Two plugins must not attempt to register the same key.
5. Start the server normally. Do not use Bukkit `/reload`, PlugMan, or another hot-loader.
6. Check startup logs for duplicate keys, missing tags, invalid registry data, unsupported effects, and disabled integrations.
7. Verify the key map against the enchantments actually registered by the server.

Adding, removing, or changing an enchantment key is registry work and always requires a full server restart. `/enchantmentreform reload` cannot rebuild the enchantment registry.

## Test existing items

Test copies of real items from each storage path used by the server:

* online and offline player inventories;
* equipped armor and off-hand items;
* ender chests;
* ordinary containers and shulker boxes;
* enchanted books;
* auction houses, mailboxes, backpacks, vaults, and virtual storage;
* villager trades, loot tables, kits, shops, and saved-item plugins.

For every migrated enchantment, verify:

* the old item still contains the expected key and level;
* the display name and tooltip are acceptable;
* the effect activates in the correct slot and situation;
* anvil combining and enchanting behavior are correct;
* newly generated items and previously existing items behave the same;
* restarting the server does not remove or change the enchantment.

Do not open the production server until representative items, offline data, and external storage have all been checked.

## Rollback

If validation fails:

1. Stop the server immediately.
2. Do not allow players to continue moving or modifying affected items.
3. Restore the world, player data, plugin files, plugin data, and external databases from the same backup point.
4. Restore the old plugin before starting the server.
5. Correct the key map or replacement definitions in the test environment, then repeat the migration.

Restoring only the plugin JAR while keeping item and world data modified after the migration can create a mixed state. Roll back all item-bearing storage consistently.

## Final checklist

* [ ] The server is stopped and a verified off-site backup exists.
* [ ] Every old enchantment key appears exactly once in the mapping worksheet.
* [ ] Every new definition uses the intended old namespaced key.
* [ ] Maximum levels, supported items, slots, conflicts, and effects were reviewed.
* [ ] Paper, Spigot, Minecraft-version, and integration requirements were checked.
* [ ] The old plugin and EnchantmentReform never register the same key together.
* [ ] Existing items and newly created items were tested on a server copy.
* [ ] Offline inventories and external item-storage systems were included.
* [ ] Startup logs are clean.
* [ ] A complete rollback procedure was tested or documented.

See [Enchantment configuration](../configs/enchantment-configuration.md) for the full definition format and [Built-in Enchantments and Supported Versions](builtin-enchantments.md) for examples and platform requirements.

# ❓ FAQ

## Enchantment Color does not display?

Try restart the server.

## My server fail to run after install this plugin.

This is often caused by a problem with your enchantment configuration, which causes the server to crash during the registration of new enchantments. Please carefully recall which files you have just modified and check them thoroughly.

## I want to display enchantment description in my lore.

Go to [EnchantmentSlots](https://www.spigotmc.org/resources/enchantmentslots-add-enchantment-slot-feature-to-your-server-1-20-5.113048/).

## I want mob to be able to use enchantments as well.

Because it is somewhat unreliable for enchantments to work on both mobs and players simultaneously, this function has been separated into [EnchantedMobs](https://www.spigotmc.org/resources/enchantedmobs-dynamic-mob-abilities-and-player-scaling-difficulty-61-built-in-power-1-21-3.133242/).

## Why did a new enchantment not appear after reload?

Minecraft freezes enchantment registries during bootstrap. New files, removed files, keys, supported items, levels, costs, exclusivity, rarity-derived registry values, and vanilla overrides require a full restart.

## Why does the YAML load but the effect never runs?

Check these points in order:

1. the enchantment is enabled and registered;
2. the item actually contains the enchantment;
3. the item is in an `active-slots` slot;
4. the trigger exists in the [Trigger reference](../configs/triggers.md);
5. every Power Condition receives the context it needs;
6. the modifier is compatible with the trigger's mutable result;
7. the ability resolves a valid `SOURCE`, `SKILL`, `TARGET`, block, item, or location;
8. chance, cooldown, usage limit, protection, and economy checks pass.

Enable `debug` only while diagnosing a problem and include the complete log in support requests.

## Why does a Paper example not work on Spigot?

Some registry operations and events are Paper-only. Equipment activation/deactivation, newer event types, and some registry behavior may use fallbacks or be unavailable on Spigot.

## Why does `break_blocks` skip protected or special blocks?

The ability applies a default safety blacklist, the global maximum block cap, optional whitelist/blacklist rules, chunk-loading rules, tool/durability checks, and supported protection hooks. Review both the ability section and `config.yml -> powers.break-block`.

## Can I use expressions by enchantment level?

Yes. Supported numeric fields can use `level`, top-level variables, ranges such as `1~3`, level selector maps, runtime context placeholders, and PlaceholderAPI where a player context exists.

## Where should I start when creating an enchantment?

Copy one bundled enchantment with a similar trigger, change its namespaced `key`, supported items, name/description, and power section, then restart the server and verify it in `/er gui`.

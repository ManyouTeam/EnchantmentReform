# 🎉 EnchantmentReform

Welcome to **EnchantmentReform**, this is a data-driven enchantment framework for modern servers. It registers custom enchantments, lets administrators redefine selected vanilla enchantments, and builds runtime effects from YAML instead of hard-coded event handlers.

This plugin includes:

* Custom enchantments registered from `enchantments/**/*.yml`.
* Optional overrides for vanilla enchantments in `vanilla_enchantments/`.
* Level expressions, variables, ranges, and PlaceholderAPI parsing.
* Rarity defaults, supported-item tags, tooltip sorting, and enchanting-table integration.
* Player equipment lifecycle handling through active slots and `on-activate` / `on-deactivate`.
* Combat, projectile, block, movement, fishing, trading, inventory, item, and environment triggers.
* Reusable MatchItemFormat and MatchEntityFormat rules.
* Paper, Spigot, and Folia-aware scheduling; some registry and event features remain platform-specific.
* Integrations for custom-item, economy, protection, MythicMobs, PacketEvents, NBTAPI, and EnchantmentSlots ecosystems when the corresponding plugin is installed.

{% hint style="warning" %}
Minecraft enchantment registries are frozen after server bootstrap. Adding or removing enchantments, changing registry fields, supported items, rarity-derived registry values, or vanilla enchantment definitions requires a **full server restart**. `/enchantmentreform reload` only reloads runtime-safe files and cannot rebuild the frozen registry.
{% endhint %}

## Recommended reading

* [Requirements](info/requirements.md)
* [Installation and updates](info/install.md)
* [Migrating from another enchantment plugin](info/migrating-from-other-enchantment-plugins.md)
* [Configuration files](info/configuration-files.md)
* [Enchantment configuration](configs/enchantment-configuration.md)
* [Power Triggers](configs/triggers.md)
* [Power Conditions](configs/power-conditions/)
* [Power Modifiers](configs/power-modifiers/)
* [Built-in Enchantments and Supported Versions](info/builtin-enchantments.md)
* [Abilities](configs/abilities/)

## Links

### Get the plugin

### Get support

Support is provided through the [Discord server](https://discord.gg/RZajEybhBw). Please read the server rules and plugin license before requesting assistance.

Users in mainland China who purchased the plugin may use QQ group `815351827` with proof of purchase.

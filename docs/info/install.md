# ⚙️ Install

## Want to display enchantment description in my lore?

Go to [EnchantmentSlots](https://www.spigotmc.org/resources/enchantmentslots-add-enchantment-slot-feature-to-your-server-1-20-5.113048/).

## Want mob to be able to use enchantments as well?

Because it is somewhat unreliable for enchantments to work on both mobs and players simultaneously, this function has been separated into [EnchantedMobs](https://www.spigotmc.org/resources/enchantedmobs-dynamic-mob-abilities-and-player-scaling-difficulty-61-built-in-power-1-21-3.133242/).

## Enchantment Color does not display?

Try restart the server.

## Install

* Download the plugin from the official release site. The file name should be **EnchantedMobs-X.X.X.jar**, where X is the version number.
* Put the `.jar` file into your server's `plugins` folder.
* Stop your server and then restart it. <mark style="color:red;">Cannot load plugins in any other way while the server is starting</mark>.
* When updating plugins, please be sure to remove old versions.
* Previously, you used the free version, but now to upgrade to the paid version, you only need to install the paid version on the server and remove the free version. The configuration file of the plugin does not require any changes.
* When downgrading from **a new game version** to **an old version** on the server where the plugin is located, it is important to remove the `items` folder from the configuration file.
* Default configs are just helping you understand the config framework, you need modify them that meet your needs.

{% hint style="warning" %}
Do not use Bukkit `/reload`, PlugMan, or another plugin hot-loader. Enchantments and tags are registered during server bootstrap and cannot be safely reconstructed after registries freeze.
{% endhint %}

## Update

* When updating plugins, please be sure to remove old versions.
* You should always keep the plugin updated to the latest version.
* To prevent your custom config files from being overwritten or broken, the plugin does not auto-update existing configs. You can manually merge changes from new default configs, or regenerate fresh config files.
* Before every update, read the changelog carefully. If you skip multiple versions, review all changelogs in between. Major breaking changes will be noted there.

## When a restart is required

A full restart is required after changing:

* the set of files in `enchantments/` or `vanilla_enchantments/`;
* an enchantment `key`;
* supported/primary item tags;
* registry values such as weight, max level, costs, exclusivity, slots, or obtaining sources;
* global rarity defaults that feed registry registration;
* supported-item tag definitions;
* the plugin JAR or a registry-related dependency.

`/enchantmentreform reload` is intended for runtime-safe configuration and language changes. It does not re-register frozen Minecraft registries.

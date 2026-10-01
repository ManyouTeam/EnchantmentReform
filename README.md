# ✨ Welcome to **EnchantmentReform**

> **EnchantmentReform** is a configurable enchantment, attribute, and skill framework for modern Minecraft servers!

Build custom enchantments, expand vanilla mechanics, and connect equipment effects with player progression through YAML configuration.

[Wiki](https://enchantedmobs.superiormc.cn/for-enchantmentreform) · [Discord](https://discord.gg/RZajEybhBw) · [Source Code](https://github.com/ManyouTeam/EnchantmentReform)

---

## 📖 Custom Enchantments

**EnchantmentReform** registers custom enchantments with the server's enchantment registry and gives them configurable effects.

- Define names, descriptions, maximum levels, and rarity.
- Configure supported items, active equipment slots, and enchantment conflicts.
- Set selection weights, enchanting power ranges, and anvil costs.
- Use variables and level formulas to scale effects.
- Control tooltip ordering and display enchantment descriptions.

Create effects for weapons, armor, tools, shields, fishing rods, and other supported items.

---

## ⚙️ Triggers, Conditions, Modifiers, and Abilities

Build effects by combining the plugin's reusable power components:

- **Triggers** decide when an effect runs, including combat, projectiles, block interactions, movement, fishing, and equipment changes.
- **Conditions** check requirements such as player state, equipment, entities, blocks, permissions, and skill levels.
- **Modifiers** adjust values such as damage and experience.
- **Abilities** perform actions such as applying potion effects, spawning particles, changing blocks, and running commands.

Reuse item and entity matching rules, math expressions, variables, and supported PlaceholderAPI placeholders to create effects suited to your server.

---

## 🧩 Vanilla Enchantment Customization

Attach additional powers to existing vanilla enchantments and customize supported registry settings.

- Add configurable effects to existing `minecraft:` enchantments.
- Customize selected native enchantment effects.
- Configure item enchantability and random enchanting behavior.
- Adjust anvil behavior, including over-levelled enchantments on Paper.

Registry overrides and some anvil features depend on Paper. Available features vary by server platform.

---

## 💪 Custom Attributes

Define persistent player attributes and connect their values to the same power system used by enchantments.

- Set default, minimum, and maximum attribute values.
- Apply persistent attribute modifiers.
- Scale powers and descriptions with the player's attribute value.
- Configure attribute point costs and allocation requirements.
- Show attributes in information and allocation menus.

---

## 🌱 Skills and Player Progression

The optional skill module lets players gain experience, level up, and earn rewards.

- Define skill levels and experience formulas.
- Award experience through configurable triggers and conditions.
- Grant attribute points, attribute increases, items, or command rewards.
- Configure experience multipliers and anti-abuse rules.
- Display skill progress, experience sources, and level rewards in menus.

Enable the module through `modules.skills` in `config.yml`.

---

## 🧭 Configurable Menus and Language

Give players a place to browse enchantments and manage their progression.

- Enchantment catalogue and detail menus.
- Attribute information and allocation menus.
- Skill overview, progress, source, and reward menus.
- Configurable layouts, display items, and button actions.
- Per-player language selection, MiniMessage, and legacy color formatting.

---

## 🔗 Plugin Integrations

Connect EnchantmentReform with other parts of your server when the corresponding plugins are installed.

- **Custom items:** MMOItems, ItemsAdder, Oraxen, Nexo, CraftEngine, ExecutableItems, and more.
- **Economy:** Vault, PlayerPoints, CoinsEngine, and other supported economy providers.
- **Protection:** WorldGuard, Residence, GriefPrevention, Lands, Towny, and other supported protection plugins.
- **Additional integrations:** PlaceholderAPI, MythicMobs, MythicChanger, PacketEvents, NBTAPI, and EnchantmentSlots.

The code includes Paper and Spigot implementations and Folia-aware scheduling. Registry features and event availability differ across platforms; Folia support remains experimental.

---

## 🛠️ Building

Use **Java 21 or newer** and **Maven** to build from the root `pom.xml`.

1. Obtain the local dependencies referenced by `systemPath` in `core/pom.xml` and place them in `core/lib/`. These JARs are not distributed in this repository.
2. Run the Maven build:

```shell
mvn clean package -DskipTests
```

The plugin JAR is generated in `plugin/target/`.

**Registry changes require a full server restart.** Adding or removing enchantments and changing registry definitions cannot be applied through `/enchantmentreform reload`; reload only updates runtime-safe configuration.

---

## ❤️ Support and License

Visit the [Wiki](https://enchantedmobs.superiormc.cn/for-enchantmentreform) for configuration guidance or join our [Discord server](https://discord.gg/RZajEybhBw) for support. Purchasers in mainland China can also join QQ group **815351827** with proof of purchase.

The source is publicly available under the existing [License Agreement](LICENSE). Public access does not grant permission to redistribute or resell the plugin or its configurations. Use on publicly accessible servers requires a purchased copy, as specified in the license.

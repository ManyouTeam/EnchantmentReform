# 🔗 Compatibility

EnchantmentReform loads optional integrations only when the corresponding plugin is present. Missing optional plugins do not prevent the core plugin from starting.

## Custom item providers

Built-in hooks may resolve items from:

* ItemsAdder
* Oraxen
* MMOItems
* EcoItems and EcoArmor
* eco
* NeigeItems
* ExecutableItems
* Nexo
* CraftEngine
* MythicMobs items

These hooks are used by ItemFormat, MatchItemFormat, item costs, item replacement/change abilities, drops, and configured provider item IDs.

## Economy providers

Economy-backed abilities such as `cost_price` can use supported providers when installed, including:

* Vault-compatible economies
* PlayerPoints
* ExcellentEconomy
* UltraEconomy
* EcoBits
* PEconomy
* RedisEconomy
* RoyaleEconomy
* VotingPlugin.

Use `enchantmentreform.bypass.economy` to bypass configured economy costs where supported.

## Protection plugins

Block-changing and block-breaking abilities check supported protection hooks unless the player has `enchantmentreform.bypass.protection`.

Declared integrations including:

* WorldGuard
* Residence
* GriefPrevention
* Lands
* HuskTowns
* HuskClaims
* PlotSquared
* Towny
* BentoBox
* Dominion
* SuperiorSkyblock2.

## MythicMobs

When MythicMobs is installed, EnchantmentReform can:

* resolve MythicMobs items;
* use MythicMobs-aware entity matching where supported;
* execute a configured MythicMobs skill with the `mythic_skill` ability;
* summon configured MythicMobs entities through supported summon options.

## PlaceholderAPI

PlaceholderAPI values may be used in supported text and numeric expressions when a player context is available. EnchantmentReform does not require PlaceholderAPI for normal operation.

## NBTAPI

NBTAPI enables legacy NBT-oriented MatchItemFormat rules. Prefer modern item components and normal ItemFormat matching where possible on current Minecraft versions.

## PacketEvents and NMS-backed features

PacketEvents and the version-specific NMS bridge are used by features that simulate item use, fishing interaction, client effects, or version-sensitive behavior. A feature that requires an unavailable capability should fail safely rather than preventing unrelated enchantments from working.

### Built-in enchantment-description display

When PacketEvents is installed, EnchantmentReform can add enchantment descriptions to item tooltips without modifying the real server-side item. It transforms outgoing window-items, set-slot, cursor-item, and merchant-offers packets. Creative-mode packets are left unchanged to avoid client inventory synchronization problems.

```yaml
enchantment-description:
  wrap-length: 30
  item-display:
    enabled: true
    force-enabled: false
    lore-prefix: "§y"
    remove-lore-first: true
    black-creative: true
    black-item: []
    packet-listener-priority: LOWEST
    at-first-or-last: false
    display-value:
      - '{enchants}'
    placeholder:
      auto-parse: true
      enchants:
        format: '&a{enchant_name}{enchant_level_roman}'
        auto-add-space: true
        level-hide-one: true
        description:
          format: '&7  {enchant_description}'
```

The layout follows EnchantmentSlots' item-lore configuration without any slot fields. `{enchants}` expands to one formatted block per supported enchantment. The enchantment format supports `{enchant_name}`, `{enchant_raw_name}`, `{enchant_level}`, `{enchant_level_roman}`, and `{enchant_description}`. Descriptions are always emitted when configured on the enchantment; there is no description toggle, permission, player command, local `sort` option, or configurable `enchant-level` map. Numeric levels and Roman numerals are generated directly.

When EnchantmentSlots is detected, Item Display is disabled by default because both features can add enchantment lore. EnchantmentReform logs this conflict once; set `force-enabled: true` only when both display paths are intentionally required.

Item Display always sorts its supported enchantments with EnchantmentReform's rarity, weight, and namespaced-key comparator. `lore-prefix` marks generated client lore so `remove-lore-first` can prevent duplicate packet transformations. When `{enchants}` produces content, the outgoing copy hides the vanilla enchantment tooltip. `black-item` accepts normal MatchItemFormat rules, and `at-first-or-last: true` places the configured block before existing lore. Configuration reload updates the display and packet-listener priority. If MythicChanger also applies its rule as a fake change, disable one display path to avoid duplicate descriptions.

## MythicChanger

When MythicChanger is installed, EnchantmentReform registers the `er-enchantment-description` change rule. It adds the localized name, actual level, and level-dependent description of every configured EnchantmentReform enchantment on the current item.

Use the default layout:

```yaml
changes:
  er-enchantment-description: true
```

Or customize the generated lore:

```yaml
changes:
  er-enchantment-description:
    position: LAST
    format:
      - '{name} {level-roman}'
      - '&7{description}'
    separator:
      - ''
    wrap-length: 30
```

| Field | Default | Description |
| --- | --- | --- |
| `position` | `LAST` | Places the generated block at `FIRST` or `LAST` in the existing lore. |
| `format` | See above | Lore lines emitted for each supported enchantment. |
| `separator` | One blank line | Lore inserted between enchantment blocks. Use `separator: []` for no separator. |
| `wrap-length` | `enchantment-description.wrap-length` | Maximum visible description length per line. A value at or below `0` disables wrapping. |

The format supports `{key}`, `{name}`, `{level}`, `{level-roman}`, and `{description}`. A format line containing `{description}` is repeated for every wrapped description line. Normal MythicChanger item and PlaceholderAPI placeholders remain available.

The rule reads normal item enchantments and stored enchantments on enchanted books. Only enabled enchantments with a description in EnchantmentReform are shown, and their order follows EnchantmentReform's `tooltip-order` settings. Because MythicChanger executes changes in YAML order, place this rule after rules that modify enchantments when the description should reflect those changes.

This rule and EnchantmentReform's built-in Item Display share enchantment resolution, localization, wrapping, stored-enchantment handling, and ordering. Each integration keeps its own layout configuration.

Fake changes only add the description to the display copy and do not write tracking data. Real changes replace the block previously generated by this rule before adding its current result, so repeated real changes do not accumulate duplicate descriptions. Setting the rule to `false` during a real change removes its previously generated block.

## EnchantmentSlots

When EnchantmentSlots is installed, EnchantmentReform can provide enchantment information and use the configured rarity/tooltip order for compatible lore display.

{% hint style="info" %}
A declared soft dependency only controls load order. Always check startup logs to confirm that the specific hook and capability you need were registered successfully.
{% endhint %}

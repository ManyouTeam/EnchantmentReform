# 🛠️ Configuration files

EnchantmentReform stores runtime files in `plugins/EnchantmentReform/`.

## Main files and directories

| Path | Purpose | Reload or restart? |
| --- | --- | --- |
| `config.yml` | Language, math, item hooks, enchantability overrides, rarity defaults, supported-item tags, and power safety settings. | Depends on the field; registry-fed values require restart. |
| `enchantments/` | Custom enchantment definitions. Subdirectories are supported. | Full restart for registry changes. |
| `enchantments/index.txt` | Bundled-file index used when copying default enchantments. | Full restart after changing the enchantment set. |
| `vanilla_enchantments/` | Overrides for selected vanilla enchantments. | Full restart. |
| `languages/` | Localized plugin and enchantment text. | Runtime reload is normally sufficient. |
| `menus/` | One file per GUI type, plus `settings.yml` for shared menu settings. | Runtime reload is normally sufficient. |

Existing files are not overwritten during updates. Compare customized files with the resources bundled in the new JAR.

## Important `config.yml` sections

### `config-files`

```yaml
config-files:
  language: en_US
  per-player-language: true
  force-parse-mini-message: false
```

Controls the default language, player locale selection, and MiniMessage parsing behavior.

### `math`

```yaml
math:
  enabled: true
  enable-function: true
```

Enables level expressions, ranges, variables, and registered math functions used by enchantment powers.

### `hook-item-method`

```yaml
hook-item-method: DEFAULT
```

Selects the item-provider resolution strategy. Keep `DEFAULT` unless an installed integration specifically requires another mode.

### `item-enchantability-overrides`

```yaml
item-enchantability-overrides:
  enabled: true
  ignore-equipment-when-open-other-invenotry: true
  materials:
    SHIELD: 10
```

Overrides the `minecraft:enchantable` component for matching items entering player inventory. Values must be positive integers.

### `enchant-randomly-overrides`

```yaml
enchant-randomly-overrides:
  enabled: true
  minimum-cost: 10
  maximum-cost: 30
```

Replaces `minecraft:enchant_randomly` behavior with the level-based enchanting algorithm used by `minecraft:enchant_with_levels`. The values are enchanting costs/power, not final enchantment levels.

The final plugin contains separate named-NMS Advice modules for the Minecraft `1.21.11` ABI and the `26.1+` ABI. At startup, EnchantmentReform checks whether `EnchantRandomlyFunction` contains `includeAdditionalCostComponent`, loads only the matching Advice class, and runs that module's complete ABI probe before installing Byte Buddy. If neither ABI matches, only this optional override is disabled.

### `anvil`

```yaml
anvil:
  bypass-enchantment-level-limit: false
```

On Paper, setting `bypass-enchantment-level-limit` to `true` allows an anvil to apply enchantment levels above `Enchantment#getMaxLevel()`. This includes retaining over-levelled enchantments from books and combining equal over-levelled inputs. It does not change the anvil experience-level cost; use the `modify_repair_cost` activation ability for that.

### `tooltip-order`

Controls vanilla tooltip order and compatible EnchantmentSlots lore order. Rarities are sorted by `rarity-sort-rule`; enchantments inside a rarity are sorted by weight and key.

### `rarity`

Each rarity can provide defaults for:

- display name and color prefix/suffix;
- selection weight;
- anvil cost;
- minimum and maximum enchanting cost formulas;
- curse state;
- rarity-specific trade behavior.

An enchantment file may override supported registry fields locally.

### `supported-items`

`tags` defines reusable item tags such as swords, tools, bows, shields, armor slots, fishing rods, tridents, and spears. `filters` defines the catalogue GUI filters built from those tags.

Paper can register configured tags during bootstrap. Spigot uses safe resolution fallbacks for display and matching where direct registry mutation is unavailable.

### `powers`

Important safety/runtime settings include:

```yaml
powers:
  active-enchantment-mode: SCAN
  strict-active-slot-check: true
  break-block:
    max-blocks-per-activation: 512
  location-changed-effect:
    auto-remove:
      enabled: true
      duration: 80~120
  temp-block-crack-animation:
    enabled: true
    start-at: 0.5
    update-interval: 5
    view-distance: 64
  cost-price:
    shulker-box-check: false
    check-method: Bukkit
```

- `active-enchantment-mode`: `SCAN` checks equipment at trigger time. `CACHE` uses Paper equipment-change events and falls back to scanning where needed.
- `strict-active-slot-check`: requires the enchanted item to be in one of the enchantment's configured active slots.
- `max-blocks-per-activation`: hard cap shared by `break_blocks` abilities.
- temporary block settings control restoration and client crack animation.
- `cost-price` controls item/economy cost matching behavior.

### `menus/`

GUI configuration is split into `main.yml`, `enchantment-info.yml`, `attribute-info.yml`, `attribute-detail.yml`, `skill-info.yml`, `skill-detail.yml`, and `attribute-allocation.yml`; you can also add free menus. Shared settings such as the GUI anti-duplication check are in `settings.yml`. Legacy `config.yml` menu sections are migrated into these files when the `menus` directory is first created. See the [menu documentation](../menus/README.md) for each file's options, placeholders, and actions.

## Registry-safe workflow

1. Stop the server.
2. Edit enchantment, rarity, item-tag, or vanilla override files.
3. Validate YAML indentation and unique namespaced keys.
4. Start the server and review bootstrap logs.
5. Use `/enchantmentreform menu enchantment-info` to verify the registered result.

Do not expect `/enchantmentreform reload` to add, remove, or re-register enchantments after bootstrap.

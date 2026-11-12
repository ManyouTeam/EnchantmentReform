# ⚙️ Built-in Menu Options

This page documents each option in `plugins/EnchantmentReform/menus/`. Item nodes accept the material, name, lore, CustomModelData, and other common options from [ItemFormat™](../shared-formats/itemformat-tm.md).

The tables list dynamic placeholders provided by menu code. `{lang:key}` reads text from the player's current language file. PlaceholderAPI placeholders can also be used in supported text when it is installed. Some values are only available in the specified item template.

Each layout character represents content or a button. `.` and spaces leave a slot empty, and a character may be repeated. A layout may contain at most six rows, normally with nine characters per row.

## `main.yml`: main menu

The main menu uses the free-menu format and contains only static entry buttons.

| Option | Purpose |
| --- | --- |
| `title` | Inventory title. Language and color placeholders are supported. |
| `layout` | Sets button positions and menu height. The default `S`, `A`, `U`, `E`, and `C` characters represent skills, attributes, allocation, the enchantment catalogue, and close. |
| `items.<character>` | Defines the static button represented by that layout character. |
| `items.<character>.item` | The displayed item. Item options may alternatively be placed directly below `items.<character>`. |
| `items.<character>.actions` | Actions run in order when the button is clicked. The default menu uses `open_menu` and `close`. |
| `actions.<name>.type` | Action type. `open_menu` also requires the target `menu` ID. The action name only distinguishes multiple actions. |

### Shared free-menu placeholders

These work in the `title`, static items, and dynamic content items of a free menu:

| Placeholder | Meaning |
| --- | --- |
| `{player}` | Name of the player viewing the menu. |
| `{menu}` | Current menu ID, without `.yml`. |
| `{page}` | Current page, starting at 1. |
| `{pages}` | Total pages; when there are multiple content regions, the largest collection determines this value. |
| `{points}` | Player's available attribute points. |
| `{attribute_points}` | Same as `{points}`. |

## `skill-info.yml`: skill overview

| Option | Purpose |
| --- | --- |
| `title` | Menu title, commonly using `{page}` and `{pages}`. |
| `layout` | `S` marks dynamic skill slots; other characters refer to `items`. The number of dynamic slots determines the page size. |
| `contents.S.type` | Content type. The skill overview uses `skills`. |
| `contents.S.use-skill-icon` | Defaults to `true`, using each skill file's top-level `icon` as the base item, or `EXPERIENCE_BOTTLE` if absent. Set to `false` to ignore skill icons. The menu's `item.material` takes priority; omit it to retain individual icons. Use `item.name` and `item.lore` for shared display text. See [icon examples](../configs/menus.md#skill-and-attribute-icons). |
| `contents.S.include` | Optional skill-ID allowlist. Empty means all skills. |
| `contents.S.exclude` | Optional skill-ID denylist applied after `include`. |
| `contents.S.item` | Display template for every skill entry. |
| `contents.S.actions` | Actions used for every click. |
| `contents.S.click-actions.<click type>` | Actions for one Bukkit click type, such as `LEFT`. These replace generic `actions` for that click. |
| `items` | Static navigation, information, and return buttons. `hide-on-first-page`, `hide-on-last-page`, and `hidden-item` control boundary-page appearance. |

### Skill-entry placeholders

Use these in `contents.S.item`, together with the shared free-menu placeholders. `open_skill` opens the clicked skill's details.

| Placeholder | Meaning |
| --- | --- |
| `{id}` | Skill ID. |
| `{name}` | Localized skill name. |
| `{description}` | Localized skill description. |
| `{level}` | Player's current skill level. |
| `{maximum_level}` | Skill's maximum level. |
| `{experience}` | Experience accumulated at the current level. |
| `{required_experience}` | Experience required to level up; the skill system determines this value at the maximum level. |
| `{remaining_experience}` | `required_experience - experience`, floored at 0. |
| `{progress}` | Experience progress as a percentage number without `%`; 100 when no more XP is required. |
| `{progress_bar}` | Colored 20-character progress bar. |
| `{source_count}` | Number of configured XP sources for the skill. |

## `attribute-info.yml`: attribute overview

| Option | Purpose |
| --- | --- |
| `title` | Attribute overview title. |
| `layout` | `A` marks dynamic attribute slots; other characters refer to static `items`. |
| `contents.A.type` | Content type, which must be `attributes`. |
| `contents.A.mode` | Attribute filter mode. `information` only includes attributes with `show-in-attribute-gui` enabled. |
| `contents.A.include` / `exclude` | Attribute-ID allowlist and denylist. |
| `contents.A.use-attribute-icon` | Defaults to `true`, using each attribute file's top-level `icon` as the base item, or `NETHER_STAR` if absent. Set to `false` to ignore attribute icons. The menu's `item.material` takes priority; omit it to retain individual icons. Use `item.name` and `item.lore` for shared display text. Supports every attribute mode, including `allocation`. See [icon examples](../configs/menus.md#skill-and-attribute-icons). |
| `contents.A.item` | Display template for each attribute. |
| `contents.A.actions` / `click-actions` | Generic or click-specific entry actions. The default left-click action is `open_attribute`. |
| `items` | Static page, information, and return buttons. |

### Attribute-list entry placeholders

These work in `contents.A.item` in both attribute menus and in attribute regions of other free menus.

| Placeholder | Meaning |
| --- | --- |
| `{id}` | Attribute ID. |
| `{name}` | Localized attribute name. |
| `{description}` | Localized current-effect description; same as `{current_description}`. |
| `{current_description}` | Effect description generated from the player's current final value. |
| `{next_description}` | Effect description at the projected value after one base-level upgrade. |
| `{base_value}` | Player's unlocked base level. |
| `{value}` | Current final attribute value, including effective level and modifiers. |
| `{minimum_value}` | Configured attribute minimum. |
| `{maximum_value}` | Attribute allocation maximum in this list context. |
| `{next_base}` | Next base level, capped at the allocation maximum. |
| `{next_value}` | Projected final value at `{next_base}`. |
| `{next_price}` | Attribute points required to buy one base level; same as `{price}`. |
| `{price}` | Same as `{next_price}`. |
| `{requirement}` | Display text for the next level's allocation requirements. |
| `{modifier_count}` | Number of modifiers currently affecting this attribute. |

## `attribute-allocation.yml`: attribute allocation

This menu also uses the free-menu format. These options differ from `attribute-info.yml`:

| Option | Purpose |
| --- | --- |
| `contents.A.mode` | Uses `allocation`, including only attributes with `allocation.show-in-menu` enabled. |
| `contents.A.use-attribute-icon` | Same as the attribute overview; defaults to `true`. Omit `item.material` to use each attribute file's `icon`. |
| `contents.A.item` | Usually previews current/next values, `{next_price}`, and `{requirement}`. It only controls display, not the actual price or requirements. |
| `contents.A.actions` / `click-actions` | The default opens details with `open_attribute`. `allocate_attribute` may instead allocate directly, using `add` as the number of levels. |
| `items.I` | The default information item displays available points through `{points}` or `{attribute_points}`. |

Actual prices, allocation maximums, and requirements come from the `allocation` section of each `attributes/*.yml` file.

## `enchantment-info.yml`: enchantment catalogue

Characters in `layout` refer to `contents` or `items`. Use `contents` for enchantment lists and `items` for buttons. The examples below use `Q` for enchantment slots.

| Option | Description |
| --- | --- |
| `title` | Menu title. Supports `{page}` and `{pages}`. |
| `layout` | Nine slots per row, up to six rows. Dots and spaces leave slots empty. |
| `contents.Q.type` | Set to `enchantment`. |
| `contents.Q.include` | List of enchantment keys to show. Omit it or leave it empty to allow all. |
| `contents.Q.exclude` | List of enchantment keys to hide. Exclusions override inclusions. |
| `contents.Q.use-enchantment-icon` | Defaults to `true`, using rarity and enchantment icons. Set to `false` to start with the default enchanted book. |
| `contents.Q.item` | Item template for enchantment entries. Supports the placeholders below. |
| `rarity-sort-rule` | Rarities appear in list order. Unlisted rarities follow, sorted by rarity name. An empty list sorts all rarities by name. |
| `weight-order` | Defaults to `DISABLED`, ignoring weight. `DESCENDING` puts higher weight first; `ASCENDING` puts lower weight first. |
| `description-max-lines` | Lines of level descriptions per page; defaults to `20`. Zero or negative values disable description paging. |
| `description-page-format` | Description page hint. Leave empty to hide it. |
| `items.<character>.item` | Button item. Item fields can also go directly under `items.<character>`. |
| `items.<character>.filter-type` | `RARITY` filters by rarity; `SUPPORTED_ITEM` filters by supported items. Left-click advances, right-click goes back. |
| `hide-on-first-page` / `hide-on-last-page` | Hide a button on the first or last page. |
| `hidden-item` | Item shown while the button is hidden. Omit it to leave the slot empty. |
| `actions` / `click-actions` | Click actions for buttons or enchantment entries. |

Use full keys in `include` and `exclude`, such as `minecraft:sharpness`. Matching ignores case. Each content area has its own filters and pages. An enchantment with `show-in-enchantment-gui: false` is hidden from every area.

For example, exclude Curse of Binding:

```yaml
contents:
  Q:
    type: enchantment
    include: []
    exclude:
      - minecraft:binding_curse
    item:
      name: '{name}'
      lore:
        - '{level-descriptions}'
```

Default sort priority: rarity → `order` from lowest to highest → full key. Setting `weight-order` to `ASCENDING` or `DESCENDING` compares weight after `order`, before the key. The menu's `rarity-sort-rule` and `weight-order` affect the catalogue. Item tooltips use `tooltip-order` in `config.yml`.

Fields in the menu's `item` override the enchantment icon. Omit `material` here to keep different materials for different enchantments. See [enchantment icon examples](../configs/enchantment-configuration.md#display-settings).

### Enchantment-entry placeholders

Descriptions are paged only when `item.lore` contains `{level-descriptions}`. The line limit counts wrapped descriptions, excluding fixed lore and the page hint. A level is never split across pages; an oversized level gets a page of its own.

Left-click or right-click an enchantment to see the next description page. The last page loops to the first. Each enchantment remembers its own page.

Use these in `contents.Q.item`. `{description}` and `{level-descriptions}` can be empty for vanilla enchantments without a custom description.

| Placeholder | Meaning |
| --- | --- |
| `{name}` | Enchantment display name. |
| `{description}` | Localized description of a custom enchantment at its maximum level. |
| `{level-descriptions}` | Level descriptions on the current description page. |
| `{description_page}` / `{description_pages}` | Current description page and total description pages, starting at one. |
| `{level_start}` / `{level_end}` | First and last levels on this description page. Also available in `description-page-format`. |
| `{max_level}` | Maximum enchantment level. |
| `{rarity}` | Localized rarity name. |
| `{weight}` | Enchantment selection weight. |
| `{supported_items}` | Supported item category names; may fall back to a tag key or `-`. |
| `{exclusive_with}` | Localized “All” when `exclusive-with` contains `*`; otherwise the names of registered enchantments that conflict in either direction, excluding itself, or localized “None” when empty. Lists use active registry rules, including tags. |
| `{key}` | Full enchantment key, such as `minecraft:sharpness`. |

Existing menus can show conflicts by adding `- '{lang:enchantment-menu-exclusive-with}'` to `contents.Q.item.lore`. The bundled menu includes this line by default.

### Catalogue-button placeholders

Use these in `items.<character>`. A hidden button's `hidden-item` receives only `{page}`, `{pages}`, and `{amount}`.

| Placeholder | Meaning |
| --- | --- |
| `{page}` | Current page, starting at 1. |
| `{pages}` | Total pages under the current filters. |
| `{amount}` | Number of enchantments matching the current filters. |
| `{rarity_filter}` | Localized current rarity filter, or “All”. |
| `{supported_item_filter}` | Localized current supported-item filter, or “All”. |

## `skill-detail.yml`: skill details

Skill details have separate XP-source and level-reward views and use a dedicated format.

| Option | Purpose |
| --- | --- |
| `source-title` / `reward-title` | Title for each view. They fall back to `title` when omitted. |
| `size` | Inventory size, default `54`. |
| `layout` | XP-source view layout. |
| `reward-layout` | Reward view layout; falls back to `layout`. Reward slots snake across alternating rows to draw a progression path. |
| `source-symbol` | XP-source slot character, default `S`. |
| `reward-symbol` | Level-reward slot character, default `R`. |
| `attribute-symbol` | Attribute slot character in the source view, default `A`. |
| `back-symbol` | Back button character, default `B`. It returns to the menu that opened the detail. |
| `view-symbol` | Source/reward toggle character, default `V`. |
| `source-item` | Default XP-source template. A source's own `icon` supplies its base material, while this template still formats name and lore. |
| `attribute-item` | Template for attribute entries shown above the sources. Clicking one opens attribute details. |
| `reward-item` | Base name and lore template for every level reward. |
| `reward-item-claimed` | Material style used for reached levels; name and lore still come from `reward-item`. |
| `reward-item-next` | Material style for the next unlockable level. |
| `buttons.<character>` | Other static layout buttons. |
| `buttons.<view character>.reward-view-item` | Alternate toggle appearance while viewing rewards. |
| `hide-on-first-page` / `hide-on-last-page` / `hidden-item` | Boundary-page appearance for navigation buttons. |
| `actions` / `click-actions` | Normal button actions. Back and view switching are handled automatically by their symbols. |

### Shared skill-detail placeholders

These work in titles, static buttons, and source, attribute, or reward items.

| Placeholder | Meaning |
| --- | --- |
| `{id}` | Current skill ID. |
| `{name}` | Localized skill name. |
| `{description}` | Localized skill description. |
| `{level}` | Player's current skill level. |
| `{maximum_level}` | Skill's maximum level. |
| `{experience}` | Current skill experience. |
| `{required_experience}` | Experience required for the current level. |
| `{remaining_experience}` | Remaining experience, floored at 0. |
| `{progress}` | Experience progress percentage without `%`. |
| `{progress_bar}` | Colored 20-character progress bar. |
| `{source_count}` | Number of configured XP sources. |
| `{view}` | Current view: `sources` or `rewards`. |
| `{page}` | Current page in this view, starting at 1. |
| `{pages}` | Total pages in this view. |

### XP-source item placeholders

Use these in `source-item`, alongside the shared skill-detail placeholders.

| Placeholder | Meaning |
| --- | --- |
| `{source_id}` | XP-source ID. |
| `{source_name}` | Localized source name. |
| `{source_description}` | Localized source description. |
| `{source_xp}` | Source's configured XP number or formula text; actual grants may depend on conditions and multipliers. |
| `{source_unit}` | Configured unit, with a leading space when nonempty. |
| `{source_index}` | Source position in the list, starting at 1. |

### Skill-detail attribute item placeholders

Use these in `attribute-item`, alongside the shared skill-detail placeholders.

| Placeholder | Meaning |
| --- | --- |
| `{attribute_id}` | Attribute ID. |
| `{attribute_name}` | Localized attribute name. |
| `{attribute_description}` | Effect description for the current final value. |
| `{base_value}` | Player's unlocked base level. |
| `{value}` | Current final attribute value. |
| `{maximum_value}` | Attribute allocation maximum. |
| `{price}` | Points required for one more level. |
| `{next_cost}` | Localized next-level cost text, or the maximum-level message. |
| `{points}` | Player's available attribute points. |
| `{requirement}` | Display text for next-level allocation requirements. |

### Level-reward item placeholders

Use these in `reward-item`, alongside the shared skill-detail placeholders.

| Placeholder | Meaning |
| --- | --- |
| `{reward_level}` | Skill level associated with the reward. |
| `{reward_status}` | Localized status: claimed, next, or locked. |
| `{reward_count}` | Number of configured reward entries at this level. |
| `{reward_lines}` | Display names for all rewards in configuration order; uses a localized empty message if there are none. |
| `{reward_attribute_points}` | Total attribute points rewarded at this level. |
| `{reward_attributes}` | Formatted summary of attribute rewards. |
| `{reward_command_count}` | Number of command rewards. |
| `{reward_commands}` | Formatted summary of command rewards. |
| `{reward_item_count}` | Number of item rewards. |
| `{reward_items}` | Formatted summary of item rewards. |

## `attribute-detail.yml`: attribute details

| Option | Purpose |
| --- | --- |
| `title` | Detail title, normally using `{attribute_name}`. |
| `size` | Inventory size, default `54`. |
| `layout` | Positions attribute information, modifiers, and controls. |
| `modifier-symbol` | Dynamic modifier-slot character, default `M`. Extra modifiers are paginated. |
| `back-symbol` | Back button character, default `B`; returns to the originating skill menu or attribute list. |
| `upgrade-symbol` | Upgrade control character, default `U`. |
| `enable-symbol` | Per-player attribute enable/disable control, default `E`. |
| `effective-level-symbol` | Per-player unlocked effective-level selector, default `L`. |
| `upgrade-clicks.<click type>.add` | Number of base levels purchased by that click. Price and requirements still come from the attribute. |
| `effective-level-clicks.<click type>.add` | Increases or decreases the selected effective level. |
| `effective-level-clicks.<click type>.set-to` | Selects `minimum` or `maximum`; `maximum` means the player's unlocked base level. |
| `modifier-item` | Template for each active modifier. |
| `no-modifiers-item` | Item shown in the first modifier slot when none exist. |
| `buttons.<character>` | Static button definition. |
| `buttons.U.maximum-item` | Replacement for the upgrade control after reaching `allocation.maximum-value`. |
| `buttons.E.enabled-item` / `disabled-item` | Appearances for the current enabled state. |
| `hide-on-first-page` / `hide-on-last-page` / `hidden-item` | Boundary-page appearance for navigation buttons. |
| `actions` / `click-actions` | Actions for buttons other than the automatic back, upgrade, toggle, and effective-level controls. |

### Shared attribute-detail placeholders

Use these in `title`, `buttons`, `modifier-item`, and `no-modifiers-item`.

| Placeholder | Meaning |
| --- | --- |
| `{attribute_id}` | Current attribute ID. |
| `{attribute_name}` | Localized attribute name. |
| `{attribute_description}` | Effect description at the current final value; same as `{current_description}`. |
| `{current_description}` | Effect description at the current final value. |
| `{next_description}` | Effect description at the projected value after one base-level upgrade. |
| `{base_value}` | Player's unlocked base level. |
| `{unlocked_value}` | Same as `{base_value}`. |
| `{effective_base_value}` | Player-selected effective base level, at most the unlocked level. |
| `{effective_value}` | Current final attribute value; same as `{value}`. |
| `{attribute_enabled}` | Whether the player has enabled the attribute: `true` or `false`. |
| `{attribute_status}` | Localized text for the enabled or disabled state. |
| `{value}` | Current final value, including effective level and modifiers. |
| `{minimum_value}` | Configured attribute minimum. |
| `{maximum_value}` | Configured attribute maximum; different from the allocation maximum in list menus. |
| `{allocation_maximum_value}` | Highest base level reachable through allocation. |
| `{default_value}` | Configured default attribute value. |
| `{modifier_count}` | Number of active modifiers for this attribute. |
| `{next_base}` | Next base level, capped at the allocation maximum. |
| `{next_value}` | Projected final value at `{next_base}`. |
| `{price}` | Points required for one base-level upgrade. |
| `{points}` | Player's available attribute points. |
| `{requirement}` | Display text for next-level allocation requirements. |
| `{page}` | Current modifier page, starting at 1. |
| `{pages}` | Total modifier pages. |

### Modifier-item placeholders

Only available in `modifier-item`, together with the shared attribute-detail placeholders.

| Placeholder | Meaning |
| --- | --- |
| `{modifier}` | Modifier ID. |
| `{operation}` | Internal operation enum name, such as `ADD_VALUE`. |
| `{operation_display}` | Localized operation name. |
| `{amount}` | Raw modifier amount. |
| `{amount_display}` | Player-facing amount: signed addition, or a percentage for multiplier operations. |

## `settings.yml`: shared settings

| Option | Default | Purpose |
| --- | --- | --- |
| `anti-dupe-checker` | `true` | Enables duplication protection during GUI interaction. Keep it enabled unless diagnosing compatibility with another inventory plugin. |

## Common button actions

All menus share navigation, messages, commands, menu opening, and other actions, using `actions` or `click-actions`. Skill and attribute operations require the corresponding object context. See [Menu Actions](actions.md) for types, parameters, and limitations.

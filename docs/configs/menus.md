# Free Menu Configuration

This page covers the free-menu format used by `main.yml`, `skill-info.yml`, `attribute-info.yml`, `attribute-allocation.yml`, and custom menus. See [Menu Types](../menus/README.md) for how to open them, [Built-in Menu Options](../menus/builtin-options.md) for every option and placeholder, and [Menu Actions](../menus/actions.md) for action types and parameters.

A free menu's YAML file name is its ID. Open an entry menu with:

```text
/enchantmentreform menu <menu-id>
```

`skill-detail` and `attribute-detail` require a selected object. Open them from a list with `open_skill` or `open_attribute`, or append the corresponding ID to the menu command.

## Enablement and permissions

Every menu, including the catalogue, detail menus, and custom menus, supports top-level `enabled` and `permission` options. `enabled` defaults to `true`; `false` prevents opening. `permission` defaults to an empty string, which adds no permission requirement. A non-empty value requires that permission when opening from commands, buttons, or return navigation. The menu command still requires `enchantmentreform.menu`. The default main menu uses `{lang:main-menu-*}` placeholders from `override-lang` in the language files.

```yaml
enabled: true
permission: ''
```

## Layout and content regions

Free menus use a 9-character-per-row `layout` with 1–6 rows; the row count determines inventory size. A character is the ID of a `contents` region or an `items` button; repeating it allocates multiple positions. IDs below these two sections must therefore be exactly one character. There is no `slot` or `slots` option. The catalogue and two detail menus have their own dedicated layout options.

```yaml
title: '&8Character Menu &7{page}/{pages}'
layout:
  - 'FFFFFFFFF'
  - 'FAAAAAAAF'
  - 'FAAAAAAAF'
  - 'PFFFFFFFN'

contents:
  A:
    type: attributes
    mode: information
    item:
      material: AMETHYST_SHARD
      name: '&d&l{name}'
      lore:
        - '&8Attribute'
        - ' '
        - '&fCurrent effect'
        - '&7{current_description}'
        - ' '
        - '&fAfter upgrading'
        - '&7{next_description}'
        - ' '
        - '&eClick for details'
    click-actions:
      LEFT:
        open:
          type: open_attribute
```

Content types are `skills` and `attributes`. Attribute modes are `all`, `information`, `allocation`, and `skill`; skill mode shows every attribute with `skill-menu.show` enabled and no longer filters by skill ID. Both content types support `include` and `exclude` ID lists. All content sections share the menu page number; the largest collection determines the page count.

See [Built-in Menu Options and Placeholders](../menus/builtin-options.md) for the meaning of every shared, skill, and attribute placeholder.

## Static items and actions

Static items use their one-character key in `layout`. Navigation only appears when its configured character exists.

```yaml
items:
  P:
    hide-on-first-page: true
    item:
      material: ARROW
      name: '&aPrevious page'
    hidden-item:
      material: GRAY_STAINED_GLASS_PANE
      name: ' '
    actions:
      page:
        type: previous_page
  F:
    item:
      material: BLACK_STAINED_GLASS_PANE
      name: ' '
```

Static and content items can use `actions` for every click or `click-actions.<CLICK_TYPE>` for a specific Bukkit click type.

See [Menu Actions](../menus/actions.md) for all nine free-menu actions, their parameters, and which entry types can use them.

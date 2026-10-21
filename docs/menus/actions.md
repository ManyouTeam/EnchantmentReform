# 🖱️ Menu Actions

All menus share the same action types and execution path. Main menus, skill/attribute overviews, custom menus, the enchantment catalogue, and detail pages support the common actions below. Actions requiring a skill or attribute still need that object in the current menu context.

## Configuration locations and clicks

| Entry location | Actions | Item appearance |
| --- | --- | --- |
| Static buttons in main, overview, and custom menus | `items.<character>.actions` | `items.<character>.item` |
| Dynamic skill or attribute entries | `contents.<character>.actions` | `contents.<character>.item` |
| Static catalogue and detail buttons | `buttons.<character>.actions` | `buttons.<character>` |

`items` and `buttons` describe different layout structures; action types and parameters are identical. Arbitrarily named children under `actions` run in YAML order. For one action, `type` may be placed directly under `actions`.

Every location above also supports `click-actions.LEFT`, `click-actions.RIGHT`, `click-actions.SHIFT_LEFT`, and other Bukkit click types. A matching click-specific section completely replaces generic `actions` for that click. An empty section suppresses generic actions for that click.

```yaml
items:
  H:
    item:
      material: COMPASS
      name: '&bBack to main menu'
    actions:
      open:
        type: open_menu
        menu: main
    click-actions:
      RIGHT:
        type: message
        message: '&eLeft-click to return to the main menu.'
```

The same actions can be placed under `buttons.H.actions` or `buttons.H.click-actions.RIGHT` on a detail page without changing their types.

## Common actions

| `type` | Effect | Parameters |
| --- | --- | --- |
| `previous_page` | Previous page in the current view; stops at the first page. | None. |
| `next_page` | Next page in the current view; stops at the final page. | None. |
| `refresh` | Reopens the current page. | None. |
| `close` | Closes the inventory. | None. |
| `open_menu` | Opens another registered menu, retaining the current permission-check mode. | `menu`: menu ID, default `main`. Use `enchantment-info` for the catalogue. For details requiring a selected object, use `open_skill` or `open_attribute` below. |
| `message` | Sends a chat message to the clicker. | `message`. |
| `title` | Shows a title and subtitle. | `main-title`, `sub-title`, `fade-in`, `stay`, `fade-out`. |
| `action_bar` | Shows a message above the hotbar. | `message`. |
| `announcement` | Broadcasts a chat message to all online players. | `message`. |
| `sound` | Plays a sound at the clicker's location. | `sound`; optional `volume` and `pitch`, both default `1`. |
| `particle` | Spawns particles near the clicker. | `particle`, `count`, `offset-x`, `offset-y`, `offset-z`, `speed`. |
| `effect` | Applies a potion effect to the clicker. | `potion`, `duration` (ticks), `level` (amplifier); optional `ambient`, `particles`, and `icon`, all default `true`. |
| `console_command` | Executes a command as console. | `command`, without `/`. |
| `player_command` | Executes a command as the clicker. | `command`, without `/`. |
| `op_command` | Executes a command with temporary OP privileges. | `command`, without `/`. |
| `command` | Retains the existing command syntax. | `command`; `as-console` defaults to `true`, or `false` for player execution. New configurations can use `console_command` or `player_command` directly. |
| `teleport` | Teleports the clicker. | `world`, `x`, `y`, `z`; optional `yaw` and `pitch`. |
| `entity_spawn` | Spawns an entity at a position. | `entity`; `world`, `x`, `y`, and `z` can specify a position. |
| `chance` | Runs nested actions with a percentage chance. | `rate` (0–100), `actions`. |
| `any` | Runs a random selection of nested actions. | `actions`; optional `amount`, default `1`. |
| `delay` | Runs nested actions after a delay. | `time` in server ticks, `actions`; the player must still be online. |

Nested `actions` in `chance`, `any`, and `delay` use the same format, including menu navigation and actions using the selected object context.

## Skill and attribute context

| `type` | Effect | Available locations and parameters |
| --- | --- | --- |
| `open_skill` | Opens details for the current skill. | Dynamic skill entries, skill details, and attribute details opened from skill details. |
| `open_attribute` | Opens details for the current attribute. | Dynamic attribute entries or attribute details. |
| `allocate_attribute` | Allocates points to the current attribute, then refreshes the current page. | Dynamic attribute entries or attribute details. `add` defaults to `1`, minimum `1`; point, maximum, and requirement checks still apply. |

Without the corresponding object context, these actions do nothing. Ordinary static buttons do not automatically select a skill or attribute.

## Text placeholders

Text actions in every menu support `{player}` (player name), `{menu}` (current menu ID), `{page}` (starting at 1), `{pages}` (total pages), `{slot}` (zero-based inventory slot), and `{click}` (Bukkit click type). With object context, `{skill}` and `{attribute}` contain the corresponding IDs; otherwise they are empty. PlaceholderAPI placeholders also work when installed.

## Clicks handled by the menu itself

The `back-symbol` and `view-symbol` in `skill-detail.yml`, and the `back-symbol`, `upgrade-symbol`, `enable-symbol`, and `effective-level-symbol` in `attribute-detail.yml`, handle clicks directly. Their primary behavior cannot be replaced by `actions` or `click-actions`. Dynamic attributes in skill details open attribute details directly. Catalogue buttons with `filter-type` switch the filter before button actions are considered.

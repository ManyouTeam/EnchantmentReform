# Menu Actions

Menus use two action formats. `main.yml`, `skill-info.yml`, `attribute-info.yml`, `attribute-allocation.yml`, and custom menus use [free-menu actions](#free-menu-actions). `enchantment-info.yml`, `skill-detail.yml`, and `attribute-detail.yml` use [dedicated-menu button actions](#dedicated-menu-button-actions). Action types are not interchangeable between the two formats.

## Free-menu actions

Place static-button actions at `items.<character>.actions` and skill or attribute entry actions at `contents.<character>.actions`. To distinguish clicks, use `click-actions.LEFT`, `click-actions.RIGHT`, `click-actions.SHIFT_LEFT`, or another Bukkit click type. A matching click-specific action replaces generic `actions` for that click.

Each arbitrarily named child of `actions` is one action, run in YAML order. For a single action, `type` may also be placed directly under `actions`.

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
```

| `type` | Effect | Parameters and limits |
| --- | --- | --- |
| `open_menu` | Opens another registered menu. | `menu`: target menu ID, default `main`. `enchantment-info` opens the dedicated catalogue. Do not use it for a detail menu that needs a selected entry. |
| `open_skill` | Opens details for the clicked skill. | Only works on a dynamic `type: skills` entry, which supplies the skill context. |
| `open_attribute` | Opens details for the clicked attribute. | Only works on a dynamic `type: attributes` entry. |
| `allocate_attribute` | Allocates points to the clicked attribute, then refreshes. | Only works on an attribute entry. `add` sets the number of levels, default `1`, minimum `1`. Point, maximum, and requirement checks still apply. |
| `previous_page` | Opens the previous page. | Stops at the first page. |
| `next_page` | Opens the next page. | Stops at the final page. |
| `refresh` | Rebuilds the current page. | No extra parameters. |
| `close` | Closes the player's inventory. | No extra parameters. |
| `command` | Runs one command. | `command`: command without `/`. `as-console` defaults to `true`; set it to `false` to execute as the player. The command text supports `{player}` and `{menu}`. |

## Dedicated-menu button actions

Dedicated menus place actions at `buttons.<character>.actions.<action name>`. The action name is arbitrary; `type` determines behavior. The item properties are directly under `buttons.<character>`, without an `item` wrapper.

```yaml
buttons:
  P:
    material: ARROW
    name: '&aPrevious page'
    actions:
      page:
        type: previous_page
```

Dedicated buttons support these action types:

| `type` | Effect | Main parameters |
| --- | --- | --- |
| `previous_page` | Previous page in the current view. | None. |
| `next_page` | Next page in the current view. | None. |
| `refresh` | Redisplays the current page. | None. |
| `close` | Closes the inventory. | None. |
| `message` | Sends a chat message to the clicker. | `message`. |
| `title` | Shows a screen title and subtitle. | `main-title`, `sub-title`, `fade-in`, `stay`, `fade-out`. |
| `action_bar` | Shows a message above the hotbar. | `message`. |
| `announcement` | Broadcasts a chat message to all online players. | `message`. |
| `sound` | Plays a sound at the clicker's location. | `sound`; optional `volume` and `pitch`, both default `1`. |
| `particle` | Spawns particles near the clicker. | `particle`, `count`, `offset-x`, `offset-y`, `offset-z`, `speed`. |
| `effect` | Applies a potion effect to the clicker. | `potion`, `duration` (ticks), `level` (amplifier); optional `ambient`, `particles`, and `icon`, all default `true`. |
| `console_command` | Executes a command as console. | `command`, without `/`. |
| `op_command` | Executes a command with temporary OP privileges. | `command`, without `/`. |
| `player_command` | Executes a command as the clicker. | `command`, without `/`. |
| `teleport` | Teleports the clicker. | `world`, `x`, `y`, `z`; optional `yaw` and `pitch`. |
| `entity_spawn` | Spawns an entity at a position. | Required `entity`; `world`, `x`, `y`, and `z` specify a position. |
| `chance` | Runs nested actions with a percentage chance. | `rate` from 0–100 and nested `actions`. |
| `any` | Runs a random selection of nested actions. | Nested `actions`; optional `amount`, default `1`. |
| `delay` | Runs nested actions after a delay. | `time` in server ticks and nested `actions`; the player must still be online. |

Text actions in dedicated menus can use `{page}` (starting at 1), `{pages}` (total pages), `{slot}` (zero-based inventory slot), and `{click}` (Bukkit click type). PlaceholderAPI placeholders also work when installed.

Although `conditional` is registered in the shared action manager, menu contexts have no condition checker, so its condition never succeeds. `mythicmobs_spawn` is not registered for menu actions.

The `back-symbol` and `view-symbol` in `skill-detail.yml`, and the `back-symbol`, `upgrade-symbol`, `enable-symbol`, and `effective-level-symbol` in `attribute-detail.yml`, handle clicks directly. Their primary behavior cannot be replaced by `actions`. A catalogue button with `filter-type` switches the filter before any button action is considered.

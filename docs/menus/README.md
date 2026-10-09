# Menu Types

Built-in menus are stored in `plugins/EnchantmentReform/menus/`. Except for `settings.yml`, each YAML file name is also its menu ID and can be opened with:

```text
/enchantmentreform menu <menu-id>
```

The console must specify an online player: `enchantmentreform menu <menu-id> <player> [skill-id|attribute-id]`. For example, `enchantmentreform menu main Steve` or `enchantmentreform menu skill-detail Steve mining`. Player command syntax is unchanged. Menu enablement and permission checks apply to the target player. Console Tab completion suggests enabled menus, online players, and detail IDs.

Names, descriptions, materials, layouts, and buttons are configurable. Run `/enchantmentreform reload` after editing them.

Reading guide: [Built-in Menu Options and Placeholders](builtin-options.md) explains each YAML option and dynamic text value; [Free Menu Configuration](../configs/menus.md) provides reusable layout examples; [Menu Actions](actions.md) lists both action formats, their parameters, and where they work.

## Built-in menus

| Menu ID | File | Purpose | How to open |
| --- | --- | --- | --- |
| `main` | `main.yml` | The default hub for skills, attributes, attribute allocation, and the enchantment catalogue. | `/enchantmentreform menu main` |
| `enchantment-info` | `enchantment-info.yml` | Lists enchantments with level descriptions, rarity, weight, and supported items. It can filter by rarity and item type. | `/enchantmentreform menu enchantment-info` |
| `skill-info` | `skill-info.yml` | Lists skills with their level, XP, progress, and number of XP sources. | `/enchantmentreform menu skill-info` |
| `skill-detail` | `skill-detail.yml` | Shows one skill and switches between its XP sources and level-reward path. It also lists available attributes. | Click a skill, or use `/enchantmentreform menu skill-detail <skill-id>` |
| `attribute-info` | `attribute-info.yml` | A read-only attribute overview with base values, final values, effects, and modifier counts. | `/enchantmentreform menu attribute-info` |
| `attribute-detail` | `attribute-detail.yml` | Shows one attribute's modifiers, effective-level controls, upgrade requirements, and upgrade control. | Click an attribute, or use `/enchantmentreform menu attribute-detail <attribute-id>` |
| `attribute-allocation` | `attribute-allocation.yml` | The global attribute-allocation entry point, including before/after values, prices, and requirements. | `/enchantmentreform menu attribute-allocation` |

## Free menus

You can add any `.yml` file to `menus/`. Its file name is registered automatically as the menu ID; for example, `profile.yml` is opened with `/enchantmentreform menu profile`.

A free menu can contain static buttons and `skills` or `attributes` content regions. Actions can open another menu, open a skill or attribute detail, change pages, refresh or close the inventory, allocate attribute points, or run a command. See [Free Menu Configuration](../configs/menus.md) for the complete layout, content-region, placeholder, and action formats.

`enchantment-info`, `skill-detail`, and `attribute-detail` use dedicated YAML formats. Their `buttons` actions differ from free-menu `items` actions; see [Menu Actions](actions.md).

## Shared settings

`settings.yml` is not an openable menu. It contains settings shared by every menu:

| Option | Default | Description |
| --- | --- | --- |
| `anti-dupe-checker` | `true` | Enables the GUI anti-duplication check. Keep it enabled unless troubleshooting a compatibility problem. |

## Data sources

- Skill menus read `skills/*.yml`; the complete skill module must also be enabled at `config.yml -> modules.skills`.
- Attribute menus read `attributes/*.yml`. The `show-in-attribute-gui`, `skill-menu.show`, and `allocation.show-in-menu` options control visibility in their respective menus.
- The enchantment catalogue reads registered vanilla and custom enchantments.
- Menu text supports language placeholders, color codes, and the dynamic placeholders supplied by that menu.

# 🧱 Basic and metadata rules

This page documents MatchItemFormat rules for provider IDs, material, tags, rarity, display name, and lore. Rules at the same level use logical AND; list entries inside one rule normally use OR.

## Rules on this page

- `items`
- `material`
- `material-tag`
- `rarity`
- `has-name`
- `contains-name`
- `has-lore`
- `contains-lore`

## `items`

Matches item IDs understood by registered item-provider hooks.

```yaml
match-item:
  items:
    - namespace:custom_sword
    - another_provider_item
```

The accepted ID syntax depends on the provider integration. Prefer a stable provider namespace and internal ID over matching display text.

When the provider plugin or hook is not available, the ID cannot be resolved reliably. Check startup logs and the compatibility page.

## `material`

Matches Bukkit or namespaced material identifiers.

```yaml
match-item:
  material:
    - DIAMOND_SWORD
    - NETHERITE_SWORD
```

Use material enum names supported by the target Minecraft version. A material removed or renamed by a newer API will not match.

A single value and a list may be accepted depending on parser behavior; lists are clearer when several materials are allowed.

## `material-tag`

Matches Minecraft/item tags.

```yaml
match-item:
  material-tag:
    - minecraft:swords
    - minecraft:axes
```

Tags are preferable to long material lists because they follow the server registry and can include mod/plugin-defined tag content where supported.

Common vanilla tag examples include:

- `minecraft:swords`
- `minecraft:axes`
- `minecraft:pickaxes`
- `minecraft:shovels`
- `minecraft:hoes`
- `minecraft:damageable`

Verify tag names against the target server version. EnchantmentReform's own supported-item tags used by enchantment registration are related but configured separately under `config.yml -> supported-items.tags`.

## Combining material and tag

Rules at the same level use AND, so this configuration is usually impossible unless the item satisfies both:

```yaml
match-item:
  material:
    - DIAMOND_SWORD
  material-tag:
    - minecraft:pickaxes
```

Use `any` when either group should pass:

```yaml
match-item:
  any:
    sword:
      material-tag:
        - minecraft:swords
    special-pickaxe:
      material:
        - DIAMOND_PICKAXE
```

## `rarity`

Matches modern item rarity where the server API exposes it.

```yaml
match-item:
  rarity:
    - RARE
    - EPIC
```

This rule requires a server version with item-rarity support. Do not confuse an item's client rarity/component with the custom enchantment's `rarity` field.

If the rule is unavailable on the current server, it may not be registered. Unknown MatchItemFormat keys are ignored, so always verify optional rule registration in logs.

## `has-name`

Requires or rejects a custom display name.

```yaml
match-item:
  has-name: true
```

```yaml
match-item:
  has-name: false
```

A translated vanilla item name is not necessarily a custom name. This rule checks item metadata/component state as implemented by the matcher.

## `contains-name`

Checks whether the display/custom name contains configured text.

```yaml
match-item:
  contains-name:
    - Legendary
    - 传说
```

Entries normally use OR. Text comparison may pass through color/component normalization implemented by the shared matcher, but do not assume formatting codes are always stripped.

Name matching is less stable than provider IDs or persistent metadata because players and other plugins can rename items.

## `has-lore`

Requires or rejects lore.

```yaml
match-item:
  has-lore: true
```

An empty lore component may behave differently from no lore depending on the server API and item serialization.

## `contains-lore`

Checks whether one or more lore lines contain configured text.

```yaml
match-item:
  contains-lore:
    - Soulbound
    - Cannot be traded
```

Use this for human-readable markers only when a more stable provider ID, PDC/component rule, or enchantment rule is unavailable.

## Complete examples

### Named sword

```yaml
match-item:
  material-tag:
    - minecraft:swords
  has-name: true
  contains-name:
    - Hunter
```

The item must be a sword, have a custom name, and contain `Hunter`.

### Provider item or vanilla fallback

```yaml
match-item:
  any:
    provider:
      items:
        - namespace:miner_pickaxe
    vanilla:
      material:
        - NETHERITE_PICKAXE
      contains-lore:
        - Mining Tool
```

### Reject disabled items

```yaml
match-item:
  material-tag:
    - minecraft:damageable
  not:
    contains-lore:
      - Disabled
```

## Matching translated or component text

Modern Minecraft stores names and lore as components. A plugin may use MiniMessage, legacy color codes, translation components, or raw JSON components. For dependable automation:

1. prefer provider/item IDs;
2. prefer material/tags;
3. prefer enchantment or stable component/PDC data;
4. use name/lore text last.

## Common mistakes

- expecting list rules at the same level to use OR with other keys;
- using an invalid material or tag for the server version;
- matching custom enchantment rarity with the item-rarity rule;
- relying on a provider ID without its hook;
- treating a vanilla translated name as a custom name;
- matching color-formatted text without checking normalization;
- assuming unknown optional keys fail the match—they are ignored when unregistered.

See [Logical and NBT Rules](logic-nbt.md) for `any`, `not`, and optional NBT rules, and [Enchantment and ItemFormat Rules](enchantments-format.md) for enchantment-aware matching.

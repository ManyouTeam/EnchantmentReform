# ✨ Enchantment and ItemFormat Rules

This page documents MatchItemFormat rules for direct enchantments, stored enchantments, enchantment levels/counts, enchantability, and serialized ItemFormat comparison.

## Rules on this page

* `has-enchants`
* `has-stored-enchants`
* `contains-enchants`
* `contains-enchants-amount`
* `enchantable`
* `item-format`

## Direct and stored enchantments

Minecraft stores direct item enchantments separately from enchantments stored in an enchanted book. Choose the rule that matches the component being inspected.

## `has-enchants`

Requires one or more direct enchantment keys.

```yaml
match-item:
  has-enchants:
    - minecraft:sharpness
    - enchantmentreform:full_health_strike
```

List behavior follows the rule implementation; use a separate `any` group when it must be explicit that either enchantment is sufficient.

Use namespaced keys. Display names are translated and not stable identifiers.

## `has-stored-enchants`

Requires enchantments stored on an enchanted book or another compatible stored-enchantment component.

```yaml
match-item:
  material:
    - ENCHANTED_BOOK
  has-stored-enchants:
    - enchantmentreform:auto_smelt
```

A direct enchantment on a sword does not satisfy a stored-enchantment rule, and an enchanted-book entry does not necessarily satisfy a direct-enchantment rule.

## `contains-enchants`

Checks enchantment keys together with level requirements.

```yaml
match-item:
  contains-enchants:
    minecraft:sharpness: 3
```

Depending on parser syntax, a numeric value can represent an exact level or minimum threshold. Use the range/comparison structure shown by current bundled examples when exact bounds are required.

Example with several keys:

```yaml
match-item:
  contains-enchants:
    minecraft:unbreaking: 2
    enchantmentreform:vein_mining: 1
```

Rules at the same level are generally AND, so the item must satisfy each configured key unless nested logical groups are used.

## `contains-enchants-amount`

Checks how many matching enchantments are present.

```yaml
match-item:
  contains-enchants-amount:
    min: 2
    max: 5
```

Use this for generic rules such as "an item with at least two enchantments". It is not a substitute for checking specific keys when the identity of the enchantment matters.

Stored and direct enchantments may be counted separately depending on the rule implementation. Test enchanted books and normal equipment independently.

## `enchantable`

Checks whether the item is enchantable under the configured rule.

```yaml
match-item:
  enchantable: true
```

Enchantability can involve material/component data and the current Minecraft version. It does not mean every custom enchantment supports the item. To check whether a particular EnchantmentReform enchantment accepts an item, use its supported-item set or a matching tag equivalent.

An item may also be enchantable while already enchanted.

## `item-format`

Matches selected serialized ItemFormat fields.

```yaml
match-item:
  item-format:
    material: DIAMOND_SWORD
    name: '&bExample Sword'
```

This rule is useful when the same ItemFormat vocabulary is already used to create items. It can compare more structured metadata than simple material/name/lore rules.

Only fields understood by the current ItemFormat matcher participate. Avoid copying a complete serialized item when only two stable fields are needed; overly strict comparisons break when unrelated metadata changes.

## Custom enchantment keys

EnchantmentReform enchantments are registered with namespaced keys:

```yaml
key: enchantmentreform:example
```

Use that same key in MatchItemFormat:

```yaml
match-item:
  has-enchants:
    - enchantmentreform:example
```

Changing the enchantment key creates a different registry identity and breaks matching against the old key. Keep keys stable across updates.

## Direct versus active enchantment

These rules inspect the item component. They do not necessarily mean the enchantment is currently active.

An enchantment can exist on an item but be inactive because:

* the item is not in an `active-slots` slot;
* activation abilities suppress it;
* the enchantment file is disabled or failed to register;
* the item no longer belongs to the supported item set after a configuration change.

Use power runtime/slot logic when activity, rather than item data, is the requirement.

## Example: high-level custom enchantment

```yaml
match-item:
  material-tag:
    - minecraft:pickaxes
  contains-enchants:
    enchantmentreform:vein_mining: 3
```

## Example: vanilla or custom protection

```yaml
match-item:
  any:
    vanilla:
      has-enchants:
        - minecraft:protection
    custom:
      has-enchants:
        - enchantmentreform:magic_guard
```

## Example: enchanted-book filter

```yaml
match-item:
  material:
    - ENCHANTED_BOOK
  has-stored-enchants:
    - enchantmentreform:full_health_strike
```

## Example: flexible ItemFormat comparison

```yaml
match-item:
  item-format:
    material: NETHERITE_SWORD
  not:
    contains-lore:
      - Disabled
```

## Acquisition and matching

`obtaining-sources` controls how an enchantment can naturally appear. It does not affect whether MatchItemFormat can detect the enchantment after it is already present.

Likewise, `exclusive-with` controls compatibility during enchantment operations; it does not hide either key from matching.

## Common mistakes

* using `has-enchants` for an enchanted-book stored entry;
* using display names instead of namespaced enchantment keys;
* confusing item enchantability with support for a specific enchantment;
* assuming an enchantment on an item is active in the current slot;
* changing a custom enchantment key and expecting old items to match;
* over-constraining `item-format` with unstable metadata;
* assuming count rules include direct and stored components identically.

See [Enchantment configuration](../../configs/enchantment-configuration.md), [ItemFormat™](../itemformat-tm.md), and [Logical and NBT Rules](logic-nbt.md).

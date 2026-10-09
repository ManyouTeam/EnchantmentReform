# 🧠 Logical and NBT rules

This page documents MatchItemFormat composition and optional legacy NBT matching.

## Rules on this page

- `none`
- `any`
- `not`
- `contains-nbt`
- `nbt-string`
- `nbt-byte`
- `nbt-int`
- `nbt-double`

The NBT rules are registered only when NBTAPI is available while EnchantmentReform enables.

## Default AND behavior

Every recognized rule at the same level must pass:

```yaml
match-item:
  material-tag:
    - minecraft:swords
  has-name: true
  contains-name:
    - Legendary
```

The item must satisfy all three rule groups.

## `none`

Forces a MatchItemFormat section to fail when enabled.

```yaml
match-item:
  none: true
```

This is useful as an explicit disabled placeholder or a generated configuration value. It is clearer to remove an unused optional filter when possible.

## `any`

Passes when at least one nested group matches.

```yaml
match-item:
  any:
    sword:
      material-tag:
        - minecraft:swords
    axe:
      material-tag:
        - minecraft:axes
```

Each child under `any` is a complete MatchItemFormat group. Rules inside one child still use AND.

### Mixed provider and vanilla alternative

```yaml
match-item:
  any:
    provider:
      items:
        - namespace:custom_pickaxe
    vanilla:
      material:
        - NETHERITE_PICKAXE
      has-enchants:
        - minecraft:efficiency
```

This passes for the provider item, or for a Netherite pickaxe with Efficiency.

## `not`

Rejects a nested match.

```yaml
match-item:
  material-tag:
    - minecraft:damageable
  not:
    contains-lore:
      - Disabled
```

The item must be damageable and must not have lore containing `Disabled`.

### Reject several alternatives

```yaml
match-item:
  not:
    any:
      cursed:
        has-enchants:
          - minecraft:binding_curse
      blocked-name:
        contains-name:
          - Admin Item
```

## Nested logic

The following expression means:

```text
(sword OR axe) AND NOT disabled
```

```yaml
match-item:
  any:
    sword:
      material-tag:
        - minecraft:swords
    axe:
      material-tag:
        - minecraft:axes
  not:
    contains-lore:
      - Disabled
```

Keep nesting shallow when possible. Deep logic is harder to debug and can usually be replaced with reusable item IDs/tags.

## Optional NBT rules

Modern Minecraft item data increasingly uses data components rather than legacy arbitrary NBT. The rules below exist for compatibility through NBTAPI and should not be the first choice for new configuration.

{% hint style="warning" %}
If NBTAPI is not loaded when EnchantmentReform registers match rules, these keys are unknown and therefore ignored. An ignored optional key can make a match broader than intended. Verify startup logs before relying on NBT rules.
{% endhint %}

## `contains-nbt`

Checks whether the item contains a configured NBT structure or path/value set.

```yaml
match-item:
  contains-nbt:
    custom-key: custom-value
```

The exact serialization syntax follows the NBTAPI-backed matcher implementation. Start from a working bundled/provider example.

## `nbt-string`

Checks a string NBT value.

```yaml
match-item:
  nbt-string:
    path: custom.id
    value: example_sword
```

Path and field names are case-sensitive where the underlying NBT is case-sensitive.

## `nbt-byte`

Checks a byte value, commonly used for boolean-like flags.

```yaml
match-item:
  nbt-byte:
    path: custom.bound
    value: 1
```

Do not assume every boolean component is represented as a legacy byte on modern versions.

## `nbt-int`

Checks an integer value.

```yaml
match-item:
  nbt-int:
    path: custom.tier
    value: 3
```

Use an integer rule only when the stored tag type is actually integer. Numeric NBT types are not always automatically converted.

## `nbt-double`

Checks a double value.

```yaml
match-item:
  nbt-double:
    path: custom.power
    value: 1.5
```

Exact floating-point comparison can be fragile. Prefer a stable integer tier or provider ID where possible.

## NBT versus PDC and provider IDs

Prefer these identifiers in order:

1. registered custom-item provider ID;
2. namespaced enchantment key;
3. stable material/tag and component metadata;
4. plugin-owned PersistentDataContainer/component rule where supported;
5. legacy NBT matching only when necessary.

Provider plugins may rewrite their internal NBT between versions while keeping the public item ID stable.

## Debugging logic

1. Test each nested group as the entire `match-item` section.
2. Confirm every rule key is registered.
3. Replace optional NBT checks with a material/provider-ID check temporarily.
4. Verify the actual NBT type, path, and value with a trusted inspection tool.
5. Add `not` only after the positive rule works.
6. Log or display the exact item used by the trigger; delayed/equipment events may provide a different item than expected.

## Common mistakes

- expecting top-level rules to use OR;
- placing several individual rule keys under `any` without grouping them correctly;
- accidentally negating a broader group than intended;
- relying on an NBT rule when NBTAPI was not registered;
- comparing an integer tag with `nbt-double` or vice versa;
- matching provider-private NBT instead of the public item ID;
- assuming unknown keys make the match fail.

See [Basic and Metadata Rules](basic-metadata.md) and [Enchantment and ItemFormat Rules](enchantments-format.md).

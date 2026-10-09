# 🛡️ Equipment and integration rules

This page documents MatchEntityFormat rules for equipment and optional mob-plugin integrations.

## Rules on this page

- `equip`
- `mythicmobs`
- `levelled-mobs`

The integration rules are registered only when their plugins and compatible hooks are available while EnchantmentReform enables.

## `equip`

Matches one or more equipment slots using nested MatchItemFormat sections.

```yaml
match-entity:
  equip:
    main-hand:
      material-tag:
        - minecraft:swords
    helmet:
      material:
        - NETHERITE_HELMET
```

Every configured slot must match its nested item rules unless the equipment matcher documents another composition mode.

Typical slot keys can include:

- `main-hand`
- `off-hand`
- `helmet`
- `chestplate`
- `leggings`
- `boots`

Use the exact slot names accepted by the current implementation. Slot naming may differ from Bukkit enum names because the configuration parser provides user-facing aliases.

## Matching an empty slot

An absent item is usually represented by air/empty `ItemStack` behavior. Use the MatchItemFormat form supported by the equipment matcher rather than assuming `material: AIR` always means an empty slot.

When an empty-slot condition matters, test it separately with the exact entity type and server version.

## Equipment example: ranged skeleton

```yaml
match-entity:
  entity-types:
    - SKELETON
    - STRAY
  equip:
    main-hand:
      material:
        - BOW
        - CROSSBOW
```

This is stricter than `ranged: true`: it checks the actual main-hand item.

## Equipment example: enchanted weapon

```yaml
match-entity:
  equip:
    main-hand:
      material-tag:
        - minecraft:swords
      has-enchants:
        - minecraft:sharpness
```

All MatchItemFormat rules can be nested under a slot, including logical groups and optional provider/NBT rules.

## `mythicmobs`

Matches MythicMobs internal mob IDs when the MythicMobs hook is loaded.

```yaml
match-entity:
  mythicmobs:
    - SkeletonKing
    - EliteZombie
```

Use MythicMobs internal IDs, not display names. A MythicMob's Bukkit entity type can still be checked with `entity-types`, but the internal ID is more precise.

### Combining base type and MythicMobs ID

```yaml
match-entity:
  entity-types:
    - SKELETON
  mythicmobs:
    - SkeletonKing
```

Both rules must pass. This can protect against accidental ID or hook mismatches but is unnecessary when the MythicMobs ID alone is sufficient.

## `levelled-mobs`

Matches LevelledMobs data when the LevelledMobs hook is loaded.

```yaml
match-entity:
  levelled-mobs:
    # level/rule fields supported by the hook
```

The exact configuration structure depends on the bundled integration. Start from a current example or hook implementation and verify it against the installed LevelledMobs version.

Typical uses include filtering by generated level, managed status, or another piece of LevelledMobs metadata exposed by the hook.

## Optional rule registration

If MythicMobs or LevelledMobs is absent or incompatible, the corresponding MatchEntityFormat rule may not be registered.

Unknown keys are ignored by MatchEntityFormat. This means an unavailable optional rule can make a matcher broader than intended.

{% hint style="warning" %}
Always confirm startup logs show the required integration hook before relying on `mythicmobs` or `levelled-mobs` for security, rewards, or destructive abilities.
{% endhint %}

## Safe integration fallback

When a power must do nothing unless the integration is present, combine the integration rule with another stable restriction where practical:

```yaml
match-entity:
  entity-types:
    - WITHER_SKELETON
  mythicmobs:
    - InfernalGuard
```

This does not replace the hook check, but it prevents the matcher from becoming completely unrestricted if the optional key is ignored.

For high-risk actions, disable the enchantment/configuration entirely when the required dependency is missing.

## Equipment changes during delayed execution

An entity can change equipment after it is selected. A `nearby_entities` ability may match the entity first, then a delayed child runs later.

When exact equipment must still be present:

- avoid long delays;
- repeat the `match_entity` condition inside the delayed branch where possible;
- do not assume a captured `ItemStack` reference remains current;
- account for mobs picking up or dropping equipment.

## Custom-item equipment

Nested MatchItemFormat can use provider IDs:

```yaml
match-entity:
  equip:
    main-hand:
      items:
        - namespace:boss_sword
```

The corresponding item-provider hook must also be registered. MythicMobs entity matching and MythicMobs item matching are separate integrations.

## Example: target an equipped MythicMob

```yaml
match-entity:
  mythicmobs:
    - SkeletonKing
  equip:
    main-hand:
      material:
        - BOW
    helmet:
      has-name: true
```

## Example: exclude armored targets

```yaml
match-entity:
  monster: true
  not:
    equip:
      chestplate:
        material-tag:
          - minecraft:chest_armor
```

Verify that the tag exists and that empty equipment behaves as expected.

## Common mistakes

- using display names instead of MythicMobs internal IDs;
- relying on an optional key without checking hook registration;
- assuming unknown integration keys fail closed;
- using Bukkit slot names when the configuration parser expects aliases;
- expecting `ranged: true` and bow equipment matching to be identical;
- checking equipment once, then executing a delayed action after the item changed;
- confusing an entity-provider integration with an item-provider integration.

See [MatchItemFormat](../match-item-format/README.md), [Identity and State Rules](identity-state.md), and [Logical Rules](logic.md).

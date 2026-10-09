# 🧠 Logical rules

This page documents MatchEntityFormat composition. Rules at the same level use logical AND; use `any` for alternatives and `not` for exclusions.

## Rules on this page

- `none`
- `any`
- `not`

## Default AND behavior

```yaml
match-entity:
  monster: true
  entity-types:
    - ZOMBIE
    - HUSK
  entity-health:
    max: 20
```

The entity must be a Bukkit `Monster`, must be either a Zombie or Husk, and must satisfy the health rule.

## `none`

Forces the matcher to fail when enabled.

```yaml
match-entity:
  none: true
```

This is useful for explicit disabled/generated configuration, but removing an unused matcher is normally clearer.

## `any`

Passes when at least one nested MatchEntityFormat group matches.

```yaml
match-entity:
  any:
    zombie:
      entity-types:
        - ZOMBIE
        - HUSK
    skeleton:
      entity-types:
        - SKELETON
        - STRAY
```

Each child such as `zombie` or `skeleton` is a complete matcher. Rules inside a child still use AND.

### Alternative by integration

```yaml
match-entity:
  any:
    vanilla-boss:
      entity-types:
        - WITHER
        - ENDER_DRAGON
    mythic-boss:
      mythicmobs:
        - SkeletonKing
        - InfernalDragon
```

Remember that an unavailable optional integration key is ignored rather than automatically failing. Do not use this pattern for destructive/reward-sensitive logic without confirming the hook is active.

## `not`

Rejects a nested MatchEntityFormat group.

```yaml
match-entity:
  monster: true
  not:
    entity-types:
      - CREEPER
```

This matches monsters except Creepers.

### Reject several categories

```yaml
match-entity:
  not:
    any:
      players:
        entity-types:
          - PLAYER
      named-boss:
        entity-contains-name:
          - Boss
      protected-tag:
        entity-tag:
          - protected
```

## Combining AND, OR, and NOT

The following means:

```text
monster AND (zombie OR skeleton) AND NOT protected
```

```yaml
match-entity:
  monster: true
  any:
    zombie:
      entity-types:
        - ZOMBIE
        - HUSK
        - DROWNED
    skeleton:
      entity-types:
        - SKELETON
        - STRAY
        - WITHER_SKELETON
  not:
    entity-tag:
      - protected
```

## Multiple requirements in one alternative

```yaml
match-entity:
  any:
    armed-zombie:
      entity-types:
        - ZOMBIE
      equip:
        main-hand:
          material-tag:
            - minecraft:swords
    ranged-skeleton:
      entity-types:
        - SKELETON
      ranged: true
```

An alternative group can contain as many AND rules as necessary.

## Inverting a single rule

Use `not` around the smallest matcher possible:

```yaml
match-entity:
  not:
    animal: true
```

Avoid broad inversions that accidentally match entities with missing/unsupported data.

## Missing entities and living-entity restriction

A missing entity does not match. MatchEntityFormat is designed for `LivingEntity`; non-living entities should be handled by an ability or trigger that explicitly supports them.

Logical wrappers do not make a missing/non-living entity valid. For example, `not: { monster: true }` should not be treated as a safe way to match every non-monster object.

## Optional keys and fail-open behavior

Unknown MatchEntityFormat keys are ignored because no registered rule handles them. This matters for:

- `mythicmobs` without MythicMobs hook;
- `levelled-mobs` without LevelledMobs hook;
- platform-specific classification rules;
- misspelled keys.

A matcher containing only an ignored key can become effectively unrestricted.

For important filters:

1. verify startup hook registration;
2. include a stable built-in restriction where possible;
3. test a known matching and known non-matching entity;
4. disable the dependent enchantment when the required integration is absent.

## Example: nearby hostile selection

```yaml
abilities:
  nearby:
    type: nearby_entities
    radius: 8
    amount: 6
    match-entity:
      monster: true
      not:
        any:
          creeper:
            entity-types:
              - CREEPER
          protected:
            entity-tag:
              - no_enchantment_target
    abilities:
      glow:
        type: particle
        target: TARGET
        particle: ENCHANT
        amount: 5
```

## Example: custom boss or high-health vanilla mob

```yaml
match-entity:
  any:
    custom:
      mythicmobs:
        - SkeletonKing
    vanilla:
      monster: true
      entity-health:
        min: 100
```

## Debugging nested matchers

1. Test each child as the entire `match-entity` section.
2. Remove `not` and verify the positive group first.
3. Confirm the selected target is a living entity.
4. Check exact entity type, tag, PDC, equipment, and integration ID.
5. Confirm optional rules were registered.
6. Add alternatives back one at a time.
7. Keep nesting shallow and give groups descriptive names.

## Common mistakes

- expecting top-level rule keys to use OR;
- placing several unrelated rule keys directly under `any` without complete child groups;
- negating a broader group than intended;
- assuming an unknown integration key fails closed;
- using `not` to match missing or non-living entities;
- forgetting that each alternative's internal rules use AND;
- creating a matcher so broad that `nearby_entities` targets players or protected mobs.

See [Identity and State Rules](identity-state.md) and [Equipment and Integration Rules](equipment-integrations.md).

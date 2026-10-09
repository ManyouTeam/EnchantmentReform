# 🔍 Match Entity Format

`MatchEntityFormat` is the shared format used whenever EnchantmentReform tests a living entity.

It is used by the `match_entity` Power Condition and entity-selection abilities such as `nearby_entities`.

```yaml
match-entity:
  monster: true
  entity-types:
    - ZOMBIE
    - SKELETON
  entity-health: 20
```

## Evaluation rules

A MatchEntityFormat section does not use a `type` field. Every recognized key enables one rule, and rules at the same level use logical **AND**. List entries inside one rule normally use **OR**.

## Missing and invalid input

* A missing MatchEntityFormat section matches everything.
* A missing entity does not match.
* The matcher accepts `LivingEntity`; non-living entities must be filtered by an ability that supports them rather than this format.
* Unknown keys are ignored.
* `none: true` forces failure.

## Logical composition

Use `any` for alternatives:

```yaml
match-entity:
  any:
    skeleton:
      entity-types:
        - SKELETON
      ranged: true
    mythic:
      mythicmobs:
        - SkeletonKing
```

Use `not` to reject nested matches:

```yaml
match-entity:
  monster: true
  not:
    entity-types:
      - CREEPER
```

## Equipment matching

`equip` contains a MatchItemFormat section for one or more equipment slots:

```yaml
match-entity:
  equip:
    main-hand:
      material-tag:
        - minecraft:swords
    helmet:
      material:
        - CARVED_PUMPKIN
```

## Built-in rules

| Rule key               | Purpose                                                                                     |
| ---------------------- | ------------------------------------------------------------------------------------------- |
| `entity-types`         | Matches Bukkit entity types.                                                                |
| `none`                 | Forces failure when true.                                                                   |
| `entity-contains-name` | Matches text in the entity's custom/display name.                                           |
| `entity-health`        | Checks current entity health against the configured threshold/range.                        |
| `entity-tag`           | Matches Bukkit/Minecraft entity tags.                                                       |
| `entity-pdc`           | Matches configured PersistentDataContainer values.                                          |
| `ranged`               | Checks whether the entity qualifies as a ranged attacker under the platform implementation. |
| `monster`              | Requires or rejects Bukkit `Monster`.                                                       |
| `animal`               | Requires or rejects Bukkit animal classification.                                           |
| `equip`                | Matches equipment using nested MatchItemFormat sections.                                    |
| `mythicmobs`           | Matches MythicMobs internal IDs when the hook is loaded.                                    |
| `levelled-mobs`        | Matches LevelledMobs data when that plugin is loaded.                                       |
| `any`                  | Passes when one nested group passes.                                                        |
| `not`                  | Rejects a nested match.                                                                     |

## Optional rules

| Rule                        | Requirement                                                               |
| --------------------------- | ------------------------------------------------------------------------- |
| `mythicmobs`                | MythicMobs must be loaded before EnchantmentReform registers match rules. |
| `levelled-mobs`             | LevelledMobs must be loaded.                                              |
| Paper ranged classification | Paper may provide richer ranged-entity classification than Spigot.        |

An unavailable optional rule is not registered; its key is therefore ignored. Confirm hook registration in startup logs.

## Complete example

```yaml
match-entity:
  monster: true
  entity-health: 20
  any:
    ranged:
      ranged: true
      equip:
        main-hand:
          material:
            - BOW
            - CROSSBOW
    custom:
      mythicmobs:
        - SkeletonKing
  not:
    entity-types:
      - CREEPER
```

## Related pages

* [MatchItemFormat](../match-item-format/)
* [Power Conditions](../../configs/power-conditions/)
* [Abilities](../../configs/abilities/)

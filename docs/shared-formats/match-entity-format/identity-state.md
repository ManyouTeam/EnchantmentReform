# 🧟 Identity and state rules

This page documents MatchEntityFormat rules for Bukkit entity types, names, health, tags, PersistentDataContainer values, ranged classification, monsters, and animals.

## Rules on this page

- `entity-types`
- `entity-contains-name`
- `entity-health`
- `entity-tag`
- `entity-pdc`
- `ranged`
- `monster`
- `animal`

MatchEntityFormat accepts living entities. Rules at the same level use logical AND; list values inside one rule normally use OR.

## `entity-types`

Matches Bukkit entity types.

```yaml
match-entity:
  entity-types:
    - ZOMBIE
    - HUSK
    - DROWNED
```

Use enum names available on the target server version. Custom MythicMobs normally retain a Bukkit base entity type, so use the MythicMobs integration rule when the internal mob ID matters.

## `entity-contains-name`

Checks whether the entity's custom/display name contains configured text.

```yaml
match-entity:
  entity-contains-name:
    - Boss
    - Elite
```

Name matching is convenient but fragile because:

- names can be translated or color-formatted;
- other plugins can change names dynamically;
- ordinary mobs may share the same text;
- an unnamed entity may expose a vanilla translated display component rather than a custom name.

Prefer tags, PDC, or integration IDs for stable identification.

## `entity-health`

Checks current health against a configured value or range.

```yaml
match-entity:
  entity-health:
    min: 1
    max: 20
```

The exact accepted scalar/range syntax follows the rule implementation. Current health is not maximum health and can change between target selection and delayed execution.

For percentage-based checks in powers, the `health_percent` Power Condition is often clearer.

## `entity-tag`

Matches scoreboard/entity tags.

```yaml
match-entity:
  entity-tag:
    - elite
    - summoned_by_plugin
```

Entity tags are stable, lightweight identifiers when your server controls their assignment. Tag matching is case-sensitive where Bukkit's tag storage is case-sensitive.

Do not confuse entity tags with Minecraft registry tags for entity types.

## `entity-pdc`

Matches values stored in the entity's PersistentDataContainer.

```yaml
match-entity:
  entity-pdc:
    namespace:key: expected-value
```

The exact configuration structure depends on the PDC matcher and stored data type. Namespace, key, and type must match the writer plugin.

PDC is a better stable identifier than display name when your own plugin or datapack controls the entity. Do not depend on another plugin's undocumented keys without version testing.

## `ranged`

Checks whether the entity qualifies as a ranged attacker under the platform implementation.

```yaml
match-entity:
  ranged: true
```

Paper can expose richer ranged classification than Spigot. Classification may include entities whose AI uses projectiles even when their current equipment is not a bow.

Use `equip` when the actual held item matters.

## `monster`

Requires or rejects Bukkit's `Monster` classification.

```yaml
match-entity:
  monster: true
```

```yaml
match-entity:
  monster: false
```

This follows Bukkit interfaces, not a custom hostility table. Some hostile or special entities may not implement the exact category expected on all versions.

## `animal`

Requires or rejects Bukkit animal classification.

```yaml
match-entity:
  animal: true
```

Tamed state, owner, age, and breedability are separate concerns and are not implied by this rule.

## Combining identity rules

```yaml
match-entity:
  monster: true
  entity-types:
    - SKELETON
    - STRAY
  ranged: true
```

The entity must satisfy all three rules.

To allow either a tagged Zombie or a named Skeleton, use `any`:

```yaml
match-entity:
  any:
    tagged-zombie:
      entity-types:
        - ZOMBIE
      entity-tag:
        - elite
    named-skeleton:
      entity-types:
        - SKELETON
      entity-contains-name:
        - Archer
```

## Health versus power conditions

Use `entity-health` when health is one part of a reusable MatchEntityFormat filter, especially inside `nearby_entities`.

Use `health` or `health_percent` Power Conditions when:

- the target selector is already known;
- you need percent-of-maximum logic;
- you want ordinary condition inversion/nesting;
- the health check belongs directly to one power.

## Example: nearby low-health monsters

```yaml
abilities:
  nearby:
    type: nearby_entities
    radius: 8
    amount: 5
    match-entity:
      monster: true
      entity-health:
        max: 10
    abilities:
      mark:
        type: particle
        target: TARGET
        particle: CRIT
        amount: 5
```

## Example: reject player-like entities

```yaml
match-entity:
  monster: true
  not:
    entity-types:
      - PLAYER
      - ARMOR_STAND
```

MatchEntityFormat is intended for living entities; non-living types may already fail before individual rules run.

## Common mistakes

- using names where a stable tag/PDC/integration ID exists;
- confusing current health and maximum health;
- expecting monster/animal categories to match a custom semantic category exactly;
- assuming ranged means the entity currently holds a bow;
- matching non-living entities with MatchEntityFormat;
- using an entity type enum that does not exist on the target version;
- assuming PDC values are automatically converted between data types.

See [Equipment and Integration Rules](equipment-integrations.md) and [Logical Rules](logic.md).

# ✨ Native Enchantment Effects

The root `effects` section defines **Minecraft-native enchantment effect components**. These effects are registered as part of the enchantment itself and are handled by Minecraft's normal enchantment engine.

Use `effects` for mechanics that already exist in the vanilla enchantment system, such as attribute modifiers, native damage changes, damage immunity, post-attack effects, or location-based block effects. Use [`powers`](powers/README.md) when you need EnchantmentReform triggers, Power Conditions, Power Modifiers, Abilities, random chances, cooldowns, or state.

An enchantment may use both systems at the same time.

{% hint style="warning" %}
`effects` is registry data. Changes require a **full server restart**. `/enchantmentreform reload` cannot rebuild an already-frozen enchantment registry entry.
{% endhint %}

## `effects` compared with `powers`

| Feature | `effects` | `powers` |
| --- | --- | --- |
| Execution engine | Minecraft's native enchantment engine | EnchantmentReform's runtime power engine |
| Syntax | Vanilla enchantment effect-component data written as YAML | Trigger → condition → modifier → ability |
| Level scaling | Vanilla level-based values such as `minecraft:linear` | `{level}`, variables, selectors, and math expressions |
| Conditions | Vanilla loot-condition predicates in `requirements` | Power Conditions |
| Chance and cooldown | Only when supported by the native component/effect schema | Common `random`, `cooldown`, and `times` fields |
| Reload behavior | Full restart required | Runtime sections may be re-read where reload supports them |

## Base syntax

```yaml
effects:
  minecraft:<effect_component>:
    <value required by that Minecraft component>
```

The key directly below `effects` is a Minecraft enchantment effect-component key. The value's shape depends on that component: it may be a list, an object, a number, or another native data structure.

EnchantmentReform does not invent another nested schema for this section. It converts the YAML maps and lists into native data and passes them to the current server's `EnchantmentEffectComponents` codec.

Top-level component keys without a namespace are automatically treated as `minecraft:<key>`, but explicit namespaced keys are recommended:

```yaml
effects:
  minecraft:attributes: []
```

Nested identifiers should always be written with their full namespace, for example `minecraft:max_health` or `enchantmentreform:my_modifier`.

## Effects by Minecraft version

Use `effects-by-version` in custom enchantments or vanilla enchantment overrides when native data formats differ between releases:

```yaml
# Default for older releases.
effects:
  minecraft:damage_immunity:
    - effect: {}
      requirements:
        condition: minecraft:damage_source_properties
        predicate:
          tags:
            - id: minecraft:burn_from_stepping
              expected: true

effects-by-version:
  - min-version: '26.3'
    effects:
      minecraft:damage_immunity:
        - effect: {}
          requirements:
            type: minecraft:damage_source_properties
            predicate:
              tags:
                - id: '#minecraft:burn_from_stepping'
                  expected: true
```

Rules are evaluated in list order. The **first matching rule replaces the entire default `effects` map**; components are not merged. Only the selected map is passed to the server's native codec. All rule selectors are validated, including selectors in unmatched rules.

| Field | Meaning |
| --- | --- |
| `versions` | One quoted version or a list of quoted versions, e.g. `'26.3'` or `['1.21.11', '26.2']`. Any listed version may match. |
| `min-version` | Inclusive lower bound. |
| `max-version` | Inclusive upper bound. |
| `effects` | Complete native effect map for the matching rule; required. `{}` explicitly clears all native components. |

Each rule requires at least one selector. When selectors are combined, all must match. Versions are compared numerically: `1.21.10` is newer than `1.21.9`, and `26.3` equals `26.3.0`. Selectors and the server version must be release numbers with two or three components; quote selector values so YAML does not convert them to numbers.

If no rule matches, the root `effects` is used. If the root is absent, native effects remain unconfigured (vanilla overrides preserve the original effects). An empty matched map is still an explicit replacement. Version selection is available during Paper bootstrap and on Spigot, and changes require a full restart.

[Minecraft 26.3's format changes](https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3) include predicate `condition` → `type`, Block State `Name` → `id` / `Properties` → `properties`, and provider `minecraft:simple_state_provider` → `minecraft:simple`. After branch selection, known schema changes are automatically migrated as described below; other differences still require explicit version-specific data.

In 26.3 damage-source predicates, tag references in `tags[].id` must use a quoted `'#minecraft:tag_name'`, such as `'#minecraft:burn_from_stepping'`. Without `#`, the value identifies a concrete damage type; using a tag name as a type ID can produce `Unbound values` when registries freeze. Keep the original tag format in the older-version default branch.

## Automatic migration of older effects

Loading-time migration is enabled by default. After choosing an `effects-by-version` branch (or the root fallback), the plugin migrates a copy of the selected effects for the current Minecraft release. This applies to both custom enchantments and vanilla overrides. Registration and runtime readers use the migrated data; the original YAML file is preserved.

For 26.3 or newer releases, supported transformations are:

* Rename the old predicate discriminator `condition` to `type` inside `requirements` and nested `terms` / `term`.
* Expand implicit predicate lists to explicit `minecraft:all_of`.
* Rename Block State fields `Name` / `Properties` to `id` / `properties`.
* Rename known providers: `dual_noise_provider`, `noise_provider`, `noise_threshold_provider`, `randomized_int_state_provider`, `rule_based_state_provider`, `simple_state_provider`, and `weighted_state_provider`.
* Prefix damage tag IDs with `#` in legacy `condition: minecraft:damage_source_properties` predicates, preserving custom namespaces and `expected`.
* Repair partially converted `type` predicates that still use plain `burn_from_stepping` or `bypasses_invulnerability` IDs.

Modern data keeps its meaning and migration is idempotent. Other plain IDs in modern damage predicates remain concrete damage types; custom tag/type intent is not guessed. NBT and component payloads are excluded from field conversion. Servers older than 26.3 keep their original data; reverse migration is not performed.

Disable migration per enchantment with this root setting:

```yaml
auto-migrate-effects: false
```

This handles known schema changes, not every Minecraft format change. For example, noise providers have their type names converted, but their parameter structures are not rebuilt. Use `effects-by-version` for other differences. Conflicting old/new Block State fields cause a configuration error rather than a silent choice. Configuration changes and migration code updates require a full restart.

## Converting vanilla JSON to YAML

A vanilla enchantment definition may contain JSON similar to:

```json
{
  "effects": {
    "minecraft:attributes": [
      {
        "id": "enchantmentreform:example_health",
        "attribute": "minecraft:max_health",
        "amount": {
          "type": "minecraft:linear",
          "base": 2.0,
          "per_level_above_first": 1.0
        },
        "operation": "add_value"
      }
    ]
  }
}
```

Write the same structure in YAML and remove the outer JSON object:

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:example_health
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 2.0
        per_level_above_first: 1.0
      operation: add_value
```

Conversion rules:

* JSON objects become YAML sections.
* JSON arrays become YAML lists beginning with `-`.
* Strings, booleans, and numbers keep the same values.
* Empty JSON objects become `{}`.
* Namespaced identifiers should remain unchanged.

## Attribute effect example

The following enchantment grants `+2` maximum health at level I and one additional point for every level above I:

```yaml
active-slots:
  - ARMOR

max-level: 5

effects:
  minecraft:attributes:
    - id: enchantmentreform:vitality
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 2.0
        per_level_above_first: 1.0
      operation: add_value
```

### Attribute fields

| Field | Purpose |
| --- | --- |
| `id` | Stable namespaced identifier for the native attribute modifier. Avoid reusing the same ID for unrelated effects. |
| `attribute` | Minecraft attribute registry key. |
| `amount` | Native level-based value. |
| `operation` | Native attribute operation, such as `add_value`, `add_multiplied_base`, or `add_multiplied_total`, when supported by the current server version. |

The `active-slots` field in the enchantment definition determines where the enchantment must be equipped for equipment-dependent native effects to apply.

## Native level scaling

Plugin variables and math placeholders are **not expanded inside `effects`**. Do not write:

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:wrong_example
      attribute: minecraft:max_health
      amount: '{level} * 2' # Not a Power expression here.
      operation: add_value
```

Use Minecraft's native level-based value format instead:

```yaml
amount:
  type: minecraft:linear
  base: 2.0
  per_level_above_first: 2.0
```

For this linear value:

* level I = `base`;
* level II = `base + per_level_above_first`;
* level III = `base + per_level_above_first × 2`.

Other native level-based value types may be accepted by the current server version's codec. Their names and fields must match the vanilla format for that exact Minecraft version.

## Conditional native effect example

Many native effect components use entries containing an `effect` and optional vanilla loot-condition `requirements`.

This example prevents damage caused by stepping on a burning block, unless the damage source bypasses invulnerability:

```yaml
effects:
  minecraft:damage_immunity:
    - effect: {}
      requirements:
        condition: minecraft:damage_source_properties
        predicate:
          tags:
            - expected: true
              id: minecraft:burn_from_stepping
            - expected: false
              id: minecraft:bypasses_invulnerability
```

`requirements` uses Minecraft's native loot-condition and predicate syntax. It is not a Power Conditions section, so fields such as `type: health_percent` cannot be used there.

Not every effect component uses the `{ effect, requirements }` wrapper. For example, `minecraft:attributes` uses attribute entries directly. Always follow the vanilla schema of the selected component.

## Location effect example

The following native location effect replaces nearby lava below the wearer with magma blocks:

```yaml
active-slots:
  - FEET

effects:
  minecraft:location_changed:
    - effect:
        type: minecraft:replace_disk
        block_state:
          type: minecraft:simple_state_provider
          state:
            Name: minecraft:magma_block
        height: 1.0
        offset:
          - 0
          - -1
          - 0
        predicate:
          type: minecraft:all_of
          predicates:
            - type: minecraft:matching_block_tag
              offset:
                - 0
                - 1
                - 0
              tag: minecraft:air
            - type: minecraft:matching_blocks
              blocks: minecraft:lava
            - type: minecraft:matching_fluids
              fluids: minecraft:lava
        radius:
          type: minecraft:linear
          base: 3.0
          per_level_above_first: 1.0
        trigger_game_event: minecraft:block_place
      requirements:
        condition: minecraft:entity_properties
        entity: this
        predicate:
          flags:
            is_on_ground: true
```

An `offset` must be a list of three numeric coordinates in X, Y, Z order.

## Common native component categories

The exact available component keys and nested fields are controlled by the current Minecraft server version. Common categories include:

| Component category | Typical purpose |
| --- | --- |
| `minecraft:attributes` | Add native attribute modifiers while the enchantment is active. |
| `minecraft:damage` / `minecraft:damage_protection` | Modify outgoing damage or protection calculations. |
| `minecraft:damage_immunity` | Make matching damage sources deal no damage. |
| `minecraft:item_damage` | Modify durability consumption. |
| `minecraft:post_attack` | Execute native entity effects after an attack. |
| `minecraft:tick` | Execute a native entity effect while active. |
| `minecraft:location_changed` | Execute a native location effect after movement or location updates. |
| Projectile-related components | Modify projectile count, spread, ammunition use, charge time, or related behavior. |

This table is intentionally not an exhaustive schema. EnchantmentReform accepts whatever the current server's native enchantment-effect codec accepts.

## Using `effects` and `powers` together

A native attribute can be combined with a plugin-driven ability:

```yaml
max-level: 3
active-slots:
  - HAND

variables:
  kill-heal: '1 + {level}'

effects:
  minecraft:attributes:
    - id: enchantmentreform:hybrid_attack_speed
      attribute: minecraft:attack_speed
      amount:
        type: minecraft:linear
        base: 0.1
        per_level_above_first: 0.05
      operation: add_value

powers:
  on-kill:
    abilities:
      heal:
        type: set_health
        target: SOURCE
        amount: '{health} + {kill-heal}'
```

The native attribute is registered from `effects`; the kill behavior is handled independently by `powers`.

## Validation and common errors

### Invalid native schema

If a component key, effect type, predicate, registry key, or required field is invalid, server bootstrap fails with an error similar to:

```text
example.yml: invalid native enchantment effects: ...
```

Read the remainder of the codec error: it normally identifies the invalid field or value.

### Using plugin syntax inside native effects

The following systems do not apply inside `effects`:

* Power Conditions;
* Power Modifiers;
* Abilities;
* root `variables` and `{level}` math expressions;
* common power fields such as `random`, `cooldown`, and `times`.

Use `powers` when these features are required.

### Copying data from another Minecraft version

Native effect-component schemas can change between Minecraft versions. Copy definitions from vanilla enchantment data or examples made for the same server version.

### Incorrect indentation

Every component belongs directly under the root `effects` section:

```yaml
effects:
  minecraft:attributes:
    - id: enchantmentreform:example
      attribute: minecraft:max_health
      amount:
        type: minecraft:linear
        base: 1.0
        per_level_above_first: 1.0
      operation: add_value
```

Do not place native effects under `powers`, a trigger, or `abilities`.

## Bundled examples

The default configuration includes practical native-effect examples in:

```text
plugins/EnchantmentReform/enchantments/armor/vitality.yml
plugins/EnchantmentReform/enchantments/armor/jump_boost.yml
plugins/EnchantmentReform/enchantments/tools/entity_reach.yml
plugins/EnchantmentReform/enchantments/armor/lava_walker.yml
```

Use those files as templates and then replace the component-specific fields with values valid for your server version.

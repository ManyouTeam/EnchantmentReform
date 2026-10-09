# Skills

The complete skill module is controlled by `config.yml -> modules.skills`. When it is `false`, skill files, progression, XP, rewards, skill menu content, and skill commands are disabled. Every `skill_level` power condition or attribute-allocation condition is then treated as satisfied.

Each skill is a YAML file in `plugins/EnchantmentReform/skills`; the file name is its ID. Missing bundled files are extracted without overwriting existing files. The defaults provide Farming, Foraging, Mining, Excavation, Fishing, Combat, Archery, Defense, Agility, Alchemy, and Enchanting. They contain progression and XP sources only, with no bundled skill abilities.

```yaml
enabled: true
order: 3
name: '&bMining'
description: '&7Mine natural blocks to gain XP.'
maximum-level: 100
experience-formula: '100 * {level} * {level}'
icon:
  material: IRON_PICKAXE

rewards:
  every-level:
    rewards:
      - type: attribute-points
        display-name: '&e+{amount} Attribute Point'
        amount: 1
  levels:
    '10':
      rewards:
        - type: attribute-points
          display-name: '&eBonus: +{amount} Attribute Points'
          amount: 2
        - type: attributes
          display-name: '&aStats: {attributes}'
          attributes:
            endurance: 1
        - type: command
          display-name: '&6VIP for 7 days'
          command: 'grantvip {player} 7d'
        - type: item
          display-name: '&bDiamonds'
          material: DIAMOND
          amount: 3
```

`{level}` in the formula is the level being reached and `{current_level}` is the current level. Every entry in a reward list is one independently named reward: `type: attribute-points`, `type: attribute`, `type: attributes`, `type: command`, or `type: item`. Singular `attribute` uses `attribute` plus `amount`; plural `attributes` requires an `attributes` map and exposes its localized summary as `{attributes}`. Attribute rewards change custom-attribute base values. An item entry is itself an ItemFormat section. The S-shaped rewards GUI displays entries in YAML order using `display-name`, similar to AuraSkills/EcoSkills reward lists, instead of fixed category headings. Reward names support `{amount}`, `{attribute}`, `{attributes}`, `{player}`, `{skill}`, and `{level}` where applicable. Rewards are only read from typed `rewards` entries; untyped legacy reward sections are not accepted.

## XP sources

Sources reuse [TriggerManager triggers](triggers.md) and the same [power conditions](power-conditions/README.md):

```yaml
sources:
  diamond_ore:
    name: '&bDiamond Ore'
    description: '&7Mine regular or deepslate diamond ore.'
    trigger: block_break
    xp: 12
    unit: '&7/block'
    icon:
      material: DEEPSLATE_DIAMOND_ORE
    conditions:
      block:
        type: block_type
        types:
          - DIAMOND_ORE
          - DEEPSLATE_DIAMOND_ORE
    anti-abuse:
      ignore-player-placed: true
      ignore-internal-block-breaks: true
      cooldown-ms: 50
      maximum-uses-per-minute: 240
      maximum-xp-per-minute: 500
      pressure:
        threshold: 100
        reset-after-seconds: 120
        multiplier: 0.25
```

`name`, `description`, `unit`, and `icon` populate the per-skill source GUI. `trigger` accepts built-ins such as `block_break`, `melee_attack`, `attack`, `damage_by_entity`, `fish`, `enchant_item`, `consume`, and `move`, or a fully namespaced registered trigger. `xp` supports numeric trigger-context placeholders and `{amount}`, including `{original_damage}` and `{original_experience}`.

## Experience multipliers

Permission multipliers are configured in `config.yml`. Use `*` or `all` for every skill, or list one or more skill IDs. Every matching permission multiplier is multiplied together.

```yaml
skills:
  experience-multipliers:
    permissions:
      vip:
        permission: enchantmentreform.skill-multiplier.vip
        skills: ['*']
        multiplier: 1.5
      mining-event:
        permission: enchantmentreform.skill-multiplier.mining-event
        skills: [mining]
        multiplier: 2.0
```

An administrator can also set persistent per-player multipliers. The `all` multiplier and a skill-specific multiplier both apply and are multiplied with permission multipliers. Set a value to `1` to remove that override; `0` disables XP gain for its scope.

```text
/enchantmentreform setskillmultiplier <player> <skill-id|all> <multiplier>
```

Permission: `enchantmentreform.setskillmultiplier` (operator by default). This command is registered only while `modules.skills` is enabled. Multipliers apply before `maximum-xp-per-minute`, so source caps still limit the final awarded XP.

Set `manual: true` for a source that must be shown in the skill menu but is activated by a `skill_experience` ability instead of directly by its configured trigger. The ability references it with `skill` and `source`; its XP formula, anti-abuse settings, rate limit, pressure handling, and normal feedback are then reused without duplicate automatic grants.

## Shared anti-abuse layer

The following `anti-abuse` options work both on skill sources and inside any enchantment, item, or attribute power trigger section:

- `ignore-player-placed`, including fertilized/spread blocks and piston movement
- `ignore-internal-block-breaks`
- `ignore-spawner-mobs`
- `cooldown-ms`
- `target-cooldown-seconds` and `player-victim-cooldown-seconds`
- `maximum-uses-per-minute`

Skill sources enable the first three safe defaults and use a 300-second same-player-victim cooldown by default. `maximum-xp-per-minute` and `pressure` are XP-only controls for caps and diminishing returns.

Use `/enchantmentreform menu skill-info` for the overview, then click a skill for its progress. Every skill-detail GUI shows every attribute with `skill-menu.show` enabled. Clicking one opens `menus/attribute-detail.yml`, which separates current/base values, modifiers, requirements, and a clearly labelled upgrade control. The skill GUI can also switch between paginated XP sources and an S-shaped level-reward path. Reward lore uses `{reward_lines}` for the ordered, display-named list. Use `/enchantmentreform menu attribute-allocation` for global allocation; each attribute's `allocation` section controls visibility, per-level price, and common/per-level conditions.

`skills.feedback.experience` configures XP-gain feedback, throttling, and sound. `cooldown-ticks` controls the feedback interval in ticks. `display` supports `BOSS_BAR`, `ACTION_BAR`, and `BOTH`; BossBar progress is the current XP divided by the XP required for the next level. `skills.feedback.level-up` configures the level-up title and sound. Leave a sound `name` empty to disable it.

PlaceholderAPI exposes `%enchantmentreform_skill_level_<id>%`, `%enchantmentreform_skill_xp_<id>%`, `%enchantmentreform_skill_required_xp_<id>%`, and `%enchantmentreform_attribute_points%`.

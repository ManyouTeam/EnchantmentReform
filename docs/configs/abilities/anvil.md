# ⚒️ Anvil Activation Abilities

Abilities on this page are intended for `powers.activation-abilities`. They are evaluated while an anvil prepares its result rather than by a normal `on-...` trigger.

## Registry keys on this page

* `modify_repair_cost`

---

## `modify_repair_cost`

**Purpose:** Changes the experience-level cost calculated for an anvil operation when an input item or enchanted book contains the declaring enchantment.

**Context:** Paper `PrepareAnvilEvent`. EnchantmentReform scans both anvil input slots, including normal item enchantments and stored enchantments on enchanted books.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `operation` | `SET` | `SET`, `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `MIN`, or `MAX`. |
| `value` | current repair cost | Operand or formula. Supports `{original}`, `{current}`, `{level}`, root Power variables, and PlaceholderAPI when a player is available. |

### Example

```yaml
powers:
  activation-abilities:
    double-repair-cost:
      type: modify_repair_cost
      operation: MULTIPLY
      value: 2
```

### Behavior and limits

* `{original}` and `{current}` are the repair cost immediately before this configured rule runs.
* The result is rounded to the nearest integer and clamped to `0..2147483647`.
* Division by zero leaves the current cost unchanged.
* When the same enchantment appears in both input slots, its rule runs once at the highest input level.
* Rules from different enchantments run by enchantment execution priority, then by namespaced key.
* This ability does nothing when placed under a normal trigger's `abilities` section.
* The rule is applied after vanilla has produced a valid anvil result. It does not reconstruct operations that vanilla already rejected for another reason.

## Bypassing vanilla enchantment maximum levels

Paper servers may enable:

```yaml
anvil:
  bypass-enchantment-level-limit: true
```

This allows the anvil to apply enchantment levels above `Enchantment#getMaxLevel()`. It is separate from `modify_repair_cost`: the config option controls which levels the anvil may produce, while the activation ability changes the displayed experience-level cost.

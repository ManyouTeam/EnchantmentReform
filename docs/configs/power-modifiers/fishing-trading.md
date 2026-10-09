# 🎣 Fishing and Trading Modifiers

Every modifier on this page is documented independently. All entries also support the common modifier fields from the [Power Modifiers](README.md) page.

## Registry keys on this page

* `trade_uses_return`
* `trade_emerald_return`
* `fishing_catch`
* `fishing_extra_catch`
* `fishing_replace_catch`
* `fishing_smelt_catch`
* `fishing_hook_timing`

---

## `trade_uses_return`

**Purpose:** Prevents a successful Paper purchase from increasing recipe uses.

**Context:** Requires `PlayerPurchaseEvent`.

### Fields

_No type-specific fields._

### Example

```yaml
type: trade_uses_return
```

### Behavior and limits

* Sets `increaseTradeUses` to false; it does not refund ingredients.

---

## `trade_emerald_return`

**Purpose:** Returns emerald ingredients from a Paper purchase to the player.

**Context:** Requires `PlayerPurchaseEvent`.

### Fields

_No type-specific fields._

### Example

```yaml
type: trade_emerald_return
```

### Behavior and limits

* Counts adjusted first ingredient plus remaining emerald ingredients.
* Inventory overflow is dropped naturally at the player.

---

## `fishing_catch`

**Purpose:** Multiplies the stack size of an item caught by fishing.

**Context:** Requires `PlayerFishEvent` whose caught entity is an item.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `multiplier` | `1` | Catch amount multiplier, minimum 0. |

### Example

```yaml
type: fishing_catch
multiplier: 2
```

### Behavior and limits

* The rounded amount is at least 1 and at most the item's maximum stack size.

---

## `fishing_extra_catch`

**Purpose:** Drops additional weighted ItemFormat rewards when a fish is caught.

**Context:** Requires `PlayerFishEvent.State.CAUGHT_FISH`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `amount` | `0` | Number of extra weighted selections. |
| `items.<id>` | required | Weighted ItemFormat child entry. |
| `items.<id>.rate` | `1` | Positive finite relative weight. |

### Example

```yaml
type: fishing_extra_catch
amount: 2
items:
  cod:
    rate: 5
    material: COD
  treasure:
    rate: 1
    material: DIAMOND
```

### Behavior and limits

* Each selection is independent and may choose the same item again.
* Rewards drop naturally at the hook location.

---

## `fishing_replace_catch`

**Purpose:** Replaces a caught item with one randomly selected configured ItemFormat entry.

**Context:** Requires `PlayerFishEvent` whose caught entity is an item.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `items.<id>` | required | Candidate ItemFormat entries; selection is equal probability. |

### Example

```yaml
type: fishing_replace_catch
items:
  salmon:
    material: SALMON
  treasure:
    material: EMERALD
```

### Behavior and limits

* Entries are not weighted.
* An empty/invalid item list leaves the catch unchanged.

---

## `fishing_smelt_catch`

**Purpose:** Smelts a caught item in place when it has a cooking recipe.

**Context:** Requires `PlayerFishEvent` whose caught entity is an item.

### Fields

_No type-specific fields._

### Example

```yaml
type: fishing_smelt_catch
```

### Behavior and limits

* Treasure and junk without a cooking result are unchanged.
* The recipe cache is cleared when modifier types unload.

---

## `fishing_hook_timing`

**Purpose:** Scales the fishing hook's minimum and maximum wait times.

**Context:** Requires `PlayerFishEvent`.

### Fields

| Field | Default | Description |
| --- | --- | --- |
| `multiplier` | `1` | Wait-time multiplier, clamped to at least `0.05`. |

### Example

```yaml
type: fishing_hook_timing
multiplier: 0.5
```

### Behavior and limits

* Minimum wait is at least 1 tick.
* Maximum wait is kept at least as large as the modified minimum.

---

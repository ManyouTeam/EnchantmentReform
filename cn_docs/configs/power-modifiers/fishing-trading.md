# 🎣 钓鱼与交易修改器

本页面会分别说明每一种修改器。所有条目也支持[能力修改器](README.md)页面中介绍的通用修改器字段。

## 本页面的注册键

* `trade_uses_return`
* `trade_emerald_return`
* `fishing_catch`
* `fishing_extra_catch`
* `fishing_replace_catch`
* `fishing_smelt_catch`
* `fishing_hook_timing`

---

## `trade_uses_return`

**用途：**防止一次成功的 Paper 购买增加交易配方的使用次数。

**上下文：**需要 `PlayerPurchaseEvent`。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: trade_uses_return
```

### 行为与限制

* 此修改器会将 `increaseTradeUses` 设置为 false，但不会返还交易材料。

---

## `trade_emerald_return`

**用途：**将 Paper 购买过程中使用的绿宝石材料返还给玩家。

**上下文：**需要 `PlayerPurchaseEvent`。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: trade_emerald_return
```

### 行为与限制

* 返还数量包括调整后的第一项交易材料，以及其余材料中的绿宝石。
* 玩家库存放不下的溢出物品会自然掉落在玩家所在位置。

---

## `fishing_catch`

**用途：**乘算通过钓鱼获得的物品堆数量。

**上下文：**需要 `PlayerFishEvent`，且钓到的实体必须是物品实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `multiplier` | `1` | 渔获数量倍率，最小值为 0。 |

### 示例

```yaml
type: fishing_catch
multiplier: 2
```

### 行为与限制

* 计算结果会四舍五入，最小数量为 1，最大不会超过该物品的最大堆叠数量。

---

## `fishing_extra_catch`

**用途：**钓到鱼时，根据权重额外掉落 ItemFormat 奖励。

**上下文：**需要 `PlayerFishEvent.State.CAUGHT_FISH`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `amount` | `0` | 额外进行加权选择的次数。 |
| `items.<id>` | 必填 | 带权重的 ItemFormat 子条目。 |
| `items.<id>.rate` | `1` | 相对权重，必须是正数且为有限值。 |

### 示例

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

### 行为与限制

* 每次选择彼此独立，因此可能重复选中同一种物品。
* 奖励会自然掉落在鱼钩所在位置。

---

## `fishing_replace_catch`

**用途：**使用配置的 ItemFormat 条目中随机选中的一项替换钓到的物品。

**上下文：**需要 `PlayerFishEvent`，且钓到的实体必须是物品实体。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `items.<id>` | 必填 | 候选 ItemFormat 条目；每个条目的选中概率相同。 |

### 示例

```yaml
type: fishing_replace_catch
items:
  salmon:
    material: SALMON
  treasure:
    material: EMERALD
```

### 行为与限制

* 这些条目不使用权重。
* 物品列表为空或无效时，原渔获不会发生变化。

---

## `fishing_smelt_catch`

**用途：**当钓到的物品存在烹饪配方时，直接将其替换为熔炼结果。

**上下文：**需要 `PlayerFishEvent`，且钓到的实体必须是物品实体。

### 字段

_没有此类型专用的字段。_

### 示例

```yaml
type: fishing_smelt_catch
```

### 行为与限制

* 不存在烹饪结果的宝藏或垃圾物品不会发生变化。
* 修改器类型卸载时会清除配方缓存。

---

## `fishing_hook_timing`

**用途：**按倍率调整鱼钩的最短和最长等待时间。

**上下文：**需要 `PlayerFishEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `multiplier` | `1` | 等待时间倍率，最小值限制为 `0.05`。 |

### 示例

```yaml
type: fishing_hook_timing
multiplier: 0.5
```

### 行为与限制

* 最短等待时间至少为 1 Tick。
* 最长等待时间始终不会小于修改后的最短等待时间。

---
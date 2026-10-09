# 🎁 事件与掉落修改器

本页面会分别说明每一种修改器。所有条目也支持[能力修改器](README.md)页面中介绍的通用修改器字段。

## 本页面的注册键

* `elytra_boost_not_consume`
* `modify_drops`

---

## `elytra_boost_not_consume`

**用途：**控制鞘翅加速是否消耗所使用的烟花火箭物品。

**上下文：**需要 Paper 的 `PlayerElytraBoostEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `consume` | `false` | 传递给 `event.setShouldConsume` 的值。 |

### 示例

```yaml
type: elytra_boost_not_consume
consume: false
```

### 行为与限制

* 尽管注册键名称表示“不消耗”，但设置 `consume: true` 会明确启用物品消耗。

---

## `modify_drops`

**用途：**转换并重新分配方块或实体掉落物。

**上下文：**需要 `BlockDropItemEvent` 或 `EntityDeathEvent`。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `clear` | `false` | 移除全部原始掉落物。 |
| `multiplier` | `1` | 按比例调整保留下来的物品堆数量。 |
| `smelt` | `false` | 存在熔炉配方时，将掉落物替换为熔炼结果。 |
| `replacement` | 空 | 可填写材质、`EVENT_BLOCK`、`MATCHING_PLANKS` 或 `RANDOM_CONFIGURED`。 |
| `replacement-amount` | `1` | 替换选择或替换物品的数量。 |
| `append` | `false` | 添加替换物品后，仍保留转换后的原始掉落物。 |
| `replacement-items` | 空 | 用于随机替换的加权 ItemFormat 条目。 |
| `bonus-items` | 空 | 在原始掉落物后追加的加权 ItemFormat 条目。 |
| `bonus-amount` | `1` | 额外物品的选择次数。 |
| `destination` | 由事件决定 | 可填写 `EVENT`、`WORLD`、`SOURCE`、`TARGET`、`INVENTORY` 或 `ENDER_CHEST`。 |

### 示例

```yaml
type: modify_drops
smelt: true
multiplier: 2
bonus-amount: 1
bonus-items:
  gem:
    rate: 1
    material: EMERALD
destination: SOURCE
```

### 行为与限制

* 分配转换后的掉落物前，会先移除方块事件中原本生成的掉落物实体。
* 替换物品和额外物品的权重必须为正数且为有限数值。
* 物品堆会按照合法的最大堆叠数量拆分，无法放入库存的溢出物品会自然掉落到世界中。

---
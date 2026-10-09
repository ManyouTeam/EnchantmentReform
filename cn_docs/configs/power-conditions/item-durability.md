# 🛠️ 物品与耐久度条件

本页面中的每一种已注册条件都支持[能力条件](README.md)页面中介绍的通用字段。

## 本页面的注册键

* `item_damage`

---

## `item_damage`

**用途：**检查选中可损耗物品的当前耐久损耗值或预计耐久损耗值。

**上下文：**默认 `item` 为 `TRIGGER_ITEM`；默认装备持有者为 `PLAYER`。选中的物品必须使用 Bukkit 的 `Damageable` 物品元数据，并且最大耐久度必须大于 0。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `item` | `TRIGGER_ITEM` | 物品选择器字符串，或包含 `selector` 和 `holder` 的嵌套部分。 |
| `item-holder` | `PLAYER` | 使用扁平物品配置形式时的持有者选择器。 |
| `mode` | `DAMAGE` | 根据所选物品耐久状态计算出的数值。参见下方模式表。 |
| `include-event-damage` | `false` | 在 `PlayerItemDamageEvent` 中，是否计入当前事件即将应用的可修改耐久损耗。 |
| `min / max` | 无限制 | 包含边界值的数值范围。 |
| `compare / value` | 未设置 | 另一种数值比较方式，例如 `>=`、`<` 或 `=`。 |

### 模式

| 模式 | 返回值 |
| --- | --- |
| `DAMAGE` | 当前已经使用的耐久点数。计入预计事件损耗后，该值可能超过最大耐久度。 |
| `REMAINING` / `DURABILITY` | 剩余耐久点数，数值限制在 `0..maximum`。 |
| `DAMAGE_PERCENT` / `USED_PERCENT` | 已使用耐久度百分比，范围为 `0` 到 `100`。 |
| `REMAINING_PERCENT` / `DURABILITY_PERCENT` | 剩余耐久度百分比，范围为 `0` 到 `100`。 |
| `MAX` / `MAXIMUM` / `MAX_DAMAGE` / `MAX_DURABILITY` | 该物品材质注册的最大耐久度。 |

模式名称中的连字符和下划线可以互换使用。

### 检测物品是否会因当前损耗事件而损坏

```yaml
conditions:
  about-to-break:
    type: item_damage
    item: TRIGGER_ITEM
    mode: REMAINING
    include-event-damage: true
    max: 0
```

假设某个物品还剩 `1` 点耐久度，而当前事件会造成 `1` 点耐久损耗，则检查得到的剩余耐久值为 `0`，因此条件匹配。

### 检查剩余耐久度百分比

```yaml
conditions:
  low-durability:
    type: item_damage
    item:
      selector: MAIN_HAND
      holder: PLAYER
    mode: REMAINING_PERCENT
    compare: '<='
    value: 10
```

### 行为与限制

* 条件会检查第一个物品元数据实现了 `Damageable` 的选中物品。建议使用 `TRIGGER_ITEM`、`CONTEXT`、`MAIN_HAND` 和单个装备槽位等单物品选择器。
* 物品不存在、不可损耗，或最大耐久度为 0 时，条件不匹配。
* 仅当选中的物品就是当前 `PlayerItemDamageEvent` 中的物品时，`include-event-damage` 才会加入事件损耗。对于 `on-item-damage`，`TRIGGER_ITEM` 始终指向该物品。
* 事件损耗通过可修改的触发器结果读取。因此，附加在后续能力修改器或能力上的条件，可以观察更早执行的能力修改器对物品耐久损耗所作的变化。
* 解析 `min`、`max` 和 `value` 时，正在检查的数值可通过 `{original}` 和 `{current}` 使用。
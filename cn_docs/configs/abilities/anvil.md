# ⚒️ 铁砧激活能力

本页面中的能力用于 `powers.activation-abilities`。它们会在铁砧准备输出结果时进行检查，而不是由普通的 `on-...` 触发器执行。

## 本页面的注册键

* `modify_repair_cost`

---

## `modify_repair_cost`

**用途：**当铁砧输入物品或附魔书包含声明该能力的附魔时，修改铁砧操作计算出的经验等级花费。

**上下文：**Paper 的 `PrepareAnvilEvent`。EnchantmentReform 会扫描铁砧的两个输入槽位，包括普通物品上的附魔，以及附魔书中保存的附魔。

### 字段

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `operation` | `SET` | 可填写 `SET`、`ADD`、`SUBTRACT`、`MULTIPLY`、`DIVIDE`、`MIN` 或 `MAX`。 |
| `value` | 当前修复花费 | 操作数或公式。支持 `{original}`、`{current}`、`{level}`、根能力变量，以及存在玩家时的 PlaceholderAPI。 |

### 示例

```yaml
powers:
  activation-abilities:
    double-repair-cost:
      type: modify_repair_cost
      operation: MULTIPLY
      value: 2
```

### 行为与限制

* `{original}` 和 `{current}` 表示当前这条配置规则执行前的修复花费。
* 结果会四舍五入为最接近的整数，并限制在 `0..2147483647`。
* 除数为零时，当前花费保持不变。
* 同一个附魔同时出现在两个输入槽位中时，只会按照两个输入中的最高附魔等级执行一次规则。
* 来自不同附魔的规则会先按照附魔执行优先级排序，再按照命名空间键排序执行。
* 将此能力放在普通触发器的 `abilities` 部分中不会产生效果。
* 规则会在原版已经生成有效铁砧结果后应用。它不会重新构建因为其他原因而被原版拒绝的操作。

## 绕过原版附魔最高等级

Paper 服务器可以启用：

```yaml
anvil:
  bypass-enchantment-level-limit: true
```

这允许铁砧应用高于 `Enchantment#getMaxLevel()` 的附魔等级。该选项与 `modify_repair_cost` 相互独立：配置选项控制铁砧可以生成哪些附魔等级，而激活能力只负责修改显示的经验等级花费。
# 自定义属性

直接放在 `plugins/EnchantmentReform/attributes` 下的每个 `.yml` 文件定义一个持久化的玩家整数属性，子目录不会被扫描。去掉 `.yml` 后的文件名是全局唯一的属性 ID。基础值和持久 modifier 保存在玩家 PDC 中，应用 modifier 后的最终值会作为 `{level}` 传递给配置的 Power。

```yaml
enabled: true
name: '&c力量训练'
description: '&8增加 &a{bonus_damage} &8点攻击伤害。'

minimum-value: 0
maximum-value: 10
default-value: 0
show-in-attribute-gui: true

skip-power-at-default-value: true
skip-power-at-zero: true

skill-menu:
  show: true

allocation:
  show-in-menu: true
  price:
    '1': 8
    '2': 16
    '3': 32
    '4': 64
  conditions:
    strength-required:
      type: attribute_value
      attribute: strength
      compare: '>'
      value: 10
  levels:
    '5':
      conditions:
        defense-required:
          type: attribute_value
          attribute: defense
          min: 20

variables:
  bonus_damage: '{level} * 0.5'

powers:
  on-attack:
    modifiers:
      attribute-damage:
        type: damage
        operation: ADD
        value: '{bonus_damage}'
```

## 配置项

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `enabled` | `true` | 是否加载该属性。设为 false 后，该属性不会出现在菜单、命令补全中，也不会注册或执行其 Power。 |
| `name` | 文件 ID | 显示名称，支持 `{lang:...}`。 |
| `description` | 无 | 可选描述，支持 `{lang:...}`、`{level}` 和 `variables` 中的变量；描述里的 `{level}` 是查看者当前的最终属性值。 |
| `minimum-value` | `0` | 可以保存的最小值。 |
| `maximum-value` | Java 整数最大值 | 可以保存的最大值。 |
| `default-value` | `0` | 玩家没有 PDC 数据时返回的基础值。 |
| `show-in-attribute-gui` | `true` | 是否在只读属性信息 GUI 中显示；该开关与加点菜单显示开关相互独立。 |
| `skill-menu.show` | `true` | 是否在所有技能详情菜单中显示该属性并允许加点。属性不再绑定某个技能。 |
| `skip-power-at-default-value` | `false` | 最终值等于 `default-value` 时不把该属性加入 Power 执行。 |
| `skip-power-at-zero` | `false` | 最终值为零时不把该属性加入 Power 执行。 |
| `allocation.show-in-menu` | `true` | 是否在属性加点菜单中显示该属性。 |
| `allocation.maximum-value` | `maximum-value` | 玩家通过加点能够达到的最高基础值；最终值仍可由 modifier 增加到 `maximum-value`。 |
| `allocation.price` | `1` | 到达每个目标基础等级所消耗的属性点；支持单个数值或等级选择器映射，价格配置在属性文件中。 |
| `allocation.conditions` | 无 | 每次增加等级前都要检查的通用 Power Conditions。 |
| `allocation.levels.<等级>.conditions` | 无 | 玩家基础值到达指定等级时额外检查的 Power Conditions。 |
| `allocation.levels.<等级>.requirement-display` | 无 | 为下一级显示的可选、本地化要求文字。省略时，直接配置的 `skill_level` 条件会通过统一的 `skill-allocation-skill-level-requirement` 语言模板自动生成。 |
| `variables` | 无 | 描述与 Power 共用的数值/公式变量，语法与附魔变量相同。 |
| `powers` | 无 | 可选的直接能力，格式与附魔和自定义物品相同。容量型属性可以省略此项，由其他来源读取。 |

基础值和最终值超出范围时会自动限制到最小值或最大值，但管理员通过 `setattribute ... -ignore` 明确设置的值除外。所有 operation 计算完毕后，最终值会四舍五入为整数。即使默认值正好是零，两个跳过选项仍然分别生效。

一次点击增加多级时，会逐一检查所有中间目标等级的通用条件和对应等级条件；任意条件失败都会取消整次加点，且不会扣除属性点。使用 `type: attribute_value` 可以比较玩家另一个自定义属性的最终值。该条件支持 `attribute`、包含边界的 `min`/`max`，或 `compare` 搭配 `value`；例如 `compare: '>'` 与 `value: 10` 表示必须严格大于 10。加点 GUI 会从这里读取显示开关与价格，并展示每种点击方式加点前后的基础值和最终值。

使用 `type: skill_level`、`skill` 和数值比较字段可以要求指定技能等级。例如 `skill: agility` 与 `min: 10` 表示购买该目标属性等级前，敏捷必须达到 10 级。

描述会按照玩家当前的最终属性值渲染，因此基础值或 modifier 改变后，描述也会随之更新。在描述公式前添加 `%:` 可以在计算后自动追加百分号；例如属性值为 `500` 时，`'%:{level} / 10'` 会显示为 `50%`。

## Modifier

每个 modifier 包含一个命名空间 ID、一个可带小数的 amount 和一个 operation。计算顺序如下：

1. `ADD_VALUE`：直接加上 amount。
2. `ADD_MULTIPLIED_BASE`：加上“基础值 × amount”。
3. `ADD_MULTIPLIED_TOTAL`：把累计结果乘以 `1 + amount`；存在多个时依次相乘。

例如基础值为 `100`，同时拥有 `ADD_VALUE 20`、`ADD_MULTIPLIED_BASE 0.5` 和 `ADD_MULTIPLIED_TOTAL 0.1`，最终结果为 `(100 + 20 + 100 × 0.5) × 1.1 = 187`。

指令创建的 modifier 会持久化到玩家 PDC。`refresh_attribute` 创建的临时 modifier 只存在于运行时，并会在到期、附魔/自定义物品来源失效或 ability 卸载时移除。

代码可以通过 `AttributeManager.attributeManager` 使用 `getBaseValue`、最终值 `getValue`、`setValue`、`addValue`、`resetValue`、`getModifiers`、`addModifier`、`setModifier` 和 `removeModifier`。

管理员可以通过以下指令修改在线玩家的属性值：

```text
/enchantmentreform setattribute <玩家> <属性> <值>
/enchantmentreform setattribute <玩家> <属性> <值> -ignore
/enchantmentreform addattribute <玩家> <属性> <增量>
/enchantmentreform addattributemodifier <玩家> <属性> <modifier-id> <amount> <operation>
/enchantmentreform setattributemodifier <玩家> <属性> <modifier-id> <amount> <operation>
```

`setattribute` 和 `addattribute` 修改基础值。给 `setattribute` 追加 `-ignore` 后，可保存超出配置最小值/最大值的数值，且该基础值及其 modifier 结果不受配置范围限制；之后执行不带 `-ignore` 的 `setattribute`、`addattribute` 或重置属性，会恢复正常的范围限制。`addattributemodifier` 与原版添加 modifier 的行为一致，遇到已存在的 ID 会拒绝执行；`setattributemodifier` 会创建或覆盖该 ID。

玩家可以使用 `/enchantmentreform menu attribute-info` 查看全部自定义属性，包括自己的基础值、最终值、范围和当前 modifier。

安装 PlaceholderAPI 后，`%enchantmentreform_attribute_<id>%` 会显示玩家的当前属性值。例如 `%enchantmentreform_attribute_strength%` 显示 `strength` 的值。

`set_attribute` 和 `refresh_attribute` ability 同时接受 Bukkit 属性与自定义属性 ID。自定义属性可以写成 `strength`、`custom_attribute:strength` 或 `enchantmentreform:strength`；其目标必须是玩家。`set_attribute` 修改基础值，`refresh_attribute` 应用临时 modifier，并同时接受上面的新 operation 名称和旧 Bukkit 名称。

能力中的数值和公式字段可用 `{attribute:<id>}` 读取玩家的最终自定义属性值；使用 `{attribute:<id>:<variable>}` 可按该属性的最终值解析变量，例如 `{attribute:dodge:melee_dodge_chance_decimal}`。

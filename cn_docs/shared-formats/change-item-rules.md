# 🔧 物品修改格式

`change_item` 能力会将其嵌套的 `changes` 部分交给物品修改格式处理。修改规则会按照 YAML 中的顺序对当前物品执行，因此后面的规则能够读取前面规则已经写入的元数据。

```yaml
type: change_item
item:
  selector: MAIN_HAND
  holder: PLAYER
changes: {}
```

EnchantmentReform 的物品修改格式与 MythicChanger 的格式基本相同。基础格式请点击[此处](https://mythicchanger.superiormc.cn/change-rules/empty)查看 MythicChanger Wiki；本页面只介绍 EnchantmentReform 中可用的专用内容。

## 在 `changes` 中使用能力变量

这使可复用变量能够依赖当前正在修改的物品：

```yaml
variables:
  repair-amount: '{max-damage} * {level} * 0.1'
  enchant-level-change: -1

powers:
  on-item-damage:
    abilities:
      emergency-repair:
        type: change_item
        item: TRIGGER_ITEM
        changes:
          modify-enchants:
            enchantmentreform:emergency_repair: '{enchant-level-change}'
          repair-damage: '{repair-amount}'
```

执行时，`{repair-amount}` 会先展开为 `{max-damage} * {level} * 0.1`；随后替换当前物品的最大耐久度和触发附魔等级，最后才计算表达式。

当前触发器提供对应数值时，变量也可以包含 `{original_item_damage}` 等数值触发器上下文占位符。字段专用占位符之后会由所属规则解析，因此 `modify-enchants` 使用的变量还可以包含 `{current-level}` 或 `{max-level}`。

由于规则按照 YAML 顺序执行，并且 `ObjectSingleChange` 会使当前 `ItemMeta` 保持同步，因此后续规则使用的变量能够读取同一个 `changes` 部分中前面规则造成的元数据变化。

## `modify-enchants`

直接修改物品上的普通附魔等级，不需要重新构建或序列化物品。

### 加法简写

标量值会被视为附魔等级变化量：

```yaml
changes:
  modify-enchants:
    minecraft:looting: -1
    minecraft:unbreaking: 2
```

对于带有抢夺 III 和耐久 I 的物品，结果将变为抢夺 II 和耐久 III。

原版附魔可以省略 `minecraft:` 命名空间：

```yaml
changes:
  modify-enchants:
    looting: -1
```

自定义附魔必须使用完整的命名空间键：

```yaml
changes:
  modify-enchants:
    enchantmentreform:example: 1
```

### 操作形式

每个附魔也可以改用操作配置部分：

```yaml
changes:
  modify-enchants:
    minecraft:looting:
      operation: SUBTRACT
      value: 1

    minecraft:unbreaking:
      operation: MULTIPLY
      value: 2

    minecraft:sharpness:
      operation: SET
      value: 'min({current-level} + {level}, 10)'
```

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `operation` | `ADD` | 可填写 `ADD`、`SUBTRACT`、`MULTIPLY` 或 `SET`。未知值会使用 `ADD`。 |
| `value` | `0` | 操作数。支持数学表达式和 ChangeItem 占位符。 |

### 字段专用占位符

| 占位符 | 含义 |
| --- | --- |
| `{current-level}` | 当前物品中保存的附魔等级。物品没有该附魔时使用 `0`。 |
| `{max-level}` | 该附魔注册的最高等级。 |

普通 ChangeItem 占位符仍然可用，包括 `{level}`、`{amount}`、`{max-stack}`、`{damage}`、`{max-damage}` 和 `{original-damage}`。

### 行为与限制

* 计算结果会四舍五入为最接近的整数。
* 结果小于或等于 `0` 时会移除该附魔。
* 结果为正数时，会使用不安全等级处理添加或替换附魔，因此不会强制执行兼容性和原版最高等级限制。
* 缺失的附魔从等级 `0` 开始，因此正数加法结果可以为物品添加该附魔。
* 未知附魔键会被跳过。
* 无效或非有限表达式会被报告并跳过，而不会因此移除附魔。
* 此规则修改普通物品附魔，不会影响附魔书中保存的附魔。
* 注册表查询会按照规范化后的附魔键缓存；每次执行会直接从 `ItemMeta` 读取请求的等级，而不是序列化物品或扫描完整 ItemFormat。

### 降低一级并在零级时移除

```yaml
changes:
  modify-enchants:
    minecraft:looting: -1
```

| 修改前 | 修改后 |
| --- | --- |
| 抢夺 III | 抢夺 II |
| 抢夺 II | 抢夺 I |
| 抢夺 I | 移除附魔 |
| 没有抢夺 | 不发生变化 |

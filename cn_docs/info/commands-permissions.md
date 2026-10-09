# ⌨️ 命令与权限

主命令：`/enchantmentreform`

别名：`/er`、`/enchants`

## 菜单

```text
/enchantmentreform menu <菜单ID>
```

按照 YAML 文件名打开菜单。例如：`menu main`、`menu enchantment-info`、`menu skill-info`、`menu attribute-info`、`menu attribute-allocation`。

权限：

```text
enchantmentreform.menu
```

此权限默认授予所有玩家。不注册任何旧版 GUI 二级指令。

## 管理自定义属性

```text
/enchantmentreform setattribute <玩家> <属性> <值>
/enchantmentreform setattribute <玩家> <属性> <值> -ignore
/enchantmentreform addattribute <玩家> <属性> <增量>
/enchantmentreform addattributemodifier <玩家> <属性> <modifier-id> <amount> <operation>
/enchantmentreform setattributemodifier <玩家> <属性> <modifier-id> <amount> <operation>
```

前两个指令修改基础值。`setattribute` 末尾可加 `-ignore`，以绕过该属性配置的最小值与最大值。Modifier operation 包括 `ADD_VALUE`、`ADD_MULTIPLIED_BASE` 和 `ADD_MULTIPLIED_TOTAL`。四个管理权限都默认授予服务器管理员，权限节点为 `enchantmentreform.` 加对应指令名。

## 重载运行时文件

```text
/enchantmentreform reload
```

重载当前实现中可在运行时安全更新的配置、语言、菜单和能力状态。

权限：

```text
enchantmentreform.reload
```

此权限默认授予服务器管理员。

## 管理玩家技能进度

```text
/enchantmentreform setskillxp <玩家> <技能ID> <经验>
/enchantmentreform addskillxp <玩家> <技能ID> <经验>
/enchantmentreform setskilllevel <玩家> <技能ID> <等级>
/enchantmentreform addskilllevel <玩家> <技能ID> <增量>
```

`setskillxp` 修改当前等级内的经验进度，并限制在该等级所需经验以内；`addskillxp` 使用正常升级与奖励流程。直接修改等级时会限制在技能配置范围内，且不会发放等级奖励。每条指令均使用同名的 `enchantmentreform.<指令名>` 权限，默认仅管理员拥有。

## 设置玩家技能经验倍率

```text
/enchantmentreform setskillmultiplier <玩家> <技能ID|all> <倍率>
```

为指定技能或全部技能设置持久化经验倍率。`all` 倍率与指定技能倍率会相乘；使用 `1` 删除对应覆盖值，使用 `0` 停止对应范围的经验获取。仅当 `config.yml -> modules.skills` 开启时才会注册该指令。

权限：`enchantmentreform.setskillmultiplier`，默认仅服务器管理员拥有。

{% hint style="warning" %}
重载无法重建 Minecraft 附魔注册表。添加或删除附魔、修改注册表字段后，仍然必须完整重启服务器。
{% endhint %}

## 生成 ItemFormat

```text
/enchantmentreform generateitemformat
```

把玩家主手物品转换为 ItemFormat，并写入 `plugins/EnchantmentReform/generated-item-format.yml`。再次执行该指令会覆盖此文件。

权限：

```text
enchantmentreform.generateitemformat
```

此权限默认授予服务器管理员，并且指令必须由玩家执行。

## 绕过权限

| 权限 | 用途 | 默认值 |
| --- | --- | --- |
| `enchantmentreform.bypass.protection` | 允许受支持的方块能力绕过保护插件检查。 | OP |
| `enchantmentreform.bypass.economy` | 绕过 `cost_price` 等受支持的经济或物品费用。 | OP |

## 示例

```text
/er gui
/enchants gui
/er reload
/er generateitemformat
```

控制台可以执行 `reload`；GUI 命令必须由玩家执行。

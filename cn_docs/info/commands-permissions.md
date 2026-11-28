# ⌨️ 命令与权限

主命令：`/enchantmentreform`

别名：`/er`、`/enchants`

## 菜单

```text
/enchantmentreform menu [菜单ID]
/enchantmentreform menu skill-detail <技能ID>
/enchantmentreform menu attribute-detail <属性ID>
```

按照 YAML 文件名打开菜单。例如：`menu main`、`menu enchantment-info`、`menu skill-info`、`menu attribute-info`、`menu attribute-allocation`。

拥有 `enchantmentreform.enchantmentinfo.givebook` 权限的玩家，可在附魔图鉴中 Shift 点击附魔，获得一本对应的最高等级附魔书。该权限默认授予 OP。背包放不下时，附魔书会掉落在玩家脚下。

省略菜单ID时打开 `main`。控制台可以为在线玩家打开菜单：

```text
er menu Steve
er menu main Steve
er menu main Steve -b
er menu skill-detail Steve mining -b
er menu attribute-detail Steve strength -b
```

完整控制台格式为 `er menu [菜单ID] <玩家> [技能ID|属性ID] [-b]`。不加 `-b` 会检查目标玩家的菜单权限；加上后跳过菜单自身的 `permission` 检查，该状态在当前菜单的跳转、详情页和返回时保留。关闭菜单后玩家自行打开仍按正常权限检查。已禁用的菜单仍无法打开，技能奖励领取、属性升级等操作继续检查各自条件。玩家命令无法使用 `-b` 跳过权限。目标离线、菜单不存在或详情上下文无效时显示相应提示；命令支持菜单、在线玩家、上下文和 `-b` 补全。

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

## 管理临时能力

```text
/er temppower set <玩家> <ID> [等级]
/er temppower remove <玩家> <ID>
/er temppower clear <玩家>
/er temppower list <玩家>
```

目标必须在线；省略等级时使用对应 `temp_powers/<ID>.yml` 的 `default-level`。等级必须为正整数。权限为 `enchantmentreform.temppower`，默认仅管理员拥有。玩家和控制台均可执行，其他插件也可派发命令。

`remember: false` 的授权及等级在插件重载或重新启用后保留，仅在玩家重新进服或服务器重启后清空；`true` 则持续保留。完整配置和生命周期规则见[临时能力](../configs/temp-powers.md)。

## 随机给予附魔书

```text
/er randombook [玩家] all [数量] [--level <等级|范围>] [--target <支持物品标签>] [--rarity <稀有度ID>]
/er randombook [玩家] levels <附魔强度> [数量] [--target <支持物品标签>] [--rarity <稀有度ID>]
```

玩家省略玩家名时给自己发书，控制台必须指定在线玩家。数量默认为 1，范围为 1–64。每本独立抽取，背包放不下的书会掉落在目标玩家脚下。

`all` 为每本书随机抽取一种附魔。加上 `--level 3` 时，只抽支持 III 级的附魔并发放 III 级附魔书。`--level 5~10` 会在 5 至 10 级与所选附魔支持等级的交集内抽取一级。`levels`（也可写作 `level`）按原版附魔强度生成，一本书可含多个相容附魔。任一次抽取失败时整批不发放。

`--target enchantmentreform:sword` 可筛选适用于 `config.yml` 中对应 `supported-items.tags` 标签的附魔。填写配置中的标签键，无需加 `#`。末尾加 `--rarity common` 可筛选配置中的稀有度。两个筛选可以同时使用，例如 `/er randombook levels 30 5 --target enchantmentreform:sword --rarity common`。

权限为 `enchantmentreform.randombook`，默认仅管理员拥有。

## 给予指定附魔书

```text
/er givebook [玩家] <附魔ID> [等级]
```

玩家省略目标时对自己执行，控制台须指定在线玩家。例如 `/er givebook minecraft:sharpness 5` 给自己一本锋利 V 附魔书。省略等级时发放该附魔的最高等级，也可指定 1 至 255 级。简写 ID 优先查找原版，再查找 EnchantmentReform，其他插件请使用完整命名空间。

权限为 `enchantmentreform.givebook`，默认仅管理员拥有。

## 随机附魔主手物品

```text
/er randomenchant [玩家] all [次数] [--level <等级|范围>] [--rarity <稀有度ID>]
/er randomenchant [玩家] levels <附魔强度> [次数] [--rarity <稀有度ID>]
```

玩家省略目标时会为自己的主手物品附魔，控制台须指定在线玩家。

例如 `/er randomenchant Steve all 2 --level 3 --rarity common` 会给 Steve 的主手物品添加两个相容的普通稀有度 III 级附魔。`all` 模式的 `--level` 也支持 `5~10` 这样的闭区间。每次抽取均排除已存在、互斥或不适用的附魔，levels 模式按物品和附魔强度执行原版算法。所有抽取成功后才写回主手物品。

权限为 `enchantmentreform.randomenchant`，默认仅管理员拥有。

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

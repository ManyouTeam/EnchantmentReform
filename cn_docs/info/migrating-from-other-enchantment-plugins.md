# 🔄 从其他附魔插件迁移

最安全的迁移方案，是在对应的 EnchantmentReform 定义中保留每个旧附魔的完整命名空间键。

例如，旧插件将生命窃取注册为：

```text
oldenchants:lifesteal
```

那么替代它的 EnchantmentReform 文件也应使用相同的键：

```yaml
enabled: true
key: oldenchants:lifesteal

name: Life Steal
description: Steals health from the target.

supported-items: enchantmentreform:melee_weapon
primary-items: enchantmentreform:melee_weapon
max-level: 3

# Configure rarity, slots, obtaining sources, powers, and other fields here.
```

文件名和目录不需要与旧插件一致。根字段 `key` 才是 Minecraft 保存的权威身份。

{% hint style="danger" %}
**迁移前务必备份服务器。**错误的注册表迁移可能影响玩家库存、末影箱、容器、潜影盒、保存物品文件、交易和离线玩家数据。不要直接使用未经测试的生产世界作为第一次迁移测试。
{% endhint %}

## 需要备份的内容

正常关闭服务器，然后创建能够完整恢复的备份，范围包括：

* 所有世界文件夹，包括玩家数据和区域文件；
* 旧附魔插件的 JAR 及其完整数据文件夹；
* 已经存在时的 `plugins/EnchantmentReform/`；
* 服务器的 `plugins/` 目录和配置文件；
* 库存、经济、邮箱、拍卖或存储插件使用的外部数据库；
* 任何可能包含物品的代理端、跨服库存或同步数据。

将备份复制到独立位置，并确认它确实能够恢复。只把备份保存在正在运行的服务器目录中，并不能提供充分保护。

## 建立一对一键映射

移除旧插件前，列出它注册的每一个附魔。可以从其配置、文档、命令、API 或注册表输出中获取这些键。

创建类似下方的工作表：

| 旧附魔 | 旧键 | EnchantmentReform 文件 | 新 `key` | 已检查等级 | 就绪 |
|---|---|---|---|---|---|
| Life Steal | `oldenchants:lifesteal` | `melee/life_steal.yml` | `oldenchants:lifesteal` | 1–3 | 是 |
| Telepathy | `oldenchants:telepathy` | `tools/telepathy.yml` | `oldenchants:telepathy` | 1 | 是 |
| Heavy Curse | `oldenchants:heavy_curse` | `curse/heavy_curse.yml` | `oldenchants:heavy_curse` | 1–2 | 待处理 |

将**每一个旧键准确映射到一个新定义**：

```text
old key A -> new definition A using old key A
old key B -> new definition B using old key B
old key C -> new definition C using old key C
```

命名空间、拼写、下划线和旧插件实际注册的字母大小写都应保持不变。不要让两个旧键映射到同一个新键，也不要把某个键重新用于不相关的效果。

## 为什么必须保留键

使用原生 Minecraft 附魔组件的物品保存的是注册表引用，而不是旧插件的显示名称或配置文件名。当 EnchantmentReform 使用相同的键注册替代附魔时，这些物品可以直接将原有引用解析到新定义，无需逐个重写物品。

修改命名空间会破坏这种身份关系：

```text
oldenchants:lifesteal != enchantmentreform:lifesteal
```

如果新定义使用 `enchantmentreform:lifesteal`，现有物品仍然指向 `oldenchants:lifesteal`。这时必须另外编写物品转换流程，处理所有可能保存物品的位置。

{% hint style="warning" %}
保留键只适用于通过 Minecraft 附魔注册表和物品组件表示的附魔。有些插件通过 Lore、Persistent Data Container、定制 NBT 或私有数据库模拟附魔。这些格式由旧插件自行管理，可能必须使用它提供的迁移命令、API 或专用转换工具。
{% endhint %}

## 创建替代定义

针对键映射表中的每一行：

1. 在 `plugins/EnchantmentReform/enchantments/` 下创建一个 YAML 文件。
2. 将根字段 `key` 设置为旧插件的完整键。
3. 使用 EnchantmentReform 的触发器、条件、修改器和能力重新实现预期效果。
4. 与旧插件的最高等级保持一致，或明确记录如何处理超出新最高等级的物品。
5. 配置支持物品、主要物品、活跃槽位、互斥关系、稀有度、花费和获取来源。
6. 检查替代实现是否要求 Paper、更新的 Minecraft 版本或可选集成。
7. 只有测试完全部受支持等级后，才能将对应映射标记为就绪。

效果的内部实现方式不需要与旧插件相同，但最终行为应有意保持等价。请特别检查：

* 百分比使用的是 `10`、`0.10`，还是类似 `1.10` 的倍率；
* 冷却时间使用的单位，以及冷却是否共享；
* 伤害来源，以及攻击者和目标方向；
* 能力在哪些装备槽位中处于活跃状态；
* 最高等级，以及如何处理携带高于新最高等级的物品；
* 与原版或其他自定义附魔的冲突；
* 附魔书和直接附在装备上的附魔是否采用相同行为。

## 执行迁移

请先在生产服务器的测试副本上执行：

1. 关闭服务器。
2. 确认备份完整。
3. 安装 EnchantmentReform，并将所有完成的替代定义放入其 `enchantments/` 文件夹。
4. 启动前移除或禁用旧附魔插件。两个插件不能同时尝试注册同一个键。
5. 正常启动服务器。不要使用 Bukkit `/reload`、PlugMan 或其他热加载器。
6. 检查启动日志中是否存在重复键、缺失标签、无效注册表数据、不受支持的效果和被禁用的集成。
7. 将键映射表与服务器实际注册的附魔进行核对。

添加、移除或修改附魔键属于注册表操作，始终需要完整重启服务器。`/enchantmentreform reload` 无法重建附魔注册表。

## 测试现有物品

从服务器使用的每一种存储路径中复制真实物品进行测试：

* 在线和离线玩家库存；
* 已装备的护甲和副手物品；
* 末影箱；
* 普通容器和潜影盒；
* 附魔书；
* 拍卖行、邮箱、背包、仓库和虚拟存储；
* 村民交易、战利品表、礼包、商店和保存物品插件。

针对每个迁移的附魔，验证：

* 旧物品仍然包含预期的键和等级；
* 显示名称和物品提示可以接受；
* 效果会在正确的槽位和情况下激活；
* 铁砧合并和附魔行为正确；
* 新生成物品和原有物品表现一致；
* 重启服务器不会移除或改变附魔。

在代表性物品、离线数据和外部存储全部检查完成前，不要开放生产服务器。

## 回滚

如果验证失败：

1. 立即关闭服务器。
2. 不要让玩家继续移动或修改受影响的物品。
3. 从同一个备份时间点恢复世界、玩家数据、插件文件、插件数据和外部数据库。
4. 启动服务器前恢复旧插件。
5. 在测试环境中修正键映射或替代定义，然后重新执行迁移。

只恢复插件 JAR，却保留迁移后已经修改的物品和世界数据，可能形成混合状态。所有能够保存物品的数据都必须一致回滚。

## 最终检查清单

* [ ] 服务器已经关闭，并且存在经过验证的异地备份。
* [ ] 映射工作表中每个旧附魔键都只出现一次。
* [ ] 每个新定义都使用预期的旧命名空间键。
* [ ] 已检查最高等级、支持物品、槽位、冲突和效果。
* [ ] 已检查 Paper、Spigot、Minecraft 版本和集成要求。
* [ ] 旧插件和 EnchantmentReform 不会同时注册同一个键。
* [ ] 已在服务器副本中测试现有物品和新创建物品。
* [ ] 测试范围包含离线库存和外部物品存储系统。
* [ ] 启动日志没有错误。
* [ ] 已测试或记录完整回滚流程。

完整定义格式请参阅[附魔配置](../configs/enchantment-configuration.md)，示例和平台要求请参阅[内置附魔与支持版本](builtin-enchantments.md)。

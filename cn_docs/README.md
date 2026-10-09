# 🎉 EnchantmentReform

欢迎使用 **EnchantmentReform**。这是一个面向现代服务端、由数据驱动的附魔框架。它可以注册自定义附魔，允许管理员重新定义部分原版附魔，并通过 YAML 构建运行时效果，而不是将效果硬编码到事件监听器中。

此插件包含以下功能：

* 从 `enchantments/**/*.yml` 注册自定义附魔。
* 可选择在 `vanilla_enchantments/` 中覆盖原版附魔。
* 支持等级表达式、变量、范围和 PlaceholderAPI 解析。
* 支持稀有度默认值、适用物品标签、提示文本排序和附魔台集成。
* 通过有效装备槽和 `on-activate` / `on-deactivate` 处理玩家装备的生命周期。
* 提供战斗、弹射物、方块、移动、钓鱼、交易、背包、物品和环境等触发器。
* 提供可复用的 MatchItemFormat 和 MatchEntityFormat 规则。
* 提供适配 Paper、Spigot 和 Folia 的调度方式；部分注册表和事件功能仍取决于具体平台。
* 安装对应插件后，可与自定义物品、经济、领地保护、MythicMobs、PacketEvents、NBTAPI 和 EnchantmentSlots 等生态集成。

{% hint style="warning" %}
Minecraft 的附魔注册表会在服务端启动完成后冻结。添加或删除附魔、修改注册表字段、适用物品、由稀有度决定的注册表值，或修改原版附魔定义后，都必须**完整重启服务端**。`/enchantmentreform reload` 只能重载可在运行时安全更新的文件，无法重建已经冻结的注册表。
{% endhint %}

## 推荐阅读

* [运行要求](info/requirements.md)
* [安装与更新](info/install.md)
* [从其他附魔插件迁移](info/migrating-from-other-enchantment-plugins.md)
* [配置文件](info/configuration-files.md)
* [附魔配置](configs/enchantment-configuration.md)
* [能力触发器](configs/triggers.md)
* [能力条件](configs/power-conditions/)
* [能力修改器](configs/power-modifiers/)
* [内置附魔与支持版本](info/builtin-enchantments.md)
* [能力](configs/abilities/)

## 链接

### 获取插件

### 获取支持

你可以通过 [Discord 服务器](https://discord.gg/RZajEybhBw) 获取支持。请求帮助前，请先阅读服务器规则和插件许可证。

已购买插件的中国大陆用户可以凭购买证明加入 QQ 群 `815351827`。

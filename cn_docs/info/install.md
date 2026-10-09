# ⚙️ 安装

## 想在物品 Lore 中显示附魔描述？

请使用 [EnchantmentSlots](https://www.spigotmc.org/resources/enchantmentslots-add-enchantment-slot-feature-to-your-server-1-20-5.113048/)。

## 想让怪物也能使用附魔？

由于让同一套附魔同时对怪物和玩家生效的实现不够可靠，此功能已被拆分到 [EnchantedMobs](https://www.spigotmc.org/resources/enchantedmobs-dynamic-mob-abilities-and-player-scaling-difficulty-61-built-in-power-1-21-3.133242/) 中。

## 附魔颜色没有显示？

请尝试重启服务器。

## 安装

* 从官方发布页面下载插件。文件名应为 **EnchantmentReform-X.X.X.jar**，其中 X 代表版本号。
* 将 `.jar` 文件放入服务器的 `plugins` 文件夹。
* 关闭服务器后重新启动。<mark style="color:red;">不要在服务器运行时通过其他方式加载插件</mark>。
* 更新插件时，请务必删除旧版本的插件文件。
* 如果之前使用的是免费版本，现在需要升级到付费版本，只需删除免费版本并在服务器中安装付费版本。插件配置文件不需要修改。
* 如果需要将安装了此插件的服务器从**较新的游戏版本**降级到**较旧的版本**，请务必删除配置目录中的 `items` 文件夹。
* 默认配置仅用于帮助你了解配置结构，请根据服务器实际需求进行修改。

{% hint style="warning" %}
请勿使用 Bukkit 的 `/reload`、PlugMan 或其他插件热加载器。附魔和标签会在服务端启动阶段注册，注册表冻结后无法安全地重新构建。
{% endhint %}

## 更新

* 更新插件时，请务必删除旧版本的插件文件。
* 建议始终将插件更新到最新版本。
* 为防止自定义配置被覆盖或损坏，插件不会自动更新已有配置。你可以手动合并新版默认配置中的改动，或重新生成全新的配置文件。
* 每次更新前请仔细阅读更新日志。如果跨越多个版本更新，请阅读中间所有版本的更新日志。重大且不兼容的改动会在其中注明。

## 何时必须重启

修改以下内容后必须完整重启服务器：

* `enchantments/` 或 `vanilla_enchantments/` 中的文件集合；
* 附魔的 `key`；
* 适用物品标签或主要物品标签；
* 权重、最高等级、费用、互斥关系、有效槽位或获取来源等注册表值；
* 会参与注册表注册的全局稀有度默认值；
* 适用物品标签定义；
* 插件 JAR 或与注册表有关的依赖插件。

`/enchantmentreform reload` 仅用于重载可在运行时安全更新的配置和语言文件，不会重新注册已经冻结的 Minecraft 注册表。
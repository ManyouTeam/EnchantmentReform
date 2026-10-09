# 📝 ItemFormat™

当配置需要构建、替换、比较、掉落、收取或转换物品时，EnchantmentReform 会使用共享的 ItemFormat 结构。

常见用途包括：

* `drop_item` 和配置的额外或替换掉落物；
* `replace_item` 和 `change_item`；
* `cost_price` 的物品价格；
* `summon` 或其他需要物品的集成选项；
* MatchItemFormat 的 `item-format` 规则；
* 钓鱼替换物和额外渔获条目。

配置物品通常是一个 YAML 配置部分，其中包含材质或提供器 ID，并可选择配置数量、名称、Lore、附魔、组件，以及共享构建器支持的插件专用字段。

```yaml
item:
  material: DIAMOND
  amount: '1 + level'
  name: '&bEnchanted Diamond'
  lore:
    - '&8Created by EnchantmentReform'
```

由提供器实现的物品会使用已安装集成所支持的物品挂钩和 ID 格式。

完整的共享 ItemFormat 字段参考请参阅项目链接的 [UltimateShop 格式文档](https://ultimateshop.superiormc.cn/format/itemformat-tm)。EnchantmentReform 的专用行为由使用该物品配置部分的能力或能力修改器决定；例如，加权条目可能会在普通物品数据之外额外使用 `rate` 字段。
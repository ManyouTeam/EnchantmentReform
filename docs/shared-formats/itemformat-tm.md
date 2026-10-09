# 📝 ItemFormat™

EnchantmentReform uses the shared ItemFormat structure when a configuration must build, replace, compare, drop, charge, or transform an item.

Common uses include:

* `drop_item` and configured bonus/replacement drops;
* `replace_item` and `change_item`;
* `cost_price` item costs;
* `summon` or integration options that request an item;
* MatchItemFormat's `item-format` rule;
* fishing replacement/extra-catch entries.

A configured item is normally a YAML section containing a material/provider ID plus optional amount, name, lore, enchantments, components, and plugin-specific fields supported by the shared builder.

```yaml
item:
  material: DIAMOND
  amount: '1 + level'
  name: '&bEnchanted Diamond'
  lore:
    - '&8Created by EnchantmentReform'
```

Provider-backed items use the item hook and ID format supported by the installed integration.

For the complete shared ItemFormat field reference, see the [UltimateShop format](https://ultimateshop.superiormc.cn/format/itemformat-tm) documentation linked by the project. EnchantmentReform-specific behavior is determined by the ability/modifier that consumes the item section; for example, weighted entries may add a `rate` field outside the normal item data.

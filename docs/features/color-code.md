# 🎨 Color Code

EnchantmentReform text fields may be parsed through the plugin's text formatter and, when enabled, MiniMessage-compatible formatting.

## Legacy colors

```text
&6Legendary Enchantment
&cError: insufficient cost
```

## Hex colors

```text
&#ff0000Red text
```

## Gradient-style endpoints

The project also uses endpoint color syntax in rarity and display formats:

```text
&<#F43F5E>Mythic Enchantment&<#C084FC>
```

Whether a field renders a continuous gradient or preserves endpoint formatting depends on the text parser and receiving Minecraft component.

## Language placeholders

Formatting may surround language placeholders:

```yaml
name: '&<#60A5FA>{lang:example-name}&<#22D3EE>'
```

Avoid putting raw untrusted user input inside MiniMessage tags. Test colors in the exact output surface—chat, action bar, GUI title, item lore, or enchantment tooltip—because client rendering rules differ.
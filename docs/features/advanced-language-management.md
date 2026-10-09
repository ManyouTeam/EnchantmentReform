# 🌏 Advanced Language Management

## Default language

Set the default language file in `config.yml`:

```yaml
config-files:
  language: en_US
  per-player-language: true
  force-parse-mini-message: false
```

Language files are stored in `plugins/EnchantmentReform/languages/`. The configured value is the filename without `.yml`.

To add a translation, copy an existing language file, rename it to the desired locale such as `zh_CN.yml`, and translate its values without changing required keys or placeholders.

## Per-player language

When `per-player-language` is enabled, EnchantmentReform chooses a loaded language file from the player's client locale. If no matching file exists, it falls back to the configured default language.

## Language placeholders

Use the language placeholder in supported message, name, description, menu, and feedback fields:

```text
{lang:enchantment-menu-title}
```

Custom keys can be added under `override-lang` in each language file:

```yaml
override-lang:
  example-enchantment-name: 'Example'
  example-enchantment-description: 'Deals extra damage.'
```

Then reference them from an enchantment:

```yaml
name: '{lang:example-enchantment-name}'
description: '&8{lang:example-enchantment-description}'
```

Keep every locale's placeholder arguments consistent. For example, if one translation uses `{remaining}`, all translations of that message should preserve it.
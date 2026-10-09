# 🌏 高级语言管理

## 默认语言

在 `config.yml` 中设置默认语言文件：

```yaml
config-files:
  language: en_US
  per-player-language: true
  force-parse-mini-message: false
```

语言文件存放在 `plugins/EnchantmentReform/languages/` 中。配置值填写不带 `.yml` 后缀的文件名。

如需添加翻译，请复制一个已有的语言文件，将其重命名为目标地区代码，例如 `zh_CN.yml`，然后翻译其中的值。不要修改必需的配置键或占位符。

## 每位玩家使用不同语言

启用 `per-player-language` 后，EnchantmentReform 会根据玩家客户端的地区设置选择已经加载的语言文件。如果没有对应文件，则回退到配置的默认语言。

## 语言占位符

可以在受支持的消息、名称、描述、菜单和反馈字段中使用语言占位符：

```text
{lang:enchantment-menu-title}
```

每个语言文件都可以在 `override-lang` 下添加自定义键：

```yaml
override-lang:
  example-enchantment-name: 'Example'
  example-enchantment-description: 'Deals extra damage.'
```

然后在附魔中引用它们：

```yaml
name: '{lang:example-enchantment-name}'
description: '&8{lang:example-enchantment-description}'
```

所有地区语言中的占位符参数必须保持一致。例如，如果某种翻译使用了 `{remaining}`，该消息的其他翻译也必须保留它。
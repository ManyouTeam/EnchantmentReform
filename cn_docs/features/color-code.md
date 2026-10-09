# 🎨 颜色代码

EnchantmentReform 的文本字段可以通过插件自带的文本格式化器进行解析；启用相关选项后，也可以使用兼容 MiniMessage 的格式。

## 传统颜色代码

```text
&6Legendary Enchantment
&cError: insufficient cost
```

## 十六进制颜色

```text
&#ff0000Red text
```

## 渐变端点格式

项目还会在稀有度和显示格式中使用端点颜色语法：

```text
&<#F43F5E>Mythic Enchantment&<#C084FC>
```

字段最终显示为连续渐变，还是保留两个端点颜色格式，取决于所使用的文本解析器和接收该文本的 Minecraft 组件。

## 语言占位符

格式代码可以包裹语言占位符：

```yaml
name: '&<#60A5FA>{lang:example-name}&<#22D3EE>'
```

不要将未经处理、不可控的用户输入直接放入 MiniMessage 标签中。请在实际输出位置测试颜色效果，例如聊天栏、动作栏、GUI 标题、物品 Lore 或附魔提示文本，因为不同客户端界面的渲染规则可能不同。
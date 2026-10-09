# 💻 开发者 API

EnchantmentReform 提供用于注册自定义能力、能力条件、能力修改器、MatchItem/MatchEntity 规则和触发器的注册表。

请在 EnchantmentReform 启用完成后注册扩展。如果你的插件没有此 API 就无法运行，请添加硬依赖：

```yaml
depend:
  - EnchantmentReform
```

## 依赖坐标

源码模块使用以下依赖：

```xml
<dependency>
    <groupId>cn.superiormc.enchantmentreform</groupId>
    <artifactId>core</artifactId>
    <version>${enchantmentreform.version}</version>
    <scope>provided</scope>
</dependency>
```

请使用与服务器版本匹配的发布构件，或将对应源码版本安装到你的开发仓库。不要使用与生产环境不同的 EnchantmentReform 或服务器代际进行编译。

## 注册键规范化

能力、条件和修改器的字符串注册表会将键转换为小写，并将 `-` 替换为 `_`。因此，`my-ability` 和 `my_ability` 会解析为同一个键。规范化后重复的键会被拒绝。

## 访问插件

```java
import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.TriggerManager;

TriggerManager triggerManager = EnchantmentReform.instance.getTriggerManager();
```

静态实例会在 `onEnable` 期间赋值；不要在 EnchantmentReform 启用完成前访问它。

## 自定义能力

```java
import cn.superiormc.enchantmentreform.managers.AbilityManager;

@Override
public void onEnable() {
    AbilityManager.abilityManager.register("launch_ring", LaunchRingAbility::new);
}
```

```java
import cn.superiormc.enchantmentreform.objects.abilities.AbstractAbility;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public final class LaunchRingAbility extends AbstractAbility {

    public LaunchRingAbility(ConfigurationSection section) {
        super("launch_ring", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (target == null) {
            return false;
        }

        double strength = getDouble("strength", 1.0, context);
        Vector velocity = target.getVelocity();
        velocity.setY(strength);
        target.setVelocity(velocity);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}
```

返回 `true` 会请求取消当前可取消的触发器事件。基类会处理通用条件、概率、冷却、使用次数、动态数值、选择器和位置辅助方法。

位置可以使用 `location: SOURCE`，也可以使用包含 `location.target` 和 `location.distance` 的配置节。前者使用顶层 `offset.x/y/z`，后者使用 `location.offset.x/y/z`。旧的 `location.offset-x/y/z` 仍然兼容，新写法在各坐标轴上优先。能力冷却和次数限制仍按来源实体记录。

## 自定义能力条件

```java
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;

PowerConditionsManager.powerConditions.register(new PowerConditionIsNamed());
```

```java
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.powerconditions.AbstractPowerCondition;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionIsNamed extends AbstractPowerCondition {

    public PowerConditionIsNamed() {
        super("is_named");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }

        LivingEntity target = context.livingEntity(
                condition.getString("target", "TARGET"),
                EntitySelector.TARGET
        );
        return target != null && target.getCustomName() != null;
    }
}
```

基础条件会在 `onMatch` 返回后应用 `not: true` 等通用反转逻辑。

## 自定义能力修改器

```java
import cn.superiormc.enchantmentreform.managers.PowerModifiersManager;

PowerModifiersManager.powerModifiers.register(
        "minimum_damage",
        MinimumDamageModifier::new
);
```

```java
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.powermodifiers.AbstractPowerModifier;
import org.bukkit.configuration.ConfigurationSection;

public final class MinimumDamageModifier extends AbstractPowerModifier {

    public MinimumDamageModifier(ConfigurationSection section) {
        super("minimum_damage", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        double current = context.result().damage(context.triggerData());
        double minimum = getDouble(
                "value",
                1.0,
                context,
                "original",
                String.valueOf(current)
        );
        context.result().damage(Math.max(current, minimum));
    }
}
```

修改器基类会在调用 `onApply` 前检查通用条件、随机概率和冷却。

## 自定义匹配规则

```java
MatchItemManager.matchItemManager.registerNewRule(new CustomItemRule());
MatchEntityManager.matchEntityManager.registerNewRule(new CustomEntityRule());
```

- 继承 `AbstractMatchItemRule`，并实现针对 `ObjectSingleMatchItem` 的匹配逻辑。
- 继承 `AbstractMatchEntityRule`，并实现针对 `ObjectSingleMatchEntity` 的匹配逻辑。
- 当规则的配置键不存在时，`configNotContains` 必须返回 `true`。
- MatchEntityFormat 会接收一个 `LivingEntity`、可选玩家和可选 `PowerContext`。

## 手动触发器

手动触发器会公开一个带命名空间的能力配置部分，而不需要适配 Bukkit 事件。

```java
import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.ManualTrigger;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    private ManualTrigger ragePulse;

    @Override
    public void onEnable() {
        ragePulse = EnchantmentReform.instance
                .getTriggerManager()
                .register(this, "rage_pulse");
    }

    public TriggerResult fireRagePulse(LivingEntity owner, LivingEntity target) {
        TriggerData data = TriggerData.builder(owner)
                .source(owner)
                .skill(owner)
                .target(target)
                .location(target.getLocation())
                .build();

        return EnchantmentReform.instance
                .getTriggerManager()
                .fire(ragePulse, data);
    }

    @Override
    public void onDisable() {
        EnchantmentReform.instance
                .getTriggerManager()
                .unregisterAll(this);
    }
}
```

对于名为 `MyPlugin` 的插件，请使用触发器的命名空间键：

```yaml
powers:
  'myplugin:rage_pulse':
    conditions:
      close-enough:
        type: distance
        max: 12
    abilities:
      effect:
        type: particle
        target: TARGET
        particle: ANGRY_VILLAGER
```

`TriggerData.builder(owner)` 必须提供所有者。来源、技能实体、目标、方块、位置、移动起止位置、Bukkit 事件、物品或槽位、Tick 和带类型的额外数据均为可选项。

## 基于事件的触发器

高级集成可以继承 `AbstractTrigger<E>`，提供唯一键和配置键，通过 `getEventClasses()` 声明订阅的事件类（默认订阅构造器传入的载荷类），然后进行注册：

事件路由使用 `dispatch(EventClass.class, event)`，仅精确匹配显式传入的类，不自动匹配父类。多事件订阅需重写 `getEventClasses()`；返回空集合表示仅支持按键定向分发。没有内置监听器的自定义事件需自行注册 Bukkit 监听器并调用类分发方法。使用旧 `TriggerEventType` / `getEventTypes()` API 的集成需要修改并重新编译。

```java
EnchantmentReform.instance
        .getTriggerManager()
        .register(this, customTrigger);
```

插件禁用时始终调用 `unregisterAll(plugin)`。

## 生命周期清理

自定义实现可以重写以下清理钩子：

```java
public void onUnload()
public void onEntityUnload(UUID entityId)
```

请保存 UUID，而不是一直持有已经卸载的 Bukkit 实体。清理逻辑作用于整个类型，不应依赖某个临时 YAML 配置部分实例。

## 兼容性建议

- 在 Folia 上使用调度器安全的实体和方块操作。
- 修改数据前验证所需的上下文实体、事件、物品、方块和结果是否存在。
- 不要假设 Paper 专属事件在 Spigot 上也存在。
- 保持已经发布的类型 ID 稳定，因为管理员的 YAML 配置依赖这些 ID。
- 避免覆盖内置键。
- 针对每个受支持的 EnchantmentReform 和服务器代际重新编译并测试。

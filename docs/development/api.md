# 💻 Developer API

EnchantmentReform exposes registries for custom abilities, Power Conditions, Power Modifiers, MatchItem/MatchEntity rules, and triggers.

Register extensions after EnchantmentReform has enabled. Add a hard dependency when your plugin cannot function without the API:

```yaml
depend:
  - EnchantmentReform
```

## Dependency coordinates

The source modules use:

```xml
<dependency>
    <groupId>cn.superiormc.enchantmentreform</groupId>
    <artifactId>core</artifactId>
    <version>${enchantmentreform.version}</version>
    <scope>provided</scope>
</dependency>
```

Use the matching release artifact or install the matching source version into your development repository. Do not compile against a different EnchantmentReform/server generation than production.

## Registry key normalization

Ability, condition, and modifier string registries convert keys to lowercase and replace `-` with `_`. `my-ability` and `my_ability` therefore resolve to the same key. Duplicate normalized keys are rejected.

## Accessing the plugin

```java
import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.managers.TriggerManager;

TriggerManager triggerManager = EnchantmentReform.instance.getTriggerManager();
```

The static instance is assigned during `onEnable`; never access it before EnchantmentReform has enabled.

## Custom ability

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

Returning `true` requests cancellation of the current cancellable trigger. The base class handles common conditions, chance, cooldown, usage count, dynamic values, selectors, and location helpers.

Locations accept a scalar selector such as `location: SOURCE` or a section with `location.target` and `location.distance`. Use top-level `offset.x/y/z` for scalar locations and `location.offset.x/y/z` for section locations. Legacy `location.offset-x/y/z` remains supported; the new keys take precedence per axis. Ability cooldowns and usage limits remain scoped to the source entity.

## Custom Power Condition

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

The base condition applies common inversion such as `not: true` after `onMatch` returns.

## Custom Power Modifier

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

The modifier base evaluates common conditions, random chance, and cooldown before `onApply`.

## Custom match rules

```java
MatchItemManager.matchItemManager.registerNewRule(new CustomItemRule());
MatchEntityManager.matchEntityManager.registerNewRule(new CustomEntityRule());
```

- Extend `AbstractMatchItemRule` and implement matching against `ObjectSingleMatchItem`.
- Extend `AbstractMatchEntityRule` and implement matching against `ObjectSingleMatchEntity`.
- `configNotContains` must return true when the rule's configuration key is absent.
- MatchEntityFormat receives a `LivingEntity`, optional player, and optional PowerContext.

## Manual trigger

A manual trigger exposes a namespaced power section without adapting a Bukkit event.

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

For a plugin named `MyPlugin`, use the trigger's namespaced key:

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

`TriggerData.builder(owner)` requires an owner. Source, skill, target, block, location, movement locations, Bukkit event, item/slot, tick, and typed extras are optional.

## Event-backed trigger

Advanced integrations may extend `AbstractTrigger<E>`, provide a unique key/configuration key, declare subscribed event classes through `getEventClasses()` (the constructor payload class is subscribed by default), and register it:

```java
EnchantmentReform.instance
        .getTriggerManager()
        .register(this, customTrigger);
```

Routing uses `dispatch(EventClass.class, event)` and matches only the explicitly supplied class; it does not automatically dispatch to superclass subscribers. Existing listeners retain their priority, cancellation checks, and routing order. Override `getEventClasses()` for multiple event classes, or return an empty set for targeted dispatch only. Use `dispatch(key, event)` to target one registered trigger. For events without a built-in listener, register your own Bukkit listener and call the class-based dispatch method. Integrations using the removed `TriggerEventType`/`getEventTypes()` API must be recompiled.

Always call `unregisterAll(plugin)` during disable.

## Lifecycle cleanup

Custom implementations may override cleanup hooks such as:

```java
public void onUnload()
public void onEntityUnload(UUID entityId)
```

Store UUIDs instead of retaining unloaded Bukkit entities. Cleanup is type-wide and should not depend on one temporary YAML section instance.

## Compatibility guidance

- Use scheduler-safe entity/block operations for Folia.
- Validate required context entities, events, items, blocks, and results before mutation.
- Do not assume Paper-only events exist on Spigot.
- Keep published type IDs stable because administrator YAML depends on them.
- Avoid replacing built-in keys.
- Recompile and retest against every supported EnchantmentReform/server generation.

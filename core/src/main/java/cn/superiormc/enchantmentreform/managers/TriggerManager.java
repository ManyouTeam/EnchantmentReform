package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.api.trigger.AbstractTrigger;
import cn.superiormc.enchantmentreform.api.trigger.ManualTrigger;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import cn.superiormc.enchantmentreform.objects.ObjectPower;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.PowerSourceDefinition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.triggers.*;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.power.PowerExecutionResult;
import cn.superiormc.enchantmentreform.power.TrackedPowerSource;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public final class TriggerManager extends AbstractManager {

    private static final String NAMESPACE = "enchantmentreform";

    public static TriggerManager triggerManager;

    private final KeyedRegistry<NamespacedKey, AbstractTrigger<?>> triggers = new KeyedRegistry<>();

    private final Map<Plugin, Set<NamespacedKey>> owners = new LinkedHashMap<>();

    private final Map<Class<?>, List<AbstractTrigger<?>>> eventTriggerMap = new LinkedHashMap<>();

    private final ActiveEnchantmentManager activeEnchantments;

    private final TriggerRuntime runtime;

    public TriggerManager() {
        triggerManager = this;
        activeEnchantments = new ActiveEnchantmentManager();
        runtime = new TriggerRuntime(this);
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        register(new TickTrigger());
        register(new TargetTickTrigger());
        register(new AttackTrigger());
        register(new MeleeAttackTrigger());
        register(new DamageTrigger());
        register(new DamageByEntityTrigger());
        register(new KillTrigger());
        register(new DeathTrigger());
        register(new ShootTrigger());
        register(new ShootBowTrigger());
        register(new ProjectileHitTrigger());
        register(new ProjectileLaunchTrigger());
        register(new ProjectileTickTrigger());
        register(new PotionEffectTrigger());
        register(new CombustTrigger());
        register(new RegainTrigger());
        register(new ExhaustionTrigger());
        register(new TargetTrigger());
        register(new UntagTrigger());
        register(new CreeperExplodeTrigger());
        register(new SpawnTrigger());
        register(new RespawnTrigger());
        register(new ActivateTrigger());
        register(new DeactivateTrigger());
        register(new BlockBreakTrigger());
        register(new BlockDamageTrigger());
        register(new BlockDropItemTrigger());
        register(new BlockPlaceTrigger());
        register(new InteractTrigger());
        register(new ConsumeTrigger());
        register(new FishTrigger());
        register(new ItemDamageTrigger());
        register(new FoodLevelChangeTrigger());
        register(new AirChangeTrigger());
        register(new ExpChangeTrigger());
        register(new ToggleFlightTrigger());
        register(new ToggleSneakTrigger());
        register(new SwapHandTrigger());
        register(new RiptideTrigger());
        register(new ItemHeldTrigger());
        register(new EnchantItemTrigger());
        register(new MoveTrigger());
        register(new InputTrigger());
        register(new ShieldBlockTrigger());
        register(new PiglinBarterTrigger());
        register(new VibrationReceiveTrigger());
        if (EnchantmentReform.methodUtil.methodID().equals("paper")) {
            register(new LoadCrossbowTrigger());
            register(new KnockbackTrigger());
            register(new EffectTickTrigger());
            register(new PhantomPreSpawnTrigger());
            register(new EndermanAttackPlayerTrigger());
            register(new WardenAngerTrigger());
            register(new JumpTrigger());
            register(new ItemGroupCooldownTrigger());
            register(new ShieldDisableTrigger());
            register(new PurchaseTrigger());
            register(new TradeTrigger());
            register(new InsideBlockTrigger());
            register(new BlockBreakProgressUpdateTrigger());
            register(new NameEntityTrigger());
            register(new ElytraBoostTrigger());
            register(new PickupExperienceTrigger());
            if (CommonUtil.getYearVersion(26, 2, 0)) {
                register(new LungeTrigger());
                register(new AttemptSmashAttackTrigger());
            }
        }
    }

    public synchronized void register(AbstractTrigger<?> trigger) {
        registerTrigger(trigger);
    }

    public synchronized ManualTrigger register(Plugin owner, String id) {
        NamespacedKey key = new NamespacedKey(owner, id);
        ManualTrigger trigger = new ManualTrigger(key, key.toString());
        register(owner, trigger);
        return trigger;
    }

    public synchronized void register(Plugin owner, AbstractTrigger<?> trigger) {
        if (owner == null) {
            throw new IllegalArgumentException("owner cannot be null");
        }
        registerTrigger(trigger);
        owners.computeIfAbsent(owner, ignored -> new LinkedHashSet<>()).add(trigger.key());
    }

    public synchronized Optional<AbstractTrigger<?>> get(String id) {
        return get(key(id));
    }

    public synchronized Optional<AbstractTrigger<?>> get(NamespacedKey key) {
        return Optional.ofNullable(triggers.get(key));
    }

    public synchronized Collection<AbstractTrigger<?>> values() {
        return triggers.values();
    }

    public synchronized void unregisterAll(Plugin owner) {
        Set<NamespacedKey> keys = owners.remove(owner);
        if (keys == null) return;
        keys.forEach(triggers::remove);
        rebuildEventTriggerMap();
    }

    public void dispatch(String id, Object event) {
        dispatch(key(id), event);
    }

    public void dispatch(NamespacedKey key, Object event) {
        AbstractTrigger<?> trigger;
        synchronized (this) {
            trigger = triggers.get(key);
        }
        if (trigger != null) {
            trigger.dispatch(event, runtime);
        }
    }

    /** Dispatches only to subscribers of this exact class, preserving listener routing and order. */
    public <E> void dispatch(Class<E> eventClass, E event) {
        java.util.Objects.requireNonNull(eventClass, "eventClass");
        eventClass.cast(java.util.Objects.requireNonNull(event, "event"));
        List<AbstractTrigger<?>> eventTriggers;
        synchronized (this) {
            eventTriggers = List.copyOf(eventTriggerMap.getOrDefault(eventClass, List.of()));
        }
        for (AbstractTrigger<?> trigger : eventTriggers) {
            trigger.dispatch(event, runtime);
        }
    }

    public TriggerResult fire(AbstractTrigger<?> trigger, TriggerData data) {
        validate(trigger, data);
        TriggerResult result = new TriggerResult();
        List<ActivePowerSource> activePowers = isTrigger(trigger, "interact")
                ? activeEnchantments.findActiveForInteraction(data)
                : activeEnchantments.findActive(data);
        Map<PowerSourceDefinition, Boolean> duplicateGates = new IdentityHashMap<>();
        Map<PowerSourceDefinition, Integer> gateLevels = new IdentityHashMap<>();
        for (ActivePowerSource active : activePowers) {
            if (canAttempt(trigger, data, active)) {
                gateLevels.merge(active.source(), active.level(), Math::max);
            }
        }
        for (ActivePowerSource active : activePowers) {
            execute(trigger, data, active, result, duplicateGates,
                    gateLevels.getOrDefault(active.source(), active.level()));
        }
        handleSkills(trigger, data, result);
        result.apply(data);
        return result;
    }

    public TriggerResult fireActive(AbstractTrigger<?> trigger, TriggerData data, ActivePowerSource active) {
        if (active == null) {
            throw new IllegalArgumentException("active power source cannot be null");
        }
        validate(trigger, data);
        TriggerResult result = new TriggerResult();
        execute(trigger, data, active, result, new IdentityHashMap<>(), active.level());
        result.apply(data);
        return result;
    }

    /** Fires a trigger against sources captured at projectile launch instead of current equipment. */
    public TriggerResult fireTracked(AbstractTrigger<?> trigger, TriggerData data,
                                     Collection<TrackedPowerSource> trackedSources) {
        validate(trigger, data);
        TriggerResult result = new TriggerResult();
        List<ActivePowerSource> activePowers = trackedSources == null ? List.of() : trackedSources.stream()
                .filter(java.util.Objects::nonNull)
                .map(TrackedPowerSource::active)
                .toList();
        Map<PowerSourceDefinition, Boolean> duplicateGates = new IdentityHashMap<>();
        Map<PowerSourceDefinition, Integer> gateLevels = new IdentityHashMap<>();
        for (ActivePowerSource active : activePowers) {
            if (canAttempt(trigger, data, active)) {
                gateLevels.merge(active.source(), active.level(), Math::max);
            }
        }
        for (ActivePowerSource active : activePowers) {
            execute(trigger, data, active, result, duplicateGates,
                    gateLevels.getOrDefault(active.source(), active.level()));
        }
        handleSkills(trigger, data, result);
        result.apply(data);
        return result;
    }

    public List<TrackedPowerSource> captureProjectileSources(TriggerData data) {
        if (data == null) {
            return List.of();
        }
        return activeEnchantments.findActive(data).stream()
                .filter(active -> active.power() != null)
                .filter(active -> active.power().getSection("on-projectile-tick") != null
                        || active.power().getSection("on-projectile-hit") != null)
                .map(TrackedPowerSource::from)
                .toList();
    }

    public ActiveEnchantmentManager activeEnchantments() {
        return activeEnchantments;
    }

    public TriggerRuntime runtime() {
        return runtime;
    }

    public void close() {
        runtime.close();
        activeEnchantments.clearAll();
        triggers.clear();
        synchronized (this) {
            owners.clear();
            eventTriggerMap.clear();
        }
        triggerManager = null;
    }

    @Override
    public void onPluginDisable() {
        close();
    }

    private void registerOptional(String eventClassName, String triggerClassName) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Class.forName(eventClassName, false, classLoader);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return;
        }

        try {
            Class<?> triggerClass = Class.forName(triggerClassName, true, classLoader);
            AbstractTrigger<?> trigger = (AbstractTrigger<?>) triggerClass
                    .getDeclaredConstructor()
                    .newInstance();
            register(trigger);
        } catch (ReflectiveOperationException | LinkageError | ClassCastException exception) {
            EnchantmentReform.instance.getLogger().log(
                    Level.WARNING,
                    "Could not register optional trigger " + triggerClassName,
                    exception);
        }
    }

    private void registerTrigger(AbstractTrigger<?> trigger) {
        // Validate all subscriptions before mutating the registry or index.
        Set<? extends Class<?>> eventClasses = Set.copyOf(trigger.getEventClasses());
        for (Class<?> eventClass : eventClasses) {
            if (!trigger.eventClass().isAssignableFrom(eventClass)) {
                throw new IllegalArgumentException("Incompatible event subscription: " + eventClass.getName());
            }
        }
        triggers.register(trigger.key(), trigger);
        for (Class<?> eventClass : eventClasses) {
            eventTriggerMap.computeIfAbsent(eventClass, ignored -> new ArrayList<>()).add(trigger);
        }
    }

    private void rebuildEventTriggerMap() {
        eventTriggerMap.clear();
        for (AbstractTrigger<?> trigger : triggers.values()) {
            for (Class<?> eventClass : trigger.getEventClasses()) {
                eventTriggerMap.computeIfAbsent(eventClass, ignored -> new ArrayList<>()).add(trigger);
            }
        }
    }

    private void validate(AbstractTrigger<?> trigger, TriggerData data) {
        if (trigger == null || data == null || !triggers.contains(trigger.key())) {
            throw new IllegalArgumentException("Unregistered trigger: " + trigger);
        }
    }

    private void execute(AbstractTrigger<?> trigger, TriggerData data,
                         ActivePowerSource active, TriggerResult result,
                         Map<PowerSourceDefinition, Boolean> duplicateGates,
                         int gateLevel) {
        if (!canAttempt(trigger, data, active)) {
            return;
        }
        ObjectPower power = active.power();
        PowerContext context = new PowerContext(
                power, active.level(), trigger, data, active.item(), active.slot(), result);
        if (active.source() instanceof PowerEnchantmentDefinition enchantment
                && !enchantment.meetsConditions(data.player(), context)) {
            return;
        }
        if (!power.isEnabled() || !power.meetsTriggerConditions(context)) {
            return;
        }
        Entity skill = data.skillEntity() == null ? data.player() : data.skillEntity();
        boolean allowed;
        if (active.source().isDuplicateAllowed()) {
            Boolean cached = duplicateGates.get(active.source());
            if (cached == null) {
                cached = power.willUseThisPower(
                        data.player(), skill, gateLevel, trigger.configKey());
                duplicateGates.put(active.source(), cached);
            }
            allowed = cached;
        } else {
            allowed = power.willUseThisPower(
                    data.player(), skill, active.level(), trigger.configKey());
        }
        if (!allowed) {
            return;
        }

        PowerExecutionResult execution = power.executeTrigger(context);
        if (!execution.executed()) {
            return;
        }
        result.markExecuted(active);
        if (execution.cancelled()) {
            result.cancel();
        }
    }

    private boolean canAttempt(AbstractTrigger<?> trigger,
                               TriggerData data,
                               ActivePowerSource active) {
        ObjectPower power = active.power();
        return power != null
                && power.getSection(trigger.configKey()) != null
                && (!isTrigger(trigger, "interact") || matchesInteractionHand(data, active))
                && (!isTickTrigger(trigger)
                || isScheduledTick(power, trigger, active.level(), data.tick()));
    }

    private boolean matchesInteractionHand(TriggerData data, ActivePowerSource active) {
        if (active.slot() == null) {
            return true;
        }
        EquipmentSlot eventHand = data.triggerItemSlot() == EquipmentSlot.OFF_HAND
                ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
        EquipmentSlot activeHand = active.slot() == EquipmentSlot.OFF_HAND
                ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
        return eventHand == activeHand;
    }

    private void handleSkills(AbstractTrigger<?> trigger, TriggerData data, TriggerResult result) {
        if (SkillManager.skillManager != null) {
            SkillManager.skillManager.handleTrigger(trigger, data, result);
        }
    }

    private boolean isTickTrigger(AbstractTrigger<?> trigger) {
        return isTrigger(trigger, "tick") || isTrigger(trigger, "target_tick");
    }

    private boolean isTrigger(AbstractTrigger<?> trigger, String id) {
        return trigger.key().equals(key(id));
    }

    private boolean isScheduledTick(ObjectPower power, AbstractTrigger<?> trigger, int level, long tick) {
        int interval = Math.max(1, power.getInt(trigger.configKey() + ".interval", 20, level));
        return tick % interval == 0L;
    }

    private static NamespacedKey key(String id) {
        return new NamespacedKey(NAMESPACE, id);
    }
}

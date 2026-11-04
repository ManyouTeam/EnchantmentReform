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
import cn.superiormc.enchantmentreform.objects.triggers.ActivateTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.AirChangeTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.AttackTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.BlockBreakTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.BlockDamageTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.BlockDropItemTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.BlockPlaceTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.BreedTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.CombustTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ConsumeTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.CreeperExplodeTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.DamageByEntityTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.DamageTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.DeactivateTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.DeathTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.EnchantItemTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ExpChangeTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.FishTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.FoodLevelChangeTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.InputTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.InteractTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ItemDamageTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ItemHeldTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.KillTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.MeleeAttackTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.MoveTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.PiglinBarterTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.PotionEffectTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ProjectileHitTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ProjectileLaunchTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ProjectileTickTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.RegainTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.RespawnTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.RiptideTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ShearTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ShieldBlockTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ShootBowTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ShootTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.SpawnTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.SwapHandTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.TargetTickTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.TargetTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.TickTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ToggleFlightTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ToggleSneakTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.BrewTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.CookTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.GainLevelTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ItemBrokenTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.TameTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.CraftItemTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.FertilizeBlockTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.ForgeItemTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.GrindstoneItemTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.MilkMobTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.AnvilRepairTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.AnvilEnchantTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.AnvilRenameTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.HoneyExtractTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.TriggerRuntime;
import cn.superiormc.enchantmentreform.objects.triggers.UntagTrigger;
import cn.superiormc.enchantmentreform.objects.triggers.VibrationReceiveTrigger;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import cn.superiormc.enchantmentreform.power.PowerExecutionResult;
import cn.superiormc.enchantmentreform.power.TrackedPowerSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public final class TriggerManager extends AbstractManager {

    private static final String NAMESPACE = "enchantmentreform";

    public static TriggerManager triggerManager;

    private final KeyedRegistry<NamespacedKey, AbstractTrigger<?>> triggers = new KeyedRegistry<>();

    private final Map<Plugin, Set<NamespacedKey>> owners = new LinkedHashMap<>();

    private final Map<Class<?>, List<AbstractTrigger<?>>> eventTriggers = new LinkedHashMap<>();

    private final ActiveEnchantmentManager activeEnchantments;

    private final TriggerRuntime runtime;

    private volatile boolean closed;

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
        register(new ShearTrigger());
        register(ShearTrigger.cutLeash());
        register(new BreedTrigger());
        register(new TameTrigger());
        register(new GainLevelTrigger());
        register(new ItemBrokenTrigger());
        register(new BrewTrigger());
        register(new CookTrigger());
        register(new AnvilRepairTrigger());
        register(new AnvilEnchantTrigger());
        register(new AnvilRenameTrigger());
        register(new HoneyExtractTrigger());
        register(new CraftItemTrigger());
        register(new FertilizeBlockTrigger());
        register(new ForgeItemTrigger());
        register(new GrindstoneItemTrigger());
        register(new MilkMobTrigger());
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
        registerOptional("ExhaustionTrigger", true);
        boolean paper = EnchantmentReform.methodUtil != null && "paper".equals(EnchantmentReform.methodUtil.methodID());
        registerOptional("LoadCrossbowTrigger", paper);
        registerOptional("HoneyCombExtractTrigger", paper);
        registerOptional("KnockbackTrigger", paper);
        registerOptional("EffectTickTrigger", paper);
        registerOptional("PhantomPreSpawnTrigger", paper);
        registerOptional("EndermanAttackPlayerTrigger", paper);
        registerOptional("WardenAngerTrigger", paper);
        registerOptional("JumpTrigger", paper);
        registerOptional("ItemGroupCooldownTrigger", paper);
        registerOptional("ShieldDisableTrigger", paper);
        registerOptional("PurchaseTrigger", paper);
        registerOptional("TradeTrigger", paper);
        registerOptional("InsideBlockTrigger", paper);
        registerOptional("BlockBreakProgressUpdateTrigger", paper);
        registerOptional("NameEntityTrigger", paper);
        registerOptional("ElytraBoostTrigger", paper);
        registerOptional("PickupExperienceTrigger", paper);
        registerOptional("LungeTrigger", paper);
        registerOptional("AttemptSmashAttackTrigger", paper);
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
        return List.copyOf(triggers.values());
    }

    public synchronized void unregisterAll(Plugin owner) {
        Set<NamespacedKey> keys = owners.remove(owner);
        if (keys == null) {
            return;
        }
        keys.forEach(triggers::remove);
        rebuildEventTriggers();
    }

    public void dispatch(String id, Object event) {
        dispatch(key(id), event);
    }

    public void dispatch(NamespacedKey key, Object event) {
        if (closed) {
            return;
        }
        get(key).ifPresent(trigger -> trigger.dispatch(event, runtime));
    }

    /** Dispatches only to subscribers of this exact class, preserving listener routing and order. */
    public <E> void dispatch(Class<E> eventClass, E event) {
        Objects.requireNonNull(eventClass, "eventClass");
        eventClass.cast(Objects.requireNonNull(event, "event"));
        if (closed) {
            return;
        }
        List<AbstractTrigger<?>> selected;
        synchronized (this) {
            selected = List.copyOf(eventTriggers.getOrDefault(eventClass, List.of()));
        }
        for (AbstractTrigger<?> trigger : selected) {
            trigger.dispatch(event, runtime);
        }
    }

    public TriggerResult fire(AbstractTrigger<?> trigger, TriggerData data) {
        if (closed) {
            return new TriggerResult();
        }
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
        if (closed) {
            return new TriggerResult();
        }
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
        if (closed) {
            return new TriggerResult();
        }
        validate(trigger, data);
        TriggerResult result = new TriggerResult();
        List<ActivePowerSource> activePowers = trackedSources == null ? List.of() : trackedSources.stream()
                .filter(Objects::nonNull)
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

    public synchronized Set<Class<?>> eventClasses() {
        return new LinkedHashSet<>(eventTriggers.keySet());
    }

    public TriggerRuntime runtime() {
        return runtime;
    }

    public synchronized void close() {
        closed = true;
        runtime.close();
        activeEnchantments.clearAll();
        triggers.clear();
        owners.clear();
        eventTriggers.clear();
        triggerManager = null;
    }

    @Override
    public void onPluginDisable() {
        close();
    }

    private void registerOptional(String name, boolean enabled) {
        if (!enabled) {
            return;
        }
        try {
            Class<?> type = Class.forName("cn.superiormc.enchantmentreform.objects.triggers." + name);
            register((AbstractTrigger<?>) type.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException | NoClassDefFoundError exception) {
            // Optional events are absent on older server APIs.
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (exception.getCause() instanceof NoClassDefFoundError || exception.getCause() instanceof ClassNotFoundException) {
                return;
            }
            if (ErrorManager.errorManager != null) {
                ErrorManager.errorManager.sendErrorMessage("§cError: Could not register trigger " + name + ": " + exception.getMessage());
            }
        }
    }

    private void registerTrigger(AbstractTrigger<?> trigger) {
        if (closed) {
            throw new IllegalStateException("Trigger manager is closed");
        }
        Set<? extends Class<?>> classes = Set.copyOf(trigger.getEventClasses());
        for (Class<?> eventClass : classes) {
            if (!trigger.eventClass().isAssignableFrom(eventClass)) {
                throw new IllegalArgumentException("Incompatible event subscription: " + eventClass.getName());
            }
        }
        triggers.register(trigger.key(), trigger);
        for (Class<?> eventClass : classes) {
            eventTriggers.computeIfAbsent(eventClass, ignored -> new ArrayList<>()).add(trigger);
        }
    }

    private void rebuildEventTriggers() {
        eventTriggers.clear();
        for (AbstractTrigger<?> trigger : triggers.values()) {
            for (Class<?> eventClass : trigger.getEventClasses()) {
                eventTriggers.computeIfAbsent(eventClass, ignored -> new ArrayList<>()).add(trigger);
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
        if (!power.isEnabled()) {
            return;
        }
        context = power.prepareTrigger(context);
        if (context == null) {
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
        Objects.requireNonNull(id, "id");
        String normalized = id.contains(":") ? id : NAMESPACE + ":" + id;
        NamespacedKey key = NamespacedKey.fromString(normalized);
        if (key == null) {
            throw new IllegalArgumentException("Invalid trigger id: " + id);
        }
        return key;
    }
}

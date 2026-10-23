package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.AntiAbuseManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import cn.superiormc.enchantmentreform.managers.PowerModifiersManager;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import cn.superiormc.enchantmentreform.objects.counting.CountBatch;
import cn.superiormc.enchantmentreform.objects.counting.TriggerCounter;
import cn.superiormc.enchantmentreform.power.PowerExecutionResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class ObjectPower extends AbstractConfiguredSection<PowerContext> {

    protected boolean enabled;

    private final PowerVariables variables;

    private record TriggerCounting(TriggerCounter counter, CountBatch batches) { }

    private final Map<String, TriggerCounting> counters = new ConcurrentHashMap<>();

    public ObjectPower(String id, ConfigurationSection section) {
        this(id, section, PowerVariables.from(section.getConfigurationSection("variables")));
    }

    public ObjectPower(String id, ConfigurationSection section, PowerVariables variables) {
        super(id, section);
        enabled = section.getBoolean("enabled", true);
        this.variables = variables == null ? PowerVariables.empty() : variables;
        for (String key : section.getKeys(false)) {
            ConfigurationSection configured = section.getConfigurationSection(key);
            if (key.startsWith("on-") && configured != null) {
                counters.put(key, new TriggerCounting(new TriggerCounter(configured,
                        "enchantmentreform:" + key.substring(3).replace('-', '_')), new CountBatch(configured, true)));
            }
        }
    }

    public PowerExecutionResult executeTrigger(PowerContext context) {
        String trigger = context.trigger().configKey();
        ConfigurationSection triggerSection = section.getConfigurationSection(trigger);
        if (triggerSection == null) {
            return PowerExecutionResult.SKIPPED;
        }
        PowerModifiersManager.powerModifiers.apply(
                triggerSection.getConfigurationSection("modifiers"), context);
        return new PowerExecutionResult(true, AbilityManager.abilityManager.execute(
                triggerSection.getConfigurationSection("abilities"), context));
    }

    /** Checks trigger conditions and claims the shared anti-abuse gate before power limits. */
    public boolean meetsTriggerConditions(PowerContext context) {
        ConfigurationSection triggerSection = section.getConfigurationSection(
                context.trigger().configKey());
        return triggerSection != null
                && PowerConditionsManager.powerConditions.matches(
                triggerSection.getConfigurationSection("conditions"), context)
                && (AntiAbuseManager.antiAbuseManager == null
                || AntiAbuseManager.antiAbuseManager.allows(
                triggerSection.getConfigurationSection("anti-abuse"), context,
                "power/" + id + "/" + context.trigger().key(), false));
    }

    /** Count matching quantities before applying the power's chance, cooldown, and use limits. */
    public PowerContext prepareTrigger(PowerContext context) {
        ConfigurationSection configured = section.getConfigurationSection(context.trigger().configKey());
        if (configured == null) {
            return null;
        }
        TriggerCounting counting = counters.computeIfAbsent(context.trigger().configKey(), key ->
                new TriggerCounting(new TriggerCounter(configured, context.trigger().key().toString()),
                        new CountBatch(configured, true)));
        var quantity = counting.counter().measure(context, value -> PowerConditionsManager.powerConditions.matches(
                configured.getConfigurationSection("conditions"), value));
        if (quantity.signum() <= 0 || AntiAbuseManager.antiAbuseManager != null
                && !AntiAbuseManager.antiAbuseManager.allows(configured.getConfigurationSection("anti-abuse"),
                context, "power/" + id + "/" + context.trigger().key(), false)) {
            return null;
        }
        return counting.batches().settle(context, quantity);
    }

    public void clearCounting(UUID player) {
        counters.values().forEach(counting -> counting.batches().clear(player));
    }

    public boolean willUseThisPower(Entity entity, Entity skillEntity, int level, String eventKey) {
        if (!enabled || (eventKey != null && !eventKey.isEmpty() && !section.contains(eventKey))) {
            return false;
        }
        if (skillEntity != null && PowerManager.powerManager.isUsedPower(skillEntity)) {
            return true;
        }
        PowerStateStore.Key timesKey = PowerStateStore.key(entity, id, "power-times", "limit", null);
        PowerStateStore.Key cooldownKey = PowerStateStore.key(entity, id, "power-cooldown", "limit", null);
        int maxTimes = Math.max(0, getInt("limit.times", 0, level));
        if (maxTimes > 0 && PowerStateStore.get(timesKey) >= maxTimes) {
            return false;
        }
        if (PowerStateStore.get(cooldownKey) > 0.0D) {
            return false;
        }
        double randomChance = getDouble("limit.random", 1.0D, level);
        if (randomChance < 1.0D && ThreadLocalRandom.current().nextDouble() > randomChance) {
            return false;
        }
        double cooldown = getDouble("limit.cooldown", 0.0D, level);
        if (cooldown > 0.0D && !PowerStateStore.tryAcquireCooldown(cooldownKey, cooldown)) {
            return false;
        }
        if (maxTimes > 0) {
            PowerStateStore.update(timesKey, value -> value + 1.0D, maxTimes, 0.0D);
        }
        return true;
    }

    public String getPlaceholder() {
        return section.getString("placeholder", id);
    }

    public String getVariable(String name, int level) {
        return variables.resolve(name, level);
    }

    @Override
    protected String resolvePowerVariable(String name, PowerContext context, int level) {
        String contextual = super.resolvePowerVariable(name, context, level);
        return contextual == null ? variables.resolve(name, level) : contextual;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

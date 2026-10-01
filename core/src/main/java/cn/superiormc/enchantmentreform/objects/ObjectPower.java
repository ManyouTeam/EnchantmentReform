package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.AntiAbuseManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import cn.superiormc.enchantmentreform.managers.PowerModifiersManager;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import cn.superiormc.enchantmentreform.power.PowerExecutionResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.concurrent.ThreadLocalRandom;

public class ObjectPower extends AbstractConfiguredSection<PowerContext> {

    protected boolean enabled;

    private final PowerVariables variables;

    public ObjectPower(String id, ConfigurationSection section) {
        this(id, section, PowerVariables.from(section.getConfigurationSection("variables")));
    }

    public ObjectPower(String id, ConfigurationSection section, PowerVariables variables) {
        super(id, section);
        enabled = section.getBoolean("enabled", true);
        this.variables = variables == null ? PowerVariables.empty() : variables;
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

package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractPowerModifier extends AbstractConfiguredSection<PowerContext> {

    protected AbstractPowerModifier(String type, ConfigurationSection section) {
        super(type, section);
    }

    public final void apply(PowerContext context) {
        if (shouldApply(context)) {
            onApply(context);
        }
    }

    protected abstract void onApply(PowerContext context);

    /** Cleans type-wide runtime state. Implementations must not depend on this instance's section. */
    public void onUnload() {
    }

    /** Cleans type-wide runtime state associated with one entity. */
    public void onEntityUnload(UUID entityId) {
    }

    protected final double modifyValue(PowerContext context, double original) {
        double operand = getDouble("value", original, context, "original", String.valueOf(original));
        return switch (section.getString("operation", "SET").toUpperCase()) {
            case "ADD" -> original + operand;
            case "SUBTRACT" -> original - operand;
            case "MULTIPLY" -> original * operand;
            case "DIVIDE" -> operand == 0.0D ? original : original / operand;
            case "MIN" -> Math.min(original, operand);
            case "MAX" -> Math.max(original, operand);
            default -> operand;
        };
    }

    private boolean shouldApply(PowerContext context) {
        if (!PowerConditionsManager.powerConditions.matches(section.getConfigurationSection("conditions"), context)) {
            return false;
        }
        double chance = getDouble("random", 1.0D, context);
        if (chance < 1.0D && ThreadLocalRandom.current().nextDouble() > Math.max(0.0D, chance)) {
            return false;
        }
        double seconds = getDouble("cooldown", 0.0D, context);
        if (seconds <= 0.0D || context.source() == null) {
            return true;
        }
        PowerStateStore.Key key = PowerStateStore.key(context, "modifier-cooldown",
                section.getCurrentPath(), false);
        return PowerStateStore.tryAcquireCooldown(key, seconds);
    }
}

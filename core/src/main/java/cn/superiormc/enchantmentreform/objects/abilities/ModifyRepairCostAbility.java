package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/**
 * Activation-only ability that changes the experience-level cost calculated by an anvil.
 * It is evaluated by {@code AnvilListener}; normal trigger execution does not run it.
 */
public final class ModifyRepairCostAbility extends AbstractAbility {

    public ModifyRepairCostAbility(ConfigurationSection section) {
        super("ModifyRepairCost", section);
    }

    /** Applies this configured operation to the current anvil repair cost. */
    public int modify(int current, PowerContext context) {
        double original = Math.max(0, current);
        double operand = getDouble(
                "value",
                original,
                context,
                "original", Double.toString(original),
                "current", Double.toString(original));

        String configuredOperation = section.getString("operation", "SET");
        String operation = configuredOperation == null
                ? "SET"
                : configuredOperation.trim().toUpperCase(Locale.ROOT);

        double result = switch (operation) {
            case "ADD" -> original + operand;
            case "SUBTRACT" -> original - operand;
            case "MULTIPLY" -> original * operand;
            case "DIVIDE" -> operand == 0.0D ? original : original / operand;
            case "MIN" -> Math.min(original, operand);
            case "MAX" -> Math.max(original, operand);
            default -> operand;
        };

        if (!Double.isFinite(result) || result <= 0.0D) {
            return 0;
        }
        if (result >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(result);
    }

    @Override
    public boolean execute(PowerContext context) {
        // This ability is evaluated while preparing an anvil result.
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }
}

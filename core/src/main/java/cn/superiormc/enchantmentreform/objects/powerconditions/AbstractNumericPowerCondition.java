package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;

abstract class AbstractNumericPowerCondition extends AbstractPowerCondition {

    protected AbstractNumericPowerCondition(String type) {
        super(type);
    }

    @Override
    protected final boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }
        Double current = currentValue(context, condition);
        if (current == null) {
            return false;
        }
        if (condition.contains("value") || condition.contains("compare")) {
            double target = condition.getDouble("value", current, context,
                    "original", String.valueOf(current), "current", String.valueOf(current));
            if (!compare(current, target, condition.getString("compare", ">="))) {
                return false;
            }
        }
        double minimum = condition.getDouble("min", -Double.MAX_VALUE, context,
                "original", String.valueOf(current), "current", String.valueOf(current));
        double maximum = condition.getDouble("max", Double.MAX_VALUE, context,
                "original", String.valueOf(current), "current", String.valueOf(current));
        return current >= minimum && current <= maximum;
    }

    protected abstract Double currentValue(PowerContext context, ObjectSingleCondition condition);

    private boolean compare(double current, double target, String rawOperator) {
        String operator = rawOperator == null ? ">=" : rawOperator.trim().toLowerCase();
        return switch (operator) {
            case ">", "gt" -> current > target;
            case ">=", "=>", "gte" -> current >= target;
            case "<", "lt" -> current < target;
            case "<=", "=<", "lte" -> current <= target;
            case "!=", "<>", "ne" -> current != target;
            case "=", "==", "eq" -> current == target;
            default -> current >= target;
        };
    }
}

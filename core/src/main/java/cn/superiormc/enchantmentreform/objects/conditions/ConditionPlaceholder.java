package cn.superiormc.enchantmentreform.objects.conditions;

import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.entity.Player;

public class ConditionPlaceholder<S extends AbstractConfiguredSection<C>, C> extends AbstractCheckCondition<S, C> {

    public ConditionPlaceholder() {
        super("placeholder");
        setRequiredArgs("placeholder", "rule", "value");
    }

    @Override
    protected boolean onCheckCondition(S singleCondition, Player player, C context) {
        String placeholder = singleCondition.getString("placeholder", player, context);
        String value = singleCondition.getString("value", player, context);
        try {
            switch (singleCondition.getString("rule")) {
                case ">=":
                    return Double.parseDouble(placeholder) >= Double.parseDouble(value);
                case ">":
                    return Double.parseDouble(placeholder) > Double.parseDouble(value);
                case "=":
                    return Double.parseDouble(placeholder) == Double.parseDouble(value);
                case "<":
                    return Double.parseDouble(placeholder) < Double.parseDouble(value);
                case "<=":
                    return Double.parseDouble(placeholder) <= Double.parseDouble(value);
                case "==":
                    return placeholder.equals(value);
                case "!=":
                    return !placeholder.equals(value);
                case "*=":
                    return placeholder.contains(value);
                case "=*":
                    return value.contains(placeholder);
                case "!*=":
                    return !placeholder.contains(value);
                case "!=*":
                    return !value.contains(placeholder);
                default:
                    ErrorManager.errorManager.sendErrorMessage("§cError: Your placeholder condition can not being correctly load.");
                    return true;
            }
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Your placeholder condition can not being correctly load.");
            return true;
        }
    }
}

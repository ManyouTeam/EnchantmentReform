package cn.superiormc.enchantmentreform.objects.conditions;

import cn.superiormc.enchantmentreform.objects.AbstractConfiguredSection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ConditionAny<S extends AbstractConfiguredSection<C>, C> extends AbstractCheckCondition<S, C> {

    private final ConditionChecker<S, C> checker;

    public ConditionAny(ConditionChecker<S, C> checker) {
        super("any");
        this.checker = checker;
        setRequiredArgs("conditions");
    }

    @Override
    protected boolean onCheckCondition(S singleCondition, Player player, C context) {
        ConfigurationSection anySection = singleCondition.getSection().getConfigurationSection("conditions");
        if (anySection == null) {
            return true;
        }
        return checker.check(anySection, singleCondition, player, context);
    }

    @FunctionalInterface
    public interface ConditionChecker<S, C> {
        boolean check(ConfigurationSection section, S singleCondition, Player player, C context);
    }
}

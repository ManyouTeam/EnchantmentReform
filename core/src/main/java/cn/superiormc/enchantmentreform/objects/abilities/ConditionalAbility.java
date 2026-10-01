package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import org.bukkit.configuration.ConfigurationSection;

public class ConditionalAbility extends AbstractAbility {

    public ConditionalAbility(ConfigurationSection section) {
        super("Conditional", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection cases = section.getConfigurationSection("cases");
        boolean trueOrFalse = false;
        boolean matched = false;
        if (cases != null) {
            for (String key : cases.getKeys(false)) {
                ConfigurationSection single = cases.getConfigurationSection(key);
                if (single == null) {
                    continue;
                }
                if (!PowerConditionsManager.powerConditions.matches(
                        single.getConfigurationSection("conditions"), context)) {
                    continue;
                }
                matched = true;
                if (AbilityManager.abilityManager.execute(single.getConfigurationSection("abilities"), context)) {
                    trueOrFalse = true;
                }
            }
        }

        ConfigurationSection elseAbilities = section.getConfigurationSection("else-abilities");
        if (!matched && elseAbilities != null) {
            if (AbilityManager.abilityManager.execute(elseAbilities, context)) {
                trueOrFalse = true;
            }
        }
        return trueOrFalse;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }
}

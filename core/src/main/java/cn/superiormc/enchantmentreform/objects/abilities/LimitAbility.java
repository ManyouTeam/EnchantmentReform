package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import org.bukkit.configuration.ConfigurationSection;

public class LimitAbility extends AbstractAbility {

    public LimitAbility(ConfigurationSection section) {
        super("Limit", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection abilities = section.getConfigurationSection("abilities");
        if (abilities == null) {
            return false;
        }
        return AbilityManager.abilityManager.execute(abilities, context);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

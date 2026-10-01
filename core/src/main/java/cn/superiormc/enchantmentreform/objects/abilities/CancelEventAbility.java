package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;

public class CancelEventAbility extends AbstractAbility {

    public CancelEventAbility(ConfigurationSection section) {
        super("CancelEvent", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        return true;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return null;
    }
}

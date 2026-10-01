package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ExecuteActionAbility extends AbstractAbility {

    public ExecuteActionAbility(ConfigurationSection section) {
        super("ExecuteAction", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity recipient = getTargetEntity(context);
        ConfigurationSection actions = section.getConfigurationSection("actions");
        if (!(recipient instanceof Player player) || actions == null) {
            return false;
        }
        AbilityManager.abilityManager.executeActions(actions, context.withPlayer(player));
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ExperienceAbility extends AbstractAbility {

    public ExperienceAbility(ConfigurationSection section) {
        super("Experience", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof Player player)) {
            return false;
        }
        int amount = getInt("amount", 1, context);
        String mode = getString("mode", "POINTS", context);
        if (mode.equalsIgnoreCase("LEVEL") || mode.equalsIgnoreCase("LEVELS")) {
            player.giveExpLevels(amount);
        } else {
            player.giveExp(amount);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

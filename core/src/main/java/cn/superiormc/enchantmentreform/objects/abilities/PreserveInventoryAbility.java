package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PreserveInventoryAbility extends AbstractAbility {

    public PreserveInventoryAbility(ConfigurationSection section) {
        super("PreserveInventory", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.event() instanceof PlayerDeathEvent event)) {
            return false;
        }
        if (section.getBoolean("inventory", true)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }
        if (section.getBoolean("experience", true)) {
            event.setKeepLevel(true);
            context.result().experienceAmount(0);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

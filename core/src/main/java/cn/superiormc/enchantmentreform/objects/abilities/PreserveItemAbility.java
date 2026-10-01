package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class PreserveItemAbility extends AbstractAbility {

    public PreserveItemAbility(ConfigurationSection section) {
        super("PreserveItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.source() instanceof Player player) || !(context.event() instanceof PlayerDeathEvent event)
                || context.item() == null) {
            return false;
        }
        ItemStack preserved = context.item().clone();
        event.getDrops().removeIf(drop -> drop.isSimilar(preserved));
        SchedulerUtil.runTaskLater(player, () -> {
            var leftovers = player.getInventory().addItem(preserved);
            for (ItemStack item : leftovers.values()) player.getWorld().dropItemNaturally(player.getLocation(), item);
        }, 1L);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

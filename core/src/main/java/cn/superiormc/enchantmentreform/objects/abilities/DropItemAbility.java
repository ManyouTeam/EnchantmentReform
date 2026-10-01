package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public final class DropItemAbility extends AbstractAbility {

    public DropItemAbility(ConfigurationSection section) {
        super("DropItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location location = getLocation(context);
        if (location == null || location.getWorld() == null) {
            return false;
        }

        ItemStack selected = getItem(context);
        if (selected == null || selected.getType().isAir() || selected.getAmount() <= 0) {
            return false;
        }
        int amount = requestedAmount(context, selected.getAmount());
        ItemStack item = selected.clone();
        item.setAmount(amount);
        selected.setAmount(selected.getAmount() - amount);

        Item dropped = section.getBoolean("naturally", true)
                ? location.getWorld().dropItemNaturally(location, item)
                : location.getWorld().dropItem(location, item);
        if (section.contains("pickup-delay")) {
            dropped.setPickupDelay(Math.max(0, getInt("pickup-delay", 0, context)));
        }
        return false;
    }

    private int requestedAmount(PowerContext context, int available) {
        if (!section.contains("amount")) {
            return available;
        }
        return Math.max(1, Math.min(available, getInt("amount", available, context)));
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/** Builds an ItemFormat item and either gives it to a player or drops it. */
public final class GiveItemAbility extends AbstractAbility {

    public GiveItemAbility(ConfigurationSection section) {
        super("GiveItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        String delivery = getString("delivery", "INVENTORY", context).trim().toUpperCase(Locale.ROOT);
        Entity target = getTargetEntity(context);
        Player player = target instanceof Player selected ? selected : context.player();
        if (!delivery.equals("DROP") && !(target instanceof Player)) {
            return false;
        }
        ConfigurationSection itemSection = section.getConfigurationSection("item");
        if (itemSection == null) {
            return false;
        }
        ItemStack built = BuildItem.buildItemStack(player, itemSection);
        if (built == null || built.getType().isAir()) {
            return false;
        }
        ItemStack item = built.clone();
        item.setAmount(Math.max(1, getInt("amount", item.getAmount(), context)));

        if (delivery.equals("DROP")) {
            return dropItem(context, item);
        }

        Player recipient = (Player) target;
        var leftovers = recipient.getInventory().addItem(item);
        if (section.getBoolean("drop-overflow", true)) {
            leftovers.values().forEach(leftover ->
                    recipient.getWorld().dropItemNaturally(recipient.getLocation(), leftover));
        }
        return false;
    }

    private boolean dropItem(PowerContext context, ItemStack item) {
        Location location = getLocation(context);
        if (location == null || location.getWorld() == null) {
            return false;
        }
        Item dropped = section.getBoolean("naturally", true)
                ? location.getWorld().dropItemNaturally(location, item)
                : location.getWorld().dropItem(location, item);
        if (section.contains("pickup-delay")) {
            dropped.setPickupDelay(Math.max(0, getInt("pickup-delay", 0, context)));
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.PLAYER;
    }
}

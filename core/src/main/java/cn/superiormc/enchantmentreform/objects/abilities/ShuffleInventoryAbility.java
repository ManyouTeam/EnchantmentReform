package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShuffleInventoryAbility extends AbstractAbility {

    public ShuffleInventoryAbility(ConfigurationSection section) {
        super("ShuffleInventory", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof Player player)) {
            return false;
        }

        PlayerInventory inv = player.getInventory();
        int start = Math.max(0, getInt("start-slot", 0, context));
        int end = Math.min(inv.getSize() - 1, getInt("end-slot", 35, context));
        if (start > end) {
            return false;
        }

        List<Integer> occupied = new ArrayList<>();
        for (int slot = start; slot <= end; slot++) {
            ItemStack item = inv.getItem(slot);
            if (!isEmpty(item)) {
                occupied.add(slot);
            }
        }
        if (occupied.size() <= 1) {
            return false;
        }

        List<ItemStack> items = new ArrayList<>();
        for (Integer slot : occupied) {
            items.add(inv.getItem(slot));
        }
        Collections.shuffle(items, ThreadLocalRandom.current());
        for (int i = 0; i < occupied.size(); i++) {
            inv.setItem(occupied.get(i), items.get(i));
        }

        return false;
    }

    private boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnhanceHeldItemAbility extends AbstractAbility {

    public EnhanceHeldItemAbility(ConfigurationSection section) {
        super("EnhanceHeldItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return false;
        }
        String hand = getString("hand", "MAIN_HAND").toUpperCase();
        ItemStack item = switch (hand) {
            case "OFF_HAND" -> equipment.getItemInOffHand();
            default -> equipment.getItemInMainHand();
        };
        if (!(equipment instanceof PlayerInventory)) {
            equipment.setItemInMainHandDropChance(0f);
            equipment.setItemInOffHandDropChance(0f);
        }
        if (item.getType().isAir()) {
            return false;
        }
        int minEnchant = getInt("enchant.min-amount", 1, context);
        int maxEnchant = getInt("enchant.max-amount", 3, context);
        int minLevel = getInt("enchant.min-level", 1, context);
        int maxLevel = getInt("enchant.max-level", 4, context);

        addRandomEnchants(item,
                randomInt(minEnchant, maxEnchant),
                minLevel,
                maxLevel);

        return false;
    }

    private void addRandomEnchants(ItemStack item, int amount, int minLevel, int maxLevel) {
        List<Enchantment> candidates = new ArrayList<>();
        for (Enchantment ench : Enchantment.values()) {
            if (ench.canEnchantItem(item)) {
                candidates.add(ench);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        for (int i = 0; i < amount && !candidates.isEmpty(); i++) {
            int index = ThreadLocalRandom.current().nextInt(candidates.size());
            Enchantment enchantment = candidates.remove(index);
            int level = randomInt(
                    minLevel,
                    Math.min(maxLevel, enchantment.getMaxLevel())
            );
            item.addUnsafeEnchantment(enchantment, level);
        }
    }

    private int randomInt(int min, int max) {
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

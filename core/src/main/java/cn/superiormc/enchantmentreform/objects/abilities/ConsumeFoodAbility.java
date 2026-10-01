package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ConsumeFoodAbility extends AbstractAbility {

    private static final NamespacedKey FOOD_REMAINDER = new NamespacedKey(EnchantmentReform.instance, "consume_food_remainder");

    public ConsumeFoodAbility(ConfigurationSection section) {
        super("ConsumeFood", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof Player player)) {
            return false;
        }

        double amount = Math.max(0.0D, getDouble("amount", 0.25D, context,
                "food", String.valueOf(player.getFoodLevel()),
                "saturation", String.valueOf(player.getSaturation())));
        if (amount <= 0.0D) {
            return false;
        }

        if (section.contains("saturation-amount") || section.contains("food-amount")) {
            consumeSaturation(player, Math.max(0.0D, getDouble("saturation-amount", 0.0D, context)));
            consumeFood(player, Math.max(0.0D, getDouble("food-amount", 0.0D, context)));
            return false;
        }

        if (getBoolean("saturation-first", true)) {
            double remaining = consumeSaturation(player, amount);
            consumeFood(player, remaining);
        } else {
            consumeFood(player, amount);
        }
        return false;
    }

    private double consumeSaturation(Player player, double amount) {
        float oldSaturation = player.getSaturation();
        float newSaturation = Math.max(0.0F, oldSaturation - (float) amount);
        player.setSaturation(newSaturation);
        return Math.max(0.0D, amount - oldSaturation);
    }

    private void consumeFood(Player player, double amount) {
        if (amount <= 0.0D) {
            return;
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        double total = amount + pdc.getOrDefault(FOOD_REMAINDER, PersistentDataType.DOUBLE, 0.0D);
        int foodToConsume = (int) Math.floor(total);
        double remainder = total - foodToConsume;

        if (foodToConsume > 0) {
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - foodToConsume));
        }
        if (remainder > 0.0D) {
            pdc.set(FOOD_REMAINDER, PersistentDataType.DOUBLE, remainder);
        } else {
            pdc.remove(FOOD_REMAINDER);
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

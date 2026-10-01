package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class SetFoodAbility extends AbstractAbility {

    public SetFoodAbility(ConfigurationSection section) {
        super("SetFood", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (section.contains("cost")) {
            consume(player, Math.max(0.0D, getDouble("cost", 0.0D, context)));
            return false;
        }
        int food = Math.max(0, getInt("food", 1, context, "original", String.valueOf(player.getFoodLevel())));
        float saturation = (float) Math.max(0.0D, getDouble("saturation", 0.0D, context, "original", String.valueOf(player.getSaturation())));
        player.setFoodLevel(Math.min(20, food));
        player.setSaturation(Math.min(20.0F, saturation));
        return false;
    }

    private void consume(Player player, double cost) {
        float saturation = player.getSaturation();
        if (saturation >= cost) {
            player.setSaturation((float) (saturation - cost));
            return;
        }
        player.setSaturation(0.0F);
        int foodCost = (int) Math.ceil(cost - saturation);
        player.setFoodLevel(Math.max(0, player.getFoodLevel() - foodCost));
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PowerConditionBestTool extends AbstractPowerCondition {

    public PowerConditionBestTool() {
        this("best_tool");
    }

    public PowerConditionBestTool(String type) {
        super(type);
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        Block block = context == null ? null : context.block();
        if (block == null) {
            return false;
        }

        Entity entity = context.entity(condition.getSection().getString("target"), EntitySelector.SOURCE);
        if (!(entity instanceof Player player)) {
            return false;
        }

        String hand = condition.getSection().getString("hand", "MAIN_HAND");
        ItemStack tool = hand != null && hand.equalsIgnoreCase("OFF_HAND")
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
        boolean expected = condition.getSection().getBoolean("value", true);
        return block.isPreferredTool(tool) == expected;
    }
}

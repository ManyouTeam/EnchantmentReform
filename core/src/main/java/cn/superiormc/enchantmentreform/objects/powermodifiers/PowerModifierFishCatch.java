package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public final class PowerModifierFishCatch extends AbstractPowerModifier {

    public PowerModifierFishCatch(ConfigurationSection section) {
        super("fishing_catch", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof PlayerFishEvent event) || !(event.getCaught() instanceof Item item)) {
            return;
        }
        double multiplier = Math.max(0.0D, getDouble("multiplier", 1.0D, context));
        ItemStack stack = item.getItemStack();
        int amount = Math.max(1, (int) Math.round(stack.getAmount() * multiplier));
        stack.setAmount(Math.min(stack.getMaxStackSize(), amount));
        item.setItemStack(stack);
    }
}

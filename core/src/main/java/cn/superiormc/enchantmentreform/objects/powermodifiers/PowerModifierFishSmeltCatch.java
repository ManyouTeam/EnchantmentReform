package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.SmeltUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public final class PowerModifierFishSmeltCatch extends AbstractPowerModifier {

    public PowerModifierFishSmeltCatch(ConfigurationSection section) {
        super("fishing_smelt_catch", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof PlayerFishEvent event) || !(event.getCaught() instanceof Item item)) {
            return;
        }
        ItemStack caught = item.getItemStack();
        if (!SmeltUtil.hasSmeltResult(caught)) {
            return;
        }
        ItemStack cooked = SmeltUtil.smelt(caught);
        if (cooked == null || cooked.getType().isAir()) {
            return;
        }
        item.setItemStack(cooked);
    }
}

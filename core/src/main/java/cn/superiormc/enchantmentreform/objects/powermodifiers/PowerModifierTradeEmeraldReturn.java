package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.HashMap;
import java.util.List;

public final class PowerModifierTradeEmeraldReturn extends AbstractPowerModifier {

    public PowerModifierTradeEmeraldReturn(ConfigurationSection section) {
        super("trade_uses", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (context.event() instanceof PlayerPurchaseEvent event) {
            int amount = emeraldCost(event.getTrade());
            if (amount <= 0) {
                return;
            }

            Player player = event.getPlayer();
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(
                    new ItemStack(Material.EMERALD, amount));
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            return;
        }
    }

    private int emeraldCost(MerchantRecipe recipe) {
        List<ItemStack> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            return 0;
        }
        int amount = emeralds(recipe.getAdjustedIngredient1());
        for (int index = 1; index < ingredients.size(); index++) {
            amount += emeralds(ingredients.get(index));
        }
        return amount;
    }

    private int emeralds(ItemStack item) {
        return item != null && item.getType() == Material.EMERALD ? item.getAmount() : 0;
    }

}

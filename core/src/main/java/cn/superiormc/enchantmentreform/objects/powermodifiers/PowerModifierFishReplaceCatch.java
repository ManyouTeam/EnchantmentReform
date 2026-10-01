package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerModifierFishReplaceCatch extends AbstractPowerModifier {

    public PowerModifierFishReplaceCatch(ConfigurationSection section) {
        super("fishing_replace_catch", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof PlayerFishEvent event) || !(event.getCaught() instanceof Item item)) {
            return;
        }
        ItemStack replacement = getReplacement(event);
        if (replacement == null || replacement.getType().isAir()) {
            return;
        }
        item.setItemStack(replacement);
    }

    private ItemStack getReplacement(PlayerFishEvent event) {
        ConfigurationSection items = section.getConfigurationSection("items");
        List<String> itemKeys = getItemKeys(items);
        ConfigurationSection itemSection = items.getConfigurationSection(
                itemKeys.get(ThreadLocalRandom.current().nextInt(itemKeys.size())));
        return BuildItem.buildItemStack(event.getPlayer(), itemSection);
    }

    private List<String> getItemKeys(ConfigurationSection items) {
        if (items == null) {
            return List.of();
        }
        return items.getKeys(false).stream()
                .filter(key -> items.getConfigurationSection(key) != null)
                .toList();
    }
}

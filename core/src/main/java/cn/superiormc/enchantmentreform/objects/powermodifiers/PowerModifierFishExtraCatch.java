package cn.superiormc.enchantmentreform.objects.powermodifiers;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PowerModifierFishExtraCatch extends AbstractPowerModifier {

    public PowerModifierFishExtraCatch(ConfigurationSection section) {
        super("fishing_extra_catch", section);
    }

    @Override
    protected void onApply(PowerContext context) {
        if (!(context.event() instanceof PlayerFishEvent event)
                || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        List<WeightedItem> items = getItems(context);
        if (items.isEmpty()) {
            return;
        }
        int amount = Math.max(0, getInt("amount", 0, context));
        double weightScale = items.stream().mapToDouble(WeightedItem::weight).max().orElse(1.0D);
        double totalWeight = items.stream().mapToDouble(item -> item.weight() / weightScale).sum();
        for (int index = 0; index < amount; index++) {
            WeightedItem selected = selectItem(items, weightScale, totalWeight);
            ItemStack item = BuildItem.buildItemStack(event.getPlayer(), selected.section());
            if (item != null && !item.getType().isAir()) {
                event.getHook().getWorld().dropItemNaturally(event.getHook().getLocation(), item);
            }
        }
    }

    private List<WeightedItem> getItems(PowerContext context) {
        ConfigurationSection items = section.getConfigurationSection("items");
        if (items == null) {
            return List.of();
        }
        return items.getKeys(false).stream()
                .map(key -> {
                    ConfigurationSection item = items.getConfigurationSection(key);
                    double weight = getDouble("items." + key + ".rate", 1.0D, context);
                    return new WeightedItem(item, weight);
                })
                .filter(item -> item.section() != null
                        && Double.isFinite(item.weight())
                        && item.weight() > 0.0D)
                .toList();
    }

    private WeightedItem selectItem(List<WeightedItem> items, double weightScale, double totalWeight) {
        double value = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (WeightedItem item : items) {
            value -= item.weight() / weightScale;
            if (value < 0.0D) {
                return item;
            }
        }
        return items.getLast();
    }

    private record WeightedItem(ConfigurationSection section, double weight) {
    }
}

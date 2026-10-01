package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.ItemSelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.AbstractAbility;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;

public final class PowerConditionItemDamage extends AbstractNumericPowerCondition {

    public PowerConditionItemDamage() {
        super("item_damage");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        ConfigurationSection section = condition.getSection();
        ConfigurationSection selection = section.getConfigurationSection("item");
        String rawSelector = selection == null
                ? section.getString("item", "TRIGGER_ITEM")
                : selection.getString("selector", "TRIGGER_ITEM");
        String rawHolder = selection == null
                ? section.getString("item-holder", "PLAYER")
                : selection.getString("holder", "PLAYER");

        ItemSelector selector = ItemSelector.parse(rawSelector, ItemSelector.TRIGGER_ITEM);
        List<ItemStack> items = AbstractAbility.resolveItems(
                context,
                selector,
                EntitySelector.parse(rawHolder, EntitySelector.PLAYER));

        ItemStack item = null;
        Damageable damageable = null;
        for (ItemStack current : items) {
            ItemMeta meta = current.getItemMeta();
            if (meta instanceof Damageable currentDamageable) {
                item = current;
                damageable = currentDamageable;
                break;
            }
        }
        if (item == null || damageable == null) {
            return null;
        }

        int maximumDamage = item.getType().getMaxDurability();
        if (maximumDamage <= 0) {
            return null;
        }

        double damage = Math.max(0.0D, damageable.getDamage());
        if (section.getBoolean("include-event-damage", false)
                && context.event() instanceof PlayerItemDamageEvent event
                && isEventItem(selector, item, context, event)) {
            int eventDamage = context.result() == null
                    ? event.getDamage()
                    : context.result().itemDamage(context.triggerData());
            damage += Math.max(0, eventDamage);
        }

        String mode = section.getString("mode", "DAMAGE");
        String normalized = mode == null ? "DAMAGE"
                : mode.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        double boundedDamage = Math.min(maximumDamage, damage);
        return switch (normalized) {
            case "REMAINING", "DURABILITY" -> maximumDamage - boundedDamage;
            case "DAMAGE_PERCENT", "USED_PERCENT" -> boundedDamage / maximumDamage * 100.0D;
            case "REMAINING_PERCENT", "DURABILITY_PERCENT" ->
                    (maximumDamage - boundedDamage) / maximumDamage * 100.0D;
            case "MAX", "MAXIMUM", "MAX_DAMAGE", "MAX_DURABILITY" -> (double) maximumDamage;
            default -> damage;
        };
    }

    private static boolean isEventItem(ItemSelector selector, ItemStack item, PowerContext context,
                                       PlayerItemDamageEvent event) {
        if (selector == ItemSelector.TRIGGER_ITEM) {
            return true;
        }
        ItemStack triggerItem = context.triggerItem();
        return item == event.getItem() || item == triggerItem;
    }
}

package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.ItemSelector;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class DamageItemAbility extends AbstractAbility {

    private static final ThreadLocal<Set<DamageTarget>> PAPER_DAMAGE_GUARD =
            ThreadLocal.withInitial(HashSet::new);

    public DamageItemAbility(ConfigurationSection section) {
        super("DamageItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        double originalDamage = context.originalDamage();
        double currentDamage = context.result() == null
                ? originalDamage : context.result().damage(context.triggerData());
        String[] damageArguments = {
                "damage", String.valueOf(currentDamage),
                "original", String.valueOf(originalDamage)
        };
        int amount;
        if (section.contains("rounding")) {
            double configuredAmount = Math.max(0.0D, getDouble(
                    "amount", 1.0D, context, damageArguments));
            amount = roundAmount(configuredAmount);
        } else {
            amount = Math.max(0, getInt("amount", 1, context, damageArguments));
        }
        if (amount == 0) {
            return false;
        }
        if (mode() == Mode.PAPER && EnchantmentReform.methodUtil.methodID().equals("paper")) {
            damageWithPaper(context, amount);
        } else {
            for (ItemStack item : getItems(context)) {
                damageDirectly(item, amount);
            }
        }
        return false;
    }

    private int roundAmount(double amount) {
        double rounded = switch (rounding()) {
            case CEIL -> Math.ceil(amount);
            case ROUND -> Math.round(amount);
            case FLOOR -> Math.floor(amount);
        };
        return rounded >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rounded;
    }

    private void damageDirectly(ItemStack item, int amount) {
        if (item == null || item.getType() == Material.AIR || item.getType().getMaxDurability() <= 0) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable) || meta.isUnbreakable()) {
            return;
        }
        int max = item.getType().getMaxDurability();
        damageable.setDamage(Math.min(max, damageable.getDamage() + amount));
        item.setItemMeta(meta);
    }

    private void damageWithPaper(PowerContext context, int amount) {
        for (EquippedItem equippedItem : resolveEquippedItems(context)) {
            DamageTarget target = new DamageTarget(equippedItem.holder().getUniqueId(), equippedItem.slot());
            Set<DamageTarget> active = PAPER_DAMAGE_GUARD.get();
            if (!active.add(target)) {
                continue;
            }
            try {
                equippedItem.holder().damageItemStack(equippedItem.slot(), amount);
            } finally {
                active.remove(target);
                if (active.isEmpty()) {
                    PAPER_DAMAGE_GUARD.remove();
                }
            }
        }
    }

    private List<EquippedItem> resolveEquippedItems(PowerContext context) {
        ConfigurationSection itemSection = section.getConfigurationSection("item");
        String rawSelector = itemSection == null
                ? section.getString("item", "CONTEXT")
                : itemSection.getString("selector", "CONTEXT");
        ItemSelector selector = ItemSelector.parse(rawSelector, ItemSelector.CONTEXT);

        if (selector == ItemSelector.CONTEXT) {
            return equipped(context.player(), context.contextSlot());
        }
        if (selector == ItemSelector.TRIGGER_ITEM) {
            EquipmentSlot slot = context.triggerData() == null
                    ? null : context.triggerData().triggerItemSlot();
            return equipped(context.player(), slot);
        }

        String rawHolder = itemSection == null
                ? section.getString("item-holder", getDefaultTargetEntityType().name())
                : itemSection.getString("holder", getDefaultTargetEntityType().name());
        LivingEntity holder = context.livingEntity(
                EntitySelector.parse(rawHolder, EntitySelector.TARGET));
        if (holder == null) {
            return List.of();
        }

        List<EquippedItem> result = new ArrayList<>();
        switch (selector) {
            case MAIN_HAND -> result.add(new EquippedItem(holder, EquipmentSlot.HAND));
            case OFF_HAND -> result.add(new EquippedItem(holder, EquipmentSlot.OFF_HAND));
            case HELMET -> result.add(new EquippedItem(holder, EquipmentSlot.HEAD));
            case CHESTPLATE -> result.add(new EquippedItem(holder, EquipmentSlot.CHEST));
            case LEGGINGS -> result.add(new EquippedItem(holder, EquipmentSlot.LEGS));
            case BOOTS -> result.add(new EquippedItem(holder, EquipmentSlot.FEET));
            case ARMOR -> addArmor(result, holder);
            case ALL_EQUIPMENT -> {
                result.add(new EquippedItem(holder, EquipmentSlot.HAND));
                result.add(new EquippedItem(holder, EquipmentSlot.OFF_HAND));
                addArmor(result, holder);
            }
            default -> {
            }
        }
        return result;
    }

    private List<EquippedItem> equipped(Player player, EquipmentSlot slot) {
        return player == null || slot == null ? List.of() : List.of(new EquippedItem(player, slot));
    }

    private void addArmor(List<EquippedItem> result, LivingEntity holder) {
        result.add(new EquippedItem(holder, EquipmentSlot.HEAD));
        result.add(new EquippedItem(holder, EquipmentSlot.CHEST));
        result.add(new EquippedItem(holder, EquipmentSlot.LEGS));
        result.add(new EquippedItem(holder, EquipmentSlot.FEET));
    }

    private Mode mode() {
        try {
            return Mode.valueOf(section.getString("mode", "DIRECT").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return Mode.DIRECT;
        }
    }

    private Rounding rounding() {
        try {
            return Rounding.valueOf(section.getString("rounding", "FLOOR").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return Rounding.FLOOR;
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }

    private enum Mode {
        DIRECT,
        PAPER
    }

    private enum Rounding {
        FLOOR,
        CEIL,
        ROUND
    }

    private record EquippedItem(LivingEntity holder, EquipmentSlot slot) {
    }

    private record DamageTarget(UUID holderId, EquipmentSlot slot) {
    }
}

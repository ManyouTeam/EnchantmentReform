package cn.superiormc.enchantmentreform.utils;

import com.google.common.collect.Multimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;

public final class AttributeUtil {

    private AttributeUtil() {
        // Utility class.
    }

    public static EquipmentSlotGroup getAutomaticEquipmentSlotGroup(ItemStack item) {
        EquipmentSlot slot = getAutomaticEquipmentSlot(item);
        String groupName = slot == EquipmentSlot.HAND ? "MAINHAND"
                : slot == EquipmentSlot.OFF_HAND ? "OFFHAND" : slot.name();
        EquipmentSlotGroup group = EquipmentSlotGroup.getByName(groupName);
        return group != null ? group : EquipmentSlotGroup.ANY;
    }

    public static EquipmentSlot getAutomaticEquipmentSlot(ItemStack item) {
        if (CommonUtil.getMinorVersion(21, 3)) {
            return item.getType().getEquipmentSlot();
        }

        String material = item.getType().name();
        if (material.endsWith("_HELMET") || material.equals("CARVED_PUMPKIN")
                || material.equals("PLAYER_HEAD") || material.equals("ZOMBIE_HEAD")
                || material.equals("CREEPER_HEAD") || material.equals("DRAGON_HEAD")
                || material.equals("SKELETON_SKULL") || material.equals("WITHER_SKELETON_SKULL")
                || material.equals("PIGLIN_HEAD")) {
            return EquipmentSlot.HEAD;
        }
        if (material.endsWith("_CHESTPLATE") || material.equals("ELYTRA")) {
            return EquipmentSlot.CHEST;
        }
        if (material.endsWith("_LEGGINGS")) {
            return EquipmentSlot.LEGS;
        }
        if (material.endsWith("_BOOTS")) {
            return EquipmentSlot.FEET;
        }
        return EquipmentSlot.HAND;
    }

    public static void copyDefaultAttributeModifiers(ItemMeta meta, ItemStack item) {
        if (!CommonUtil.getMinorVersion(21, 3) || meta.getAttributeModifiers() != null) {
            return;
        }
        EquipmentSlot slot = getAutomaticEquipmentSlot(item);
        Multimap<Attribute, AttributeModifier> defaultModifiers =
                item.getType().getDefaultAttributeModifiers(slot);
        if (!defaultModifiers.isEmpty()) {
            meta.setAttributeModifiers(defaultModifiers);
        }
    }

    /** Adds a modifier, merging matching additive modifiers like the add-attributes rule. */
    public static void addOrMergeAttributeModifier(ItemMeta meta,
                                                   Attribute attribute,
                                                   AttributeModifier modifier) {
        double amount = modifier.getAmount();
        Collection<AttributeModifier> existingModifiers =
                meta.getAttributeModifiers(attribute);
        if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER
                && existingModifiers != null) {
            for (AttributeModifier existing : new ArrayList<>(existingModifiers)) {
                if (existing.getOperation() == AttributeModifier.Operation.ADD_NUMBER
                        && existing.getName().equals(modifier.getName())) {
                    amount += existing.getAmount();
                    meta.removeAttributeModifier(attribute, existing);
                }
            }
        }

        meta.addAttributeModifier(attribute, amount == modifier.getAmount()
                ? modifier
                : new AttributeModifier(modifier.getKey(), amount, modifier.getOperation(),
                modifier.getSlotGroup()));
    }

    public static boolean isDefaultAttributeModifier(ItemStack item, Attribute attribute,
                                                     AttributeModifier modifier) {
        if (!CommonUtil.getMinorVersion(21, 3)) {
            return false;
        }
        EquipmentSlot slot = getAutomaticEquipmentSlot(item);
        return item.getType().getDefaultAttributeModifiers(slot).containsEntry(attribute, modifier);
    }

    public static void removeCustomAttributeModifiers(ItemMeta meta, ItemStack item,
                                                      BiPredicate<Attribute, AttributeModifier> predicate) {
        Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();
        if (modifiers == null) {
            return;
        }

        boolean removedModifier = false;
        Collection<Map.Entry<Attribute, AttributeModifier>> entries =
                new ArrayList<>(modifiers.entries());
        for (Map.Entry<Attribute, AttributeModifier> entry : entries) {
            if (!isDefaultAttributeModifier(item, entry.getKey(), entry.getValue())
                    && predicate.test(entry.getKey(), entry.getValue())) {
                removedModifier |= meta.removeAttributeModifier(entry.getKey(), entry.getValue());
            }
        }
        if (removedModifier && meta.getAttributeModifiers() != null
                && meta.getAttributeModifiers().isEmpty()) {
            meta.setAttributeModifiers(null);
        }
    }

    public static void removeCustomAttributeModifiersByName(ItemMeta meta, ItemStack item,
                                                            Collection<String> names,
                                                            boolean contains) {
        removeCustomAttributeModifiers(meta, item, (attribute, modifier) -> {
            String modifierName = modifier.getName();
            for (String name : names) {
                if (name == null || name.isEmpty()) {
                    continue;
                }
                if (contains ? modifierName.contains(name) : modifierName.equals(name)) {
                    return true;
                }
            }
            return false;
        });
    }
}

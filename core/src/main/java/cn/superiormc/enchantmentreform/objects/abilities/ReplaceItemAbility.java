package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.methods.BuildItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ReplaceItemAbility extends AbstractAbility {

    public ReplaceItemAbility(ConfigurationSection section) {
        super("ReplaceItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return false;
        }
        String slot = getString("slot", "MAIN_HAND").toUpperCase();
        ItemStack item = BuildItem.buildItemStack(null, getSection("item"));

        switch (slot.toUpperCase()) {
            case "MAIN_HAND" -> {
                equipment.setItemInMainHand(item);
                if (!(equipment instanceof PlayerInventory)) {
                    equipment.setItemInMainHandDropChance(0f);
                }
            }
            case "OFF_HAND" -> {
                equipment.setItemInOffHand(item);
                if (!(equipment instanceof PlayerInventory)) {
                    equipment.setItemInOffHandDropChance(0f);
                }
            }
            case "HELMET" -> {
                equipment.setHelmet(item);
                if (!(equipment instanceof PlayerInventory)) {
                    equipment.setHelmetDropChance(0f);
                }
            }
            case "CHESTPLATE" -> {
                equipment.setChestplate(item);
                if (!(equipment instanceof PlayerInventory)) {
                    equipment.setChestplateDropChance(0f);
                }
            }
            case "LEGGINGS" -> {
                equipment.setLeggings(item);
                if (!(equipment instanceof PlayerInventory)) {
                    equipment.setLeggingsDropChance(0f);
                }
            }
            case "BOOTS" -> {
                equipment.setBoots(item);
                if (!(equipment instanceof PlayerInventory)) {
                    equipment.setBootsDropChance(0f);
                }
            }
        }

        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
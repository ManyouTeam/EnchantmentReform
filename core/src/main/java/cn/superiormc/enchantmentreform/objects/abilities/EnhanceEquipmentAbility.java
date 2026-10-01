package cn.superiormc.enchantmentreform.objects.abilities;

import com.google.common.base.Enums;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnhanceEquipmentAbility extends AbstractAbility {

    public EnhanceEquipmentAbility(ConfigurationSection section) {
        super("EnhanceEquipment", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return false;
        }

        String materialPrefix = getString("armor-material", "NETHERITE").toUpperCase();
        List<String> slots = section.getStringList("pieces");
        if (slots.isEmpty()) {
            slots = List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
        }
        for (String slot : slots) {
            applyArmorPiece(equipment, materialPrefix, slot, context);
        }
        return false;
    }

    private void applyArmorPiece(EntityEquipment equipment, String materialPrefix, String slot, PowerContext context) {
        Material material = Enums.getIfPresent(Material.class, materialPrefix + "_" + slot.toUpperCase()).orNull();
        if (material == null) {
            return;
        }

        ItemStack item = new ItemStack(material);
        int minEnchant = Math.max(0, getInt("enchant.min-amount", 1, context));
        int maxEnchant = Math.max(minEnchant, getInt("enchant.max-amount", 3, context));
        int minLevel = Math.max(1, getInt("enchant.min-level", 1, context));
        int maxLevel = Math.max(minLevel, getInt("enchant.max-level", 4, context));

        addRandomEnchants(item,
                randomInt(minEnchant, maxEnchant),
                minLevel,
                maxLevel);

        switch (slot.toUpperCase()) {
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
    }

    private void addRandomEnchants(ItemStack item, int amount, int minLevel, int maxLevel) {

        ConfigurationSection enchantSection = section.getConfigurationSection("enchant");

        List<Enchantment> candidates = new ArrayList<>();
        if (enchantSection != null && enchantSection.isList("enchantments")) {
            for (String name : enchantSection.getStringList("enchantments")) {
                Enchantment ench = Enchantment.getByName(name.toUpperCase());
                if (ench != null && ench.canEnchantItem(item)) {
                    candidates.add(ench);
                }
            }
        } else {
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.canEnchantItem(item)) {
                    candidates.add(enchantment);
                }
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        for (int i = 0; i < amount && !candidates.isEmpty(); i++) {
            int index = ThreadLocalRandom.current().nextInt(candidates.size());
            Enchantment enchantment = candidates.remove(index);
            int enchLevel = randomInt(
                    minLevel,
                    Math.min(maxLevel, enchantment.getMaxLevel())
            );
            item.addUnsafeEnchantment(enchantment, enchLevel);
        }
    }

    private int randomInt(int min, int max) {
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}
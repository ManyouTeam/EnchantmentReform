package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class Equip extends AbstractMatchEntityRule {

    public Equip() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        LivingEntity entity = match.getEntity();
        ConfigurationSection equipSection = match.section.getConfigurationSection("equip");
        if (equipSection == null) {
            return false;
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return false;
        }

        // 主手
        if (equipSection.contains("main-hand")) {
            if (match(match, equipSection, "main-hand", equipment.getItemInMainHand())) {
                return true;
            }
        }

        // 副手
        if (equipSection.contains("off-hand")) {
            if (match(match, equipSection, "off-hand", equipment.getItemInOffHand())) {
                return true;
            }
        }

        // 头盔
        if (equipSection.contains("helmet")) {
            if (match(match, equipSection, "helmet", equipment.getHelmet())) {
                return true;
            }
        }

        // 胸甲
        if (equipSection.contains("chestplate")) {
            if (match(match, equipSection, "chestplate", equipment.getChestplate())) {
                return true;
            }
        }

        // 护腿
        if (equipSection.contains("leggings")) {
            if (match(match, equipSection, "leggings", equipment.getLeggings())) {
                return true;
            }
        }

        // 靴子
        if (equipSection.contains("boots")) {
            if (match(match, equipSection, "boots", equipment.getBoots())) {
                return true;
            }
        }
        return false;
    }

    private boolean match(ObjectSingleMatchEntity match, ConfigurationSection equipSection, String key, ItemStack item) {
        ConfigurationSection itemSection = equipSection.getConfigurationSection(key);
        if (itemSection == null) {
            return false;
        }

        return MatchItemManager.matchItemManager.getMatch(itemSection, match.getPlayer(), item, match.getContext());
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("equip");
    }
}

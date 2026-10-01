package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class Ranged extends AbstractMatchEntityRule {

    public Ranged() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        boolean result = isHoldingRangedWeapon(entity);
        if (section.getBoolean("ranged")) {
            return result;
        }
        return !result;
    }

    private boolean isHoldingRangedWeapon(LivingEntity entity) {

        EntityEquipment equipmentSlot = entity.getEquipment();
        if (equipmentSlot == null) {
            return false;
        }
        ItemStack item = equipmentSlot.getItemInMainHand();

        Material mat = item.getType();
        return mat == Material.BOW
                || mat == Material.CROSSBOW
                || mat == Material.TRIDENT;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        return !section.contains("ranged");
    }
}

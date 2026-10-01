package cn.superiormc.enchantmentreform.power;

import cn.superiormc.enchantmentreform.objects.ObjectPower;
import cn.superiormc.enchantmentreform.objects.PowerSourceDefinition;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public record ActivePowerSource(
        PowerSourceDefinition source,
        ObjectPower power,
        int level,
        ItemStack item,
        EquipmentSlot slot
) {
}

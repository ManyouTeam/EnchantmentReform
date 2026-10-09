package cn.superiormc.enchantmentreform.power;

import cn.superiormc.enchantmentreform.objects.ObjectPower;
import cn.superiormc.enchantmentreform.objects.PowerSourceDefinition;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public record TrackedPowerSource(
        PowerSourceDefinition source,
        ObjectPower power,
        int level,
        ItemStack item,
        EquipmentSlot slot
) {
    public TrackedPowerSource {
        item = item == null ? null : item.clone();
    }

    public static TrackedPowerSource from(ActivePowerSource active) {
        return new TrackedPowerSource(active.source(), active.power(), active.level(),
                active.item(), active.slot());
    }

    public ActivePowerSource active() {
        return new ActivePowerSource(source, power, level, item == null ? null : item.clone(), slot);
    }

    @Override
    public ItemStack item() {
        return item == null ? null : item.clone();
    }
}

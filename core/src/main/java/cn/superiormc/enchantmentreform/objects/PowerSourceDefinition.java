package cn.superiormc.enchantmentreform.objects;

import org.bukkit.inventory.EquipmentSlot;

public interface PowerSourceDefinition {

    String powerSourceId();

    ObjectPower getPower();

    boolean isActiveOn(EquipmentSlot slot);

    boolean isDuplicateAllowed();

    int getExecutionPriority();
}

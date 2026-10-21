package cn.superiormc.enchantmentreform.gui.action;

import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.skills.SkillDefinition;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public record GUIActionContext(GUIPageController gui,
                               Inventory inventory,
                               ClickType clickType,
                               int slot,
                               String menuId,
                               String returnMenuId,
                               SkillDefinition skill,
                               ObjectCustomAttribute attribute,
                               boolean bypassPermission) {

    public GUIActionContext(GUIPageController gui, Inventory inventory, ClickType clickType, int slot) {
        this(gui, inventory, clickType, slot, "", "main", null, null, false);
    }
}

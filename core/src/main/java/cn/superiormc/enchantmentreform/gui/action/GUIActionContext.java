package cn.superiormc.enchantmentreform.gui.action;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public record GUIActionContext(GUIPageController gui,
                               Inventory inventory,
                               ClickType clickType,
                               int slot) {
}

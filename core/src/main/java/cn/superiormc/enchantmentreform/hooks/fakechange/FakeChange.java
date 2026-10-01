package cn.superiormc.enchantmentreform.hooks.fakechange;

import org.bukkit.inventory.ItemStack;

public interface FakeChange {

    void reload();

    boolean isEnabled();

    ItemStack apply(ItemStack item, FakeChangeContext context);
}

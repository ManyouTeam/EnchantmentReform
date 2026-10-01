package cn.superiormc.enchantmentreform.hooks.fakechange;

import org.bukkit.entity.Player;

public record FakeChangeContext(Player player,
                                Surface surface,
                                int windowId,
                                int slot,
                                boolean playerInventory) {

    public enum Surface {
        WINDOW_ITEM,
        SET_SLOT,
        CURSOR,
        MERCHANT
    }
}

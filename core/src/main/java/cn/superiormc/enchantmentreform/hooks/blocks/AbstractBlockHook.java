package cn.superiormc.enchantmentreform.hooks.blocks;

import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlockHook {

    public boolean canCheck(String materialString) {
        return materialString.startsWith(getCheckerName() + ":");
    }

    public abstract boolean check(Block block, String materialString, Location location);

    @Nullable
    public abstract String getBlockId(Block block);

    public abstract void placeBlock(String materialString, Location location);

    protected boolean isValidMaterialFormat(String[] parts, int minParts) {
        if (parts.length < minParts) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §cError: Your " + getCheckerName() + " material does not meet" +
                    " the format claimed in plugin Wiki!");
            return false;
        }
        return true;
    }

    protected abstract String getCheckerName();

}

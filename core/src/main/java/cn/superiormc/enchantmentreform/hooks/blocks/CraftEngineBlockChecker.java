package cn.superiormc.enchantmentreform.hooks.blocks;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class CraftEngineBlockChecker extends AbstractBlockHook {

    private static final String DEFAULT_NAMESPACE = "craftengine";

    @Override
    public String getBlockId(Block block) {
        if (!CommonUtil.checkPluginLoad("CraftEngine")) {
            return null;
        }

        try {
            ImmutableBlockState craftEngineBlock = CraftEngineBlocks.getCustomBlockState(block);
            if (craftEngineBlock == null || craftEngineBlock.owner() == null || craftEngineBlock.owner().value() == null) {
                return null;
            }
            return "craftengine:" + craftEngineBlock.owner().value().id();
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean check(Block block, String materialString, Location location) {
        if (!CommonUtil.checkPluginLoad("CraftEngine")) {
            ErrorManager.errorManager.sendErrorMessage("§cError: CraftEngine is not loaded but you are using block from it!");
            return false;
        }
        String[] parts = materialString.split(":");
        if (!isValidMaterialFormat(parts, 2)) {
            return false;
        }
        try {
            ImmutableBlockState craftEngineBlock = CraftEngineBlocks.getCustomBlockState(block);
            if (craftEngineBlock == null || craftEngineBlock.owner() == null) {
                return false;
            }
            BlockDefinition customBlock = craftEngineBlock.owner().value();
            if (customBlock == null) {
                return false;
            }
            String actualId = customBlock.id().toString();
            if (parts.length == 2) {
                return actualId.equals(parts[1]) || actualId.endsWith(":" + parts[1]);
            }
            return (parts[1] + ":" + parts[2]).equals(actualId);
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public void placeBlock(String materialString, Location location) {
        if (!CommonUtil.checkPluginLoad("CraftEngine")) {
            ErrorManager.errorManager.sendErrorMessage("§cError: CraftEngine is not loaded but you are using block from it!");
            return;
        }
        String[] parts = materialString.split(":");
        if (!isValidMaterialFormat(parts, 2)) {
            return;
        }
        try {
            Key key = parts.length == 2
                    ? Key.of(DEFAULT_NAMESPACE, parts[1])
                    : Key.of(parts[1], parts[2]);
            CraftEngineBlocks.place(location, key, true);
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getCheckerName() {
        return "craftengine";
    }
}

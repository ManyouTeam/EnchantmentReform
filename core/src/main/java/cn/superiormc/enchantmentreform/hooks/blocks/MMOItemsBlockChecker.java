package cn.superiormc.enchantmentreform.hooks.blocks;

import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Optional;

public class MMOItemsBlockChecker extends AbstractBlockHook {

    @Override
    public String getBlockId(Block block) {
        if (!CommonUtil.checkPluginLoad("MMOItems")) {
            return null;
        }

        try {
            Optional<CustomBlock> opt = MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData());
            return opt.map(customBlock -> "mmoitems:" + customBlock.getId()).orElse(null);
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean check(Block block, String materialString, Location location) {
        if (!CommonUtil.checkPluginLoad("MMOItems")) {
            ErrorManager.errorManager.sendErrorMessage("§cError: MMOItems is not loaded but you are using block from it as totem layout!");
            return false;
        }
        String[] parts = materialString.split(":");
        if (!isValidMaterialFormat(parts, 2)) {
            return false;
        }
        try {
            Optional<CustomBlock> opt = MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData());
            return opt.filter(customBlock -> customBlock.getId() == Integer.parseInt(parts[1])).isPresent();
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public void placeBlock(String materialString, Location location) {
        if (!CommonUtil.checkPluginLoad("MMOItems")) {
            ErrorManager.errorManager.sendErrorMessage("§cError: MMOItems is not loaded but you are using block from it as totem layout!");
            return;
        }
        String[] parts = materialString.split(":");
        if (!isValidMaterialFormat(parts, 2)) {
            return;
        }
        try {
            CustomBlock customBlock = MMOItems.plugin.getCustomBlocks().getBlock(Integer.parseInt(parts[1]));
            if (customBlock != null) {
                location.getBlock().setType(customBlock.getState().getType());
                location.getBlock().setBlockData(customBlock.getState().getBlockData());
            }
        } catch (Exception e) {
            if (ConfigManager.configManager.getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getCheckerName() {
        return "mmoitems";
    }
}

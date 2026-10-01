package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.hooks.BlockPriceUtil;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class PlaceBlockAbility extends AbstractAbility {

    public PlaceBlockAbility(ConfigurationSection section) {
        super("PlaceBlock", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location location = getLocation(context);
        if (location == null || location.getWorld() == null) {
            return executeFailure(context);
        }

        Block block = location.getBlock();
        if (!section.getBoolean("replace-existing", false) && !block.getType().isAir()) {
            return executeFailure(context.withBlock(block));
        }

        Player player = context.player();
        if (player == null && context.source() instanceof Player source) {
            player = source;
        }
        if (player != null && HookManager.hookManager != null
                && !HookManager.hookManager.getProtectionCanPlace(player, block.getLocation())) {
            return executeFailure(context.withBlock(block));
        }

        String specification = getString("block", "STONE", context);
        if (!BlockPriceUtil.place(block.getLocation(), specification)) {
            return executeFailure(context.withBlock(block));
        }

        PowerContext blockContext = context.withBlock(block);
        if (context.result() != null) {
            context.result().recordChangedBlocks(1);
        }
        return AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), blockContext);
    }

    private boolean executeFailure(PowerContext context) {
        ConfigurationSection abilities = section.getConfigurationSection("failure-abilities");
        if (abilities == null) {
            abilities = section.getConfigurationSection("else-abilities");
        }
        return AbilityManager.abilityManager.execute(abilities, context);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

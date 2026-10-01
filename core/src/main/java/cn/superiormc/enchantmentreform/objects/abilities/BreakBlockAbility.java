package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.HookManager;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public final class BreakBlockAbility extends AbstractAbility {

    public BreakBlockAbility(ConfigurationSection section) {
        super("BreakBlock", section);
    }

    @Override
    public boolean breakOtherBlock() {
        return true;
    }

    @Override
    public boolean execute(PowerContext context) {
        Block block = context.block();
        ItemStack tool = context.contextItem().clone();
        BreakMode mode = getMode();
        if (block == null) {
            return false;
        }
        breakBlock(mode, getBreaker(context),  block, tool);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private void breakBlock(BreakMode mode, Player breaker, Block block, ItemStack tool) {
        if (block.getType().isAir()) {
            return;
        }
        if (!HookManager.hookManager.getProtectionCanBreak(breaker, block.getLocation())) {
            return;
        }
        if (mode == BreakMode.PLAYER) {
            breakAsPlayer(breaker, block);
            return;
        }
        block.breakNaturally(tool.clone());
    }

    private boolean breakAsPlayer(Player breaker, Block block) {
        if (PowerManager.powerManager != null) {
            PowerManager.powerManager.markInternalBlockBreak(block);
        }
        boolean broken = breaker.breakBlock(block);
        if (!broken && PowerManager.powerManager != null) {
            PowerManager.powerManager.clearInternalBlockBreak(block);
        }
        return broken;
    }

    private Player getBreaker(PowerContext context) {
        String selector = section.getString("breaker", "SOURCE").toUpperCase(Locale.ROOT);
        Entity entity;
        if (selector.equals("TARGET")) {
            entity = context.target();
        } else if (selector.equals("SKILL")) {
            entity = context.skill();
        } else {
            entity = context.source();
        }
        return entity instanceof Player player ? player : null;
    }

    private BreakMode getMode() {
        String value = section.getString("mode", "NATURAL")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
        return switch (value) {
            case "PLAYER", "PLAYER_BREAK", "PLAYER_BREAK_BLOCK", "BREAK_BLOCK" -> BreakMode.PLAYER;
            case "NATURAL", "BLOCK", "BREAK_NATURALLY", "BLOCK_BREAK_NATURALLY" -> BreakMode.NATURAL;
            default -> BreakMode.NATURAL;
        };
    }

    protected enum BreakMode {
        PLAYER,
        NATURAL
    }

}

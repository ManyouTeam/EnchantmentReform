package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Locale;

public final class PowerConditionLiquidTransition extends AbstractPowerCondition {

    public PowerConditionLiquidTransition() {
        super("liquid_transition");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null
                || !(condition.getContext().event() instanceof PlayerMoveEvent event)) {
            return false;
        }
        Location to = event.getTo();
        if (to == null || !event.getFrom().getWorld().equals(to.getWorld())) {
            return false;
        }
        LiquidType configured = configuredLiquid(condition);
        if (configured == LiquidType.INVALID) {
            return false;
        }
        boolean fromMatches = matches(configured, liquidAt(event.getFrom().getBlock()));
        boolean toMatches = matches(configured, liquidAt(to.getBlock()));
        String transition = condition.getString("transition", "ENTER")
                .strip().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (transition) {
            case "EXIT", "LEAVE" -> fromMatches && !toMatches;
            case "ENTER" -> !fromMatches && toMatches;
            default -> false;
        };
    }

    private boolean matches(LiquidType configured, LiquidType actual) {
        return actual != null && (configured == LiquidType.ANY || configured == actual);
    }

    private LiquidType configuredLiquid(ObjectSingleCondition condition) {
        String configured = condition.getString("liquid", "ANY")
                .strip().toUpperCase(Locale.ROOT);
        return switch (configured) {
            case "ANY" -> LiquidType.ANY;
            case "WATER" -> LiquidType.WATER;
            case "LAVA" -> LiquidType.LAVA;
            default -> LiquidType.INVALID;
        };
    }

    private LiquidType liquidAt(Block block) {
        Material material = block.getType();
        if (material == Material.LAVA) {
            return LiquidType.LAVA;
        }
        if (material == Material.WATER
                || material == Material.BUBBLE_COLUMN
                || material == Material.KELP
                || material == Material.KELP_PLANT
                || material == Material.SEAGRASS
                || material == Material.TALL_SEAGRASS
                || block.getBlockData() instanceof Waterlogged waterlogged
                && waterlogged.isWaterlogged()) {
            return LiquidType.WATER;
        }
        return null;
    }

    private enum LiquidType {
        ANY,
        WATER,
        LAVA,
        INVALID
    }
}

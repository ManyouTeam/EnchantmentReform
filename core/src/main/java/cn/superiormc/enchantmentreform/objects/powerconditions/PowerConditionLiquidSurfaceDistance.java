package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;

public final class PowerConditionLiquidSurfaceDistance extends AbstractNumericPowerCondition {

    public PowerConditionLiquidSurfaceDistance() {
        super("liquid_surface_distance");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Entity entity = context.entity(
                condition.getSection().getString("target"), EntitySelector.PLAYER);
        if (entity == null) {
            return null;
        }
        Location eye = entity instanceof LivingEntity living
                ? living.getEyeLocation() : entity.getLocation();
        World world = eye.getWorld();
        if (world == null || eye.getBlockY() < world.getMinHeight()
                || eye.getBlockY() >= world.getMaxHeight()) {
            return null;
        }

        LiquidType liquid = liquidAt(eye.getBlock());
        LiquidType required = configuredLiquid(condition);
        if (liquid == null || required == LiquidType.INVALID
                || required != LiquidType.ANY && required != liquid) {
            return null;
        }

        double scanLimit = scanLimit(condition, context, eye, world);
        if (!Double.isFinite(scanLimit) || scanLimit < 0.0D) {
            return null;
        }
        int highestY = Math.min(world.getMaxHeight() - 1,
                (int) Math.floor(eye.getY() + scanLimit) + 1);
        for (int y = eye.getBlockY(); y <= highestY; y++) {
            Block block = world.getBlockAt(eye.getBlockX(), y, eye.getBlockZ());
            LiquidType current = liquidAt(block);
            if (current == liquid) {
                continue;
            }
            if (current != null || !block.isPassable()) {
                return null;
            }
            double distance = Math.max(0.0D, y - eye.getY());
            return distance <= scanLimit ? distance : null;
        }
        return null;
    }

    private double scanLimit(ObjectSingleCondition condition, PowerContext context,
                             Location eye, World world) {
        double worldLimit = Math.max(0.0D, world.getMaxHeight() - eye.getY());
        if (condition.contains("scan-limit")) {
            return condition.getDouble("scan-limit", worldLimit, context);
        }
        return worldLimit;
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

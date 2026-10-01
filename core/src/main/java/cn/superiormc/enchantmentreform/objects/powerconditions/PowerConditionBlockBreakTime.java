package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class PowerConditionBlockBreakTime extends AbstractNumericPowerCondition {

    public PowerConditionBlockBreakTime() {
        this("block_break_time");
    }

    public PowerConditionBlockBreakTime(String type) {
        super(type);
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        Block block = context.block();
        if (block == null) {
            return null;
        }

        Entity entity = context.entity(condition.getSection().getString("target"), EntitySelector.SOURCE);
        if (!(entity instanceof Player player)) {
            return null;
        }

        float progressPerTick = block.getBreakSpeed(player);
        if (progressPerTick <= 0.0F) {
            return Double.POSITIVE_INFINITY;
        }

        double ticks = Math.max(1.0D, Math.ceil(1.0D / progressPerTick));
        String unit = condition.getSection().getString("unit", "TICKS");
        String normalized = unit == null ? "TICKS" : unit.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "SECOND", "SECONDS", "S" -> ticks / 20.0D;
            case "MILLISECOND", "MILLISECONDS", "MS" -> ticks * 50.0D;
            default -> ticks;
        };
    }
}

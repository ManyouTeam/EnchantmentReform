package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityAirChangeEvent;

public final class PowerConditionAirChange extends AbstractNumericPowerCondition {

    public PowerConditionAirChange() {
        super("air_change");
    }

    @Override
    protected Double currentValue(PowerContext context, ObjectSingleCondition condition) {
        if (!(context.event() instanceof EntityAirChangeEvent event)
                || !(event.getEntity() instanceof Player player)
                || context.triggerData() == null
                || context.result() == null) {
            return null;
        }
        int previous = context.triggerData().extra(BuiltinContextKeys.PREVIOUS_AIR)
                .orElse(player.getRemainingAir());
        return (double) (context.result().airAmount(context.triggerData()) - previous);
    }
}

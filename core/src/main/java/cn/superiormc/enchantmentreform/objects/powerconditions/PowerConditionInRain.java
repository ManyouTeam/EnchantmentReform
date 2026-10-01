package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.Entity;

public final class PowerConditionInRain extends AbstractPowerCondition {

    public PowerConditionInRain() {
        super("in_rain");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return entity != null && entity.isInRain() == condition.getSection().getBoolean("value", true);
    }
}

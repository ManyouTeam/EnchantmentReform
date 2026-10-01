package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionInLava extends AbstractPowerCondition {

    public PowerConditionInLava() {
        super("in_lava");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        var context = condition.getContext();
        Entity entity = context == null ? null : context.entity(
                condition.getSection().getString("target"), EntitySelector.TARGET);
        return entity != null && (entity.getLocation().getBlock().getType() == Material.LAVA)
                == condition.getSection().getBoolean("value", true);
    }
}

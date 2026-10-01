package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.entity.LivingEntity;

public final class PowerConditionHealthPercent extends AbstractPowerCondition {

    public PowerConditionHealthPercent() {
        super("health_percent");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null) {
            return false;
        }
        LivingEntity entity = condition.getContext().livingEntity(
                condition.getSection().getString("target"), EntitySelector.SOURCE);
        if (entity == null) {
            return false;
        }
        double maximum = CommonUtil.getMaxHealth(entity);
        double percent = maximum <= 0.0D ? 0.0D : entity.getHealth() / maximum * 100.0D;
        return maximum >= condition.getDouble("min-max-health", 0.0D, condition.getContext())
                && maximum <= condition.getDouble("max-max-health", Double.MAX_VALUE, condition.getContext())
                && percent >= condition.getDouble("min", 0.0D, condition.getContext())
                && percent <= condition.getDouble("max", 100.0D, condition.getContext());
    }
}

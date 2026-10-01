package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;

public final class PowerConditionHeadshot extends AbstractPowerCondition {

    public PowerConditionHeadshot() {
        super("headshot");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        if (condition.getContext() == null || !(condition.getContext().target() instanceof LivingEntity target)
                || !(condition.getContext().skill() instanceof Projectile projectile)) {
            return false;
        }
        double tolerance = condition.getDouble("tolerance", 0.45D, condition.getContext());
        return Math.abs(projectile.getLocation().getY() - target.getEyeLocation().getY()) <= tolerance;
    }
}

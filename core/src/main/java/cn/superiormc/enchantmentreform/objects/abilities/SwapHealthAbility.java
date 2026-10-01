package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public final class SwapHealthAbility extends AbstractAbility {

    public SwapHealthAbility(ConfigurationSection section) {
        super("SwapHealth", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.source() instanceof LivingEntity source) || !(context.target() instanceof LivingEntity target)) {
            return false;
        }
        double sourcePercent = source.getHealth() / CommonUtil.getMaxHealth(source);
        double targetPercent = target.getHealth() / CommonUtil.getMaxHealth(target);
        source.setHealth(Math.max(.1D, Math.min(CommonUtil.getMaxHealth(source), CommonUtil.getMaxHealth(source) * targetPercent)));
        target.setHealth(Math.max(.1D, Math.min(CommonUtil.getMaxHealth(target), CommonUtil.getMaxHealth(target) * sourcePercent)));
        return false;
    }
    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Location;

import java.util.Set;

public final class PowerConditionWorld extends AbstractPowerCondition {

    public PowerConditionWorld() {
        super("world");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        Location location = condition.getContext() == null ? null : condition.getContext().location();
        Set<String> worlds = CommonUtil.values(condition.getSection(), "worlds", "world");
        return !worlds.isEmpty() && location != null && location.getWorld() != null
                && worlds.contains(location.getWorld().getName().toUpperCase(java.util.Locale.ROOT));
    }
}

package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PreserveExperienceAbility extends AbstractAbility {

    public PreserveExperienceAbility(ConfigurationSection section) {
        super("PreserveExperience", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (!(context.source() instanceof Player player) || !(context.event() instanceof PlayerDeathEvent)) {
            return false;
        }
        int retained = (int) Math.round(player.getTotalExperience() * Math.max(0.0D, Math.min(1.0D, getDouble("ratio", .5D, context))));
        int current = context.result().experienceAmount(context.triggerData());
        context.result().experienceAmount(Math.max(0, current - retained));
        SchedulerUtil.runTaskLater(player, () -> player.giveExp(retained), 1L);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

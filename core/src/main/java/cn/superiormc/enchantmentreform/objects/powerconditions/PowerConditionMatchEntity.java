package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.managers.MatchEntityManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class PowerConditionMatchEntity extends AbstractPowerCondition {

    public PowerConditionMatchEntity() {
        super("match_entity");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        ConfigurationSection section = condition.getSection();
        PowerContext context = condition.getContext();
        if (context == null) {
            return false;
        }
        LivingEntity living = context.livingEntity(
                section.getString("target"), EntitySelector.TARGET);
        if (living == null) {
            return false;
        }
        Player player = context.source() instanceof Player source ? source : context.player();
        ConfigurationSection rules = section.getConfigurationSection("match");
        return MatchEntityManager.matchEntityManager.getMatch(rules == null ? section : rules, living, player, context);
    }
}

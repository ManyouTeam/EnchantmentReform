package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

public final class PowerConditionCatchMatchItem extends AbstractPowerCondition {

    public PowerConditionCatchMatchItem() {
        super("catch_match_item");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        PowerContext context = condition.getContext();
        if (context == null
                || !(context.event() instanceof PlayerFishEvent event)
                || !(event.getCaught() instanceof Item item)) {
            return false;
        }

        ConfigurationSection section = condition.getSection();
        ConfigurationSection rules = section.getConfigurationSection("match");
        ConfigurationSection actualRules = rules == null ? section : rules;
        return MatchItemManager.matchItemManager.getMatch(
                actualRules, event.getPlayer(), item.getItemStack(), context);
    }
}

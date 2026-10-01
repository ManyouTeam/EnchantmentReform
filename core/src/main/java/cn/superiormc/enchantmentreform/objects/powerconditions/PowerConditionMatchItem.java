package cn.superiormc.enchantmentreform.objects.powerconditions;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.ItemSelector;
import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.AbstractAbility;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PowerConditionMatchItem extends AbstractPowerCondition {

    public PowerConditionMatchItem() {
        super("match_item");
    }

    @Override
    protected boolean onMatch(ObjectSingleCondition condition) {
        ConfigurationSection section = condition.getSection();
        PowerContext context = condition.getContext();
        if (context == null) return false;

        ConfigurationSection selection = section.getConfigurationSection("item");
        String rawSelector = selection == null
                ? section.getString("item", "CONTEXT")
                : selection.getString("selector", "CONTEXT");
        String rawHolder = selection == null
                ? section.getString("item-holder", "TARGET")
                : selection.getString("holder", "TARGET");
        List<ItemStack> items = AbstractAbility.resolveItems(
                context,
                ItemSelector.parse(rawSelector, ItemSelector.CONTEXT),
                EntitySelector.parse(rawHolder, EntitySelector.TARGET));

        Player player = context.source() instanceof Player source ? source : context.player();
        ConfigurationSection rules = section.getConfigurationSection("match");
        ConfigurationSection actualRules = rules == null ? section : rules;
        return items.stream().anyMatch(item ->
                MatchItemManager.matchItemManager.getMatch(actualRules, player, item, context));
    }
}

package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.ChangesManager;
import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public final class ChangeItemAbility extends AbstractAbility {

    public ChangeItemAbility(ConfigurationSection section) {
        super("ChangeItem", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection match = getSection("match-item");
        ConfigurationSection changes = getSection("changes");
        Player source = context.source() instanceof Player player ? player : context.player();
        for (ItemStack item : getItems(context)) {
            if (MatchItemManager.matchItemManager.getMatch(match, source, item, context)) {
                ChangesManager.changesManager.setChange(changes, item, source, context);
            }
        }
        if (source != null && TriggerManager.triggerManager != null) {
            TriggerManager.triggerManager.activeEnchantments().clear(source);
        }
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

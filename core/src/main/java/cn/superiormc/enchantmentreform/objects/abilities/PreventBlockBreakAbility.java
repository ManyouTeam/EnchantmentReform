package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.power.BlockBreakProtectionUtil;
import cn.superiormc.enchantmentreform.power.BlockBreakProtectionUtil.MatchBlock;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public final class PreventBlockBreakAbility extends AbstractAbility {

    public PreventBlockBreakAbility(ConfigurationSection section) {
        super("PreventBlockBreak", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location location = getLocation(context);
        if (location == null || location.getWorld() == null) {
            return failure(context);
        }
        String scope = section.getString("scope", "PLAYER").trim().toUpperCase(Locale.ROOT);
        UUID player = null;
        if (scope.equals("PLAYER")) {
            if (!(getTargetEntity(context) instanceof Player target)) {
                return failure(context);
            }
            player = target.getUniqueId();
        } else if (!scope.equals("ALL")) {
            return failure(context);
        }
        MatchBlock match;
        try {
            match = MatchBlock.valueOf(
                    section.getString("match-block", "STATE").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return failure(context);
        }
        if (!BlockBreakProtectionUtil.protect(player, location.getBlock(), getInt("duration", 20, context), match)) {
            return failure(context);
        }
        return AbilityManager.abilityManager.execute(
                section.getConfigurationSection("abilities"), context.withBlock(location.getBlock()));
    }

    private boolean failure(PowerContext context) {
        return AbilityManager.abilityManager.execute(section.getConfigurationSection("failure-abilities"), context);
    }

    @Override
    public void onUnload() {
        BlockBreakProtectionUtil.clearProtection();
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

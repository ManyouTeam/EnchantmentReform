package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class SetItemCooldownAbility extends AbstractAbility {

    public SetItemCooldownAbility(ConfigurationSection section) {
        super("SetItemCooldown", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (!(entity instanceof Player player)) {
            return false;
        }
        String configured = getString("material", "SHIELD", context).strip();
        Material material = Material.matchMaterial(configured);
        if (material == null) {
            material = Material.matchMaterial(configured.toUpperCase(Locale.ROOT));
        }
        if (material == null || material.isAir()) {
            return false;
        }
        int ticks = Math.max(0, getInt("ticks", getInt("duration", 0, context), context));
        player.setCooldown(material, ticks);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

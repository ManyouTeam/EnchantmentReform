package cn.superiormc.enchantmentreform.objects.abilities;

import com.google.common.base.Enums;
import org.bukkit.EntityEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class VanillaAnimationAbility extends AbstractAbility {

    public VanillaAnimationAbility(ConfigurationSection section) {
        super("VanillaAnimation", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        if (entity == null || !entity.isValid()) {
            return false;
        }

        EntityEffect effect = Enums.getIfPresent(
                EntityEffect.class,
                getString("animation", getString("entity-effect", "HURT")).toUpperCase()
        ).orNull();

        if (effect == null) {
            return false;
        }

        entity.playEffect(effect);
        return false;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

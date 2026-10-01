package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class RemovePotionEffectAbility extends AbstractAbility {

    public RemovePotionEffectAbility(ConfigurationSection section) {
        super("RemovePotionEffect", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (!(target instanceof LivingEntity living)) {
            return false;
        }

        List<String> potionKeys = new ArrayList<>(getStringList("potions"));
        if (potionKeys.isEmpty()) {
            String potion = getString("potion", "");
            if (!potion.isBlank()) {
                potionKeys.add(potion);
            }
        }

        int limit = effectLimit(context);
        List<PotionEffectType> selected = PotionEffectSelector.parse(potionKeys)
                .selectEffects(living.getActivePotionEffects(), limit);
        int removed = 0;
        for (PotionEffectType type : selected) {
            if (living.getPotionEffect(type) == null) {
                continue;
            }
            living.removePotionEffect(type);
            removed++;
            if (removed >= limit) {
                break;
            }
        }

        return false;
    }

    private int effectLimit(PowerContext context) {
        int configured = getInt("max-effects", -1, context);
        return configured < 0 ? Integer.MAX_VALUE : configured;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

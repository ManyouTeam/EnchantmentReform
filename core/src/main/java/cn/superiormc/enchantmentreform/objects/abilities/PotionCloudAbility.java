package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionCloudAbility extends AbstractAbility {

    public PotionCloudAbility(ConfigurationSection section) {
        super("PotionCloud", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Location loc = getLocation(context);
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        AreaEffectCloud cloud = loc.getWorld().spawn(loc, AreaEffectCloud.class);
        cloud.setRadius((float) getDouble("radius", 3.0, context));
        cloud.setDuration(getInt("duration", 120, context));
        int pDuration = getInt("potion-duration", 100, context);
        int pAmp = getInt("potion-amplifier", 1, context);
        PotionEffectType potionEffectType = (PotionEffectType) Registry.EFFECT.get(CommonUtil.parseNamespacedKey(getString("potion", "POISON")));
        if (potionEffectType != null) {
            if (getBoolean("accumulate", false)) {
                for (Entity nearby : loc.getWorld().getNearbyEntities(loc, cloud.getRadius(), cloud.getRadius(), cloud.getRadius())) {
                    if (!(nearby instanceof LivingEntity living)) {
                        continue;
                    }
                    PotionEffect existing = living.getPotionEffect(potionEffectType);
                    if (existing != null) {
                        pDuration = accumulateDuration(existing.getDuration(), pDuration);
                        pAmp = Math.max(pAmp, existing.getAmplifier());
                        break;
                    }
                }
            }
            cloud.addCustomEffect(new PotionEffect(potionEffectType, pDuration, pAmp), true);
        }
        return false;
    }

    private int accumulateDuration(int existingDuration, int newDuration) {
        if (existingDuration == PotionEffect.INFINITE_DURATION || newDuration == PotionEffect.INFINITE_DURATION) {
            return PotionEffect.INFINITE_DURATION;
        }
        long total = (long) existingDuration + newDuration;
        return total >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }

}

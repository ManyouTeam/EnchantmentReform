package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public final class ExtendPotionEffectsAbility extends AbstractAbility {

    /** Guards against reentrancy: addPotionEffect/removePotionEffect re-fire the effect event. */
    private static final ThreadLocal<Boolean> APPLYING = ThreadLocal.withInitial(() -> false);

    public ExtendPotionEffectsAbility(ConfigurationSection section) {
        super("ExtendPotionEffects", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        if (APPLYING.get()) {
            return false;
        }
        Entity target = getTargetEntity(context);
        if (!(target instanceof LivingEntity living)) {
            return false;
        }

        List<String> configured = new ArrayList<>(getStringList("potions"));
        if (configured.isEmpty()) {
            String potion = getString("potion", "HARMFUL");
            if (!potion.isBlank()) {
                configured.add(potion);
            }
        }

        double percentage = getDouble("percentage", 0.0D, context);
        double multiplier = section.contains("multiplier")
                ? getDouble("multiplier", 1.0D, context)
                : 1.0D + percentage / 100.0D;
        if (multiplier <= 0.0D) {
            return false;
        }

        PotionEffectSelector selector = PotionEffectSelector.parse(configured);
        if (selector.isEmpty()) {
            return false;
        }

        // When fired from an on-potion-effect trigger, operate directly on the effect that is
        // being applied (or changed) so newly gained effects are captured before they land.
        if (context.event() instanceof EntityPotionEffectEvent event) {
            return modifyIncomingEffect(event, living, selector, multiplier);
        }

        int limit = getInt("max-effects", -1, context);
        if (limit < 0) {
            limit = Integer.MAX_VALUE;
        }
        List<PotionEffectType> selected = selector.selectEffects(living.getActivePotionEffects(), limit);
        for (PotionEffectType type : selected) {
            PotionEffect existing = living.getPotionEffect(type);
            if (existing == null || existing.getDuration() == PotionEffect.INFINITE_DURATION) {
                continue;
            }
            int duration = scaledDuration(existing.getDuration(), multiplier);
            if (duration == existing.getDuration()) {
                continue;
            }
            if (duration <= 0) {
                applyWithoutRecursion(() -> living.removePotionEffect(type));
            } else {
                applyWithoutRecursion(() -> living.addPotionEffect(new PotionEffect(
                        type,
                        duration,
                        existing.getAmplifier(),
                        existing.isAmbient(),
                        existing.hasParticles(),
                        existing.hasIcon())));
            }
        }
        return false;
    }

    private boolean modifyIncomingEffect(EntityPotionEffectEvent event,
                                         LivingEntity living,
                                         PotionEffectSelector selector,
                                         double multiplier) {
        PotionEffect incoming = event.getNewEffect();
        if (incoming == null) {
            return false;
        }
        if (!selector.matches(incoming.getType())) {
            return false;
        }
        if (incoming.getDuration() == PotionEffect.INFINITE_DURATION) {
            return false;
        }
        int duration = scaledDuration(incoming.getDuration(), multiplier);
        if (duration == incoming.getDuration()) {
            return false;
        }
        if (duration <= 0) {
            event.setCancelled(true);
            applyWithoutRecursion(() -> living.removePotionEffect(incoming.getType()));
            return true;
        }
        event.setCancelled(true);
        applyWithoutRecursion(() -> living.addPotionEffect(new PotionEffect(
                incoming.getType(),
                duration,
                incoming.getAmplifier(),
                incoming.isAmbient(),
                incoming.hasParticles(),
                incoming.hasIcon())));
        return true;
    }

    private void applyWithoutRecursion(Runnable action) {
        APPLYING.set(true);
        try {
            action.run();
        } finally {
            APPLYING.set(false);
        }
    }

    private int scaledDuration(int duration, double multiplier) {
        double calculated = duration * multiplier;
        if (!Double.isFinite(calculated) || calculated >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(calculated);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }
}

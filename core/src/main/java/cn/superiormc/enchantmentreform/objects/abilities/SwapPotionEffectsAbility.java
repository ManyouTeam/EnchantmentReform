package cn.superiormc.enchantmentreform.objects.abilities;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SwapPotionEffectsAbility extends AbstractAbility {

    public SwapPotionEffectsAbility(ConfigurationSection section) {
        super("SwapPotionEffects", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity sourceEntity = getSourceEntity(context);
        Entity targetEntity = getTargetEntity(context);
        if (!(sourceEntity instanceof LivingEntity source) || !(targetEntity instanceof LivingEntity target)) {
            return false;
        }
        if (source == target) {
            return false;
        }

        PotionEffectSelector selector = selector();
        int limit = effectLimit(context);
        if (limit <= 0) {
            return false;
        }

        switch (direction()) {
            case SOURCE_TO_TARGET -> transfer(source, target, selector, limit);
            case TARGET_TO_SOURCE -> transfer(target, source, selector, limit);
            case SWAP -> swap(source, target, selector, limit);
        }
        return false;
    }

    private void transfer(LivingEntity from, LivingEntity to, PotionEffectSelector selector, int limit) {
        List<PotionEffectType> selected = selector.selectEffects(from.getActivePotionEffects(), limit);
        int transferred = 0;
        for (PotionEffectType type : selected) {
            PotionEffect effect = from.getPotionEffect(type);
            if (effect == null) {
                continue;
            }
            from.removePotionEffect(type);
            to.addPotionEffect(effect, true);
            transferred++;
            if (transferred >= limit) {
                break;
            }
        }
    }

    private void swap(LivingEntity source, LivingEntity target, PotionEffectSelector selector, int limit) {
        Set<PotionEffectType> available = new LinkedHashSet<>();
        source.getActivePotionEffects().forEach(effect -> available.add(effect.getType()));
        target.getActivePotionEffects().forEach(effect -> available.add(effect.getType()));
        for (PotionEffectType type : selector.selectTypes(available, limit)) {
            PotionEffect sourceEffect = source.getPotionEffect(type);
            PotionEffect targetEffect = target.getPotionEffect(type);
            if (sourceEffect == null && targetEffect == null) {
                continue;
            }
            if (sourceEffect != null) {
                source.removePotionEffect(type);
            }
            if (targetEffect != null) {
                target.removePotionEffect(type);
            }
            if (targetEffect != null) {
                source.addPotionEffect(targetEffect, true);
            }
            if (sourceEffect != null) {
                target.addPotionEffect(sourceEffect, true);
            }
        }
    }

    private PotionEffectSelector selector() {
        List<String> potionKeys = new ArrayList<>(getStringList("potions"));
        if (potionKeys.isEmpty()) {
            String potion = getString("potion", "");
            if (!potion.isBlank()) {
                potionKeys.add(potion);
            }
        }
        if (potionKeys.isEmpty()) {
            potionKeys.add("ALL");
        }
        return PotionEffectSelector.parse(potionKeys);
    }

    private int effectLimit(PowerContext context) {
        int configured = getInt("max-effects", -1, context);
        return configured < 0 ? Integer.MAX_VALUE : configured;
    }

    private Direction direction() {
        String configured = section.getString("direction", "SOURCE_TO_TARGET")
                .trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return switch (configured) {
            case "TARGET_TO_SOURCE", "TARGET->SOURCE", "TARGET-SOURCE" -> Direction.TARGET_TO_SOURCE;
            case "SWAP", "BOTH", "BIDIRECTIONAL", "SOURCE<->TARGET" -> Direction.SWAP;
            default -> Direction.SOURCE_TO_TARGET;
        };
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.TARGET;
    }

    private enum Direction {
        SOURCE_TO_TARGET,
        TARGET_TO_SOURCE,
        SWAP
    }

}

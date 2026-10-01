package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.MatchEntityManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;

public class NearbyEntitiesAbility extends AbstractAbility {

    public NearbyEntitiesAbility(ConfigurationSection section) {
        super("NearbyEntities", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        ConfigurationSection abilities = section.getConfigurationSection("abilities");
        Location baseLocation = getBaseLocation(context);
        if (abilities == null || baseLocation == null || baseLocation.getWorld() == null) {
            return false;
        }
        executeAround(baseLocation, abilities, context);
        return false;
    }

    private Location getBaseLocation(PowerContext context) {
        Entity target = getTargetEntity(context);
        if (target != null) {
            return target.getLocation();
        }
        return getLocation(context);
    }

    private void executeAround(Location baseLocation, ConfigurationSection abilities, PowerContext context) {
        double radius = Math.max(0, getDouble("radius", 5, context));
        double radiusX = Math.max(0, getDouble("radius-x", radius, context));
        double radiusY = Math.max(0, getDouble("radius-y", radius, context));
        double radiusZ = Math.max(0, getDouble("radius-z", radius, context));

        List<Entity> candidates = baseLocation.getWorld()
                .getNearbyEntities(baseLocation, radiusX, radiusY, radiusZ)
                .stream().filter(entity -> acceptEntity(entity, context)
                        && insideConfiguredCone(entity, context))
                .sorted(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(baseLocation)))
                .toList();
        int maximumTargets = Math.max(0, getInt("max-targets", 0, context));
        int accepted = 0;
        for (Entity entity : candidates) {
            AbilityManager.abilityManager.execute(abilities, context.withTarget(entity));
            accepted++;
            if (maximumTargets > 0 && accepted >= maximumTargets) {
                break;
            }
        }
        if (accepted > 0) {
            AbilityManager.abilityManager.execute(
                    section.getConfigurationSection("after-abilities"),
                    withTargetCount(context, accepted));
        }
    }

    private boolean acceptEntity(Entity entity, PowerContext context) {
        if (!entity.isValid()) {
            return false;
        }
        Entity source = context.source();
        if (!section.getBoolean("include-source", false)
                && source != null && entity.getUniqueId().equals(source.getUniqueId())) {
            return false;
        }
        Entity originalTarget = context.target();
        if (section.getBoolean("exclude-target", false)
                && originalTarget != null
                && entity.getUniqueId().equals(originalTarget.getUniqueId())) {
            return false;
        }
        if (entity instanceof Item) {
            return section.getBoolean("include-items", false);
        }
        if (!(entity instanceof LivingEntity living)) {
            return section.getBoolean("include-non-living", false);
        }
        if (living instanceof ArmorStand
                && !section.getBoolean("include-armor-stands", false)) {
            return false;
        }
        if (!section.getBoolean("include-living", true)) {
            return false;
        }
        return MatchEntityManager.matchEntityManager.getMatch(
                section.getConfigurationSection("match-entity"), living);
    }

    private boolean insideConfiguredCone(Entity candidate, PowerContext context) {
        if (!section.contains("max-angle")) {
            return true;
        }
        double maximumAngle = Math.max(0.0D,
                Math.min(180.0D, getDouble("max-angle", 180.0D, context)));
        Entity directionSource = context.entity(EntitySelector.parse(
                section.getString("direction-source"), EntitySelector.SOURCE));
        if (directionSource == null) {
            return false;
        }
        Location origin = directionSource instanceof LivingEntity living
                ? living.getEyeLocation() : directionSource.getLocation();
        Vector look = origin.getDirection();
        Location candidateCenter = candidate instanceof LivingEntity living
                ? living.getLocation().add(0.0D, living.getHeight() * 0.5D, 0.0D)
                : candidate.getLocation();
        Vector toCandidate = candidateCenter.toVector().subtract(origin.toVector());
        if (look.lengthSquared() < 1.0E-8D || toCandidate.lengthSquared() < 1.0E-8D) {
            return true;
        }
        double cosine = look.normalize().dot(toCandidate.normalize());
        return cosine >= Math.cos(Math.toRadians(maximumAngle));
    }

    private PowerContext withTargetCount(PowerContext context, int accepted) {
        TriggerData triggerData = context.triggerData();
        if (triggerData == null) {
            return context;
        }
        TriggerData changed = triggerData.toBuilder()
                .extra(BuiltinContextKeys.TARGET_COUNT, accepted)
                .build();
        return context.withTriggerData(changed);
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }
}

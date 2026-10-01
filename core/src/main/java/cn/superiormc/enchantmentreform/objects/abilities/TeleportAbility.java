package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.LocationSelector;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class TeleportAbility extends AbstractAbility {

    public TeleportAbility(ConfigurationSection section) {
        super("Teleport", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity entity = getTargetEntity(context);
        Location location = getTeleportLocation(context, entity);
        if (entity != null && location != null) {
            SchedulerUtil.teleport(entity, location);
        }
        return false;
    }

    private Location getTeleportLocation(PowerContext context, Entity entity) {
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        String selector = locationSection == null
                ? getString("location", "CONTEXT", context)
                : getString("location.target", "CONTEXT", context);
        ImportantLocation importantLocation = ImportantLocation.parse(selector);
        if (importantLocation == null) {
            return LocationSelector.parse(selector, null) == null ? null : getLocation(context);
        }

        Entity owner = getLocationOwner(context, entity, locationSection);
        Location location = switch (importantLocation) {
            case BED_SPAWN -> owner instanceof Player player ? player.getBedSpawnLocation() : null;
            case RESPAWN -> getRespawnLocation(owner);
            case WORLD_SPAWN -> getWorldSpawn(context, owner, locationSection);
            case MAIN_WORLD_SPAWN -> getMainWorldSpawn();
            case LAST_DEATH -> owner instanceof Player player ? player.getLastDeathLocation() : null;
            case COMPASS_TARGET -> owner instanceof Player player ? player.getCompassTarget() : null;
        };
        return applyOffset(location, context, locationSection);
    }

    private Entity getLocationOwner(PowerContext context, Entity entity, ConfigurationSection locationSection) {
        String path = locationSection == null ? "location-owner" : "location.owner";
        String rawOwner = getString(path, "", context);
        if (rawOwner.isBlank()) {
            return entity;
        }
        EntitySelector selector = EntitySelector.parse(rawOwner, null);
        return selector == null ? entity : context.entity(selector);
    }

    private Location getRespawnLocation(Entity owner) {
        if (!(owner instanceof Player player)) {
            return null;
        }
        Location respawn = player.getRespawnLocation();
        return respawn == null ? player.getWorld().getSpawnLocation() : respawn;
    }

    private World getWorld(PowerContext context, Entity owner, ConfigurationSection locationSection) {
        String path = locationSection == null ? "world" : "location.world";
        String worldName = getString(path, "", context);
        if (!worldName.isBlank()) {
            return Bukkit.getWorld(worldName);
        }
        if (owner != null) {
            return owner.getWorld();
        }
        if (context.player() != null) {
            return context.player().getWorld();
        }
        if (context.source() != null) {
            return context.source().getWorld();
        }
        List<World> worlds = Bukkit.getWorlds();
        return worlds.isEmpty() ? null : worlds.getFirst();
    }

    private Location getWorldSpawn(PowerContext context, Entity owner, ConfigurationSection locationSection) {
        World world = getWorld(context, owner, locationSection);
        return world == null ? null : world.getSpawnLocation();
    }

    private Location getMainWorldSpawn() {
        List<World> worlds = Bukkit.getWorlds();
        return worlds.isEmpty() ? null : worlds.getFirst().getSpawnLocation();
    }

    private Location applyOffset(Location location, PowerContext context, ConfigurationSection locationSection) {
        if (location == null) {
            return null;
        }
        String offset = locationSection == null ? "offset" : "location.offset";
        return location.clone().add(
                getDouble(offset + ".x", 0.0D, context),
                getDouble(offset + ".y", 0.0D, context),
                getDouble(offset + ".z", 0.0D, context));
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private enum ImportantLocation {
        BED_SPAWN,
        RESPAWN,
        WORLD_SPAWN,
        MAIN_WORLD_SPAWN,
        LAST_DEATH,
        COMPASS_TARGET;

        private static ImportantLocation parse(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            normalized = switch (normalized) {
                case "BED", "BED_SPAWN_LOCATION" -> "BED_SPAWN";
                case "RESPAWN_POINT", "RESPAWN_LOCATION", "PERSONAL_SPAWN" -> "RESPAWN";
                case "SPAWN", "CURRENT_WORLD_SPAWN" -> "WORLD_SPAWN";
                case "SERVER_SPAWN", "DEFAULT_WORLD_SPAWN" -> "MAIN_WORLD_SPAWN";
                case "LAST_DEATH_LOCATION" -> "LAST_DEATH";
                case "COMPASS" -> "COMPASS_TARGET";
                default -> normalized;
            };
            try {
                return valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }
}

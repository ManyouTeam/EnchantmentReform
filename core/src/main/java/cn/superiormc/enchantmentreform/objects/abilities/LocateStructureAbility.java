package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.TextUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.util.StructureSearchResult;

import java.util.Locale;

public final class LocateStructureAbility extends AbstractAbility {

    public LocateStructureAbility(ConfigurationSection section) {
        super("LocateStructure", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity source = context.source();
        if (!(source instanceof Player player)) {
            return false;
        }

        Registry<StructureType> registry = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.STRUCTURE_TYPE);
        int radius = Math.max(1, getInt("radius", 32, context));
        boolean findUnexplored = getBoolean("find-unexplored", false);
        LocatedStructure nearest = null;

        for (String configured : getStringList("structures", player, context)) {
            NamespacedKey key = parseStructureTypeKey(configured);
            if (key == null) {
                continue;
            }
            StructureType type = registry.get(key);
            if (type == null) {
                continue;
            }

            StructureSearchResult result = player.getWorld().locateNearestStructure(
                    player.getLocation(), type, radius, findUnexplored);
            if (result == null) {
                continue;
            }

            Location found = result.getLocation();
            if (nearest == null
                    || found.distanceSquared(player.getLocation())
                    < nearest.location().distanceSquared(player.getLocation())) {
                nearest = new LocatedStructure(
                        found,
                        result.getStructure().getStructureType().getKey().toString(),
                        configured);
            }
        }

        if (nearest == null) {
            sendMessage(player, getString("not-found", "", context));
            return false;
        }

        Location found = nearest.location();
        String structureName = displayName(
                "structure-names", nearest.structureKey(), nearest.configured(), context);
        String message = getString("found", "", context,
                "distance", Integer.toString((int) Math.round(found.distance(player.getLocation()))),
                "direction", direction(context, player.getLocation(), found),
                "x", Integer.toString(found.getBlockX()),
                "y", Integer.toString(found.getBlockY()),
                "z", Integer.toString(found.getBlockZ()),
                "structure", structureName,
                "structure_key", nearest.structureKey());
        sendMessage(player, message);
        return false;
    }

    private NamespacedKey parseStructureTypeKey(String configured) {
        if (configured == null || configured.isBlank()) {
            return null;
        }
        String normalized = configured.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains(":")) {
            normalized = NamespacedKey.MINECRAFT + ":" + normalized;
        }
        return NamespacedKey.fromString(normalized);
    }

    private String direction(PowerContext context, Location from, Location to) {
        double x = to.getX() - from.getX();
        double z = to.getZ() - from.getZ();
        String key;
        if (Math.abs(x) > Math.abs(z)) {
            key = x >= 0 ? "east" : "west";
        } else {
            key = z >= 0 ? "south" : "north";
        }
        return getString("directions." + key, key, context);
    }

    private String displayName(String sectionName,
                               String namespacedKey,
                               String configured,
                               PowerContext context) {
        ConfigurationSection names = section.getConfigurationSection(sectionName);
        if (names == null) {
            return namespacedKey;
        }
        String pathOnly = namespacedKey.substring(namespacedKey.indexOf(':') + 1);
        for (String key : names.getKeys(false)) {
            if (key.equalsIgnoreCase(namespacedKey)
                    || key.equalsIgnoreCase(pathOnly)
                    || key.equalsIgnoreCase(configured)) {
                return getString(sectionName + "." + key, namespacedKey, context);
            }
        }
        return namespacedKey;
    }

    private void sendMessage(Player player, String message) {
        if (!message.isBlank()) {
            TextUtil.sendMessage(player, message);
        }
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private record LocatedStructure(Location location, String structureKey, String configured) {
    }
}

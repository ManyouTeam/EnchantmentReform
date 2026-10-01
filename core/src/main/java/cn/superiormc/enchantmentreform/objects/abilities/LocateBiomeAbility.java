package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BiomeSearchResult;

import java.util.ArrayList;
import java.util.List;

public final class LocateBiomeAbility extends AbstractAbility {

    public LocateBiomeAbility(ConfigurationSection section) {
        super("LocateBiome", section);
    }

    @Override
    public boolean execute(PowerContext context) {
        Entity source = context.source();
        if (!(source instanceof Player player)) {
            return false;
        }

        Registry<Biome> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        List<Biome> biomes = new ArrayList<>();
        for (String configured : getStringList("biomes", player, context)) {
            NamespacedKey key = CommonUtil.parseNamespacedKey(configured);
            if (key == null) {
                continue;
            }
            Biome biome = registry.get(key);
            if (biome != null && !biomes.contains(biome)) {
                biomes.add(biome);
            }
        }

        if (biomes.isEmpty()) {
            sendMessage(player, getString("not-found", "", context));
            return false;
        }

        int radius = Math.max(1, getInt("radius", 1024, context));
        int horizontalInterval = Math.max(1, getInt("horizontal-interval", 32, context));
        int verticalInterval = Math.max(1, getInt("vertical-interval", 64, context));
        BiomeSearchResult result = player.getWorld().locateNearestBiome(
                player.getLocation(), radius, horizontalInterval, verticalInterval,
                biomes.toArray(Biome[]::new));
        if (result == null) {
            sendMessage(player, getString("not-found", "", context));
            return false;
        }

        Location found = result.getLocation();
        String biomeKey = result.getBiome().getKey().toString();
        String biomeName = displayName("biome-names", biomeKey, context);
        String message = getString("found", "", context,
                "distance", Integer.toString((int) Math.round(found.distance(player.getLocation()))),
                "direction", direction(context, player.getLocation(), found),
                "x", Integer.toString(found.getBlockX()),
                "y", Integer.toString(found.getBlockY()),
                "z", Integer.toString(found.getBlockZ()),
                "biome", biomeName,
                "biome_key", biomeKey);
        sendMessage(player, message);
        return false;
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

    private String displayName(String sectionName, String namespacedKey, PowerContext context) {
        ConfigurationSection names = section.getConfigurationSection(sectionName);
        if (names == null) {
            return namespacedKey;
        }
        String pathOnly = namespacedKey.substring(namespacedKey.indexOf(':') + 1);
        for (String key : names.getKeys(false)) {
            if (key.equalsIgnoreCase(namespacedKey) || key.equalsIgnoreCase(pathOnly)) {
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
}

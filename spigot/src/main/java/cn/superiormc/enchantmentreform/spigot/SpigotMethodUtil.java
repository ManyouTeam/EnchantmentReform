package cn.superiormc.enchantmentreform.spigot;

import cn.superiormc.enchantmentreform.spigot.listeners.SpigotTempBlockListener;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.SpecialMethodUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.projectiles.ProjectileSource;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotMethodUtil implements SpecialMethodUtil {

    private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"(https?://textures\\.minecraft\\.net/texture/[^\"]+)\"");
    private final Map<UUID, Map<String, BossBar>> bossBarCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> bossBarGenerations = new ConcurrentHashMap<>();

    public Map<String, PlayerProfile> playerProfiles = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PlayerProfile> eldest) {
                    return size() > 256;
                }
            });

    @Override
    public String methodID() {
        return "spigot";
    }

    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void dispatchCommand(Player player, String command) {
        Bukkit.dispatchCommand(player, command);
    }

    @Override
    public void dispatchOpCommand(Player player, String command) {
        boolean playerIsOp = player.isOp();
        try {
            player.setOp(true);
            Bukkit.dispatchCommand(player, command);
        } finally {
            player.setOp(playerIsOp);
        }
    }

    @Override
    public ItemStack getItemObject(Object object) {
        if (object instanceof ItemStack) {
            return (ItemStack) object;
        }
        return null;
    }

    @Override
    public Object makeItemToObject(ItemStack item) {
        return item;
    }

    @Override
    public void spawnEntity(Location location, EntityType entity) {
        location.getWorld().spawnEntity(location, entity);
    }

    @Override
    public void playerTeleport(Player player, Location location) {
        player.teleport(location);
    }

    @Override
    public SkullMeta setSkullMeta(SkullMeta meta, String skull) {
        if (!CommonUtil.getMajorVersion(19)) {
            return meta;
        }
        if (skull.length() > 16) {
            try {
                URL skinUrl = resolveSkinUrl(skull);
                if (skinUrl == null) {
                    return meta;
                }
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "custom_head");
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(skinUrl);
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!playerProfiles.containsKey(skull)) {
                playerProfiles.put(skull, Bukkit.getOfflinePlayer(skull).getPlayerProfile());
            }
            meta.setOwnerProfile(playerProfiles.get(skull));
        }
        return meta;
    }

    @Override
    public String serializeSkull(SkullMeta meta) {
        if (!CommonUtil.getMajorVersion(19)) {
            return null;
        }
        try {
            PlayerProfile ownerProfile = meta.getOwnerProfile();
            if (ownerProfile != null) {
                String name = ownerProfile.getName();
                if (name != null && !name.trim().isEmpty()) {
                    return name;
                }
                URL skinUrl = ownerProfile.getTextures().getSkin();
                if (skinUrl != null) {
                    return encodeSkinUrl(skinUrl);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    @Override
    public void setItemName(ItemMeta meta, String name, Player player) {
        meta.setDisplayName(TextUtil.parse(name, player));
    }

    @Override
    public void setItemItemName(ItemMeta meta, String itemName, Player player) {
        if (itemName.isEmpty()) {
            meta.setItemName(" ");
        } else {
            meta.setItemName(TextUtil.parse(itemName, player));
        }
    }

    @Override
    public void setItemLore(ItemMeta meta, List<String> lores, Player player) {
        if (lores == null) {
            meta.setLore(null);
            return;
        }
        List<String> newLore = new ArrayList<>();
        for (String lore : lores) {
            for (String singleLore : lore.split("\\\\n")) {
                if (singleLore.isEmpty()) {
                    newLore.add(" ");
                    continue;
                }
                newLore.add(TextUtil.parse(singleLore, player));
            }
        }
        meta.setLore(newLore);
    }

    @Override
    public void sendChat(Player player, String text) {
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(TextUtil.parse(text));
        } else {
            player.sendMessage(TextUtil.parse(text, player));
        }
    }

    @Override
    public void sendHoverChat(Player player, String text, String command) {
        if (player != null) {
            player.spigot().sendMessage(net.md_5.bungee.api.chat.TextComponent.fromLegacyText(TextUtil.parse(text, player)));
        }
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (player != null) {
            player.sendTitle(TextUtil.parse(title, player), TextUtil.parse(subTitle, player), fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        if (player == null) {
            return;
        }
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(TextUtil.parse(message, player))
        );
    }

    @Override
    public void sendBossBar(Player player, String title, float progress, String color, String style) {
        sendBossBar(player, title, progress, color, style, null);
    }

    @Override
    public void sendBossBar(Player player, String title, float progress, String color, String style, String key) {
        if (player == null) {
            return;
        }
        String barKey = key == null || key.isEmpty() ? "temp-" + UUID.randomUUID() : key;
        Map<String, BossBar> playerBars = bossBarCache.computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>());
        BossBar bar = playerBars.get(barKey);
        title = TextUtil.parse(title, player);
        if (bar == null) {
            bar = Bukkit.createBossBar(
                    title,
                    color == null ? BarColor.WHITE : BarColor.valueOf(color.toUpperCase()),
                    style == null ? BarStyle.SOLID : BarStyle.valueOf(style.toUpperCase())
            );
            bar.addPlayer(player);
            bar.setVisible(true);
            playerBars.put(barKey, bar);
        } else {
            bar.setTitle(title);
            bar.setColor(color == null ? BarColor.WHITE : BarColor.valueOf(color.toUpperCase()));
            bar.setStyle(style == null ? BarStyle.SOLID : BarStyle.valueOf(style.toUpperCase()));
        }
        bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        Map<String, Long> generations = bossBarGenerations.computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>());
        long generation = generations.getOrDefault(barKey, 0L) + 1L;
        generations.put(barKey, generation);
        BossBar finalBar = bar;
        long finalGeneration = generation;
        UUID playerId = player.getUniqueId();
        SchedulerUtil.runTaskLater(player, () -> {
            Long currentGeneration = generations.get(barKey);
            if (currentGeneration != null && currentGeneration != finalGeneration) {
                return;
            }
            finalBar.removeAll();
            playerBars.remove(barKey);
            generations.remove(barKey);
            removeEmptyBossBarCaches(playerId, playerBars, generations);
        }, 60);
    }

    @Override
    public void hideBossBar(Player player, String key) {
        if (player == null || key == null || key.isEmpty()) {
            return;
        }
        Map<String, BossBar> playerBars = bossBarCache.get(player.getUniqueId());
        if (playerBars == null) {
            return;
        }
        BossBar bar = playerBars.remove(key);
        if (bar != null) {
            bar.removeAll();
        }
        // Keep the generation until the pending delayed task runs, so an older task
        // cannot remove a newer BossBar that reuses this key.
        removeEmptyBossBarCaches(player.getUniqueId(), playerBars, null);
    }

    @Override
    public void clearBossBars(Player player) {
        Map<String, BossBar> playerBars = bossBarCache.remove(player.getUniqueId());
        if (playerBars != null) {
            for (BossBar bar : playerBars.values()) {
                bar.removeAll();
            }
        }
        bossBarGenerations.remove(player.getUniqueId());
    }

    private void removeEmptyBossBarCaches(UUID playerId,
                                           Map<String, BossBar> playerBars,
                                           Map<String, Long> generations) {
        if (playerBars != null && playerBars.isEmpty()) {
            bossBarCache.remove(playerId, playerBars);
        }
        if (generations != null && generations.isEmpty()) {
            bossBarGenerations.remove(playerId, generations);
        }
    }

    @Override
    public Inventory createNewInv(Player player, int size, String text, InventoryHolder holder) {
        return Bukkit.createInventory(holder, size, TextUtil.parse(text, player));
    }

    @Override
    public String legacyParse(String text) {
        if (text == null) {
            return "";
        }
        return TextUtil.colorize(text);
    }

    @Override
    public String getItemName(ItemMeta meta) {
        return meta.getDisplayName();
    }

    @Override
    public String getItemItemName(ItemMeta meta) {
        return meta.getItemName();
    }

    @Override
    public List<String> getItemLore(ItemMeta meta) {
        return meta.getLore();
    }

    @Override
    public String getEntityName(LivingEntity entity) {
        if (entity.getCustomName() != null) {
            return entity.getCustomName();
        }
        return entity.getName();
    }

    @Override
    public void setEntityName(LivingEntity entity, String name) {
        entity.setCustomName(TextUtil.parse(name));
    }

    @Override
    public String getEntityCustomName(LivingEntity entity) {
        return entity.getCustomName();
    }

    @Override
    public ItemStack editItemStack(ItemStack item, Player player, ConfigurationSection section, int amount, String... args) {
        return item;
    }

    @Override
    public ConfigurationSection serializeItemStack(ItemStack item) {
        return null;
    }

    @Override
    public void dropPrivateItem(Player player, ItemStack itemStack, Location loc) {
        dropItem(player, itemStack, loc);
    }

    @Override
    public Item dropItem(Player player, ItemStack itemStack, Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return null;
        }
        return world.dropItemNaturally(loc, itemStack);
    }

    @Override
    public Entity getDamager(Entity damager) {
        if (damager instanceof Player) {
            return damager;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Entity entity) {
                return entity;
            }
        }

        if (damager instanceof TNTPrimed tnt) {
            return tnt.getSource();
        }

        if (damager instanceof AreaEffectCloud cloud) {
            ProjectileSource source = cloud.getSource();
            if (source instanceof Entity entity) {
                return entity;
            }
        }

        if (damager instanceof Tameable tameable) {
            AnimalTamer owner = tameable.getOwner();
            if (owner instanceof Entity entity) {
                return entity;
            }
        }

        if (damager instanceof EvokerFangs evoker) {
            return evoker.getOwner();
        }
        return null;
    }

    @Override
    public void tempBlockManager() {
        new SpigotTempBlockListener();
    }

    @Override
    public void createExplosion(Location loc, Entity entity, float yield, boolean setFire, boolean breakBlocks) {
        loc.getWorld().createExplosion(loc, yield, setFire, breakBlocks);
    }

    private URL resolveSkinUrl(String skull) throws Exception {
        if (skull == null) {
            return null;
        }
        String trimmedSkull = skull.trim();
        if (trimmedSkull.isEmpty()) {
            return null;
        }
        if (trimmedSkull.startsWith("http://textures.minecraft.net/texture/")
                || trimmedSkull.startsWith("https://textures.minecraft.net/texture/")) {
            return new URL(trimmedSkull);
        }
        String json = new String(Base64.getDecoder().decode(trimmedSkull), StandardCharsets.UTF_8);
        Matcher matcher = TEXTURE_URL_PATTERN.matcher(json);
        if (matcher.find()) {
            return new URL(matcher.group(1));
        }
        return null;
    }

    private String encodeSkinUrl(URL skinUrl) {
        String textureJson = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(textureJson.getBytes(StandardCharsets.UTF_8));
    }
}

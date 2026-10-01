package cn.superiormc.enchantmentreform.paper;

import cn.superiormc.enchantmentreform.listeners.EnchantmentPowerListener;
import cn.superiormc.enchantmentreform.listeners.PaperEnchantmentPowerListener;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.paper.methods.BuildItemPaper;
import cn.superiormc.enchantmentreform.paper.methods.DebuildItemPaper;
import cn.superiormc.enchantmentreform.paper.listeners.PaperTempBlockListener;
import cn.superiormc.enchantmentreform.paper.utils.PaperTextUtil;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SchedulerUtil;
import cn.superiormc.enchantmentreform.utils.SpecialMethodUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PaperMethodUtil implements SpecialMethodUtil {

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
        return "paper";
    }

    @Override
    public void dispatchCommand(String command) {
        SchedulerUtil.runSync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public void dispatchCommand(Player player, String command) {
        SchedulerUtil.runSync(player, () -> Bukkit.dispatchCommand(player, command));
    }

    @Override
    public void dispatchOpCommand(Player player, String command) {
        SchedulerUtil.runSync(player, () -> runOpCommand(player, command));
    }

    private void runOpCommand(Player player, String command) {
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
        if (CommonUtil.getMajorVersion(15)) {
            return ItemStack.deserializeBytes((byte[]) object);
        }
        return null;
    }

    @Override
    public Object makeItemToObject(ItemStack item) {
        if (CommonUtil.getMajorVersion(15)) {
            return item.serializeAsBytes();
        }
        return item;
    }

    @Override
    public void spawnEntity(Location location, EntityType entity) {
        SchedulerUtil.runSync(location, () -> location.getWorld().spawnEntity(location, entity));
    }

    @Override
    public void playerTeleport(Player player, Location location) {
        SchedulerUtil.teleport(player, location);
    }

    @Override
    public SkullMeta setSkullMeta(SkullMeta meta, String skull) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "");
        if (skull.length() > 16) {
            profile.setProperty(new ProfileProperty("textures", skull));
            meta.setPlayerProfile(profile);
        } else {
            if (!playerProfiles.containsKey(skull)) {
                playerProfiles.put(skull, Bukkit.getOfflinePlayer(skull).getPlayerProfile());
            }
            meta.setPlayerProfile(playerProfiles.get(skull));
        }
        return meta;
    }

    @Override
    public String serializeSkull(SkullMeta meta) {
        PlayerProfile profile = meta.getPlayerProfile();
        if (profile != null) {
            String name = profile.getName();
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
            for (ProfileProperty property : profile.getProperties()) {
                if ("textures".equalsIgnoreCase(property.getName()) && property.getValue() != null && !property.getValue().isEmpty()) {
                    return property.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void setItemName(ItemMeta meta, String name, Player player) {
        if (PaperTextUtil.containsLegacyCodes(name)) {
            name = "<!i>" + name;
        }
        meta.displayName(PaperTextUtil.modernParse(name, player));
    }

    @Override
    public void setItemItemName(ItemMeta meta, String itemName, Player player) {
        if (!itemName.isEmpty()) {
            if (PaperTextUtil.containsLegacyCodes(itemName)) {
                itemName = "<!i>" + itemName;
            }
            meta.itemName(PaperTextUtil.modernParse(itemName, player));
        } else {
            meta.itemName();
        }
    }

    @Override
    public void setItemLore(ItemMeta meta, List<String> lores, Player player) {
        if (lores == null) {
            meta.lore(null);
            return;
        }
        List<Component> veryNewLore = new ArrayList<>();
        for (String lore : lores) {
            for (String singleLore : lore.split("\\\\n")) {
                if (PaperTextUtil.containsLegacyCodes(singleLore)) {
                    singleLore = "<!i>" + singleLore;
                }
                veryNewLore.add(PaperTextUtil.modernParse(singleLore, player));
            }
        }
        meta.lore(veryNewLore);
    }

    @Override
    public void sendChat(Player player, String text) {
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(PaperTextUtil.modernParse(text));
        } else {
            player.sendMessage(PaperTextUtil.modernParse(text, player));
        }
    }

    @Override
    public void sendHoverChat(Player player, String text, String command) {
        if (player == null) {
            return;
        }
        player.sendMessage(PaperTextUtil.modernParse(text, player)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(PaperTextUtil.modernParse(command, player))));
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        player.showTitle(Title.title(PaperTextUtil.modernParse(title, player),
                PaperTextUtil.modernParse(subTitle, player),
                Title.Times.times(Ticks.duration(fadeIn),
                        Ticks.duration(stay),
                        Ticks.duration(fadeOut))));
    }

    @Override
    public void sendActionBar(Player player, String message) {
        if (player != null) {
            player.sendActionBar(PaperTextUtil.modernParse(message, player));
        }
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
        if (style != null && style.equalsIgnoreCase("SOLID")) {
            style = "PROGRESS";
        }
        String barKey = key == null || key.isEmpty() ? "temp-" + UUID.randomUUID() : key;
        Map<String, BossBar> playerBars = bossBarCache.computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>());
        BossBar bar = playerBars.get(barKey);
        if (bar == null) {
            bar = BossBar.bossBar(
                    title == null ? Component.empty() : PaperTextUtil.modernParse(title, player),
                    Math.max(0f, Math.min(1f, progress)),
                    color == null ? BossBar.Color.PINK : BossBar.Color.valueOf(color.toUpperCase()),
                    style == null ? BossBar.Overlay.PROGRESS : BossBar.Overlay.valueOf(style.toUpperCase())
            );
            playerBars.put(barKey, bar);
            player.showBossBar(bar);
        } else {
            bar.name(title == null ? Component.empty() : PaperTextUtil.modernParse(title, player));
            bar.progress(Math.max(0f, Math.min(1f, progress)));
            bar.color(color == null ? BossBar.Color.PINK : BossBar.Color.valueOf(color.toUpperCase()));
            bar.overlay(style == null ? BossBar.Overlay.PROGRESS : BossBar.Overlay.valueOf(style.toUpperCase()));
        }
        Map<String, Long> generations = bossBarGenerations.computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>());
        long keepTicks = Math.max(20L, ConfigManager.configManager.getInt("mob-display.bossbar.keep-ticks", 60));
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
            player.hideBossBar(finalBar);
            playerBars.remove(barKey);
            generations.remove(barKey);
            removeEmptyBossBarCaches(playerId, playerBars, generations);
        }, keepTicks);
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
            player.hideBossBar(bar);
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
                player.hideBossBar(bar);
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
        return Bukkit.createInventory(holder, size, PaperTextUtil.modernParse(text, player));
    }

    @Override
    public String legacyParse(String text) {
        if (text == null) {
            return "";
        }
        if (!ConfigManager.configManager.getBoolean("config-files.force-parse-mini-message")) {
            return TextUtil.colorize(text);
        }
        return LegacyComponentSerializer.legacySection().serialize(PaperTextUtil.modernParse(text));
    }

    @Override
    public String getItemName(ItemMeta meta) {
        return PaperTextUtil.changeToString(meta.displayName());
    }

    @Override
    public String getItemItemName(ItemMeta meta) {
        return PaperTextUtil.changeToString(meta.itemName());
    }

    @Override
    public List<String> getItemLore(ItemMeta meta) {
        return PaperTextUtil.changeToString(meta.lore());
    }

    @Override
    public String getEntityName(LivingEntity entity) {
        if (entity.customName() != null) {
            return PaperTextUtil.changeToString(entity.customName());
        }
        return PaperTextUtil.changeToString(entity.name());
    }

    @Override
    public void setEntityName(LivingEntity entity, String name) {
        entity.customName(PaperTextUtil.modernParse(name));
    }

    @Override
    public String getEntityCustomName(LivingEntity entity) {
        if (entity.customName() != null) {
            return PaperTextUtil.changeToString(entity.customName());
        }
        return null;
    }

    @Override
    public ItemStack editItemStack(ItemStack item, Player player, ConfigurationSection section, int amount, String... args) {
        if (!CommonUtil.getMinorVersion(21, 6)) {
            return item;
        }
        return BuildItemPaper.editItemStack(item, player, section, amount, args);
    }

    @Override
    public ConfigurationSection serializeItemStack(ItemStack item) {
        if (!CommonUtil.getMinorVersion(21, 6)) {
            return null;
        }
        return DebuildItemPaper.serializeItemStack(item);
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
        Item item = world.dropItemNaturally(loc, itemStack);
        if (CommonUtil.getMinorVersion(19, 1) && player != null) {
            item.setOwner(player.getUniqueId());
        }
        return item;
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

        if (damager instanceof LightningStrike lightning) {
            return lightning.getCausingEntity();
        }

        if (damager instanceof EvokerFangs evoker) {
            return evoker.getOwner();
        }
        return null;
    }

    @Override
    public void tempBlockManager() {
        new PaperTempBlockListener();
    }

    @Override
    public EnchantmentPowerListener enchantmentPowerListener(TriggerManager triggerManager) {
       return new PaperEnchantmentPowerListener(triggerManager);
    }

    @Override
    public void createExplosion(Location loc, Entity entity, float yield, boolean setFire, boolean breakBlocks) {
        loc.getWorld().createExplosion(entity, loc, yield, setFire, breakBlocks);
    }
}

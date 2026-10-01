package cn.superiormc.enchantmentreform.utils;

import cn.superiormc.enchantmentreform.listeners.EnchantmentPowerListener;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public interface SpecialMethodUtil {

    String methodID();

    void dispatchCommand(String command);

    void dispatchCommand(Player player, String command);

    void dispatchOpCommand(Player player, String command);

    ItemStack getItemObject(Object object);

    Object makeItemToObject(ItemStack item);

    void spawnEntity(Location location, EntityType entity);

    void playerTeleport(Player player, Location location);

    SkullMeta setSkullMeta(SkullMeta meta, String skull);

    String serializeSkull(SkullMeta meta);

    void setItemName(ItemMeta meta, String name, Player player);

    void setItemItemName(ItemMeta meta, String itemName, Player player);

    void setItemLore(ItemMeta meta, List<String> lore, Player player);

    void sendChat(Player player, String text);

    void sendHoverChat(Player player, String text, String command);

    void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut);

    void sendActionBar(Player player, String message);

    void sendBossBar(Player player, String title, float progress, String color, String style);

    void sendBossBar(Player player, String title, float progress, String color, String style, String key);

    void hideBossBar(Player player, String key);

    void clearBossBars(Player player);

    Inventory createNewInv(Player player, int size, String text, InventoryHolder holder);

    String legacyParse(String text);

    String getItemName(ItemMeta meta);

    String getItemItemName(ItemMeta meta);

    List<String> getItemLore(ItemMeta meta);

    String getEntityName(LivingEntity entity);

    void setEntityName(LivingEntity entity, String name);

    String getEntityCustomName(LivingEntity entity);

    ItemStack editItemStack(ItemStack item, Player player, ConfigurationSection section, int amount, String... args);

    ConfigurationSection serializeItemStack(ItemStack item);

    void dropPrivateItem(Player player, ItemStack itemStack, Location loc);

    Item dropItem(Player player, ItemStack itemStack, Location loc);

    Entity getDamager(Entity damager);

    void tempBlockManager();

    default EnchantmentPowerListener enchantmentPowerListener(TriggerManager triggerManager) {
        return new EnchantmentPowerListener(triggerManager);
    }

    default double getDestroySpeed(Block block, ItemStack tool) {
        if (block == null || tool == null) {
            return 1.0D;
        }
        double blockHardness = Math.max(0.0D, block.getType().getHardness());
        double toolHardness = Math.max(0.0D, tool.getType().getHardness());
        return toolHardness <= 0.0D ? 1.0D : Math.max(1.0D, toolHardness / Math.max(1.0D, blockHardness));
    }

    void createExplosion(Location loc, Entity entity, float yield, boolean setFire, boolean breakBlocks);
}

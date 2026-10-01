package cn.superiormc.enchantmentreform.managers;

import cn.gtemc.itembridge.api.ItemBridge;
import cn.gtemc.itembridge.api.util.Pair;
import cn.gtemc.itembridge.core.BukkitItemBridge;
import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.hooks.blocks.*;
import cn.superiormc.enchantmentreform.hooks.economy.*;
import cn.superiormc.enchantmentreform.hooks.enchantmentslots.EnchantmentReformProvider;
import cn.superiormc.enchantmentreform.hooks.mythicchanger.EnchantmentDescriptionRule;
import cn.superiormc.enchantmentreform.hooks.items.*;
import cn.superiormc.enchantmentreform.hooks.protection.*;
import cn.superiormc.enchantmentreform.papi.PlaceholderAPIExpansion;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HookManager extends AbstractManager implements Listener {

    public static HookManager hookManager;

    private Map<String, AbstractEconomyHook> economyHooks;

    private Map<String, AbstractItemHook> itemHooks;

    private Map<String, AbstractProtectionHook> protectionHooks;

    private Map<String, AbstractBlockHook> blockHooks;

    private ItemBridge<ItemStack, Player> itemBridgeHook = null;

    public PlaceholderAPIExpansion papi = null;

    private boolean enchantmentSlotsHookRegistered;

    private boolean mythicChangerRuleRegistered;

    public HookManager() {
        hookManager = this;
        initNormalHook();
        initProtectionHook();
        initEconomyHook();
        initItemHook();
        initBlockHook();
        if (ConfigManager.configManager.getString("hook-item-method").equalsIgnoreCase("ITEMBRIDGE")) {
            itemBridgeHook = BukkitItemBridge.builder()
                    .onHookSuccess(p -> TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fUSItemBridge successfully hook into " + p + "."))
                    .detectSupportedPlugins()
                    .build();
        }
        Bukkit.getPluginManager().registerEvents(this, EnchantmentReform.instance);
    }

    private void initNormalHook() {
        if (CommonUtil.checkPluginLoad("MythicChanger")) {
            tryRegisterMythicChangerRule();
        }
        if (CommonUtil.checkPluginLoad("PlaceholderAPI")) {
            papi = new PlaceholderAPIExpansion(EnchantmentReform.instance);
            TextUtil.sendMessage(null, TextUtil.pluginPrefix()
                    + " §fHooking into PlaceholderAPI...");
            if (papi.register()) {
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fFinished hook!");
            }
        }
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        tryRegisterEnchantmentSlotsHook();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("MythicChanger")) {
            tryRegisterMythicChangerRule();
        }
        if (event.getPlugin().getName().equalsIgnoreCase("packetevents")
                && FakeChangeManager.fakeChangeManager == null
                && FakeChangeManager.enableThis()) {
            new FakeChangeManager();
            AbstractManager.initializeManagers();
        }
    }

    private void tryRegisterMythicChangerRule() {
        if (mythicChangerRuleRegistered) {
            return;
        }
        try {
            EnchantmentDescriptionRule.register();
            mythicChangerRuleRegistered = true;
            TextUtil.sendMessage(null, TextUtil.pluginPrefix()
                    + " §fRegistered MythicChanger enchantment description rule.");
        } catch (Throwable throwable) {
            EnchantmentReform.instance.getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Could not register the MythicChanger enchantment description rule.",
                    throwable);
        }
    }

    private void tryRegisterEnchantmentSlotsHook() {
        if (enchantmentSlotsHookRegistered || !CommonUtil.getClass("cn.superiormc.enchantmentslots.EnchantmentSlots")) {
            return;
        }
        try {
            EnchantmentSlotsHook.register();
            enchantmentSlotsHookRegistered = true;
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooked into EnchantmentSlots.");
        } catch (Throwable throwable) {
            EnchantmentReform.instance.getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Could not register the EnchantmentReform provider with EnchantmentSlots.",
                    throwable);
        }
    }

    private void initEconomyHook() {
        economyHooks = new HashMap<>();
        if (CommonUtil.checkPluginLoad("Vault")) {
            registerNewEconomyHook("Vault", new EconomyVaultHook());
        }
        if (CommonUtil.checkPluginLoad("PlayerPoints")) {
            registerNewEconomyHook("PlayerPoints", new EconomyPlayerPointsHook());
        }
        if (CommonUtil.checkPluginLoad("CoinsEngine")) {
            registerNewEconomyHook("CoinsEngine", new EconomyCoinsEngineHook());
        }
        if (CommonUtil.checkPluginLoad("ExcellentEconomy")) {
            registerNewEconomyHook("ExcellentEconomy", new EconomyExcellentEconomyHook());
        }
        if (CommonUtil.checkPluginLoad("UltraEconomy")) {
            registerNewEconomyHook("UltraEconomy", new EconomyUltraEconomyHook());
        }
        if (CommonUtil.checkPluginLoad("EcoBits")) {
            registerNewEconomyHook("EcoBits", new EconomyEcoBitsHook());
        }
        if (CommonUtil.checkPluginLoad("PEconomy")) {
            registerNewEconomyHook("PEconomy", new EconomyPEconomyHook());
        }
        if (CommonUtil.checkPluginLoad("RedisEconomy")) {
            registerNewEconomyHook("RedisEconomy", new EconomyRedisEconomyHook());
        }
        if (CommonUtil.checkPluginLoad("RoyaleEconomy")) {
            registerNewEconomyHook("RoyaleEconomy", new EconomyRoyaleEconomyHook());
        }
        if (CommonUtil.checkPluginLoad("VotingPlugin")) {
            registerNewEconomyHook("VotingPlugin", new EconomyVotingPluginHook());
        }
    }

    private void initItemHook() {
        itemHooks = new HashMap<>();
        if (CommonUtil.checkPluginLoad("ItemsAdder")) {
            registerNewItemHook("ItemsAdder", new ItemItemsAdderHook());
        }
        if (CommonUtil.checkPluginLoad("Oraxen")) {
            registerNewItemHook("Oraxen", new ItemOraxenHook());
        }
        if (CommonUtil.checkPluginLoad("MMOItems")) {
            registerNewItemHook("MMOItems", new ItemMMOItemsHook());
        }
        if (CommonUtil.checkPluginLoad("EcoItems")) {
            registerNewItemHook("EcoItems", new ItemEcoItemsHook());
        }
        if (CommonUtil.checkPluginLoad("EcoArmor")) {
            registerNewItemHook("EcoArmor", new ItemEcoArmorHook());
        }
        if (CommonUtil.checkPluginLoad("MythicMobs")) {
            registerNewItemHook("MythicMobs", new ItemMythicMobsHook());
        }
        if (CommonUtil.checkPluginLoad("eco")) {
            registerNewItemHook("eco", new ItemecoHook());
        }
        if (CommonUtil.checkPluginLoad("NeigeItems")) {
            registerNewItemHook("NeigeItems", new ItemNeigeItemsHook());
        }
        if (CommonUtil.checkPluginLoad("ExecutableItems")) {
            registerNewItemHook("ExecutableItems", new ItemExecutableItemsHook());
        }
        if (CommonUtil.checkPluginLoad("Nexo")) {
            registerNewItemHook("Nexo", new ItemNexoHook());
        }
        if (CommonUtil.checkPluginLoad("CraftEngine")) {
            registerNewItemHook("CraftEngine", new ItemCraftEngineHook());
        }
    }

    private void initProtectionHook() {
        protectionHooks = new HashMap<>();
        if (CommonUtil.checkPluginLoad("WorldGuard")) {
            registerNewProtectionHook("WorldGuard", new ProtectionWorldGuardHook());
        }
        if (CommonUtil.checkPluginLoad("Residence")) {
            registerNewProtectionHook("Residence", new ProtectionResidenceHook());
        }
        if (CommonUtil.checkPluginLoad("GriefPrevention")) {
            registerNewProtectionHook("GriefPrevention", new ProtectionGriefPreventionHook());
        }
        if (CommonUtil.checkPluginLoad("Lands")) {
            registerNewProtectionHook("Lands", new ProtectionLandsHook());
        }
        if (CommonUtil.checkPluginLoad("HuskTowns")) {
            registerNewProtectionHook("HuskTowns", new ProtectionHuskTownsHook());
        }
        if (CommonUtil.checkPluginLoad("HuskClaims")) {
            registerNewProtectionHook("HuskClaims", new ProtectionHuskClaimsHook());
        }
        if (CommonUtil.checkPluginLoad("PlotSquared")) {
            registerNewProtectionHook("PlotSquared", new ProtectionPlotSquaredHook());
        }
        if (CommonUtil.checkPluginLoad("Towny")) {
            registerNewProtectionHook("Towny", new ProtectionTownyHook());
        }
        if (CommonUtil.checkPluginLoad("BentoBox")) {
            registerNewProtectionHook("BentoBox", new ProtectionBentoBoxHook());
        }
        if (CommonUtil.checkPluginLoad("Dominion")) {
            registerNewProtectionHook("Dominion", new ProtectionDominionHook());
        }
        if (CommonUtil.checkPluginLoad("SuperiorSkyblock2")) {
            registerNewProtectionHook("SuperiorSkyblock2", new ProtectionSuperiorSkyblock2Hook());
        }
    }

    private void initBlockHook() {
        blockHooks = new HashMap<>();
        registerNewBlockHook("Vanilla", new MinecraftBlockChecker());
        if (CommonUtil.checkPluginLoad("CraftEngine")) {
            registerNewBlockHook("CraftEngine", new CraftEngineBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("Oraxen")) {
            registerNewBlockHook("Oraxen", new OraxenBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("Nexo")) {
            registerNewBlockHook("Nexo", new NexoBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("ItemsAdder")) {
            registerNewBlockHook("ItemsAdder", new ItemsAdderBlockChecker());
        }
        if (CommonUtil.checkPluginLoad("MMOItems")) {
            registerNewBlockHook("MMOItems", new MMOItemsBlockChecker());
        }
    }

    public void registerNewEconomyHook(String pluginName,
                                       AbstractEconomyHook economyHook) {
        if (!economyHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into " + pluginName + "...");
            economyHooks.put(pluginName, economyHook);
        }
    }

    public void registerNewItemHook(String pluginName,
                                    AbstractItemHook itemHook) {
        if (!itemHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into " + pluginName + "...");
            itemHooks.put(pluginName, itemHook);
        }
    }

    public void registerNewProtectionHook(String pluginName,
                                          AbstractProtectionHook protectionHook) {
        if (!protectionHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into " + pluginName + "...");
            protectionHooks.put(pluginName, protectionHook);
        }
    }

    public void registerNewBlockHook(String pluginName, AbstractBlockHook checker) {
        if (!blockHooks.containsKey(pluginName)) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fHooking into: " + pluginName + "...");
            blockHooks.put(pluginName, checker);
        }
    }

    public double getEconomyAmount(Player player, String pluginName, String currencyID) {
        if (!economyHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return 0;
        }
        AbstractEconomyHook economyHook = economyHooks.get(pluginName);
        if (!economyHook.isEnabled()) {
            return 0;
        }
        return economyHook.getEconomy(player, currencyID);
    }

    public int getEconomyAmount(Player player, String vanillaType) {
        vanillaType = vanillaType.toLowerCase();
        if (vanillaType.equals("exp")) {
            return player.getTotalExperience();
        }
        else if (vanillaType.equals("levels")) {
            return player.getLevel();
        }
        ErrorManager.errorManager.sendErrorMessage("§cError: You set economy type to "
                + vanillaType + " in shop config, however for now UltimateShop does not support it!");
        return 0;
    }

    public boolean getPrice(Player player,
                            String pluginName,
                            String currencyID,
                            double value,
                            boolean take) {
        if (value < 0) {
            return false;
        }
        if (!economyHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return false;
        }
        AbstractEconomyHook economyHook = economyHooks.get(pluginName);
        if (player.hasPermission("ultimateshop.bypassprice")) {
            return true;
        }
        return economyHook.isEnabled() && economyHook.checkEconomy(player, value, take, currencyID);
    }

    public boolean getPrice(Player player, String vanillaType, int value, boolean take) {
        vanillaType = vanillaType.toLowerCase();
        if (vanillaType.equals("exp")) {
            if (player.getTotalExperience() >= value) {
                if (take) {
                    player.giveExp(-value);
                }
                return true;
            }
            return false;
        }
        else if (vanillaType.equals("levels")) {
            if (player.getLevel() >= value) {
                if (take) {
                    player.giveExpLevels(-value);
                }
                return true;
            }
            return false;
        }
        ErrorManager.errorManager.sendErrorMessage("§cError: You set economy type to "
                + vanillaType + " in shop config, however for now UltimateShop does not support it!");
        return false;
    }

    public ItemStack getHookItem(Player player, String pluginName, String itemID) {
        if (itemBridgeHook != null) {
            Optional<ItemStack> tempVal1 = itemBridgeHook.build(pluginName, player, itemID);
            if (tempVal1.isPresent()) {
                return tempVal1.get();
            }
        }
        if (!itemHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return null;
        }
        AbstractItemHook itemHook = itemHooks.get(pluginName);
        return itemHook.getHookItemByID(player, itemID);
    }

    public void giveEconomy(String pluginName, String currencyName, Player player, double value) {
        if (!economyHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return;
        }
        AbstractEconomyHook economyHook = economyHooks.get(pluginName);
        if (!economyHook.isEnabled()) {
            return;
        }
        economyHook.giveEconomy(player, value, currencyName);
    }

    public void giveEconomy(String vanillaType, Player player, int value) {
        vanillaType = vanillaType.toLowerCase();
        if (vanillaType.equals("exp")) {
            player.giveExp(value);
            return;
        } else if (vanillaType.equals("levels")) {
            player.giveExpLevels(value);
            return;
        }
        ErrorManager.errorManager.sendErrorMessage("§cError: You set economy type to "
                + vanillaType + " in shop config, however for now UltimateShop does not support it!");
    }

    public void takeEconomy(String pluginName, String currencyName, Player player, double value) {
        if (!economyHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return;
        }
        AbstractEconomyHook economyHook = economyHooks.get(pluginName);
        if (!economyHook.isEnabled()) {
            return;
        }
        economyHook.takeEconomy(player, value, currencyName);
    }

    public String parseItemID(ItemStack hookItem, boolean useTier) {
        if (!hookItem.hasItemMeta()) {
            return hookItem.getType().name().toLowerCase();
        }
        if (itemBridgeHook != null) {
            Pair<String, String> tempVal1 = itemBridgeHook.getFirstId(hookItem);
            if (tempVal1 != null) {
                return tempVal1.right;
            }
        }
        for (AbstractItemHook itemHook : itemHooks.values()) {
            String tempVal1 = itemHook.getSimplyIDByItemStack(hookItem, useTier);
            if (tempVal1 != null) {
                return tempVal1;
            }
        }
        return hookItem.getType().name().toLowerCase();
    }

    public String getHookItemID(String pluginName, ItemStack hookItem) {
        if (!hookItem.hasItemMeta()) {
            return null;
        }
        if (itemBridgeHook != null) {
            String tempVal1 = itemBridgeHook.getIds(hookItem).get(pluginName);
            if (tempVal1 != null) {
                return tempVal1;
            }
        }
        if (!itemHooks.containsKey(pluginName)) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Can not hook into "
                    + pluginName + " plugin, maybe we do not support this plugin, or your server didn't correctly load " +
                    "this plugin!");
            return null;
        }
        AbstractItemHook itemHook = itemHooks.get(pluginName);
        return itemHook.getIDByItemStack(hookItem);
    }

    public String[] getHookItemPluginAndID(ItemStack hookItem) {
        if (itemBridgeHook != null) {
            Pair<String, String> tempVal1 = itemBridgeHook.getFirstId(hookItem);
            if (tempVal1 != null) {
                return new String[]{tempVal1.left, tempVal1.right};
            }
        }
        for (AbstractItemHook itemHook : itemHooks.values()) {
            String itemID = itemHook.getIDByItemStack(hookItem);
            if (itemID != null) {
                return new String[]{itemHook.getPluginName(), itemHook.getIDByItemStack(hookItem)};
            }
        }
        return null;
    }

    public boolean getProtectionCanUse(Player player, Location location) {
        if (player.isOp() || player.hasPermission("ultimateshop.bypass.protection")) {
            return true;
        }
        for (AbstractProtectionHook protectionHook : protectionHooks.values()) {
            if (!protectionHook.canUse(player, location)) {
                return false;
            }
        }
        return true;
    }

    public boolean getProtectionCanBreak(Player player, Location location) {
        if (player.isOp() || player.hasPermission("ultimateshop.bypass.protection")) {
            return true;
        }
        for (AbstractProtectionHook protectionHook : protectionHooks.values()) {
            if (!protectionHook.canBreak(player, location)) {
                return false;
            }
        }
        return true;
    }

    public boolean getProtectionCanPlace(Player player, Location location) {
        if (player.isOp() || player.hasPermission("ultimateshop.bypass.protection")) {
            return true;
        }
        for (AbstractProtectionHook protectionHook : protectionHooks.values()) {
            if (!protectionHook.canPlace(player, location)) {
                return false;
            }
        }
        return true;
    }

    public AbstractBlockHook getSuitableChecker(String materialString) {
        for (AbstractBlockHook checker : blockHooks.values()) {
            if (checker.canCheck(materialString)) {
                return checker;
            }
        }
        return null;
    }

    @Nullable
    public String getBlockId(Block block) {
        AbstractBlockHook minecraftChecker = null;

        for (AbstractBlockHook checker : blockHooks.values()) {
            if (checker instanceof MinecraftBlockChecker) {
                minecraftChecker = checker;
                continue;
            }

            String blockId = checker.getBlockId(block);
            if (blockId != null) {
                return blockId;
            }
        }

        return minecraftChecker == null ? null : minecraftChecker.getBlockId(block);
    }

    @Nullable
    public String getMatchingBlockId(Block block, Collection<String> availableIds) {
        AbstractBlockHook minecraftChecker = null;

        for (AbstractBlockHook checker : blockHooks.values()) {
            if (checker instanceof MinecraftBlockChecker) {
                minecraftChecker = checker;
                continue;
            }

            if (availableIds.stream().noneMatch(checker::canCheck)) {
                continue;
            }

            String blockId = checker.getBlockId(block);
            if (blockId != null && availableIds.contains(blockId)) {
                return blockId;
            }
        }

        if (minecraftChecker == null || availableIds.stream().noneMatch(minecraftChecker::canCheck)) {
            return null;
        }

        String vanillaBlockId = minecraftChecker.getBlockId(block);
        if (vanillaBlockId != null && availableIds.contains(vanillaBlockId)) {
            return vanillaBlockId;
        }
        return null;
    }

    public List<String> getEconomyHookNames() {
        return new ArrayList<>(economyHooks.keySet());
    }

    public List<String> getItemHookNames() {
        return new ArrayList<>(itemHooks.keySet());
    }
}

class EnchantmentSlotsHook {
    public static void register() {
        cn.superiormc.enchantmentslots.managers.HookManager.hookManager.registerNewEnchantHook("EnchantmentReform", new EnchantmentReformProvider());
    }
}

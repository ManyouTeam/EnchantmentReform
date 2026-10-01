package cn.superiormc.enchantmentreform;

import cn.superiormc.enchantmentreform.listeners.EnchantmentPowerListener;
import cn.superiormc.enchantmentreform.managers.*;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SpecialMethodUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EnchantmentReform extends JavaPlugin {

    public static EnchantmentReform instance;

    private Metrics metrics;

    public static SpecialMethodUtil methodUtil;

    public static boolean isFolia;

    public static int yearVersion;

    public static int majorVersion;

    public static int minorVersion;

    private EnchantmentPowerListener powerListener;

    private boolean lootEnchantFunctionPatchInstalled;

    private boolean paperMode;

    private boolean spigotRegistryReady;

    private NmsBridge nmsBridge = new UnsupportedNmsBridge("Plugin has not finished enabling");

    @Override
    public void onLoad() {
        instance = this;
        paperMode = isPaperServer();
        if (paperMode) {
            return;
        }
        try {
            EnchantmentConfigManager manager = new EnchantmentConfigManager(
                    getDataFolder().toPath(),
                    getLogger()::info,
                    (message, throwable) -> getLogger().log(Level.SEVERE, message, throwable));
            Class<?> registrar = Class.forName("cn.superiormc.enchantmentreform.spigot.registry.SpigotEnchantmentRegistrar261");
            int registered = (int) registrar.getMethod(
                            "registerAll",
                            Collection.class,
                            ConfigurationSection.class,
                            Logger.class)
                    .invoke(
                            null,
                            manager.getEnchantments(),
                            manager.getSupportedItemsConfiguration(),
                            getLogger());
            spigotRegistryReady = true;
            getLogger().info("Registered " + registered + " native custom enchantments for Spigot during STARTUP.");
        } catch (Throwable throwable) {
            spigotRegistryReady = false;
            getLogger().log(
                    Level.SEVERE,
                    "Could not register native custom enchantments on this version of Spigot, plugin will disable after server enable.",
                    unwrapReflectionFailure(throwable));
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        paperMode = isPaperServer();
        if (!paperMode && !spigotRegistryReady) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            String[] versionParts = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            yearVersion = versionParts.length > 0 && versionParts[0].matches("\\d+") ? Integer.parseInt(versionParts[0]) : 1;
            majorVersion = versionParts.length > 1 && versionParts[1].matches("\\d+") ? Integer.parseInt(versionParts[1]) : 0;
            minorVersion = versionParts.length > 2 && versionParts[2].matches("\\d+") ? Integer.parseInt(versionParts[2]) : 0;
        } catch (Throwable throwable) {
            Bukkit.getConsoleSender().sendMessage(TextUtil.pluginPrefix() + " §cError: Can not get your Minecraft version! Default set to 1.0.0.");
        }
        if (paperMode) {
            try {
                Class<?> paperClass = Class.forName("cn.superiormc.enchantmentreform.paper.PaperMethodUtil");
                methodUtil = (SpecialMethodUtil) paperClass.getDeclaredConstructor().newInstance();
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPaper is found, entering Paper plugin mode...");
            } catch (Throwable throwable) {
                Bukkit.getConsoleSender().sendMessage(TextUtil.pluginPrefix() + " §cError: The plugin seems break, please download it again from site.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            try {
                Class<?> spigotClass = Class.forName("cn.superiormc.enchantmentreform.spigot.SpigotMethodUtil");
                methodUtil = (SpecialMethodUtil) spigotClass.getDeclaredConstructor().newInstance();
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fSpigot is found, entering Spigot plugin mode...");
                TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fNative custom enchantments were registered during server startup.");
            } catch (Throwable throwable) {
                Bukkit.getConsoleSender().sendMessage(TextUtil.pluginPrefix() + " §cError: The plugin seems break, please download it again from site.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        if (CommonUtil.getClass("io.papermc.paper.threadedregions.RegionizedServer")) {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fFolia is found, enabled Folia compatibility feature!");
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §6Warning: Folia support is not fully test, major bugs maybe found! " +
                    "Please do not use in production environment!");
            isFolia = true;
        }
        new ErrorManager();
        if (InitManager.initManager == null) {
            new InitManager();
        } else {
            InitManager.initManager.init();
        }
        new ConfigManager();
        new AntiAbuseManager();
        installLootEnchantFunctionPatch();
        new LanguageManager();
        new HookManager();
        new ItemManager();
        new AttributeManager();
        new MatchEntityManager();
        new MatchItemManager();
        new ChangesManager();
        new PowerConditionsManager();
        new PowerModifiersManager();
        nmsBridge = createNmsBridge();
        new AbilityManager(nmsBridge);
        new TriggerManager();
        new SkillManager();
        PowerStateStore.start();
        EnchantmentConfigManager enchantmentConfigManager = EnchantmentConfigManager.getInstance();
        if (enchantmentConfigManager == null) {
            enchantmentConfigManager = new EnchantmentConfigManager(
                    getDataFolder().toPath(),
                    getLogger()::info,
                    (message, throwable) -> getLogger().log(Level.SEVERE, message, throwable));
        }
        enchantmentConfigManager.initializePowers();
        new CommandManager();
        new ListenerManager();
        methodUtil.tempBlockManager();
        powerListener = methodUtil.enchantmentPowerListener(TriggerManager.triggerManager);
        if (FakeChangeManager.enableThis()) {
            new FakeChangeManager();
        }
        AbstractManager.initializeManagers();
        metrics = new Metrics(EnchantmentReform.instance, 32795);
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fYour server version is: " + yearVersion + "." + majorVersion + "." + minorVersion + "!");
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPlugin is loaded. Author: PQguanfang.");
    }

    public NmsBridge getNmsBridge() {
        return nmsBridge;
    }

    private boolean isPaperServer() {
        return CommonUtil.getClass("io.papermc.paper.plugin.bootstrap.PluginBootstrap")
                || CommonUtil.getClass("com.destroystokyo.paper.PaperConfig");
    }

    private Throwable unwrapReflectionFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof InvocationTargetException
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void installLootEnchantFunctionPatch() {
        if (!ConfigManager.configManager.getBoolean(
                "enchant-randomly-overrides.enabled",
                false
        )) {
            return;
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fApplying loot enchant function patch to NMS...");
        int minimumCost = ConfigManager.configManager.getInt(
                "enchant-randomly-overrides.minimum-cost",
                10
        );

        int maximumCost = ConfigManager.configManager.getInt(
                "enchant-randomly-overrides.maximum-cost",
                30
        );

        try {
            Class<?> patchClass = Class.forName(
                    "cn.superiormc.enchantmentreform.nms.named."
                            + "LootEnchantFunctionPatch"
            );

            lootEnchantFunctionPatchInstalled = (boolean) patchClass
                    .getMethod(
                            "install",
                            java.util.logging.Logger.class,
                            int.class,
                            int.class
                    )
                    .invoke(
                            null,
                            getLogger(),
                            minimumCost,
                            maximumCost
                    );
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: Could not initialize the loot enchantment function patch.");
            throwable.printStackTrace();
            lootEnchantFunctionPatchInstalled = false;
        }
    }

    private void uninstallLootEnchantFunctionPatch() {
        if (!lootEnchantFunctionPatchInstalled) {
            return;
        }

        lootEnchantFunctionPatchInstalled = false;

        try {
            Class<?> patchClass = Class.forName(
                    "cn.superiormc.enchantmentreform.nms.named."
                            + "LootEnchantFunctionPatch"
            );

            patchClass
                    .getMethod(
                            "uninstall",
                            java.util.logging.Logger.class
                    )
                    .invoke(null, getLogger());
        } catch (Throwable throwable) {
            getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Could not uninstall the loot enchantment function patch.",
                    throwable
            );
        }
    }

    private NmsBridge createNmsBridge() {
        try {
            TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fTrying create NMS Bridge...");
            Class<?> bootstrap = Class.forName("cn.superiormc.enchantmentreform.nms.named.NamedNmsBootstrap");
            return (NmsBridge) bootstrap.getMethod("create", java.util.logging.Logger.class)
                    .invoke(null, getLogger());
        } catch (Throwable throwable) {
            ErrorManager.errorManager.sendErrorMessage("§cError: NMS Bridge can not be created! Maybe your server software is modified NMS codes, " +
                    "your server version is too new or too old. Some features that use NMS will no longer work.");
            throwable.printStackTrace();
            return new UnsupportedNmsBridge("Named NMS bridge could not be loaded");
        }
    }

    @Override
    public void onDisable() {
        uninstallLootEnchantFunctionPatch();
        if (methodUtil != null) {
            Bukkit.getOnlinePlayers().forEach(methodUtil::clearBossBars);
        }
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        if (powerListener != null) {
            powerListener.close();
            powerListener = null;
        }
        boolean powerManagerRegistered = PowerManager.powerManager != null;
        AbstractManager.disableManagers();
        if (!powerManagerRegistered) {
            PowerStateStore.shutdown();
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPlugin is disabled. Author: PQguanfang.");
    }
}

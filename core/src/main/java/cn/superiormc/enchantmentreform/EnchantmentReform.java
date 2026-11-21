package cn.superiormc.enchantmentreform;

import cn.superiormc.enchantmentreform.managers.AbilityManager;
import cn.superiormc.enchantmentreform.managers.AbstractManager;
import cn.superiormc.enchantmentreform.managers.AntiAbuseManager;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.managers.ChangesManager;
import cn.superiormc.enchantmentreform.managers.CommandManager;
import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.EnchantmentConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import cn.superiormc.enchantmentreform.managers.HookManager;
import cn.superiormc.enchantmentreform.managers.InitManager;
import cn.superiormc.enchantmentreform.managers.ItemManager;
import cn.superiormc.enchantmentreform.managers.LanguageManager;
import cn.superiormc.enchantmentreform.managers.ListenerManager;
import cn.superiormc.enchantmentreform.managers.MatchEntityManager;
import cn.superiormc.enchantmentreform.managers.MatchItemManager;
import cn.superiormc.enchantmentreform.managers.PowerConditionsManager;
import cn.superiormc.enchantmentreform.managers.PowerManager;
import cn.superiormc.enchantmentreform.managers.PowerModifiersManager;
import cn.superiormc.enchantmentreform.managers.SkillManager;
import cn.superiormc.enchantmentreform.managers.TriggerManager;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.NmsLog;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import cn.superiormc.enchantmentreform.objects.abilities.PowerStateStore;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.SpecialMethodUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EnchantmentReform extends JavaPlugin {

    private static final String NMS_BOOTSTRAP = "cn.superiormc.enchantmentreform.nms.named.NamedNmsBootstrap";

    public static EnchantmentReform instance;

    private Metrics metrics;

    public static SpecialMethodUtil methodUtil;

    public static boolean isPaper = false;

    public static boolean isFolia = false;

    public static int yearVersion;

    public static int majorVersion;

    public static int minorVersion;

    private boolean spigotRegistryReady;

    private NmsBridge nmsBridge = new UnsupportedNmsBridge("Plugin has not finished enabling");

    @Override
    public void onLoad() {
        instance = this;
        isPaper = CommonUtil.getClass("io.papermc.paper.plugin.bootstrap.PluginBootstrap")
                || CommonUtil.getClass("com.destroystokyo.paper.PaperConfig");
        if (isPaper) {
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
            NmsLog.info(getLogger(), NmsLog.ENCHANTMENT_REGISTRY, "enabled (Spigot; registered: " + registered + ").");
        } catch (Throwable throwable) {
            spigotRegistryReady = false;
            NmsLog.error(getLogger(), NmsLog.ENCHANTMENT_REGISTRY,
                    "unavailable; registration failed. The plugin will be disabled.", throwable);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        if (!isPaper && !spigotRegistryReady) {
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
        if (isPaper) {
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
        AbstractManager.initializeManagers();
        metrics = new Metrics(EnchantmentReform.instance, 32795);
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fYour server version is: " + yearVersion + "." + majorVersion + "." + minorVersion + "!");
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPlugin is loaded. Author: PQguanfang.");
    }

    public NmsBridge getNmsBridge() {
        return nmsBridge;
    }

    private NmsBridge createNmsBridge() {
        try {
            Class<?> bootstrap = Class.forName(NMS_BOOTSTRAP);
            return (NmsBridge) bootstrap.getMethod("create", Logger.class)
                    .invoke(null, getLogger());
        } catch (Throwable throwable) {
            NmsLog.warning(getLogger(), NmsLog.BRIDGE, "unavailable; initialization failed.", throwable);
            return new UnsupportedNmsBridge("Named NMS bridge could not be loaded");
        }
    }

    @Override
    public void onDisable() {
        if (ErrorManager.errorManager != null) {
            try {
                Class.forName(NMS_BOOTSTRAP).getMethod("uninstallPatches", Logger.class).invoke(null, getLogger());
            } catch (ReflectiveOperationException | LinkageError exception) {
                ErrorManager.errorManager.sendErrorMessage("§c[NMS] Patch restoration failed: " + exception);
            }
        }
        if (methodUtil != null) {
            Bukkit.getOnlinePlayers().forEach(methodUtil::clearBossBars);
        }
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        boolean powerManagerRegistered = PowerManager.powerManager != null;
        AbstractManager.disableManagers();
        if (!powerManagerRegistered) {
            PowerStateStore.shutdown();
        }
        TextUtil.sendMessage(null, TextUtil.pluginPrefix() + " §fPlugin is disabled. Author: PQguanfang.");
    }
}

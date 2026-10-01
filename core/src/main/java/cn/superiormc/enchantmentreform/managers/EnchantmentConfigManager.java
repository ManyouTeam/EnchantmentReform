package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.objects.ObjectCustomEnchantment;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.VanillaEnchantmentOverride;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class EnchantmentConfigManager extends AbstractManager {

    private static EnchantmentConfigManager instance;

    private final Path dataDirectory;

    private final Path directory;

    private final Path vanillaDirectory;

    private final Consumer<String> infoLogger;

    private final BiConsumer<String, Throwable> errorLogger;

    private final Map<String, ObjectCustomEnchantment> enchantments = new LinkedHashMap<>();

    private final Map<String, VanillaEnchantmentOverride> vanillaEnchantments = new LinkedHashMap<>();

    private YamlConfiguration registryLanguage;

    private YamlConfiguration globalConfig;

    public EnchantmentConfigManager(Path dataDirectory,
                                     Consumer<String> infoLogger,
                                     BiConsumer<String, Throwable> errorLogger) {
        instance = this;
        this.dataDirectory = dataDirectory.toAbsolutePath().normalize();
        this.directory = this.dataDirectory.resolve("enchantments");
        this.vanillaDirectory = this.dataDirectory.resolve("vanilla_enchantments");
        this.infoLogger = infoLogger;
        this.errorLogger = errorLogger;
        new InitManager(this.dataDirectory, errorLogger);
        reloadDefinitions();
    }

    public static EnchantmentConfigManager getInstance() {
        return instance;
    }

    public void reloadDefinitions() {
        enchantments.clear();
        vanillaEnchantments.clear();
        try {
            Files.createDirectories(directory);
            Files.createDirectories(vanillaDirectory);
            Collection<Path> definitionFiles = findDefinitionFiles(directory);
            globalConfig = loadGlobalConfig();
            registryLanguage = loadRegistryLanguage();
            ConfigurationSection raritySettings = globalConfig.getConfigurationSection("rarity");
            int rejectedDefinitions = 0;
            for (Path file : definitionFiles) {
                if (!loadEnchantmentSafely(file, raritySettings)) {
                    rejectedDefinitions++;
                }
            }
            Collection<Path> vanillaDefinitionFiles = findDefinitionFiles(vanillaDirectory);
            for (Path file : vanillaDefinitionFiles) {
                if (!loadVanillaEnchantmentSafely(file, raritySettings)) {
                    rejectedDefinitions++;
                }
            }
            rejectedDefinitions += validateDefinitions();
            if (rejectedDefinitions > 0) {
                infoLogger.accept("Skipped " + rejectedDefinitions
                        + " invalid enchantment definition(s); see the errors above.");
            }
            infoLogger.accept("Loaded " + enchantments.size() + " enchantments.");
            infoLogger.accept("Loaded " + vanillaEnchantments.size() + " vanilla enchantment overrides.");
        } catch (Throwable throwable) {
            enchantments.clear();
            vanillaEnchantments.clear();
            errorLogger.accept("Error: Failed to load enchantments from " + directory + ". Reason: " + throwable.getMessage(), throwable);
        }
    }

    public void initializePowers() {
        new PowerManager();
        if (ItemManager.itemManager != null) {
            ItemManager.itemManager.initializePowers();
        }
        if (AttributeManager.attributeManager != null) {
            AttributeManager.attributeManager.initializePowers();
        }
        for (ObjectCustomEnchantment enchantment : enchantments.values()) {
            enchantment.initializePower();
            PowerManager.powerManager.registerPower(enchantment.getPower());
        }
        int vanillaPowerCount = 0;
        for (VanillaEnchantmentOverride enchantment : vanillaEnchantments.values()) {
            if (!enchantment.isEnabled()) {
                continue;
            }
            enchantment.initializePower();
            PowerManager.powerManager.registerPower(enchantment.getPower());
            if (enchantment.getPower() != null) {
                vanillaPowerCount++;
            }
        }
        infoLogger.accept("Initialized powers for " + vanillaPowerCount + " vanilla enchantments.");
    }

    private void loadEnchantment(Path file, ConfigurationSection raritySettings) throws Exception {
        String fileName = toLogicalFileName(file);
        YamlConfiguration config = new YamlConfiguration();
        config.load(file.toFile());
        ObjectCustomEnchantment enchantment = new ObjectCustomEnchantment(
                fileName, config, registryLanguage, raritySettings);
        if (!enchantment.isEnabled()) {
            return;
        }
        String enchantmentId = enchantment.getKey().asString().toLowerCase(Locale.ROOT);
        if (enchantments.putIfAbsent(enchantmentId, enchantment) != null) {
            throw new IllegalArgumentException("Duplicate enchantment id: " + enchantmentId);
        }
    }

    private boolean loadEnchantmentSafely(Path file,
                                          ConfigurationSection raritySettings) {
        try {
            loadEnchantment(file, raritySettings);
            return true;
        } catch (Throwable throwable) {
            reportDefinitionError(file, throwable);
            return false;
        }
    }

    private YamlConfiguration loadGlobalConfig() throws Exception {
        YamlConfiguration defaults = InitManager.loadBundledConfiguration("config.yml");
        if (defaults == null) {
            defaults = new YamlConfiguration();
        }

        YamlConfiguration globalConfig = new YamlConfiguration();
        Path configFile = directory.getParent().resolve("config.yml");
        if (Files.isRegularFile(configFile)) {
            globalConfig.load(configFile.toFile());
        }

        // Paper registers enchantments during bootstrap, before JavaPlugin#onEnable creates
        // config.yml on a first installation. Materialize bundled defaults into this bootstrap
        // configuration so nested rarity sections behave exactly like disk-backed sections.
        for (String path : defaults.getKeys(true)) {
            if (!defaults.isConfigurationSection(path) && !globalConfig.contains(path)) {
                globalConfig.set(path, defaults.get(path));
            }
        }
        globalConfig.setDefaults(defaults);
        globalConfig.options().copyDefaults(true);
        return globalConfig;
    }

    private void loadVanillaEnchantment(Path file,
                                        ConfigurationSection raritySettings) throws Exception {
        String fileName = toLogicalFileName(file);
        YamlConfiguration config = new YamlConfiguration();
        config.load(file.toFile());
        VanillaEnchantmentOverride enchantment = new VanillaEnchantmentOverride(
                fileName, config, registryLanguage, raritySettings);
        String key = enchantment.getKey().asString();
        if (vanillaEnchantments.putIfAbsent(key, enchantment) != null) {
            throw new IllegalArgumentException("Duplicate vanilla enchantment key: " + key);
        }
    }

    private boolean loadVanillaEnchantmentSafely(Path file,
                                                 ConfigurationSection raritySettings) {
        try {
            loadVanillaEnchantment(file, raritySettings);
            return true;
        } catch (Throwable throwable) {
            reportDefinitionError(file, throwable);
            return false;
        }
    }

    private int validateDefinitions() {
        int rejectedDefinitions = 0;
        boolean removedDefinitions;
        do {
            Set<String> availableCustomEnchantments = Set.copyOf(enchantments.keySet());
            List<String> invalidCustomEnchantments = new ArrayList<>();
            List<String> invalidVanillaEnchantments = new ArrayList<>();

            for (Map.Entry<String, ObjectCustomEnchantment> entry : enchantments.entrySet()) {
                try {
                    entry.getValue().validateConfiguration(availableCustomEnchantments);
                } catch (Throwable throwable) {
                    reportValidationError(entry.getValue(), throwable);
                    invalidCustomEnchantments.add(entry.getKey());
                }
            }
            for (Map.Entry<String, VanillaEnchantmentOverride> entry
                    : vanillaEnchantments.entrySet()) {
                if (!entry.getValue().isEnabled()) {
                    continue;
                }
                try {
                    entry.getValue().validateConfiguration(availableCustomEnchantments);
                } catch (Throwable throwable) {
                    reportValidationError(entry.getValue(), throwable);
                    invalidVanillaEnchantments.add(entry.getKey());
                }
            }

            invalidCustomEnchantments.forEach(enchantments::remove);
            invalidVanillaEnchantments.forEach(vanillaEnchantments::remove);
            int removedThisPass = invalidCustomEnchantments.size()
                    + invalidVanillaEnchantments.size();
            rejectedDefinitions += removedThisPass;
            removedDefinitions = removedThisPass > 0;
        } while (removedDefinitions);
        return rejectedDefinitions;
    }

    private void reportDefinitionError(Path file, Throwable throwable) {
        Path normalizedFile = file.toAbsolutePath().normalize();
        String fileName = normalizedFile.startsWith(dataDirectory)
                ? dataDirectory.relativize(normalizedFile).toString().replace('\\', '/')
                : normalizedFile.toString();
        errorLogger.accept("Invalid enchantment configuration " + fileName + ": "
                + errorReason(throwable) + ". This definition will be skipped.", throwable);
    }

    private void reportValidationError(PowerEnchantmentDefinition definition,
                                       Throwable throwable) {
        errorLogger.accept("Invalid enchantment configuration " + definition.getConfigName()
                + ": " + errorReason(throwable)
                + ". This definition will be skipped.", throwable);
    }

    private String errorReason(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank()
                ? throwable.getClass().getSimpleName()
                : message;
    }

    private YamlConfiguration loadRegistryLanguage() {
        String configuredLanguage = globalConfig.getString("config-files.language", "en_US");
        String language = normalizeLanguageResourceName(configuredLanguage);

        Path languageFile = dataDirectory.resolve("languages")
                .resolve(language + ".yml")
                .normalize();
        if (languageFile.startsWith(dataDirectory.resolve("languages").normalize())
                && Files.isRegularFile(languageFile)) {
            YamlConfiguration customLanguage = new YamlConfiguration();
            try {
                customLanguage.load(languageFile.toFile());
                return customLanguage;
            } catch (Exception exception) {
                errorLogger.accept("Failed to load registry language file " + languageFile
                        + "; falling back to the bundled language.", exception);
            }
        }

        YamlConfiguration bundledLanguage = loadBundledLanguage(language);
        if (bundledLanguage != null) {
            return bundledLanguage;
        }
        bundledLanguage = loadBundledLanguage("en_US");
        return bundledLanguage == null ? new YamlConfiguration() : bundledLanguage;
    }

    private String normalizeLanguageResourceName(String language) {
        if (language == null) {
            return "en_US";
        }
        String normalized = language.replace('-', '_');
        if (!normalized.matches("[A-Za-z0-9_]+")) {
            return "en_US";
        }
        return switch (normalized.toLowerCase(Locale.ROOT)) {
            case "en_us" -> "en_US";
            case "zh_cn" -> "zh_CN";
            default -> normalized;
        };
    }

    private YamlConfiguration loadBundledLanguage(String language) {
        return InitManager.loadBundledConfiguration("languages/" + language + ".yml");
    }

    private Collection<Path> findDefinitionFiles(Path sourceDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(this::isYaml)
                    .sorted(Comparator.comparing(path -> sourceDirectory.relativize(path).toString()))
                    .toList();
        }
    }

    public ObjectCustomEnchantment getEnchantment(String id) {
        if (id == null) {
            return null;
        }
        String lookup = id.toLowerCase(Locale.ROOT);
        ObjectCustomEnchantment direct = enchantments.get(lookup);
        if (direct != null) {
            return direct;
        }
        String normalized = lookup.replace('-', '_').replace('/', '_').replace('\\', '_');
        direct = enchantments.get(normalized);
        if (direct != null) {
            return direct;
        }
        for (ObjectCustomEnchantment enchantment : enchantments.values()) {
            if (enchantment.getKey().asString().equalsIgnoreCase(id)
                    || enchantment.getKey().value().equalsIgnoreCase(id)) {
                return enchantment;
            }
        }
        return null;
    }

    public VanillaEnchantmentOverride getVanillaEnchantment(String key) {
        return key == null ? null : vanillaEnchantments.get(key.toLowerCase(Locale.ROOT));
    }

    public Collection<ObjectCustomEnchantment> getEnchantments() {
        return enchantments.values();
    }

    public Collection<VanillaEnchantmentOverride> getVanillaEnchantments() {
        return vanillaEnchantments.values();
    }

    public PowerEnchantmentDefinition getDefinition(String id) {
        ObjectCustomEnchantment custom = getEnchantment(id);
        return custom != null ? custom : getVanillaEnchantment(id);
    }

    public ConfigurationSection getSupportedItemsConfiguration() {
        return globalConfig == null ? null : globalConfig.getConfigurationSection("supported-items");
    }

    public PowerEnchantmentDefinition getPowerEnchantment(String id) {
        PowerEnchantmentDefinition definition = getDefinition(id);
        return definition != null && definition.isEnabled() && definition.getPower() != null
                ? definition : null;
    }

    private boolean isYaml(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    private String toLogicalFileName(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}

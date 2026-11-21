package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class InitManager extends AbstractManager {

    public static InitManager initManager;

    private final Path dataDirectory;

    private final BiConsumer<String, Throwable> errorLogger;

    private boolean firstLoad;

    public InitManager() {
        this(EnchantmentReform.instance.getDataFolder().toPath(),
                (message, throwable) -> EnchantmentReform.instance.getLogger()
                        .log(Level.SEVERE, message, throwable));
    }

    // Paper bootstrap runs before a JavaPlugin instance exists.
    public InitManager(Path dataDirectory, BiConsumer<String, Throwable> errorLogger) {
        initManager = this;
        this.dataDirectory = dataDirectory;
        this.errorLogger = errorLogger;
        firstLoad = !Files.exists(dataDirectory.resolve("config.yml"));
        init();
    }

    public void init() {
        resourceOutput("config.yml", true);
        resourceOutputFolder("languages", true);
        resourceOutputFolder("menus", true);
        resourceOutputFolder("items", false);
        resourceOutputFolder("attributes", true);
        resourceOutputFolder("skills", true);
        resourceOutputFolder("temp_powers", true);
        resourceOutputFolder("enchantments", false);
        resourceOutputFolder("vanilla_enchantments", false);
    }

    private void resourceOutputFolder(String folderName, boolean regenerate) {
        String normalizedFolder = folderName.endsWith("/") ? folderName : folderName + "/";
        File sourceFile;
        try {
            sourceFile = new File(InitManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            // Maven and IDE runs load classes from a directory rather than a plugin JAR.
            if (sourceFile.isDirectory()) {
                URL resource = InitManager.class.getResource("/" + normalizedFolder);
                if (resource == null) {
                    return;
                }
                Path folder = Path.of(resource.toURI());
                try (Stream<Path> files = Files.walk(folder)) {
                    files.filter(Files::isRegularFile).forEach(file -> resourceOutput(
                            normalizedFolder + folder.relativize(file).toString().replace('\\', '/'),
                            regenerate));
                }
                return;
            }
        } catch (URISyntaxException | IOException exception) {
            errorLogger.accept("Failed to extract resource folder: " + folderName, exception);
            return;
        }

        try (JarFile jarFile = new JarFile(sourceFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String entryName = jarEntry.getName();
                if (!entryName.startsWith(normalizedFolder)) {
                    continue;
                }
                resourceOutput(entryName, regenerate);
            }
        } catch (IOException exception) {
            errorLogger.accept("Failed to extract resource folder: " + folderName, exception);
        }
    }

    private void resourceOutput(String fileName, boolean regenerate) {
        Path target = dataDirectory.resolve(fileName);
        if (Files.exists(target) || (!firstLoad && !regenerate)) {
            return;
        }
        try (InputStream input = InitManager.class.getResourceAsStream("/" + fileName)) {
            if (input == null) {
                throw new IOException("Bundled resource not found: " + fileName);
            }
            Files.createDirectories(target.getParent());
            Files.copy(input, target);
        } catch (IOException exception) {
            errorLogger.accept("Failed to extract default configuration: " + fileName, exception);
        }
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }

    public static YamlConfiguration loadBundledConfiguration(String resource) {
        try (InputStream input = InitManager.class.getResourceAsStream("/" + resource)) {
            if (input == null) {
                return null;
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load bundled configuration: " + resource, exception);
        }
    }
}

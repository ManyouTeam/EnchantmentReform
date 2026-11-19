package cn.superiormc.enchantmentreform.objects;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ObjectTempPower extends AbstractPowerSourceDefinition {

    public static final NamespacedKey DATA_KEY = NamespacedKey.fromString("enchantmentreform:temp_powers");

    private static final NamespacedKey LEVEL_KEY = NamespacedKey.fromString("enchantmentreform:level");

    private static final NamespacedKey REMEMBER_KEY = NamespacedKey.fromString("enchantmentreform:remember");

    private static final NamespacedKey SESSION_KEY = NamespacedKey.fromString("enchantmentreform:session");

    private final String id;

    private final boolean remember;

    private final boolean once;

    private final int defaultLevel;

    private final NamespacedKey entryKey;

    public ObjectTempPower(String id, YamlConfiguration config) {
        super(id, "temp_powers/" + id + ".yml", config, true);
        if (id == null || !id.matches("[a-z0-9_-]+")) {
            throw error("Temporary power id may only contain lowercase letters, numbers, '_' and '-'");
        }
        this.id = id;
        this.remember = configuredBoolean("remember", false);
        this.once = configuredBoolean("once", false);
        this.defaultLevel = rangedInt("default-level", 1, 1, Integer.MAX_VALUE);
        this.entryKey = NamespacedKey.fromString("enchantmentreform:" + this.id);
        if (!getActiveSlots().isEmpty()) {
            throw error("Temporary powers are player-bound; 'active-slots' must be empty");
        }
        if (getPowers() == null) {
            throw error("Missing required section 'powers'");
        }
    }

    public String getId() {
        return id;
    }

    public boolean isRemembered() {
        return remember;
    }

    public boolean isOnce() {
        return once;
    }

    public int getDefaultLevel() {
        return defaultLevel;
    }

    @Override
    public String powerSourceId() {
        return "temp_power:" + id;
    }

    public int getLevel(Player player, String session) {
        PersistentDataContainer powers = player.getPersistentDataContainer().get(
                DATA_KEY, PersistentDataType.TAG_CONTAINER);
        PersistentDataContainer entry = powers == null ? null : powers.get(
                entryKey, PersistentDataType.TAG_CONTAINER);
        if (entry == null) {
            return 0;
        }
        int level = entry.getOrDefault(LEVEL_KEY, PersistentDataType.INTEGER, 0);
        boolean storedRemember = entry.getOrDefault(REMEMBER_KEY, PersistentDataType.BYTE, (byte) 0) != 0;
        boolean sameSession = session.equals(entry.get(SESSION_KEY, PersistentDataType.STRING));
        // A transient grant must never become persistent after its login session has ended.
        if (level <= 0 || !sameSession && (!storedRemember || !remember)) {
            remove(player);
            return 0;
        }
        if (storedRemember != remember || !sameSession) {
            setLevel(player, level, session);
        }
        return level;
    }

    public void setLevel(Player player, int level, String session) {
        if (level <= 0) {
            throw new IllegalArgumentException("Temporary power level must be positive");
        }
        PersistentDataContainer data = player.getPersistentDataContainer();
        PersistentDataContainer powers = data.get(DATA_KEY, PersistentDataType.TAG_CONTAINER);
        if (powers == null) {
            powers = data.getAdapterContext().newPersistentDataContainer();
        }
        PersistentDataContainer entry = data.getAdapterContext().newPersistentDataContainer();
        entry.set(LEVEL_KEY, PersistentDataType.INTEGER, level);
        entry.set(REMEMBER_KEY, PersistentDataType.BYTE, remember ? (byte) 1 : (byte) 0);
        entry.set(SESSION_KEY, PersistentDataType.STRING, session);
        powers.set(entryKey, PersistentDataType.TAG_CONTAINER, entry);
        data.set(DATA_KEY, PersistentDataType.TAG_CONTAINER, powers);
    }

    public void remove(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        PersistentDataContainer powers = data.get(DATA_KEY, PersistentDataType.TAG_CONTAINER);
        if (powers != null) {
            powers.remove(entryKey);
            save(player, powers);
        }
    }

    private void save(Player player, PersistentDataContainer powers) {
        if (powers.isEmpty()) {
            player.getPersistentDataContainer().remove(DATA_KEY);
        } else {
            player.getPersistentDataContainer().set(DATA_KEY, PersistentDataType.TAG_CONTAINER, powers);
        }
    }

}

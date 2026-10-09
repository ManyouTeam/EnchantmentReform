package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.objects.ObjectCustomAttribute;
import cn.superiormc.enchantmentreform.objects.ObjectCustomItem;
import cn.superiormc.enchantmentreform.objects.PowerEnchantmentDefinition;
import cn.superiormc.enchantmentreform.objects.PowerSourceDefinition;
import cn.superiormc.enchantmentreform.objects.abilities.DisableEnchantmentsAbility;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveEnchantmentManager {

    private static final List<EquipmentSlot> TRACKED_SLOTS = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);

    private static final List<EquipmentSlot> INTERACT_SLOT_PRIORITY = List.of(
            EquipmentSlot.HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET);

    // Cache item contents only: player conditions can change without an equipment event.
    private final Map<UUID, Map<EquipmentSlot, ScannedItem>> cache = new ConcurrentHashMap<>();

    private final Map<PowerEnchantmentDefinition, List<DisableEnchantmentsAbility>> disablingAbilities =
            new ConcurrentHashMap<>();

    private volatile LookupMode mode = LookupMode.SCAN;

    public ActiveEnchantmentManager() {
        reloadMode();
    }

    public void reloadMode() {
        String configured = ConfigManager.configManager == null
                ? "SCAN"
                : ConfigManager.configManager.getString("powers.active-enchantment-mode", "SCAN");
        LookupMode requested;
        try {
            requested = LookupMode.valueOf(configured.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            requested = LookupMode.SCAN;
        }
        if (requested == LookupMode.CACHE && !EnchantmentReform.methodUtil.methodID().equals("paper")) {
            mode = LookupMode.SCAN;
        } else {
            mode = requested;
        }
        cache.clear();
        disablingAbilities.clear();
    }

    public List<ActivePowerSource> findActive(TriggerData data) {
        List<ActivePowerSource> candidates = new ArrayList<>();
        candidates.addAll(scanAttributes(data.player()));
        EquipmentSlot triggerSlot = data.triggerItemSlot() == null ? EquipmentSlot.HAND : data.triggerItemSlot();
        candidates.addAll(scanTriggerItem(data.player(), data.triggerItem(), triggerSlot));
        Map<EquipmentSlot, List<ActivePowerSource>> equipment = equipment(data.player());
        TRACKED_SLOTS.forEach(slot -> candidates.addAll(equipment.getOrDefault(slot, List.of())));
        return sorted(resolve(candidates));
    }

    /**
     * Scans the item that directly produced the event. A weapon held in either hand can produce a
     * hand-based trigger (e.g. a bow fired from the off hand), so the main/off hand distinction is
     * treated as interchangeable here: an {@code active-slots: [HAND]} enchantment still activates
     * when its item is the off-hand trigger item. Passive equipment scanning keeps the strict slot
     * check, so an idle off-hand bow does not activate on unrelated melee attacks.
     */
    private List<ActivePowerSource> scanTriggerItem(Player player, ItemStack item,
                                                    EquipmentSlot slot) {
        if (slot != EquipmentSlot.OFF_HAND) {
            return scanItem(player, item, slot);
        }
        List<ActivePowerSource> candidates = new ArrayList<>(
                scanItem(player, item, EquipmentSlot.OFF_HAND));
        for (ActivePowerSource active : scanItem(player, item, EquipmentSlot.HAND)) {
            candidates.add(new ActivePowerSource(
                    active.source(), active.power(), active.level(), active.item(),
                    EquipmentSlot.OFF_HAND));
        }
        return resolve(candidates);
    }

    /**
     * Resolves a stable active source for interaction events without preferring the hand that
     * happened to produce the current PlayerInteractEvent. Main hand wins equal-level ties,
     * followed by off hand and then worn equipment.
     */
    public List<ActivePowerSource> findActiveForInteraction(TriggerData data) {
        List<ActivePowerSource> candidates = new ArrayList<>();
        candidates.addAll(scanAttributes(data.player()));
        Map<EquipmentSlot, List<ActivePowerSource>> equipment = equipment(data.player());
        INTERACT_SLOT_PRIORITY.forEach(slot -> candidates.addAll(
                equipment.getOrDefault(slot, List.of())));
        return sorted(resolve(candidates));
    }

    public synchronized EquipmentTransition updateEquipment(
            Player player, Map<EquipmentSlot, EquipmentChange> changes) {
        if (changes == null || changes.isEmpty()) {
            return EquipmentTransition.EMPTY;
        }

        Map<EquipmentSlot, ScannedItem> current = mode == LookupMode.CACHE
                ? cache.get(player.getUniqueId())
                : null;
        if (current == null) {
            current = scanEquipment(player);
        }

        EnumMap<EquipmentSlot, ScannedItem> before = mutableCopy(current);
        EnumMap<EquipmentSlot, ScannedItem> after = mutableCopy(current);
        for (Map.Entry<EquipmentSlot, EquipmentChange> entry : changes.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            if (!TRACKED_SLOTS.contains(slot)) {
                continue;
            }
            EquipmentChange change = entry.getValue();
            before.put(slot, scanItem(change.oldItem()));
            after.put(slot, scanItem(change.newItem()));
        }

        Map<EquipmentSlot, ScannedItem> afterSnapshot = Map.copyOf(after);
        if (mode == LookupMode.CACHE) {
            cache.put(player.getUniqueId(), afterSnapshot);
        }
        return compare(aggregate(evaluateEquipment(player, before)),
                aggregate(evaluateEquipment(player, afterSnapshot)));
    }

    public void clear(Player player) {
        if (player != null) cache.remove(player.getUniqueId());
    }

    public void clearAll() {
        cache.clear();
        disablingAbilities.clear();
    }

    public LookupMode mode() {
        return mode;
    }

    private Map<EquipmentSlot, List<ActivePowerSource>> equipment(Player player) {
        Map<EquipmentSlot, ScannedItem> snapshot = mode == LookupMode.CACHE
                ? cache.computeIfAbsent(player.getUniqueId(), ignored -> scanEquipment(player))
                : scanEquipment(player);
        return evaluateEquipment(player, snapshot);
    }

    private Map<EquipmentSlot, ScannedItem> scanEquipment(Player player) {
        EnumMap<EquipmentSlot, ScannedItem> result = new EnumMap<>(EquipmentSlot.class);
        PlayerInventory inventory = player.getInventory();
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            result.put(slot, scanItem(itemAt(inventory, slot)));
        }
        return Map.copyOf(result);
    }

    private Map<EquipmentSlot, List<ActivePowerSource>> evaluateEquipment(
            Player player, Map<EquipmentSlot, ScannedItem> snapshot) {
        EnumMap<EquipmentSlot, List<ActivePowerSource>> result = new EnumMap<>(EquipmentSlot.class);
        snapshot.forEach((slot, item) -> result.put(slot, evaluateItem(player, item, slot)));
        return Map.copyOf(result);
    }

    private List<ActivePowerSource> scanItem(Player player, ItemStack item,
                                             EquipmentSlot slot) {
        return evaluateItem(player, scanItem(item), slot);
    }

    private ScannedItem scanItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return ScannedItem.EMPTY;
        EnchantmentConfigManager manager = EnchantmentConfigManager.getInstance();

        List<ScannedEnchantment> present = new ArrayList<>();
        if (manager != null) {
            item.getEnchantments().forEach((current, level) -> {
                PowerEnchantmentDefinition enchantment = manager.getPowerEnchantment(
                        current.getKey().toString());
                if (enchantment != null) {
                    present.add(new ScannedEnchantment(enchantment, level));
                }
            });
        }
        ObjectCustomItem customItem = ItemManager.itemManager == null
                ? null : ItemManager.itemManager.getCustomItem(item);
        return new ScannedItem(item, List.copyOf(present), customItem);
    }

    private List<ActivePowerSource> evaluateItem(Player player, ScannedItem scanned,
                                                EquipmentSlot slot) {
        ItemStack item = scanned.item();
        List<ScannedEnchantment> present = scanned.enchantments();
        List<DisablingRule> rules = new ArrayList<>();
        for (ScannedEnchantment source : present) {
            int effectiveLevel = source.enchantment().getEffectivePowerLevel(
                    player, source.level(), item, slot);
            if (effectiveLevel <= 0) {
                continue;
            }
            List<DisableEnchantmentsAbility> abilities = disablingAbilities(
                    source.enchantment());
            if (abilities.isEmpty()) {
                continue;
            }
            PowerContext context = new PowerContext(
                    source.enchantment().getPower(), effectiveLevel, null, null,
                    item, slot, null, player);
            if (!source.enchantment().meetsConditions(player, context)) {
                continue;
            }
            for (DisableEnchantmentsAbility ability : abilities) {
                rules.add(new DisablingRule(source.enchantment(), ability));
            }
        }

        List<ActivePowerSource> result = new ArrayList<>();
        for (ScannedEnchantment candidate : present) {
            PowerEnchantmentDefinition enchantment = candidate.enchantment();
            if (!enchantment.isActiveOn(slot) || isDisabled(enchantment, rules)) {
                continue;
            }
            int effectiveLevel = enchantment.getEffectivePowerLevel(
                    player, candidate.level(), item, slot);
            if (effectiveLevel <= 0) {
                continue;
            }
            result.add(new ActivePowerSource(
                    enchantment, enchantment.getPower(), effectiveLevel, item, slot));
        }
        ObjectCustomItem customItem = scanned.customItem();
        if (customItem != null && customItem.isActiveOn(slot)) {
            result.add(new ActivePowerSource(customItem, customItem.getPower(), 1, item, slot));
        }
        return List.copyOf(result);
    }

    private List<ActivePowerSource> scanAttributes(Player player) {
        AttributeManager manager = AttributeManager.attributeManager;
        if (manager == null || player == null) {
            return List.of();
        }
        List<ActivePowerSource> result = new ArrayList<>();
        for (ObjectCustomAttribute attribute : manager.getAttributes()) {
            if (attribute.getPower() == null || !attribute.shouldExecutePower(player)) {
                continue;
            }
            result.add(new ActivePowerSource(
                    attribute, attribute.getPower(), attribute.getValue(player), null, null));
        }
        return List.copyOf(result);
    }

    private boolean isDisabled(PowerEnchantmentDefinition candidate, List<DisablingRule> rules) {
        return rules.stream().anyMatch(rule -> rule.source() != candidate
                && rule.ability().disables(candidate));
    }

    private List<DisableEnchantmentsAbility> disablingAbilities(
            PowerEnchantmentDefinition enchantment) {
        if (enchantment.getPower() == null || AbilityManager.abilityManager == null) {
            return List.of();
        }
        return disablingAbilities.computeIfAbsent(enchantment, ignored ->
                AbilityManager.abilityManager.parseActions(
                                enchantment.getPower().getSection("activation-abilities"))
                        .stream()
                        .filter(DisableEnchantmentsAbility.class::isInstance)
                        .map(DisableEnchantmentsAbility.class::cast)
                        .toList());
    }

    private ItemStack itemAt(PlayerInventory inventory, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> inventory.getHelmet();
            case CHEST -> inventory.getChestplate();
            case LEGS -> inventory.getLeggings();
            case FEET -> inventory.getBoots();
            case HAND -> inventory.getItemInMainHand();
            case OFF_HAND -> inventory.getItemInOffHand();
            default -> null;
        };
    }

    private List<ActivePowerSource> sorted(List<ActivePowerSource> activePowers) {
        return activePowers.stream()
                .sorted(Comparator
                        .comparingInt((ActivePowerSource active) -> active.source().getExecutionPriority())
                        .reversed()
                        .thenComparing(active -> active.source().powerSourceId(),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<ActivePowerSource> aggregate(
            Map<EquipmentSlot, List<ActivePowerSource>> equipment) {
        List<ActivePowerSource> candidates = new ArrayList<>();
        TRACKED_SLOTS.forEach(slot -> candidates.addAll(equipment.getOrDefault(slot, List.of())));
        return resolve(candidates);
    }

    private List<ActivePowerSource> resolve(List<ActivePowerSource> candidates) {
        Map<OccurrenceKey, ActivePowerSource> result = new LinkedHashMap<>();
        for (ActivePowerSource active : candidates) {
            OccurrenceKey key = occurrenceKey(active);
            ActivePowerSource previous = result.get(key);
            if (previous == null || !active.source().isDuplicateAllowed()
                    && active.level() > previous.level()) {
                result.put(key, active);
            }
        }
        return List.copyOf(result.values());
    }

    private EquipmentTransition compare(List<ActivePowerSource> before,
                                        List<ActivePowerSource> after) {
        Map<OccurrenceKey, ActivePowerSource> beforeByOccurrence = indexOccurrences(before);
        Map<OccurrenceKey, ActivePowerSource> afterByOccurrence = indexOccurrences(after);
        List<ActivePowerSource> deactivated = new ArrayList<>();
        List<ActivePowerSource> activated = new ArrayList<>();
        Set<OccurrenceKey> occurrences = new LinkedHashSet<>(beforeByOccurrence.keySet());
        occurrences.addAll(afterByOccurrence.keySet());
        for (OccurrenceKey occurrence : occurrences) {
            ActivePowerSource oldActive = beforeByOccurrence.get(occurrence);
            ActivePowerSource newActive = afterByOccurrence.get(occurrence);
            if (sameEffective(oldActive, newActive)) continue;
            if (oldActive != null) deactivated.add(oldActive);
            if (newActive != null) activated.add(newActive);
        }
        return new EquipmentTransition(List.copyOf(deactivated), List.copyOf(activated));
    }

    private Map<OccurrenceKey, ActivePowerSource> indexOccurrences(
            List<ActivePowerSource> activePowers) {
        Map<OccurrenceKey, ActivePowerSource> result = new LinkedHashMap<>();
        for (ActivePowerSource active : activePowers) {
            result.put(occurrenceKey(active), active);
        }
        return result;
    }

    private OccurrenceKey occurrenceKey(ActivePowerSource active) {
        return new OccurrenceKey(active.source(),
                active.source().isDuplicateAllowed() ? active.slot() : null);
    }

    private boolean sameEffective(ActivePowerSource first, ActivePowerSource second) {
        return first == second || first != null && second != null
                && first.source() == second.source()
                && first.level() == second.level()
                && first.slot() == second.slot();
    }

    private EnumMap<EquipmentSlot, ScannedItem> mutableCopy(
            Map<EquipmentSlot, ScannedItem> source) {
        EnumMap<EquipmentSlot, ScannedItem> result = new EnumMap<>(EquipmentSlot.class);
        result.putAll(source);
        return result;
    }

    public enum LookupMode {
        SCAN,
        CACHE
    }

    public record EquipmentChange(ItemStack oldItem, ItemStack newItem) {
    }

    public record EquipmentTransition(List<ActivePowerSource> deactivated,
                                      List<ActivePowerSource> activated) {

        private static final EquipmentTransition EMPTY = new EquipmentTransition(List.of(), List.of());
    }

    private record ScannedEnchantment(PowerEnchantmentDefinition enchantment, int level) {
    }

    private record ScannedItem(ItemStack item, List<ScannedEnchantment> enchantments,
                               ObjectCustomItem customItem) {
        private static final ScannedItem EMPTY = new ScannedItem(null, List.of(), null);
    }

    private record DisablingRule(PowerEnchantmentDefinition source,
                                 DisableEnchantmentsAbility ability) {
    }

    private record OccurrenceKey(PowerSourceDefinition source, EquipmentSlot slot) {
    }
}

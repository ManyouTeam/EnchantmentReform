package cn.superiormc.enchantmentreform.api.trigger;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class BuiltinContextKeys {

    /**
     * Numeric keys are registered while this class initializes and are read-only afterwards.
     * Keeping the registry private avoids exposing the mutable initialization map.
     */
    private static final Map<String, ContextKey<? extends Number>> NUMERIC_KEYS = new HashMap<>();

    public static final ContextKey<BlockState> BROKEN_BLOCK_STATE = key("broken_block_state", BlockState.class);
    public static final ContextKey<Double> ORIGINAL_DAMAGE = key("original_damage", Double.class);
    public static final ContextKey<Double> ATTACK_COOLDOWN = key("attack_cooldown", Double.class);
    public static final ContextKey<Double> ORIGINAL_AMOUNT = key("original_amount", Double.class);
    public static final ContextKey<Float> ORIGINAL_DURATION = key("original_duration", Float.class);
    public static final ContextKey<Float> ORIGINAL_YIELD = key("original_yield", Float.class);
    public static final ContextKey<Float> ORIGINAL_RADIUS = key("original_radius", Float.class);
    public static final ContextKey<Float> BLOCK_BREAK_PROGRESS = key("block_break_progress", Float.class);
    public static final ContextKey<Integer> ORIGINAL_AIR = key("original_air", Integer.class);
    public static final ContextKey<Integer> PREVIOUS_AIR = key("previous_air", Integer.class);
    public static final ContextKey<Integer> ORIGINAL_FOOD_LEVEL = key("original_food_level", Integer.class);
    public static final ContextKey<Integer> ORIGINAL_EXPERIENCE = key("original_experience", Integer.class);
    public static final ContextKey<Integer> ENCHANTMENT_LEVEL_COST = key("enchantment_level_cost", Integer.class);
    public static final ContextKey<Integer> ORIGINAL_LAPIS = key("original_lapis", Integer.class);
    public static final ContextKey<Integer> ORIGINAL_ITEM_DAMAGE = key("original_item_damage", Integer.class);
    public static final ContextKey<Double> STATE_PREVIOUS = key("state_previous", Double.class);
    public static final ContextKey<Double> STATE_CURRENT = key("state_current", Double.class);
    public static final ContextKey<Long> STATE_TRIGGER_COUNT = key("state_trigger_count", Long.class);
    public static final ContextKey<Double> STATE_REMAINDER = key("state_remainder", Double.class);
    public static final ContextKey<Boolean> DAMAGE_BY_ENTITY = key("damage_by_entity", Boolean.class);
    public static final ContextKey<Boolean> DAMAGE_BY_BLOCK = key("damage_by_block", Boolean.class);
    public static final ContextKey<Boolean> INTERNAL_BLOCK_BREAK = key("internal_block_break", Boolean.class);
    public static final ContextKey<Float> BOW_FORCE = key("bow_force", Float.class);
    public static final ContextKey<ItemStack> BOW = key("bow", ItemStack.class);
    public static final ContextKey<ItemStack> CONSUMABLE = key("consumable", ItemStack.class);
    public static final ContextKey<EquipmentSlot> HAND = key("hand", EquipmentSlot.class);
    public static final ContextKey<Projectile> PROJECTILE = key("projectile", Projectile.class);
    public static final ContextKey<EntityDamageEvent.DamageCause> DAMAGE_CAUSE = key("damage_cause", EntityDamageEvent.DamageCause.class);
    public static final ContextKey<EntityTargetEvent.TargetReason> TARGET_REASON = key("target_reason", EntityTargetEvent.TargetReason.class);
    public static final ContextKey<Integer> TARGET_COUNT = key("target_count", Integer.class);

    private BuiltinContextKeys() {
    }

    /** Returns the numeric context key represented by a placeholder name, or {@code null}. */
    public static ContextKey<? extends Number> numericKey(String placeholder) {
        return NUMERIC_KEYS.get(placeholder);
    }

    private static <T> ContextKey<T> key(String value, Class<T> type) {
        ContextKey<T> contextKey = new ContextKey<>(new NamespacedKey("enchantmentreform", value), type);
        if (Number.class.isAssignableFrom(type)) {
            registerNumeric(value, contextKey);
        }
        return contextKey;
    }

    @SuppressWarnings("unchecked")
    private static void registerNumeric(String value, ContextKey<?> contextKey) {
        ContextKey<? extends Number> numericKey = (ContextKey<? extends Number>) contextKey;
        ContextKey<? extends Number> previous = NUMERIC_KEYS.putIfAbsent(value, numericKey);
        if (previous != null) {
            throw new IllegalStateException("Duplicate numeric context key: " + value);
        }
    }
}

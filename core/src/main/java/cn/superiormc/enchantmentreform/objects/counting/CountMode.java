package cn.superiormc.enchantmentreform.objects.counting;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.ContextKey;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Explicit counting capabilities of the built-in triggers. */
public enum CountMode {
    EVENTS("events", null, Set.of()),
    ITEMS("items", null, Set.of("fish", "block_drop_item", "piglin_barter", "trade", "purchase", "kill")),
    BLOCKS("blocks", null, Set.of("block_place")),
    DAMAGE("damage", BuiltinContextKeys.FINAL_DAMAGE, Set.of("attack", "melee_attack", "damage", "damage_by_entity")),
    BLOCKED_DAMAGE("blocked_damage", BuiltinContextKeys.ORIGINAL_DAMAGE, Set.of("shield_block")),
    HEALING("healing", BuiltinContextKeys.EFFECTIVE_HEALING, Set.of("regain")),
    DURABILITY("durability", BuiltinContextKeys.EFFECTIVE_ITEM_DAMAGE, Set.of("item_damage")),
    EXPERIENCE("experience", BuiltinContextKeys.ORIGINAL_EXPERIENCE, Set.of("exp_change", "pickup_experience")),
    DISTANCE("distance", BuiltinContextKeys.MOVEMENT_DISTANCE, Set.of("move")),
    EXHAUSTION("exhaustion", BuiltinContextKeys.EXHAUSTION_AMOUNT, Set.of("exhaustion"));

    public static final BigDecimal MAXIMUM = BigDecimal.valueOf(Long.MAX_VALUE);

    public static final int SCALE = 6;

    private final String id;

    private final ContextKey<? extends Number> key;

    private final Set<String> triggers;

    CountMode(String id, ContextKey<? extends Number> key, Set<String> triggers) {
        this.id = id;
        this.key = key;
        this.triggers = triggers;
    }

    public String id() {
        return id;
    }

    public boolean supports(String trigger) {
        return this == EVENTS || trigger.startsWith("enchantmentreform:")
                && triggers.contains(trigger.substring("enchantmentreform:".length()));
    }

    public static List<String> supported(String trigger) {
        return Arrays.stream(values()).filter(mode -> mode.supports(trigger)).map(CountMode::id).toList();
    }

    public static CountMode read(String trigger, String value) {
        return Arrays.stream(values()).filter(mode -> mode.id.equals(value) && mode.supports(trigger))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Trigger " + trigger
                        + " does not support count: " + value + "; supported: " + supported(trigger)));
    }

    public BigDecimal quantity(PowerContext context) {
        if (this == EVENTS) {
            // Preserve the original block-coordinate semantics for event-counted movement.
            if (context.trigger().key().toString().equals("enchantmentreform:move") && context.triggerData() != null) {
                var from = context.triggerData().from();
                var to = context.triggerData().to();
                if (from != null && to != null && Objects.equals(from.getWorld(), to.getWorld())
                        && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                        && from.getBlockZ() == to.getBlockZ()) {
                    return BigDecimal.ZERO;
                }
            }
            return BigDecimal.ONE;
        }
        if (key == null || context.triggerData() == null) {
            return BigDecimal.ZERO;
        }
        Number value = context.triggerData().extra(key).orElse(null);
        return value == null ? BigDecimal.ZERO : positive(value.doubleValue());
    }

    public static BigDecimal positive(double value) {
        if (!Double.isFinite(value) || value <= 0D) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value).min(MAXIMUM).setScale(SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}

package cn.superiormc.enchantmentreform.objects.counting;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

/** Session-scoped repeatable batches for powers and skill experience sources. */
public final class CountBatch {

    private record Key(UUID player, EquipmentSlot slot) { }

    private final BigDecimal every;

    private final boolean perSlot;

    private final Map<Key, BigDecimal> remainders = new ConcurrentHashMap<>();

    public CountBatch(ConfigurationSection section, boolean perSlot) {
        this.perSlot = perSlot;
        every = section.contains("count-every") ? readEvery(section.get("count-every")) : null;
    }

    private static BigDecimal readEvery(Object configured) {
        try {
            BigDecimal value = new BigDecimal(String.valueOf(configured)).stripTrailingZeros();
            if (value.signum() <= 0 || value.compareTo(CountMode.MAXIMUM) > 0 || value.scale() > CountMode.SCALE) {
                throw new IllegalArgumentException("count-every must be positive, with at most six decimal places");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid count-every: " + configured, exception);
        }
    }

    private Key key(PowerContext context) {
        return new Key(context.player().getUniqueId(), perSlot ? context.contextSlot() : null);
    }

    /** Commit only quantities that passed conditions and anti-abuse, carrying any unfinished batch. */
    public PowerContext settle(PowerContext context, BigDecimal quantity) {
        if (context == null || context.player() == null || context.triggerData() == null || quantity.signum() <= 0) {
            return null;
        }
        quantity = quantity.min(CountMode.MAXIMUM).setScale(CountMode.SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
        if (quantity.signum() == 0) {
            return null;
        }
        BigDecimal amount = quantity;
        BigDecimal remainder = BigDecimal.ZERO;
        if (every != null) {
            BigDecimal added = quantity;
            BigDecimal[] settled = new BigDecimal[2];
            remainders.compute(key(context), (key, previous) -> {
                BigDecimal total = added.add(previous == null ? BigDecimal.ZERO : previous);
                settled[0] = total.divideToIntegralValue(every);
                settled[1] = total.remainder(every).stripTrailingZeros();
                return settled[1].signum() == 0 ? null : settled[1];
            });
            amount = settled[0];
            remainder = settled[1];
        }
        if (amount.signum() == 0) {
            return null;
        }
        return context.withTriggerData(context.triggerData().toBuilder()
                .extra(BuiltinContextKeys.COUNT_AMOUNT, amount.doubleValue())
                .extra(BuiltinContextKeys.COUNT_QUANTITY, quantity.doubleValue())
                .extra(BuiltinContextKeys.COUNT_REMAINDER, remainder.doubleValue()).build());
    }

    public void initialize(Player player) {
        clear(player.getUniqueId());
    }

    public void clear(UUID player) {
        remainders.keySet().removeIf(key -> key.player().equals(player));
    }
}

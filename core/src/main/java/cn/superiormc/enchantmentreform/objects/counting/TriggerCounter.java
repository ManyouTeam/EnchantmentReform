package cn.superiormc.enchantmentreform.objects.counting;

import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;

import java.math.BigDecimal;
import java.util.function.Predicate;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Cancellable;

/** Measures matching event quantities; progress and batch storage belong to the consumer. */
public final class TriggerCounter {

    private final CountMode mode;

    public TriggerCounter(ConfigurationSection section, String trigger) {
        mode = CountMode.read(trigger, section.getString("count", "events"));
    }

    public CountMode mode() {
        return mode;
    }

    /** Read event quantities and apply conditions to each counted item or block before claiming anti-abuse. */
    public BigDecimal measure(PowerContext context, Predicate<PowerContext> conditions) {
        if (context == null || context.player() == null || context.triggerData() == null) {
            return BigDecimal.ZERO;
        }
        if (context.event() instanceof Cancellable cancelled && cancelled.isCancelled()) {
            return BigDecimal.ZERO;
        }
        if (mode == CountMode.BLOCKS) {
            long count = context.triggerData().placedBlocks().stream()
                    .filter(block -> conditions.test(context.withTriggerData(context.triggerData().withPlacedBlock(block))))
                    .count();
            return BigDecimal.valueOf(count);
        }
        if (mode != CountMode.ITEMS) {
            return conditions.test(context) ? mode.quantity(context) : BigDecimal.ZERO;
        }
        BigDecimal quantity = BigDecimal.ZERO;
        for (var item : context.triggerData().outputItems()) {
            PowerContext counted = context.withTriggerData(context.triggerData().withCountedItem(item));
            if (conditions.test(counted)) {
                quantity = quantity.add(BigDecimal.valueOf(item.getAmount())).min(CountMode.MAXIMUM);
            }
        }
        return quantity;
    }

}

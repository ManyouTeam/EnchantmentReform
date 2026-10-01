package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.AbstractTrigger;
import cn.superiormc.enchantmentreform.api.trigger.EntitySelector;
import cn.superiormc.enchantmentreform.api.trigger.TriggerData;
import cn.superiormc.enchantmentreform.api.trigger.TriggerResult;
import cn.superiormc.enchantmentreform.objects.ObjectPower;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public record PowerContext(
        ObjectPower power,
        int level,
        AbstractTrigger<?> trigger,
        TriggerData triggerData,
        ItemStack contextItem,
        EquipmentSlot contextSlot,
        TriggerResult result,
        Player player
) {

    public PowerContext(ObjectPower power,
                        int level,
                        AbstractTrigger<?> trigger,
                        TriggerData triggerData,
                        ItemStack contextItem,
                        EquipmentSlot contextSlot,
                        TriggerResult result) {
        this(power, level, trigger, triggerData, contextItem, contextSlot, result,
                triggerData == null ? null : triggerData.player());
    }

    public Entity source() {
        return triggerData == null ? null : triggerData.sourceEntity();
    }

    public Entity target() {
        return triggerData == null ? null : triggerData.targetEntity();
    }

    public Entity skill() {
        if (result != null && result.skillEntity() != null) {
            return result.skillEntity();
        }
        return triggerData == null ? null : triggerData.skillEntity();
    }

    public Entity entity(EntitySelector selector) {
        if (selector == EntitySelector.PLAYER) {
            return player;
        }
        if (selector == EntitySelector.SKILL) {
            return skill();
        }
        return triggerData == null ? null : triggerData.resolveEntity(selector);
    }

    public Entity entity(String selector, EntitySelector defaultSelector) {
        return entity(EntitySelector.parse(selector, defaultSelector));
    }

    public LivingEntity livingEntity(EntitySelector selector) {
        Entity entity = entity(selector);
        return entity instanceof LivingEntity living ? living : null;
    }

    public LivingEntity livingEntity(String selector, EntitySelector defaultSelector) {
        return livingEntity(EntitySelector.parse(selector, defaultSelector));
    }

    public Player player(EntitySelector selector) {
        Entity entity = entity(selector);
        return entity instanceof Player selected ? selected : null;
    }

    public Player player(String selector, EntitySelector defaultSelector) {
        return player(EntitySelector.parse(selector, defaultSelector));
    }

    public Event event() {
        return triggerData == null ? null : triggerData.event();
    }

    public Block block() {
        return triggerData == null ? null : triggerData.block();
    }

    /**
     * Snapshot of the block as it was before being broken, when available (e.g. during
     * {@code on-block-drop-item}). Returns {@code null} when no snapshot was captured.
     */
    public BlockState brokenBlockState() {
        return triggerData == null ? null
                : triggerData.extra(BuiltinContextKeys.BROKEN_BLOCK_STATE).orElse(null);
    }

    /** CONTEXT item: the item whose enchantment is currently being executed. */
    public ItemStack item() {
        return contextItem;
    }

    public ItemStack triggerItem() {
        return triggerData == null ? null : triggerData.triggerItem();
    }

    public Location location() {
        if (triggerData == null) {
            return null;
        }
        Location explicit = triggerData.location();
        if (explicit != null) {
            return explicit;
        }
        Location to = triggerData.to();
        if (to != null) {
            return to;
        }
        if (block() != null) {
            return block().getLocation().add(0.5D, 0.5D, 0.5D);
        }
        if (target() != null) {
            return target().getLocation();
        }
        if (skill() != null) {
            return skill().getLocation();
        }
        if (source() != null) {
            return source().getLocation();
        }
        return player == null ? null : player.getLocation();
    }

    public PowerContext withPlayer(Player value) {
        return new PowerContext(power, level, trigger, triggerData, contextItem, contextSlot, result, value);
    }

    public PowerContext withLevel(int value) {
        return new PowerContext(power, value, trigger, triggerData, contextItem, contextSlot, result, player);
    }

    public PowerContext withTriggerData(TriggerData value) {
        return new PowerContext(power, level, trigger, value, contextItem, contextSlot, result, player);
    }

    public PowerContext withTarget(Entity value) {
        TriggerData changed = triggerData == null ? null : triggerData.withTarget(value);
        return new PowerContext(power, level, trigger, changed, contextItem, contextSlot, result, player);
    }

    public PowerContext withBlock(Block value) {
        TriggerData changed = triggerData == null ? null : triggerData.withBlock(value);
        return new PowerContext(power, level, trigger, changed, contextItem, contextSlot, result, player);
    }

    public double originalDamage() {
        if (triggerData != null) {
            var stored = triggerData.extra(BuiltinContextKeys.ORIGINAL_DAMAGE);
            if (stored.isPresent()) return stored.get();
        }
        return event() instanceof EntityDamageEvent damage ? damage.getDamage() : 0.0D;
    }
}

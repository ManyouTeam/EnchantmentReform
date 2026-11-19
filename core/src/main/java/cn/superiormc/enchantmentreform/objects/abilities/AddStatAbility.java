package cn.superiormc.enchantmentreform.objects.abilities;

import cn.superiormc.enchantmentreform.objects.effects.ObjectAuraSkillsEffect;
import cn.superiormc.enchantmentreform.objects.effects.ObjectMMEffect;
import cn.superiormc.enchantmentreform.objects.effects.ObjectMMOEffect;
import cn.superiormc.enchantmentreform.objects.effects.AbstractEffect;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class AddStatAbility extends AbstractAbility {

    private final String provider;
    private final Map<Key, AbstractEffect> active = new HashMap<>();

    public AddStatAbility(ConfigurationSection section) {
        super("AddStat", section);
        provider = section.getString("provider", "MythicLib");
    }

    @Override
    protected synchronized boolean matchesContext(PowerContext context) {
        if (!Bukkit.getPluginManager().isPluginEnabled(provider) || !(getTargetEntity(context) instanceof Player player)) {
            return false;
        }
        if (super.matchesContext(context)) {
            return true;
        }
        Key key = key(context, player);
        AbstractEffect existing = active.get(key);
        if (existing != null) {
            existing.deactivate();
            active.remove(key);
        }
        return false;
    }

    @Override
    public synchronized boolean execute(PowerContext context) {
        if (!Bukkit.getPluginManager().isPluginEnabled(provider) || !(getTargetEntity(context) instanceof Player player)) {
            return false;
        }
        Key key = key(context, player);
        double value = getDouble("value", 0.0D, context);
        AbstractEffect previous = active.get(key);
        if (previous != null && previous.getValue() == value && !previous.canRetry()) {
            return false;
        }
        if (previous != null) {
            previous.deactivate();
            active.remove(key);
        }
        String identity = key + ":" + section.getCurrentPath();
        String id = "EnchantmentReform_" + UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
        if (section.getString("stat", "").isBlank()) {
            return false;
        }
        AbstractEffect effect = switch (provider) {
            case "MythicLib" -> new ObjectMMOEffect(id, player, section, value);
            case "MythicMobs" -> new ObjectMMEffect(id, player, section, value);
            case "AuraSkills" -> new ObjectAuraSkillsEffect(id, player, section, value);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
        effect.activate();
        active.put(key, effect);
        return false;
    }

    private Key key(PowerContext context, Player player) {
        UUID owner = context.player() == null ? player.getUniqueId() : context.player().getUniqueId();
        String power = context.power() == null ? "" : context.power().getId();
        return new Key(owner, player.getUniqueId(), power, context.contextSlot());
    }

    @Override
    public void onPowerSourceDeactivate(Player player, ActivePowerSource source) {
        if (source.power() == null) {
            return;
        }
        clear(key -> key.owner.equals(player.getUniqueId()) && key.power.equals(source.power().getId())
                && key.slot == source.slot());
    }

    @Override
    public void onEntityUnload(UUID entityId) {
        clear(key -> key.owner.equals(entityId) || key.target.equals(entityId));
    }

    @Override
    public void onUnload() {
        clear(key -> true);
    }

    private synchronized void clear(Predicate<Key> matches) {
        active.entrySet().removeIf(entry -> {
            if (!matches.test(entry.getKey())) {
                return false;
            }
            entry.getValue().deactivate();
            return true;
        });
    }

    @Override
    public TargetEntityType getDefaultTargetEntityType() {
        return TargetEntityType.SOURCE;
    }

    private record Key(UUID owner, UUID target, String power, EquipmentSlot slot) {}

}

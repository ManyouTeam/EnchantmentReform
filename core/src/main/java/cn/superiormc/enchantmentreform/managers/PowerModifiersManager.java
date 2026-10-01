package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.api.registry.ModifierFactory;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.powermodifiers.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PowerModifiersManager extends AbstractManager {

    public static PowerModifiersManager powerModifiers;

    private final KeyedRegistry<String, ModifierFactory> modifiers = KeyedRegistry.stringTypes();

    private final Map<Class<? extends AbstractPowerModifier>, AbstractPowerModifier> lifecycleTypes =
            new ConcurrentHashMap<>();

    public PowerModifiersManager() {
        powerModifiers = this;
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        register("cooldown_time", PowerModifierCooldownTime::new);
        register("damage", PowerModifierDamage::new);
        register("heal", PowerModifierHeal::new);
        register("duration", PowerModifierDuration::new);
        register("yield", PowerModifierYield::new);
        register("radius", PowerModifierRadius::new);
        register("item_damage", PowerModifierItemDamage::new);
        register("food", PowerModifierFood::new);
        register("elytra_boost_not_consume", PowerModifierElytraBoostNotConsume::new);
        register("exhaustion", PowerModifierExhaustion::new);
        register("experience", PowerModifierExperience::new);
        register("lunge_power", PowerModifierLungePower::new);
        register("stack_damage_modifier", PowerModifierStackDamageModifier::new);
        register("armor_pierce", PowerModifierArmorPierce::new);
        register("missing_health_damage", PowerModifierMissingHealthDamage::new);
        register("air", PowerModifierAir::new);
        register("modify_drops", PowerModifierModifyDrops::new);
        register("revive", PowerModifierRevive::new);
        register("modify_projectile", PowerModifierModifyProjectile::new);
        register("replace_projectile", PowerModifierReplaceProjectile::new);
        register("trade_uses_return", PowerModifierTradeUsesReturn::new);
        register("trade_emerald_return", PowerModifierTradeEmeraldReturn::new);
        register("fishing_catch", PowerModifierFishCatch::new);
        register("fishing_extra_catch", PowerModifierFishExtraCatch::new);
        register("fishing_replace_catch", PowerModifierFishReplaceCatch::new);
        register("fishing_smelt_catch", PowerModifierFishSmeltCatch::new);
        register("fishing_hook_timing", PowerModifierFishHookTiming::new);
        register("warden_anger", PowerModifierWardenAnger::new);
        register("vibration_reduce", PowerModifierVibrationReduce::new);
    }

    public void register(String type, ModifierFactory factory) {
        modifiers.register(type, factory);
    }

    public void onUnload() {
        List<AbstractPowerModifier> types = List.copyOf(lifecycleTypes.values());
        lifecycleTypes.clear();
        types.forEach(AbstractPowerModifier::onUnload);
    }

    public void onEntityUnload(UUID entityId) {
        if (entityId != null) {
            lifecycleTypes.values().forEach(type -> type.onEntityUnload(entityId));
        }
    }

    public void apply(ConfigurationSection sections, PowerContext context) {
        if (sections == null) {
            return;
        }
        for (String key : sections.getKeys(false)) {
            ConfigurationSection section = sections.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            ModifierFactory factory = modifiers.getRequired(section.getString("type", ""));
            AbstractPowerModifier modifier = factory.create(section);
            lifecycleTypes.putIfAbsent(modifier.getClass(), modifier);
            modifier.apply(context);
        }
    }
}

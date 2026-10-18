package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.api.registry.ModifierFactory;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.powermodifiers.AbstractPowerModifier;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierAir;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierArmorPierce;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierCooldownTime;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierDamage;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierDuration;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierElytraBoostNotConsume;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierExhaustion;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierExperience;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierFishCatch;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierFishExtraCatch;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierFishHookTiming;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierFishReplaceCatch;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierFishSmeltCatch;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierFood;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierHeal;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierItemDamage;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierLungePower;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierMissingHealthDamage;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierModifyDrops;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierModifyProjectile;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierRadius;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierReplaceProjectile;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierRevive;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierStackDamageModifier;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierTradeEmeraldReturn;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierTradeUsesReturn;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierVibrationReduce;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierWardenAnger;
import cn.superiormc.enchantmentreform.objects.powermodifiers.PowerModifierYield;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PowerModifiersManager extends AbstractManager {

    public static PowerModifiersManager powerModifiers;

    private final KeyedRegistry<String, ModifierFactory> modifiers = KeyedRegistry.stringTypes();

    private final Map<ConfigurationSection, AbstractPowerModifier> modifierInstances =
            new ConcurrentHashMap<>();

    private final Set<AbstractPowerModifier> lifecycleInstances = ConcurrentHashMap.newKeySet();

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
        List<AbstractPowerModifier> instances = List.copyOf(lifecycleInstances);
        modifierInstances.clear();
        lifecycleInstances.clear();
        instances.forEach(AbstractPowerModifier::onUnload);
    }

    public void onEntityUnload(UUID entityId) {
        if (entityId != null) {
            lifecycleInstances.forEach(instance -> instance.onEntityUnload(entityId));
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
            getModifier(section).apply(context);
        }
    }

    private AbstractPowerModifier getModifier(ConfigurationSection section) {
        return modifierInstances.computeIfAbsent(section, current -> {
            ModifierFactory factory = modifiers.getRequired(current.getString("type", ""));
            AbstractPowerModifier created = factory.create(current);
            lifecycleInstances.add(created);
            return created;
        });
    }
}

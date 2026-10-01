package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.objects.ObjectSingleCondition;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.objects.powerconditions.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PowerConditionsManager extends AbstractManager {

    public static PowerConditionsManager powerConditions;

    private final KeyedRegistry<String, AbstractPowerCondition> conditions = KeyedRegistry.stringTypes();

    private final ConditionManager<ObjectSingleCondition, PowerContext> playerConditions = new ConditionManager<>();

    public PowerConditionsManager() {
        powerConditions = this;
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        register(new PowerConditionAny());
        register(new PowerConditionNot());
        register(new PowerConditionMatchEntity());
        register(new PowerConditionMatchItem());
        register(new PowerConditionItemDamage());
        register(new PowerConditionCatchMatchItem());
        register(new PowerConditionPlayerConditions());
        register(new PowerConditionSneaking());
        register(new PowerConditionNotSneaking());
        register(new PowerConditionMinAttackCooldown());
        register(new PowerConditionBlocking());
        register(new PowerConditionPerfectGuard());
        register(new PowerConditionInWater());
        register(new PowerConditionInLava());
        register(new PowerConditionOnGround());
        register(new PowerConditionInAir());
        register(new PowerConditionFalling());
        register(new PowerConditionFallDistance());
        register(new PowerConditionSprinting());
        register(new PowerConditionSwimming());
        register(new PowerConditionFlying());
        register(new PowerConditionClimbing());
        register(new PowerConditionRiding());
        register(new PowerConditionGameMode());
        register(new PowerConditionHasPotion());
        register(new PowerConditionPotionEffectType());
        register(new PowerConditionPotionEffectType("effect_type"));
        register(new PowerConditionPotionEffectCause());
        register(new PowerConditionPotionEffectAction());
        register(new PowerConditionInSunlight());
        register(new PowerConditionNight());
        register(new PowerConditionStorm());
        register(new PowerConditionThunder());
        register(new PowerConditionClearWeather());
        register(new PowerConditionInRain());
        register(new PowerConditionInteractionAction());
        register(new PowerConditionInputType());
        register(new PowerConditionEventState());
        register(new PowerConditionSmashAttackLands());
        register(new PowerConditionWorld());
        register(new PowerConditionBlockType());
        register(new PowerConditionBlockTypeOffset());
        register(new PowerConditionInStructure());
        register(new PowerConditionBestTool());
        register(new PowerConditionBestTool("preferred_tool"));
        register(new PowerConditionBiomeChanged());
        register(new PowerConditionLiquidTransition());
        register(new PowerConditionBlockBreakTime());
        register(new PowerConditionBlockBreakTime("break_time"));
        register(new PowerConditionAgeable());
        register(new PowerConditionHeight());
        register(new PowerConditionDamageCause());
        register(new PowerConditionHealthPercent());
        register(new PowerConditionRandom());
        register(new PowerConditionFirstAttackFromMonster());
        register(new PowerConditionFirstAttackFromMonster("first_monster_attack"));
        register(new PowerConditionTargetCategory());
        register(new PowerConditionFoodChange());
        register(new PowerConditionAirChange());
        register(new PowerConditionStateValue());
        register(new PowerConditionLiquidSurfaceDistance());
        register(new PowerConditionHeadshot());
        register(new PowerConditionFatalDamage());
        register(new PowerConditionDistance());
        register(new PowerConditionBoundingBoxDistance());
        register(new PowerConditionEnvironment());
        register(new PowerConditionGliding());
        register(new PowerConditionLightLevel());
        register(new PowerConditionStationary());
        register(new PowerConditionHealth());
        register(new PowerConditionFoodLevel());
        register(new PowerConditionAttributeValue());
        register(new PowerConditionSkillLevel());
        register(new PowerConditionDamageValue());
        register(new PowerConditionFirstAttackAgainstEntity());
        register(new PowerConditionCombustDuration());
        register(new PowerConditionRegainAmount());
        register(new PowerConditionExplosionYield());
        register(new PowerConditionExplosionRadius());
        register(new PowerConditionDamageOrigin());
        register(new PowerConditionCombustOrigin());
        register(new PowerConditionTargetReason());
        register(new PowerConditionTargetCount());
        register(new PowerConditionSpawnReason());
        register(new PowerConditionKnockbackCause());
        register(new PowerConditionKnockbackCause("knockback_reason"));
        register(new PowerConditionExhaustionReason());
        register(new PowerConditionVillagerTrade());
        register(new PowerConditionCooldown());
        register(new PowerConditionCooldownMaterial());
    }

    public void register(AbstractPowerCondition condition) {
        conditions.register(condition.getType(), condition);
    }

    public void onUnload() {
        conditions.values().forEach(AbstractPowerCondition::onUnload);
    }

    public void onEntityUnload(UUID entityId) {
        if (entityId != null) {
            conditions.values().forEach(condition -> condition.onEntityUnload(entityId));
        }
    }

    public ConditionManager<ObjectSingleCondition, PowerContext> getPlayerConditions() {
        return playerConditions;
    }

    public boolean matches(ConfigurationSection section, PowerContext context) {
        return matches(section, context, null);
    }

    public boolean matches(ConfigurationSection section, PowerContext context, Player player) {
        PowerContext playerContext = context == null ? null : context.withPlayer(resolvePlayer(context, player));
        if (section == null) {
            return true;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection single = section.getConfigurationSection(key);
            if (single == null || !single.contains("type") || !matchesSingle(single, playerContext)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesAny(ConfigurationSection section, PowerContext context, Player player) {
        PowerContext playerContext = context == null ? null : context.withPlayer(resolvePlayer(context, player));
        if (section == null) {
            return true;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection single = section.getConfigurationSection(key);
            if (single != null && single.contains("type") && matchesSingle(single, playerContext)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesSingle(ConfigurationSection section, PowerContext context) {
        AbstractPowerCondition condition = conditions.getRequired(section.getString("type", ""));
        return condition.matches(new ObjectSingleCondition(section, context));
    }

    private Player resolvePlayer(PowerContext context, Player explicitPlayer) {
        if (explicitPlayer != null) {
            return explicitPlayer;
        }
        if (context != null && context.player() != null) {
            return context.player();
        }
        Entity source = context == null ? null : context.source();
        return source instanceof Player sourcePlayer ? sourcePlayer : null;
    }
}

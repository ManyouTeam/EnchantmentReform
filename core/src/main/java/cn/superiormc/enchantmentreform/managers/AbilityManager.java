package cn.superiormc.enchantmentreform.managers;

import cn.superiormc.enchantmentreform.api.registry.AbilityFactory;
import cn.superiormc.enchantmentreform.api.registry.KeyedRegistry;
import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.nms.NmsBridge;
import cn.superiormc.enchantmentreform.nms.UnsupportedNmsBridge;
import cn.superiormc.enchantmentreform.objects.ObjectAction;
import cn.superiormc.enchantmentreform.objects.ObjectSingleAction;
import cn.superiormc.enchantmentreform.objects.abilities.*;
import cn.superiormc.enchantmentreform.power.ActivePowerSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityManager extends AbstractManager {

    public static AbilityManager abilityManager;

    private final KeyedRegistry<String, AbilityFactory> abilities = KeyedRegistry.stringTypes();

    private final Map<Class<? extends AbstractAbility>, AbstractAbility> lifecycleTypes = new ConcurrentHashMap<>();

    private final ActionManager<ObjectSingleAction, PowerContext> actionManager;

    private final NmsBridge nmsBridge;

    public AbilityManager() {
        this(new UnsupportedNmsBridge("AbilityManager was created without an NMS bridge"));
    }

    public AbilityManager(NmsBridge nmsBridge) {
        abilityManager = this;
        this.nmsBridge = nmsBridge;
        actionManager = createActionManager();
        registerBuiltIns();
    }

    private void registerBuiltIns() {
        register("mark", MarkAbility::new);
        register("delay", DelayAbility::new);
        register("place_block", PlaceBlockAbility::new);
        register("place_temp_block", PlaceTempBlockAbility::new);
        register("prevent_block_break", PreventBlockBreakAbility::new);
        register("explosion", ExplosionAbility::new);
        register("remove", RemoveAbility::new);
        register("cancel_event", CancelEventAbility::new);;
        register("disable_enchantments", DisableEnchantmentsAbility::new);
        register("modify_repair_cost", ModifyRepairCostAbility::new);
        register("lightning", LightningAbility::new);
        register("particle", ParticleAbility::new);
        register("sound", SoundAbility::new);
        register("set_attribute", SetAttributeAbility::new);
        register("refresh_attribute", RefreshAttributeAbility::new);
        register("set_health", SetHealthAbility::new);
        register("set_absorption", SetAbsorptionAbility::new);
        register("attribute_layer", AttributeLayerAbility::new);
        register("set_air", SetAirAbility::new);
        register("consume_food", ConsumeFoodAbility::new);
        register("damage_item", DamageItemAbility::new);
        register("damage_entity", DamageEntityAbility::new);
        register("set_invulnerable", SetInvulnerableAbility::new);
        register("pull_target", PullTargetAbility::new);
        register("pull_location", PullLocationAbility::new);
        register("potion_cloud", PotionCloudAbility::new);
        register("potion_effect", PotionEffectAbility::new);
        register("remove_potion_effect", RemovePotionEffectAbility::new);
        register("extend_potion_effects", ExtendPotionEffectsAbility::new);
        register("freeze", FreezeAbility::new);
        register("fire", FireAbility::new);
        register("homing_projectile", HomingProjectileAbility::new);
        register("teleport_near_target", TeleportNearTargetAbility::new);
        register("guardian_beam", GuardianBeamAbility::new);
        register("sonic_boom", SonicBoomAbility::new);
        register("evoker_fangs", EvokerFangsAbility::new);
        register("shulker_bullet", LaunchProjectileAbility::new);
        register("vanilla_animation", VanillaAnimationAbility::new);
        register("arrow_rain", ArrowRainAbility::new);
        register("enhance_equipment", EnhanceEquipmentAbility::new);
        register("enhance_helditem", EnhanceHeldItemAbility::new);
        register("replace_item", ReplaceItemAbility::new);
        register("shuffle_inventory", ShuffleInventoryAbility::new);
        register("launch_projectile", LaunchProjectileAbility::new);
        register("send_message", SendMessageAbility::new);
        register("execute_command", ExecuteCommandAbility::new);
        register("execute_action", ExecuteActionAbility::new);
        register("conditional", ConditionalAbility::new);
        register("any_of", AnyOfAbility::new);
        register("cost_price", CostPriceAbility::new);
        register("limit", LimitAbility::new);
        register("repeat", RepeatAbility::new);
        register("nearby_entities", NearbyEntitiesAbility::new);
        register("nearby_block", NearbyBlockAbility::new);
        register("mythic_skill", MythicSkillAbility::new);
        register("summon", SummonAbility::new);
        register("creeper_stats", CreeperStatsAbility::new);
        register("break_blocks", BreakBlocksAbility::new);
        register("drop_item", DropItemAbility::new);
        register("give_item", GiveItemAbility::new);
        register("give_loot_table_item", GiveLootTableItemAbility::new);
        register("set_velocity", SetVelocityAbility::new);
        register("reflect_projectile", ReflectProjectileAbility::new);
        register("ricochet_projectile", RicochetProjectileAbility::new);
        register("change_item", ChangeItemAbility::new);
        register("experience", ExperienceAbility::new);
        register("skill_experience", SkillExperienceAbility::new);
        register("auto_fishing", section -> new AutoFishingAbility(section, nmsBridge));
        register("locate_structure", LocateStructureAbility::new);
        register("locate_biome", LocateBiomeAbility::new);
        register("preserve_inventory", PreserveInventoryAbility::new);
        register("set_food", SetFoodAbility::new);
        register("set_item_cooldown", SetItemCooldownAbility::new);
        register("state", StateAbility::new);
        register("auto_feed", section -> new AutoFeedAbility(section, nmsBridge));
        register("use_on", section -> new UseOnAbility(section, nmsBridge));
        register("preserve_item", PreserveItemAbility::new);
        register("grow_crop", GrowCropAbility::new);
        register("accelerate_crops", AccelerateCropsAbility::new);
        register("accelerate_work_blocks", AccelerateWorkBlocksAbility::new);
        register("break_block", BreakBlockAbility::new);
        register("replace_block", ReplaceBlockAbility::new);
        register("change_block_face", ChangeBlockFaceAbility::new);
        register("scan_blocks", ScanBlocksAbility::new);
        register("swap_health", SwapHealthAbility::new);
        register("swap_potion_effects", SwapPotionEffectsAbility::new);
        register("swap_locations", SwapLocationsAbility::new);
        register("preserve_experience", PreserveExperienceAbility::new);
        register("teleport", TeleportAbility::new);
    }

    public void register(String type, AbilityFactory factory) {
        abilities.register(type, factory);
    }

    public void onUnload() {
        List<AbstractAbility> types = List.copyOf(lifecycleTypes.values());
        lifecycleTypes.clear();
        types.forEach(AbstractAbility::onUnload);
    }

    public void onEntityUnload(UUID entityId) {
        if (entityId != null) {
            lifecycleTypes.values().forEach(type -> type.onEntityUnload(entityId));
        }
    }

    public void onPowerSourceDeactivate(Player player, ActivePowerSource source) {
        if (player != null && source != null) {
            lifecycleTypes.values().forEach(type ->
                    type.onPowerSourceDeactivate(player, source));
        }
    }

    public void executeActions(ConfigurationSection section, PowerContext context) {
        if (section == null || context == null || context.player() == null) {
            return;
        }
        createActions(section).runAllActions(context.player(), context);
    }

    public ActionManager<ObjectSingleAction, PowerContext> getActionManager() {
        return actionManager;
    }

    private ActionManager<ObjectSingleAction, PowerContext> createActionManager() {
        return new ActionManager<>(new ActionManager.BuiltInActionOptions<>(
                (section, parent, player, context) -> createActions(section).runAllActions(player, context),
                (section, parent, player, context, amount) ->
                        createActions(section).runRandomEveryActions(player, context, amount),
                (section, parent, context) ->
                        player -> createActions(section).runAllActions(player, context),
                (section, parent, player, context) ->
                        PowerConditionsManager.powerConditions.matches(section, context, player),
                action -> true,
                action -> true,
                false,
                true));
    }

    private ObjectAction<ObjectSingleAction, PowerContext> createActions(ConfigurationSection section) {
        return new ObjectAction<>(section, ObjectSingleAction::new, actionManager::doAction);
    }

    public boolean execute(ConfigurationSection eventSection, PowerContext context) {
        if (eventSection == null) {
            return false;
        }
        boolean cancel = false;
        for (AbstractAbility action : parseActions(eventSection)) {
            if (executeAbility(action, context)) {
                cancel = true;
            }
        }
        return cancel;
    }

    public boolean executeSingle(ConfigurationSection abilitySection, PowerContext context) {
        if (abilitySection == null) {
            return false;
        }
        return executeAbility(toAction(abilitySection), context);
    }

    private boolean executeAbility(AbstractAbility ability, PowerContext context) {
        if (ability == null || shouldSkipInternalBlockBreak(ability, context)
                || !ability.shouldExecute(context)) {
            return false;
        }
        return ability.execute(context);
    }

    private boolean shouldSkipInternalBlockBreak(AbstractAbility ability, PowerContext context) {
        return ability.breakOtherBlock()
                && context != null
                && context.triggerData() != null
                && context.triggerData().extra(BuiltinContextKeys.INTERNAL_BLOCK_BREAK).orElse(false);
    }

    public List<AbstractAbility> parseActions(ConfigurationSection eventSection) {
        List<AbstractAbility> actions = new ArrayList<>();
        if (eventSection == null) {
            return actions;
        }

        eventSection.getKeys(false).forEach(key -> {
            ConfigurationSection single = eventSection.getConfigurationSection(key);
            if (single == null) {
                return;
            }
            AbstractAbility action = toAction(single);
            if (action != null) {
                actions.add(action);
            }
        });
        return actions;
    }

    private AbstractAbility toAction(ConfigurationSection section) {
        String type = section.getString("type", "");
        AbstractAbility ability = abilities.getRequired(type).create(section);
        lifecycleTypes.putIfAbsent(ability.getClass(), ability);
        return ability;
    }
}

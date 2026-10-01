package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.api.trigger.BuiltinContextKeys;
import cn.superiormc.enchantmentreform.api.trigger.ContextKey;
import cn.superiormc.enchantmentreform.managers.AttributeManager;
import cn.superiormc.enchantmentreform.objects.abilities.PowerContext;
import cn.superiormc.enchantmentreform.utils.CommonUtil;
import cn.superiormc.enchantmentreform.utils.MathUtil;
import cn.superiormc.enchantmentreform.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractConfiguredSection<C> {

    private static final Pattern VARIABLE_REFERENCE = Pattern.compile("\\{([^{}]+)}");

    private static final Pattern CUSTOM_ATTRIBUTE_REFERENCE = Pattern.compile(
            "\\{(?:attribute|custom_attribute):([a-zA-Z0-9_-]+)(?::([a-zA-Z0-9_-]+))?}");

    public final ConfigurationSection section;

    protected final String id;

    protected AbstractConfiguredSection(ConfigurationSection section) {
        this(null, section);
    }

    protected AbstractConfiguredSection(String id, ConfigurationSection section) {
        this.id = id;
        this.section = section;
    }

    public ConfigurationSection getSection() {
        return section;
    }

    public ConfigurationSection getSection(String path) {
        return section.getConfigurationSection(path);
    }

    public boolean contains(String path) {
        return section.contains(path);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return section.getBoolean(path, defaultValue);
    }

    public String getString(String path) {
        return resolveString(rawValue(path, 1), "", null, null, 1);
    }

    public String getString(String path, String defaultValue) {
        return resolveString(rawValue(path, 1), defaultValue, null, null, 1);
    }

    public String getString(String path, Player player, C context) {
        return resolveString(rawValue(path, levelOf(context)), section.getString(path), player, context,
                levelOf(context));
    }

    public String getString(String path, String defaultValue, PowerContext context, String... args) {
        return resolveString(rawValue(path, context == null ? 1 : context.level()), defaultValue,
                context == null ? null : context.player(), castContext(context),
                context == null ? 1 : context.level(), args);
    }

    public List<String> getStringList(String path) {
        return getStringList(path, null, null);
    }

    public List<String> getStringList(String path, Player player, C context) {
        List<String> rawValues = section.getStringList(path);
        List<String> result = new ArrayList<>(rawValues.size());
        for (String value : rawValues) {
            result.add(resolveString(value, "", player, context, levelOf(context)));
        }
        return result;
    }

    public int getInt(String path) {
        return evaluateInt(resolveString(rawValue(path, 1), "0", null, null, 1));
    }

    public int getInt(String path, int defaultValue) {
        return evaluateInt(resolveString(rawValue(path, 1), String.valueOf(defaultValue), null, null, 1));
    }

    public int getInt(String path, int defaultValue, int level) {
        return evaluateInt(resolveString(rawValue(path, level), String.valueOf(defaultValue), null, null, level));
    }

    public int getInt(String path, int defaultValue, PowerContext context, String... args) {
        int level = context == null ? 1 : context.level();
        return evaluateInt(resolveString(rawValue(path, level), String.valueOf(defaultValue),
                context == null ? null : context.player(), castContext(context), level, args));
    }

    public double getDouble(String path) {
        return evaluate(resolveString(rawValue(path, 1), "0", null, null, 1));
    }

    public double getDouble(String path, Player player, C context) {
        return evaluate(resolveString(rawValue(path, levelOf(context)), section.getString(path), player, context,
                levelOf(context)));
    }

    public double getDouble(String path, double defaultValue, int level, String... args) {
        return evaluate(resolveString(rawValue(path, level), String.valueOf(defaultValue), null, null, level, args));
    }

    public double getDouble(String path, double defaultValue, PowerContext context, String... args) {
        int level = context == null ? 1 : context.level();
        return evaluate(resolveString(rawValue(path, level), String.valueOf(defaultValue),
                context == null ? null : context.player(), castContext(context), level, args));
    }

    public String getId() {
        return id;
    }

    /** Allows specialised contexts to add placeholders while retaining the common pipeline. */
    protected String replacePlaceholder(String content, Player player, C context) {
        if (context instanceof PowerContext powerContext && powerContext.triggerData() != null) {
            Entity source = powerContext.source();
            Entity target = powerContext.target();
            content = replaceEntityValues(content, "source", source);
            content = replaceEntityValues(content, "target", target);
            if (source != null && target != null && source.getWorld().equals(target.getWorld())) {
                content = content.replace("{distance}", String.valueOf(source.getLocation().distance(target.getLocation())));
            }
            if (powerContext.result() != null) {
                content = content.replace("{last_changed_blocks}",
                        String.valueOf(powerContext.result().lastChangedBlocks()));
                content = content.replace("{changed_blocks}",
                        String.valueOf(powerContext.result().changedBlocks()));
            }
        }
        return content;
    }

    /** Resolves an already-selected literal through the same pipeline used by configured fields. */
    protected final String resolveText(String value, Player player, C context, int level, String... args) {
        return resolveString(value, "", player, context, level, args);
    }

    private String replaceEntityValues(String content, String prefix, Entity entity) {
        if (entity == null) {
            return content;
        }
        content = content.replace("{" + prefix + "_horizontal_speed}",
                String.valueOf(Math.sqrt(entity.getVelocity().getX() * entity.getVelocity().getX()
                        + entity.getVelocity().getZ() * entity.getVelocity().getZ())));
        content = content.replace("{" + prefix + "_fall_distance}", String.valueOf(entity.getFallDistance()));
        if (entity instanceof LivingEntity living) {
            double maximum = CommonUtil.getMaxHealth(living);
            double percent = maximum <= 0.0D ? 0.0D : living.getHealth() / maximum * 100.0D;
            content = content.replace("{" + prefix + "_health}", String.valueOf(living.getHealth()));
            content = content.replace("{" + prefix + "_max_health}", String.valueOf(maximum));
            content = content.replace("{" + prefix + "_health_percent}", String.valueOf(percent));
        }
        return content;
    }

    private Object rawValue(String path, int level) {
        return LevelValueResolver.resolve(section.get(path), level);
    }

    private String resolveString(Object raw, String defaultValue, Player player, C context, int level, String... args) {
        String value = raw == null ? defaultValue : String.valueOf(raw);
        if (value == null) {
            value = "";
        }
        value = CommonUtil.modifyString(player, value, "level", String.valueOf(level));
        value = CommonUtil.modifyString(player, value, args);
        value = resolvePowerVariables(value, context, level);

        // Power variables may themselves contain {level} or call-specific arguments. Resolve these
        // again after expansion so nested variable formulas behave exactly like inline formulas.
        value = CommonUtil.modifyString(player, value, "level", String.valueOf(level));
        value = CommonUtil.modifyString(player, value, args);

        value = resolveNumericContextVariables(value, context);
        value = resolveCustomAttributeVariables(value, player);
        value = replacePlaceholder(value, player, context);
        return TextUtil.withPAPI(value, player);
    }

    private String resolvePowerVariables(String value, C context, int level) {
        if (value.indexOf('{') < 0) {
            return value;
        }
        Matcher matcher = VARIABLE_REFERENCE.matcher(value);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String replacement = resolvePowerVariable(matcher.group(1), context, level);
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    protected String resolvePowerVariable(String name, C context, int level) {
        if (!(context instanceof PowerContext powerContext) || powerContext.power() == null) {
            return null;
        }
        return powerContext.power().getVariable(name, level);
    }

    private String resolveNumericContextVariables(String value, C context) {
        if (!(context instanceof PowerContext powerContext)
                || powerContext.triggerData() == null
                || value.indexOf('{') < 0) {
            return value;
        }

        Matcher matcher = VARIABLE_REFERENCE.matcher(value);
        StringBuilder resolved = null;
        while (matcher.find()) {
            ContextKey<? extends Number> key = BuiltinContextKeys.numericKey(matcher.group(1));
            if (key == null) {
                continue;
            }
            Number replacement = powerContext.triggerData().extra(key).orElse(null);
            if (replacement == null) {
                continue;
            }
            if (resolved == null) {
                resolved = new StringBuilder(value.length() + 16);
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement.toString()));
        }
        if (resolved == null) {
            return value;
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private String resolveCustomAttributeVariables(String value, Player player) {
        if (player == null || AttributeManager.attributeManager == null
                || value.indexOf('{') < 0) {
            return value;
        }
        Matcher matcher = CUSTOM_ATTRIBUTE_REFERENCE.matcher(value);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            var attribute = AttributeManager.attributeManager.resolveAttribute(matcher.group(1));
            if (attribute == null) {
                continue;
            }
            int level = attribute.getValue(player);
            String variable = matcher.group(2);
            String replacement = variable == null
                    ? String.valueOf(level) : attribute.getVariable(variable, level);
            if (replacement == null) {
                continue;
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(
                    replacement.replace("{level}", String.valueOf(level))));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private double evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            return 0.0D;
        }
        String value = expression.trim();
        int separator = value.indexOf('~');
        if (separator > 0 && separator < value.length() - 1) {
            try {
                double lower = Double.parseDouble(value.substring(0, separator).trim());
                double upper = Double.parseDouble(value.substring(separator + 1).trim());
                return ThreadLocalRandom.current().nextDouble(Math.min(lower, upper), Math.max(lower, upper) + Math.ulp(Math.max(lower, upper)));
            } catch (NumberFormatException ignored) {
                // A formula may legitimately contain an unsupported '~'; let the formula parser handle it.
            }
        }
        return MathUtil.doCalculate(value);
    }

    private int evaluateInt(String expression) {
        if (expression == null || expression.isBlank()) {
            return 0;
        }
        String value = expression.trim();
        int separator = value.indexOf('~');
        if (separator > 0 && separator < value.length() - 1) {
            try {
                int first = Integer.parseInt(value.substring(0, separator).trim());
                int second = Integer.parseInt(value.substring(separator + 1).trim());
                int lower = Math.min(first, second);
                int upper = Math.max(first, second);
                long range = (long) upper - lower + 1L;
                return (int) (lower + ThreadLocalRandom.current().nextLong(range));
            } catch (NumberFormatException ignored) {
                // Non-integer ranges retain getDouble's continuous-range semantics before truncation.
            }
        }
        return (int) evaluate(value);
    }

    private int levelOf(C context) {
        return context instanceof PowerContext powerContext ? powerContext.level() : 1;
    }

    @SuppressWarnings("unchecked")
    private C castContext(PowerContext context) {
        return (C) context;
    }
}

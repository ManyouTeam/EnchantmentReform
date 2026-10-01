package cn.superiormc.enchantmentreform.objects.matchentity;

import cn.superiormc.enchantmentreform.objects.ObjectSingleMatchEntity;

import cn.superiormc.enchantmentreform.utils.CommonUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EntityPDC extends AbstractMatchEntityRule {

    public EntityPDC() {
        super();
    }

    @Override
    public boolean getMatch(ObjectSingleMatchEntity match) {
        ConfigurationSection section = match.getSection();
        LivingEntity entity = match.getEntity();
        ConfigurationSection pdcSection = section.getConfigurationSection("entity-pdc");
        if (pdcSection == null) {
            return false;
        }

        PersistentDataContainer pdc = entity.getPersistentDataContainer();

        for (String key : pdcSection.getKeys(false)) {
            Object expectedValue = pdcSection.get(key);
            if (expectedValue == null) continue;

            NamespacedKey namespacedKey = CommonUtil.parseNamespacedKey(key);
            if (namespacedKey == null) continue;

            // 先尝试字符串匹配
            String stringValue = pdc.get(namespacedKey, PersistentDataType.STRING);
            if (stringValue != null) {
                if (matchString(stringValue, expectedValue.toString())) {
                    return true;
                }
                continue;
            }

            // 尝试数字类型匹配
            Number number = getNumericValue(pdc, namespacedKey);
            if (number != null && matchNumber(number.doubleValue(), expectedValue.toString())) {
                return true;
            }

            // 尝试布尔
            Byte boolValue = pdc.get(namespacedKey, PersistentDataType.BYTE);
            if (boolValue != null) {
                boolean value = boolValue != 0;
                if (String.valueOf(value).equalsIgnoreCase(expectedValue.toString())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean configNotContains(ConfigurationSection section) {
        ConfigurationSection pdcSection = section.getConfigurationSection("entity-pdc");
        return pdcSection == null || pdcSection.getKeys(false).isEmpty();
    }

    // ======================
    // 🔹 工具方法
    // ======================

    private Number getNumericValue(PersistentDataContainer pdc, NamespacedKey key) {
        if (pdc.has(key, PersistentDataType.INTEGER)) {
            return pdc.get(key, PersistentDataType.INTEGER);
        } else if (pdc.has(key, PersistentDataType.DOUBLE)) {
            return pdc.get(key, PersistentDataType.DOUBLE);
        } else if (pdc.has(key, PersistentDataType.LONG)) {
            return pdc.get(key, PersistentDataType.LONG);
        } else if (pdc.has(key, PersistentDataType.FLOAT)) {
            return pdc.get(key, PersistentDataType.FLOAT);
        }
        return null;
    }

    private boolean matchString(String actual, String expected) {
        expected = expected.trim();
        // 支持通配符
        if (expected.contains("*")) {
            String regex = expected.replace("*", ".*");
            return actual.matches("(?i)" + regex);
        }
        // 模糊包含匹配
        return actual.equalsIgnoreCase(expected) || actual.toLowerCase().contains(expected.toLowerCase());
    }

    private boolean matchNumber(double actual, String rule) {
        rule = rule.trim();

        try {
            if (rule.contains("~")) {
                // 区间匹配：5~10
                String[] parts = rule.split("~");
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);
                return actual >= min && actual <= max;
            } else if (rule.startsWith(">=")) {
                double min = Double.parseDouble(rule.substring(2));
                return actual >= min;
            } else if (rule.startsWith("<=")) {
                double max = Double.parseDouble(rule.substring(2));
                return actual <= max;
            } else if (rule.startsWith(">")) {
                double min = Double.parseDouble(rule.substring(1));
                return actual > min;
            } else if (rule.startsWith("<")) {
                double max = Double.parseDouble(rule.substring(1));
                return actual < max;
            } else if (rule.startsWith("=")) {
                double val = Double.parseDouble(rule.substring(1));
                return Math.abs(actual - val) < 0.0001;
            } else {
                // 纯数字直接比较
                double val = Double.parseDouble(rule);
                return Math.abs(actual - val) < 0.0001;
            }
        } catch (Exception ignored) {
            return false;
        }
    }
}
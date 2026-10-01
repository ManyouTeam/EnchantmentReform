package cn.superiormc.enchantmentreform.objects;

public record EnchantmentKey(String namespace, String value) {

    public static EnchantmentKey parse(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(java.util.Locale.ROOT);
        int separator = normalized.indexOf(':');
        String namespace = separator < 0 ? "enchantmentreform" : normalized.substring(0, separator);
        String value = separator < 0 ? normalized : normalized.substring(separator + 1);
        if (!namespace.matches("[a-z0-9._-]+") || !value.matches("[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("Invalid enchantment key: " + raw);
        }
        return new EnchantmentKey(namespace, value);
    }

    public String asString() {
        return namespace + ":" + value;
    }
}

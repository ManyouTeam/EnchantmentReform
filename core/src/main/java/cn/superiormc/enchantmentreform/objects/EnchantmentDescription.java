package cn.superiormc.enchantmentreform.objects;

import cn.superiormc.enchantmentreform.utils.MathUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

public final class EnchantmentDescription {

    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\{level}|(?<![A-Za-z0-9_])level(?![A-Za-z0-9_])");

    private EnchantmentDescription() {}

    public static String render(String description, PowerVariables variables, int level) {
        if (description == null) {
            return null;
        }
        String rendered = variables.replace(description, level,
                formula -> renderVariable(formula, level));
        return rendered.replace("{level}", String.valueOf(level));
    }

    public static String renderVariable(String formula, int level) {
        boolean percent = formula.startsWith("%:");
        String source = percent ? formula.substring(2) : formula;
        String resolvedFormula = LEVEL_PATTERN.matcher(source).replaceAll(String.valueOf(level));
        if (resolvedFormula.contains("~")) {
            return percent ? resolvedFormula + "%" : resolvedFormula;
        }
        java.util.OptionalDouble calculated = MathUtil.tryCalculate(resolvedFormula);
        if (calculated.isEmpty()) {
            String literal = source.replace("{level}", String.valueOf(level));
            return percent ? literal + "%" : literal;
        }
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(calculated.getAsDouble()) + (percent ? "%" : "");
    }
}

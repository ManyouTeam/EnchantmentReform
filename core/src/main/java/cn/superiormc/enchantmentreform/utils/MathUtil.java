package cn.superiormc.enchantmentreform.utils;


import cn.superiormc.enchantmentreform.managers.ConfigManager;
import cn.superiormc.enchantmentreform.managers.ErrorManager;
import redempt.crunch.Crunch;
import redempt.crunch.functional.ExpressionEnv;

import java.util.OptionalDouble;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {

    public static double doCalculate(String mathStr) {
        try {
            return calculate(mathStr);
        }
        catch (Throwable throwable) {
            if (ConfigManager.configManager.getBoolean("debug")) {
                throwable.printStackTrace();
            }
            ErrorManager.errorManager.sendErrorMessage("§cError: Your number option value " +
                    mathStr + " can not be read as a number, maybe" +
                    "set math.enabled to false in config.yml maybe solve this problem!");
            return 0;
        }
    }

    /** Attempts numeric evaluation without reporting an error for legitimate text values. */
    public static OptionalDouble tryCalculate(String mathStr) {
        try {
            return OptionalDouble.of(calculate(mathStr));
        } catch (Throwable ignored) {
            return OptionalDouble.empty();
        }
    }

    private static double calculate(String mathStr) {
        if (ConfigManager.configManager != null
                && !ConfigManager.configManager.getBoolean("math.enabled", true)) {
            return Double.parseDouble(mathStr);
        }
        if (ConfigManager.configManager == null
                || ConfigManager.configManager.getBoolean("math.enable-function", true)) {
            ExpressionEnv env = new ExpressionEnv();
            env.addFunction("sum", d -> {
                double sum = 0;
                for (double value : d) {
                    sum += value;
                }
                return sum;
            });
            env.addFunction("max", d -> {
                if (d.length == 0) {
                    return 0;
                }
                double maximum = d[0];
                for (int i = 1; i < d.length; i++) {
                    maximum = Math.max(maximum, d[i]);
                }
                return maximum;
            });
            env.addFunction("min", d -> {
                if (d.length == 0) {
                    return 0;
                }
                double minimum = d[0];
                for (int i = 1; i < d.length; i++) {
                    minimum = Math.min(minimum, d[i]);
                }
                return minimum;
            });
            env.addFunction("avg", d -> {
                if (d.length == 0) {
                    return 0;
                }
                double sum = 0;
                for (double value : d) {
                    sum += value;
                }
                return sum / d.length;
            });
            env.addFunction("floor", d -> d.length == 0 ? 0 : Math.floor(d[0]));
            env.addFunction("ceil", d -> d.length == 0 ? 0 : Math.ceil(d[0]));
            env.addFunction("round", d -> d.length == 0 ? 0 : Math.round(d[0]));
            env.addFunction("random", d -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                if (d.length == 0) {
                    return random.nextDouble();
                }
                if (d.length == 1) {
                    double minimum = Math.min(0, d[0]);
                    double maximum = Math.max(0, d[0]);
                    return minimum == maximum ? minimum : random.nextDouble(minimum, maximum);
                }
                double minimum = Math.min(d[0], d[1]);
                double maximum = Math.max(d[0], d[1]);
                return minimum == maximum ? minimum : random.nextDouble(minimum, maximum);
            });
            return Crunch.compileExpression(mathStr, env).evaluate();
        }
        return Crunch.evaluateExpression(mathStr);
    }

}

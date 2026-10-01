package cn.superiormc.enchantmentreform.utils;

import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;

public class ParseUtil {

    /**
     *  Bukkit Color
     * ?
     * - "#RRGGBB"
     * - "R,G,B"
     * - ed, blue, green ?
     */
    /**
     *  Bukkit Color
     * - "#RRGGBB"
     * - "R,G,B"
     * - ed, blue, green ?
     */
    public static Color parseColor(String value) {
        value = value.trim().toLowerCase();

        if (value.startsWith("#")) {
            //  (#FF0000)
            java.awt.Color awt = java.awt.Color.decode(value);
            return Color.fromRGB(awt.getRed(), awt.getGreen(), awt.getBlue());

        } else if (value.contains(",")) {
            // RGB  (255,0,0)
            String[] parts = value.split(",");
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return Color.fromRGB(r, g, b);

        } else {
            return switch (value) {
                case "red" -> Color.RED;
                case "green" -> Color.GREEN;
                case "blue" -> Color.BLUE;
                case "white" -> Color.WHITE;
                case "black" -> Color.BLACK;
                case "yellow" -> Color.YELLOW;
                case "aqua" -> Color.AQUA;
                case "gray", "grey" -> Color.GRAY;
                case "lime" -> Color.LIME;
                case "orange" -> Color.ORANGE;
                case "purple" -> Color.PURPLE;
                case "fuchsia", "pink" -> Color.FUCHSIA;
                default -> throw new IllegalArgumentException("Unknown color: " + value);
            };
        }
    }

    /**
     * ?Color 
     */
    public static List<Color> parseColorList(List<String> rawList) {
        List<Color> colors = new ArrayList<>();

        for (String value : rawList) {
            try {
                colors.add(parseColor(value));
            } catch (Exception e) {
                return colors;
            }
        }

        return colors;
    }
}

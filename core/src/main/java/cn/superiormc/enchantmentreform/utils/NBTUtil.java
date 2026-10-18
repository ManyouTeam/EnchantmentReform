package cn.superiormc.enchantmentreform.utils;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class NBTUtil {

    public static String getObjectType(Object object) {
        if (object instanceof Byte) {
            return "byte";
        } else if (object instanceof Short) {
            return "short";
        } else if (object instanceof Integer) {
            return "int";
        } else if (object instanceof Long) {
            return "long";
        } else if (object instanceof Float) {
            return "float";
        } else if (object instanceof Double) {
            return "double";
        } else if (object instanceof String) {
            return "string";
        }
        return null;
    }

    public static ItemStack addNBT(ItemStack item, String type, String key, Object object) {
        if (!CommonUtil.checkPluginLoad("NBTAPI")) {
            return item; //  NBTAPI ?
        }
        NBTItem nbtItem = new NBTItem(item);
        setValue(nbtItem, type, key, object);
        return nbtItem.getItem(); //  ItemStack
    }

    public static boolean setPathValue(NBTItem item, String[] elements, String type) {
        if (item == null || elements == null || elements.length < 2) {
            return false;
        }
        int valueIndex = elements.length - 1;
        int keyIndex = valueIndex - 1;
        NBTCompound compound = resolveCompound(item, elements, keyIndex, true);
        if (compound == null || elements[keyIndex].isEmpty()) {
            return false;
        }
        Object value;
        if (type.equalsIgnoreCase("string")) {
            value = elements[valueIndex];
        } else {
            double calculated = MathUtil.doCalculate(elements[valueIndex]);
            value = switch (type.toLowerCase()) {
                case "byte" -> (byte) Math.round(calculated);
                case "int" -> (int) Math.round(calculated);
                default -> calculated;
            };
        }
        return setValue(compound, type, elements[keyIndex], value);
    }

    public static boolean matchesPathValue(NBTItem item, String[] elements, String type) {
        boolean stringValue = type.equalsIgnoreCase("string");
        int suffixLength = stringValue ? 1 : 2;
        if (item == null || elements == null || elements.length < suffixLength + 1) {
            return false;
        }
        int keyIndex = elements.length - suffixLength - 1;
        NBTCompound compound = resolveCompound(item, elements, keyIndex, false);
        if (compound == null || elements[keyIndex].isEmpty()) {
            return false;
        }
        String key = elements[keyIndex];
        if (stringValue) {
            return compound.hasTag(key, NBTType.NBTTagString)
                    && compound.getString(key).equals(elements[elements.length - 1]);
        }
        String operator = elements[elements.length - 2];
        String expectedValue = elements[elements.length - 1];
        try {
            return switch (type.toLowerCase()) {
                case "byte" -> compound.hasTag(key, NBTType.NBTTagByte)
                        && compare(compound.getByte(key), operator, Byte.parseByte(expectedValue));
                case "int" -> compound.hasTag(key, NBTType.NBTTagInt)
                        && compare(compound.getInteger(key), operator, Integer.parseInt(expectedValue));
                case "double" -> compound.hasTag(key, NBTType.NBTTagDouble)
                        && compare(compound.getDouble(key), operator, Double.parseDouble(expectedValue));
                default -> false;
            };
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean containsPath(NBTItem item, String[] elements) {
        if (item == null || elements == null || elements.length == 0) {
            return false;
        }
        int keyIndex = elements.length - 1;
        NBTCompound compound = resolveCompound(item, elements, keyIndex, false);
        return compound != null && !elements[keyIndex].isEmpty()
                && compound.hasTag(elements[keyIndex]);
    }

    public static boolean removePath(NBTItem item, String[] elements) {
        if (item == null || elements == null || elements.length == 0) {
            return false;
        }
        int keyIndex = elements.length - 1;
        NBTCompound compound = resolveCompound(item, elements, keyIndex, false);
        if (compound == null || elements[keyIndex].isEmpty()
                || !compound.hasTag(elements[keyIndex])) {
            return false;
        }
        compound.removeKey(elements[keyIndex]);
        if (compound != item && compound.getKeys().isEmpty() && compound.getParent() != null) {
            compound.getParent().removeKey(compound.getName());
        }
        return true;
    }

    private static NBTCompound resolveCompound(NBTItem item, String[] elements,
                                               int parentLength, boolean create) {
        NBTCompound compound = item;
        for (int index = 0; index < parentLength; index++) {
            String key = elements[index];
            if (key.isEmpty()) {
                continue;
            }
            compound = create ? compound.getOrCreateCompound(key) : compound.getCompound(key);
            if (compound == null) {
                return null;
            }
        }
        return compound;
    }

    private static boolean setValue(NBTCompound compound, String type, String key, Object value) {
        if (compound == null || type == null || key == null || value == null) {
            return false;
        }
        switch (type.toLowerCase()) {
            case "byte" -> compound.setByte(key, ((Number) value).byteValue());
            case "short" -> compound.setShort(key, ((Number) value).shortValue());
            case "int" -> compound.setInteger(key, ((Number) value).intValue());
            case "long" -> compound.setLong(key, ((Number) value).longValue());
            case "float" -> compound.setFloat(key, ((Number) value).floatValue());
            case "double" -> compound.setDouble(key, ((Number) value).doubleValue());
            case "string" -> compound.setString(key, String.valueOf(value));
            default -> {
                return false;
            }
        }
        return true;
    }

    private static boolean compare(double actual, String operator, double expected) {
        return switch (operator) {
            case ">=" -> actual >= expected;
            case ">" -> actual > expected;
            case "<=" -> actual <= expected;
            case "<" -> actual < expected;
            case "==" -> actual == expected;
            default -> false;
        };
    }

    public static Map<String, Object> getAllNBT(ItemStack item) {
        Map<String, Object> nbtData = new HashMap<>();

        if (!CommonUtil.checkPluginLoad("NBTAPI")) {
            return nbtData; //  NBTAPI ?Map
        }

        NBTItem nbtItem = new NBTItem(item);

        for (String key : nbtItem.getKeys()) { //  NBT 
            Object value = null;

            // ?
            if (nbtItem.hasTag(key)) {
                if (nbtItem.getType(key) == NBTType.NBTTagString) {
                    value = nbtItem.getString(key);
                } else if (nbtItem.getType(key) == NBTType.NBTTagInt) {
                    value = nbtItem.getInteger(key);
                } else if (nbtItem.getType(key) == NBTType.NBTTagByte) {
                    value = nbtItem.getByte(key);
                } else if (nbtItem.getType(key) == NBTType.NBTTagDouble) {
                    value = nbtItem.getDouble(key);
                } else if (nbtItem.getType(key) == NBTType.NBTTagFloat) {
                    value = nbtItem.getFloat(key);
                } else if (nbtItem.getType(key) == NBTType.NBTTagLong) {
                    value = nbtItem.getLong(key);
                } else if (nbtItem.getType(key) == NBTType.NBTTagShort) {
                    value = nbtItem.getShort(key);
                }

                nbtData.put(key, value); //  Map
            }
        }

        return nbtData;
    }


}


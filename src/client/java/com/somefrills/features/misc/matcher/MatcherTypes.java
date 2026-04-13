package com.somefrills.features.misc.matcher;

import net.minecraft.entity.EquipmentSlot;

/**
 * Central configuration for all available matcher types and their names.
 * This is the single source of truth for matcher type definitions.
 * Handles canonical names and their alternative spellings.
 */
public class MatcherTypes {

    // Matcher type names
    public static final String TYPE = "TYPE";
    public static final String NAME = "NAME";
    public static final String NAKED = "NAKED";

    // Canonical equipment slot names
    public static final String HELMET = "HELMET";
    public static final String CHEST = "CHEST";
    public static final String LEGS = "LEGS";
    public static final String FEET = "FEET";
    public static final String MAINHAND = "MAINHAND";
    public static final String OFFHAND = "OFFHAND";

    // Logical operators
    public static final String AND = "AND";
    public static final String OR = "OR";

    // Special item values for equipment slots
    public static final String EMPTY_SLOT_NONE = "none";
    public static final String EMPTY_SLOT_AIR = "air";
    public static final String ANY_ITEM = "any";
    public static final String ANY_ITEM_MARKER = "ANY_ITEM";
    public static final String MINECRAFT_PREFIX = "minecraft:";

    /**
     * Get all basic matcher type names (non-equipment).
     *
     * @return array of basic matcher names (TYPE, NAME, NAKED)
     */
    public static String[] getBasicMatchers() {
        return new String[]{TYPE, NAME, NAKED};
    }

    /**
     * Get all canonical equipment slot names.
     *
     * @return array of canonical equipment slot names
     */
    public static String[] getEquipmentSlots() {
        return new String[]{HELMET, CHEST, LEGS, FEET, MAINHAND, OFFHAND};
    }

    /**
     * Get all matcher names (basic + equipment).
     *
     * @return array of all matcher names
     */
    public static String[] getAllMatchers() {
        String[] basic = getBasicMatchers();
        String[] equipment = getEquipmentSlots();
        String[] all = new String[basic.length + equipment.length];
        System.arraycopy(basic, 0, all, 0, basic.length);
        System.arraycopy(equipment, 0, all, basic.length, equipment.length);
        return all;
    }

    /**
     * Check if a string is a logical operator (AND or OR).
     *
     * @param word the word to check
     * @return true if word is AND or OR, false otherwise
     */
    public static boolean isLogicalOperator(String word) {
        return word != null && (word.equalsIgnoreCase(AND) || word.equalsIgnoreCase(OR));
    }

    /**
     * Check if a string is a basic matcher type (TYPE, NAME, or NAKED).
     *
     * @param word the word to check
     * @return true if word is a basic matcher, false otherwise
     */
    public static boolean isBasicMatcher(String word) {
        if (word == null) return false;
        String upper = word.toUpperCase();
        return upper.equals(TYPE) || upper.equals(NAME) || upper.equals(NAKED);
    }

    /**
     * Check if a string is a valid equipment slot (including alternatives).
     * Valid slots: HEAD/HELMET, CHEST/CHESTPLATE, LEGS/LEGGINGS, FEET/BOOTS,
     * MAINHAND/MAIN_HAND/HAND, OFFHAND/OFF_HAND
     *
     * @param word the word to check
     * @return true if word is a valid equipment slot, false otherwise
     */
    public static boolean isEquipmentSlot(String word) {
        return parseEquipmentSlot(word) != null;
    }

    /**
     * Check if a string is any valid matcher type (basic or equipment).
     *
     * @param word the word to check
     * @return true if word is a valid matcher type, false otherwise
     */
    public static boolean isMatcher(String word) {
        return isBasicMatcher(word) || isEquipmentSlot(word);
    }

    /**
     * Convert a slot name (including alternatives) to EquipmentSlot enum.
     *
     * @param slotName the slot name to parse (accepts both canonical and alternative names)
     * @return the EquipmentSlot enum, or null if invalid
     */
    public static EquipmentSlot parseEquipmentSlot(String slotName) {
        if (slotName == null) return null;
        String upper = slotName.toUpperCase();
        return switch (upper) {
            case "HEAD", HELMET -> EquipmentSlot.HEAD;
            case CHEST, "CHESTPLATE" -> EquipmentSlot.CHEST;
            case LEGS, "LEGGINGS" -> EquipmentSlot.LEGS;
            case FEET, "BOOTS" -> EquipmentSlot.FEET;
            case MAINHAND, "MAIN_HAND", "HAND" -> EquipmentSlot.MAINHAND;
            case OFFHAND, "OFF_HAND" -> EquipmentSlot.OFFHAND;
            default -> null;
        };
    }

    /**
     * Check if an item ID string represents an empty slot.
     * Accepts "none", "air", or empty string.
     *
     * @param itemId the item ID to check
     * @return true if it represents an empty slot
     */
    public static boolean isEmptySlot(String itemId) {
        if (itemId == null) return false;
        String normalized = itemId.toLowerCase().trim();
        if (normalized.isEmpty()) return true;
        for (String option : getEmptySlotOptions()) {
            if (normalized.equals(option)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an item ID string represents "any item".
     *
     * @param itemId the item ID to check
     * @return true if it represents any item
     */
    public static boolean isAnyItem(String itemId) {
        if (itemId == null) return false;
        return itemId.toLowerCase().trim().equals(ANY_ITEM);
    }

    /**
     * Get all special item values for equipment slots (empty/any options).
     *
     * @return array of special item values: air, none, any
     */
    public static String[] getEmptySlotOptions() {
        return new String[]{EMPTY_SLOT_AIR, EMPTY_SLOT_NONE, ANY_ITEM};
    }

    /**
     * Normalize an item ID by removing minecraft prefix and converting to lowercase.
     *
     * @param itemId the item ID to normalize
     * @return the normalized item ID
     */
    public static String normalizeItemId(String itemId) {
        if (itemId == null) return "";
        String result = itemId;
        if (result.toLowerCase().startsWith(MINECRAFT_PREFIX)) {
            result = result.substring(MINECRAFT_PREFIX.length());
        }
        return result.toLowerCase();
    }
}




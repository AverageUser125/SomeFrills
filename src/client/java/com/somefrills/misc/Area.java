package com.somefrills.misc;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

/**
 * Enum of all known Skyblock areas.
 * Each area maps to its display name as shown in the tab list.
 */
public enum Area {
    PRIVATE_ISLAND("Private Island", Formatting.AQUA, Items.GRASS_BLOCK),
    HUB("Hub", Formatting.AQUA, Items.ENDER_PEARL),
    DUNGEON_HUB("Dungeon Hub", 0xFF5555, Items.IRON_SWORD),
    THE_BARN("The Barn", 0x55FF55, Items.WHEAT),
    THE_PARK("The Park", 0x55FF55, Items.OAK_LEAVES),
    GALATEA("Galatea", Formatting.DARK_GREEN, Items.PRISMARINE_SHARD),
    GOLD_MINE("Gold Mine", Formatting.GOLD, Items.GOLD_INGOT),
    DEEP_CAVERNS("Deep Caverns", 0xAAAAAA, Items.STONE),
    DWARVEN_MINES("Dwarven Mines", Formatting.AQUA, Items.DIAMOND_ORE),
    CRYSTAL_HOLLOWS("Crystal Hollows", Formatting.DARK_PURPLE, Items.AMETHYST_SHARD),
    SPIDERS_DEN("Spider's Den", Formatting.DARK_RED, Items.STRING),
    THE_END("The End", Formatting.DARK_PURPLE, Items.ENDER_EYE),
    CRIMSON_ISLE("Crimson Isle", 0xFF5555, Items.NETHER_BRICK),
    GARDEN("Garden", 0x55FF55, Items.OAK_SAPLING),
    THE_RIFT("The Rift", Formatting.LIGHT_PURPLE, Items.NETHER_STAR),
    BACKWATER_BAYOU("Backwater Bayou", Formatting.DARK_GREEN, Items.LILY_PAD),
    JERRYS_WORKSHOP("Jerry's Workshop", 0xFF5555, Items.REDSTONE_BLOCK),
    CATACOMBS("Catacombs", Formatting.DARK_PURPLE, Items.BONE_BLOCK),
    KUUDRA("Kuudra", Formatting.RED, Items.SANDSTONE),
    DRAGONS_NEST("Dragons Nest", Formatting.GOLD, Items.DRAGON_EGG),
    GRAVEYARD("Graveyard", Formatting.DARK_RED, Items.SOUL_SAND),
    MUSHROOM_GORGE("Mushroom Gorge", 0xFF5555, Items.MYCELIUM),
    THE_MIST("The Mist", 0xAAAAAA, Items.WHITE_STAINED_GLASS),
    MINESHAFT("Mineshaft", 0xAAAAAA, Items.PACKED_ICE),
    LOST_PRECURSOR_CITY("Lost Precursor City", Formatting.DARK_PURPLE, Items.CHISELED_STONE_BRICKS),
    UNKNOWN("Unknown", Formatting.WHITE, Items.WHITE_CONCRETE);

    private final String displayName;
    private final int color;
    private final Item item;

    Area(String displayName, Formatting color, Item item) {
        this.displayName = displayName;
        assert color.getColorValue() != null;
        this.color = color.getColorValue();
        this.item = item;
    }

    Area(String displayName, int hexColor, Item item) {
        this.displayName = displayName;
        this.color = hexColor;
        this.item = item;
    }

    /**
     * Get the display name of this area (as shown in the tab list)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the color hex value for this area
     */
    public int getColorHex() {
        return color;
    }

    /**
     * Convert a string (from tab list or user input) to an Area enum.
     * Case-insensitive matching.
     *
     * @param areaName the area name to convert
     * @return Optional containing the Area if found, empty otherwise
     */
    public static Area fromString(String areaName) {
        if (areaName == null || areaName.isEmpty()) {
            return Area.UNKNOWN;
        }

        // Try to match by display name (case-insensitive)
        for (Area area : Area.values()) {
            if (area.displayName.equalsIgnoreCase(areaName.trim())) {
                return area;
            }
        }

        return Area.UNKNOWN;
    }

    /**
     * Get all area display names
     */
    public static String[] getAllDisplayNames() {
        String[] names = new String[Area.values().length];
        for (int i = 0; i < Area.values().length; i++) {
            names[i] = Area.values()[i].displayName;
        }
        return names;
    }

    public Item getItem() {
        return item;
    }
}

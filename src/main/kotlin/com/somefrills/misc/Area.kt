package com.somefrills.misc

import net.minecraft.ChatFormatting
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

/**
 * Enum of all known Skyblock areas.
 * Each area maps to its display name as shown in the tab list.
 */
enum class Area {
    PRIVATE_ISLAND("Private Island", ChatFormatting.AQUA, Items.GRASS_BLOCK),
    HUB("Hub", ChatFormatting.AQUA, Items.ENDER_PEARL),
    DUNGEON_HUB("Dungeon Hub", 0xFF5555, Items.IRON_SWORD),
    THE_BARN("The Barn", 0x55FF55, Items.WHEAT),
    THE_PARK("The Park", 0x55FF55, Items.OAK_LEAVES),
    GALATEA("Galatea", ChatFormatting.DARK_GREEN, Items.PRISMARINE_SHARD),
    GOLD_MINE("Gold Mine", ChatFormatting.GOLD, Items.GOLD_INGOT),
    DEEP_CAVERNS("Deep Caverns", 0xAAAAAA, Items.STONE),
    DWARVEN_MINES("Dwarven Mines", ChatFormatting.AQUA, Items.DIAMOND_ORE),
    CRYSTAL_HOLLOWS("Crystal Hollows", ChatFormatting.DARK_PURPLE, Items.AMETHYST_SHARD),
    SPIDERS_DEN("Spider's Den", ChatFormatting.DARK_RED, Items.STRING),
    THE_END("The End", ChatFormatting.DARK_PURPLE, Items.ENDER_EYE),
    CRIMSON_ISLE("Crimson Isle", 0xFF5555, Items.NETHER_BRICK),
    GARDEN("Garden", 0x55FF55, Items.OAK_SAPLING),
    THE_RIFT("The Rift", ChatFormatting.LIGHT_PURPLE, Items.NETHER_STAR),
    BACKWATER_BAYOU("Backwater Bayou", ChatFormatting.DARK_GREEN, Items.LILY_PAD),
    JERRYS_WORKSHOP("Jerry's Workshop", 0xFF5555, Items.REDSTONE_BLOCK),
    CATACOMBS("Catacombs", ChatFormatting.DARK_PURPLE, Items.BONE_BLOCK),
    KUUDRA("Kuudra", ChatFormatting.RED, Items.SANDSTONE),
    DRAGONS_NEST("Dragons Nest", ChatFormatting.GOLD, Items.DRAGON_EGG),
    GRAVEYARD("Graveyard", ChatFormatting.DARK_RED, Items.SOUL_SAND),
    MUSHROOM_GORGE("Mushroom Gorge", 0xFF5555, Items.MYCELIUM),
    THE_MIST("The Mist", 0xAAAAAA, Items.WHITE_STAINED_GLASS),
    MINESHAFT("Mineshaft", 0xAAAAAA, Items.PACKED_ICE),
    LOST_PRECURSOR_CITY("Lost Precursor City", ChatFormatting.DARK_PURPLE, Items.CHISELED_STONE_BRICKS),
    LOTUS_ATOLL("Lotus Atoll", 0x55FF55, Items.LILY_OF_THE_VALLEY),
    UNKNOWN("Unknown", ChatFormatting.WHITE, Items.WHITE_CONCRETE);

    /**
     * Get the display name of this area (as shown in the tab list)
     */
    @JvmField
    val displayName: String

    /**
     * Get the color hex value for this area
     */
    val colorHex: Int

    @JvmField
    val item: Item

    constructor(displayName: String, color: ChatFormatting, item: Item) {
        this.displayName = displayName
        checkNotNull(color.color)
        this.colorHex = color.color!!
        this.item = item
    }

    constructor(displayName: String, hexColor: Int, item: Item) {
        this.displayName = displayName
        this.colorHex = hexColor
        this.item = item
    }

    companion object {
        /**
         * Convert a string (from tab list or user input) to an Area enum.
         * Case-insensitive matching.
         *
         * @param areaName the area name to convert
         * @return Optional containing the Area if found, empty otherwise
         */
        @JvmStatic
        fun fromString(areaName: String?): Area {
            if (areaName.isNullOrEmpty()) {
                return UNKNOWN
            }

            // Try to match by display name (case-insensitive)
            for (area in entries) {
                if (area.displayName.equals(areaName.trim { it <= ' ' }, ignoreCase = true)) {
                    return area
                }
            }

            return UNKNOWN
        }

        val allDisplayNames: Array<String?>
            /**
             * Get all area display names
             */
            get() {
                val names =
                    arrayOfNulls<String>(entries.size)
                for (i in entries.toTypedArray().indices) {
                    names[i] = entries[i].displayName
                }
                return names
            }
    }
}
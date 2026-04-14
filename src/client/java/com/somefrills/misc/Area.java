package com.somefrills.misc;

import java.util.Optional;

/**
 * Enum of all known Skyblock areas.
 * Each area maps to its display name as shown in the tab list.
 */
public enum Area {
    PRIVATE_ISLAND("Private Island"),
    CATACOMBS("Catacombs"),
    KUUDRA("Kuudra"),
    DUNGEON_HUB("Dungeon Hub"),
    CRIMSON_ISLE("Crimson Isle"),
    DRAGONS_NEST("Dragons Nest"),
    DEEP_CAVERNS("Deep Caverns"),
    THE_END("The End"),
    GARDEN("Garden"),
    GOLD_MINE("Gold Mine"),
    GRAVEYARD("Graveyard"),
    SPIDERS_DEN("Spider's Den"),
    MUSHROOM_GORGE("Mushroom Gorge"),
    DWARVEN_MINES("Dwarven Mines"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    THE_MIST("The Mist"),
    MINESHAFT("Mineshaft"),
    LOST_PRECURSOR_CITY("Lost Precursor City"),
    HUB("Hub"),
    UNKNOWN("Unknown");

    private final String displayName;

    Area(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the display name of this area (as shown in the tab list)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert a string (from tab list or user input) to an Area enum.
     * Case-insensitive matching.
     *
     * @param areaName the area name to convert
     * @return Optional containing the Area if found, empty otherwise
     */
    public static Optional<Area> fromString(String areaName) {
        if (areaName == null || areaName.isEmpty()) {
            return Optional.empty();
        }

        // Try to match by display name (case-insensitive)
        for (Area area : Area.values()) {
            if (area.displayName.equalsIgnoreCase(areaName.trim())) {
                return Optional.of(area);
            }
        }

        return Optional.of(Area.UNKNOWN);
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
}

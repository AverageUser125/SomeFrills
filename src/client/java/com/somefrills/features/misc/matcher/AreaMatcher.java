package com.somefrills.features.misc.matcher;

import com.somefrills.misc.Area;
import com.somefrills.misc.Utils;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Matches entities based on the current area they're in (from the scoreboard tab list).
 * Uses the Area enum for type safety.
 * Example: AREA=Private Island
 * Priority: 20
 */
public class AreaMatcher implements Matcher {
    private final Area area;

    /**
     * Create a matcher for a specific area.
     * Accepts either an Area enum or a string that will be converted to an area.
     *
     * @param area the area to match
     */
    public AreaMatcher(Area area) {
        this.area = area;
    }

    /**
     * Create a matcher from a string (for parser use).
     * The string will be converted to an Area enum if valid.
     *
     * @param areaString the area name as a string
     * @throws IllegalArgumentException if the area name is not recognized
     */
    public AreaMatcher(String areaString) throws IllegalArgumentException {
        this.area = Area.fromString(areaString)
                .orElseThrow(() -> new IllegalArgumentException("Unknown area: " + areaString));
    }

    @Override
    public Predicate<LivingEntity> compile() {
        return entity -> Utils.isInArea(this.area);
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public String toString() {
        return "AREA=" + area.getDisplayName();
    }

    /**
     * Get the Area enum this matcher uses
     */
    public Area getArea() {
        return area;
    }
}





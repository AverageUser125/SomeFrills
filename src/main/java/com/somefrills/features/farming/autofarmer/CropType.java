package com.somefrills.features.farming.autofarmer;

import org.jspecify.annotations.NonNull;

/**
 * Enum of crop types and their movement patterns.
 */
public enum CropType {
    // Rectangular pattern: melon, pumpkin, wheat, carrot, potato, nether wart
    // Movement: Move forward, if hit wall on right, move left, if hit wall on left, move right
    MELON("Melon", MovementPattern.RECTANGULAR),
    PUMPKIN("Pumpkin", MovementPattern.RECTANGULAR),
    WHEAT("Wheat", MovementPattern.RECTANGULAR),
    CARROT("Carrot", MovementPattern.RECTANGULAR),
    POTATO("Potato", MovementPattern.RECTANGULAR),
    NETHER_WART("Nether Wart", MovementPattern.RECTANGULAR),

    // Diagonal pattern: sugar cane, sunflower, rose
    // Movement: Move forward and left, if hit corner, move backwards only, etc.
    SUGAR_CANE("Sugar Cane", MovementPattern.DIAGONAL),
    SUNFLOWER("Sunflower", MovementPattern.DIAGONAL),
    MOONFLOWER("Moonflower", MovementPattern.DIAGONAL),
    ROSE("Rose", MovementPattern.DIAGONAL),

    // Mushroom pattern: always forward, move right, then left when hitting front wall
    // Movement: Forward+Right → Forward only (hit right wall) → Forward+Left (hit front wall) → repeat
    MUSHROOM("Mushroom", MovementPattern.MUSHROOM_FORWARD);

    private final String displayName;
    private final MovementPattern pattern;

    CropType(String displayName, MovementPattern pattern) {
        this.displayName = displayName;
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public MovementPattern getPattern() {
        return pattern;
    }

    /**
     * Create a new movement strategy for this crop type.
     */
    @NonNull
    public MovementStrategy getStrategy() {
        return switch (pattern) {
            case RECTANGULAR -> new RectangularMovement();
            case DIAGONAL -> new DiagonalMovement();
            case MUSHROOM_FORWARD -> new MushroomMovement();
        };
    }

    public enum MovementPattern {
        RECTANGULAR,
        DIAGONAL,
        MUSHROOM_FORWARD
    }
}


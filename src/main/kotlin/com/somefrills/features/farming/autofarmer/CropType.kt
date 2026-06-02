package com.somefrills.features.farming.autofarmer

/**
 * Enum of crop types and their movement patterns.
 */
enum class CropType(val displayName: String, val pattern: MovementPattern) {
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
    MUSHROOM("Mushroom", MovementPattern.MUSHROOM_FORWARD),

    // Coco beans pattern: forward, right, backward, right, repeat
    // Movement: Forward → Right → Backward → Right → repeat
    COCO_BEANS("Coco Beans", MovementPattern.COCO_BEANS);

    override fun toString(): String {
        return displayName
    }

    val strategy: MovementStrategy
        /**
         * Create a new movement strategy for this crop type.
         */
        get() = when (pattern) {
            MovementPattern.RECTANGULAR -> RectangularMovement()
            MovementPattern.DIAGONAL -> DiagonalMovement()
            MovementPattern.MUSHROOM_FORWARD -> MushroomMovement()
            MovementPattern.COCO_BEANS -> CocoBeansMovement()
        }

    enum class MovementPattern {
        RECTANGULAR,
        DIAGONAL,
        MUSHROOM_FORWARD,
        COCO_BEANS
    }
}


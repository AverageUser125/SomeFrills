package com.somefrills.features.farming.autofarmer;

import static com.somefrills.Main.mc;

/**
 * Rectangular pattern farming (melon, pumpkin, wheat, carrot, potato, nether wart).
 * Moves forward, detects walls on right (relative to facing) and switches to moving left,
 * then detects walls on left and switches back to moving right.
 * Movement is always relative to the player's facing direction.
 */
public class RectangularMovement implements MovementStrategy {
    private Direction currentDirection = Direction.FORWARD;
    private double lastX;
    private double lastZ;
    private int noMovementTicks = 0;
    private static final int WALL_DETECT_THRESHOLD = 5; // ticks without movement = wall hit

    public RectangularMovement() {
        reset();
    }

    @Override
    public MovementInput getMovement() {
        if (mc.player == null) {
            return MovementInput.noMovement();
        }

        // Check if player has moved
        double currentX = mc.player.getX();
        double currentZ = mc.player.getZ();

        boolean moved = Math.abs(currentX - lastX) > 0.001 || Math.abs(currentZ - lastZ) > 0.001;
        lastX = currentX;
        lastZ = currentZ;

        if (!moved) {
            noMovementTicks++;
            // Detected a wall, switch direction
            if (noMovementTicks >= WALL_DETECT_THRESHOLD) {
                currentDirection = currentDirection.nextRectangular();
                noMovementTicks = 0;
            }
        } else {
            noMovementTicks = 0;
        }

        return currentDirection.toMovementInput();
    }

    @Override
    public void reset() {
        if (mc.player != null) {
            lastX = mc.player.getX();
            lastZ = mc.player.getZ();
        }
        currentDirection = Direction.FORWARD;
        noMovementTicks = 0;
    }

    private enum Direction {
        FORWARD {
            @Override
            public Direction nextRectangular() {
                return LEFT;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(true, false, false, false);
            }
        },
        LEFT {
            @Override
            public Direction nextRectangular() {
                return FORWARD;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(false, false, true, false);
            }
        },
        RIGHT {
            @Override
            public Direction nextRectangular() {
                return FORWARD;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(false, false, false, true);
            }
        },
        BACKWARD {
            @Override
            public Direction nextRectangular() {
                return FORWARD;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(false, true, false, false);
            }
        };

        protected abstract Direction nextRectangular();

        protected abstract MovementInput toMovementInput();
    }
}


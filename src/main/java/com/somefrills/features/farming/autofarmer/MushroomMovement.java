package com.somefrills.features.farming.autofarmer;

import static com.somefrills.Main.mc;

/**
 * Mushroom pattern farming - always moving forward with lateral adjustments.
 * Cycles: Forward+Right (relative) → Forward only (hit right wall) → Forward+Left (relative, hit front wall) → repeat
 * Movement is always relative to the player's facing direction.
 */
public class MushroomMovement implements MovementStrategy {
    private State state = State.FORWARD_RIGHT;
    private double lastX;
    private double lastZ;
    private int noMovementTicks = 0;
    private static final int WALL_DETECT_THRESHOLD = 5; // ticks without movement = wall hit

    public MushroomMovement() {
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
            // Detected a wall, switch state
            if (noMovementTicks >= WALL_DETECT_THRESHOLD) {
                state = state.next();
                noMovementTicks = 0;
            }
        } else {
            noMovementTicks = 0;
        }

        return state.toMovementInput();
    }

    @Override
    public void reset() {
        if (mc.player != null) {
            lastX = mc.player.getX();
            lastZ = mc.player.getZ();
        }
        state = State.FORWARD_RIGHT;
        noMovementTicks = 0;
    }

    private enum State {
        // Move forward and right (relative to facing)
        FORWARD_RIGHT {
            @Override
            public State next() {
                return FORWARD_ONLY;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(true, false, false, true);
            }
        },
        // Move forward only (hit right wall)
        FORWARD_ONLY {
            @Override
            public State next() {
                return FORWARD_LEFT;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(true, false, false, false);
            }
        },
        // Move forward and left (relative to facing, hit front wall)
        FORWARD_LEFT {
            @Override
            public State next() {
                return FORWARD_RIGHT;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(true, false, true, false);
            }
        };

        protected abstract State next();

        protected abstract MovementInput toMovementInput();
    }
}


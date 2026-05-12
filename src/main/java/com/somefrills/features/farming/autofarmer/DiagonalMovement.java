package com.somefrills.features.farming.autofarmer;

import static com.somefrills.Main.mc;

/**
 * Diagonal pattern farming (sugar cane, sunflower, moonflower, rose).
 * Moves forward and left (relative to facing) at 45 degrees, detects corners and switches to backward,
 * then back to forward-left, cycling through corner states.
 * Movement is always relative to the player's facing direction.
 */
public class DiagonalMovement implements MovementStrategy {
    private State state = State.FORWARD_LEFT;
    private double lastX;
    private double lastZ;
    private int noMovementTicks = 0;
    private static final int CORNER_DETECT_THRESHOLD = 5; // ticks without movement = corner hit

    public DiagonalMovement() {
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
            // Detected a corner, switch state
            if (noMovementTicks >= CORNER_DETECT_THRESHOLD) {
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
        state = State.FORWARD_LEFT;
        noMovementTicks = 0;
    }

    private enum State {
        // Move forward and left (45 degrees relative to facing)
        FORWARD_LEFT {
            @Override
            public State next() {
                return BACKWARD;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(true, false, true, false);
            }
        },
        // Move backward only when corner is hit
        BACKWARD {
            @Override
            public State next() {
                return FORWARD_LEFT;
            }

            @Override
            public MovementInput toMovementInput() {
                return new MovementInput(false, true, false, false);
            }
        };

        protected abstract State next();

        protected abstract MovementInput toMovementInput();
    }
}


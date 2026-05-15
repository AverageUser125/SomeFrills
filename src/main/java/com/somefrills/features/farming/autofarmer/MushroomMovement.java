package com.somefrills.features.farming.autofarmer;

import org.jspecify.annotations.NonNull;

/**
 * Mushroom pattern farming - always moving forward with lateral adjustments.
 * Cycles: Forward+Right → Forward only → Forward+Left → repeat
 */
public class MushroomMovement implements MovementStrategy {
    private FarmingState currentState = FarmingState.FORWARD_RIGHT;

    public MushroomMovement() {
    }

    public @NonNull MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = (FarmingState) currentState.next();
    }

    private enum FarmingState implements MovementStrategy.State {
        FORWARD_RIGHT {
            @Override
            public FarmingState next() {
                return FORWARD_ONLY;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD | MovementState.RIGHT);
            }
        },
        FORWARD_ONLY {
            @Override
            public FarmingState next() {
                return FORWARD_LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD);
            }
        },
        FORWARD_LEFT {
            @Override
            public FarmingState next() {
                return FORWARD_RIGHT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD | MovementState.LEFT);
            }
        }
    }
}


package com.somefrills.features.farming.autofarmer;

import org.jspecify.annotations.NonNull;

/**
 * Rectangular pattern farming (melon, pumpkin, wheat, carrot, potato, nether wart).
 * Cycles through: Forward+Right → Forward+Left → repeat
 */
public class RectangularMovement implements MovementStrategy {
    private FarmingState currentState = FarmingState.RIGHT;

    public RectangularMovement() {
    }

    public @NonNull MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = (FarmingState) currentState.next();
    }

    private enum FarmingState implements MovementStrategy.State {
        RIGHT {
            @Override
            public FarmingState next() {
                return LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD | MovementState.RIGHT);
            }
        },
        LEFT {
            @Override
            public FarmingState next() {
                return RIGHT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD | MovementState.LEFT);
            }
        }
    }
}


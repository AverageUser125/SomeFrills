package com.somefrills.features.farming.autofarmer;

import org.jspecify.annotations.NonNull;

/**
 * Diagonal pattern farming (sugar cane, sunflower, moonflower, rose).
 * Cycles through: Backward+Left → Forward → Backward+Left
 */
public class DiagonalMovement implements MovementStrategy {
    private FarmingState currentState = FarmingState.BACKWARD;

    public DiagonalMovement() {
    }

    public @NonNull MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = (FarmingState) currentState.next();
    }

    private enum FarmingState implements MovementStrategy.State {
        BACKWARD {
            @Override
            public FarmingState next() {
                return LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.BACKWARD | MovementState.LEFT);
            }
        },
        LEFT {
            @Override
            public FarmingState next() {
                return BACKWARD;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD);
            }
        }
    }
}


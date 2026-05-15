package com.somefrills.features.farming.autofarmer;

/**
 * Diagonal pattern farming (sugar cane, sunflower, moonflower, rose).
 * Cycles through: Forward+Left → Backward → Forward+Left
 */
public class DiagonalMovement implements MovementStrategy {
    private State currentState = State.FORWARD_LEFT;

    public DiagonalMovement() {
    }

    public MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = currentState.next();
    }

    private enum State {
        FORWARD_LEFT {
            @Override
            public State next() {
                return BACKWARD;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(true, false, true, false);
            }
        },
        BACKWARD {
            @Override
            public State next() {
                return FORWARD_LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(false, true, false, false);
            }
        };

        protected abstract State next();

        protected abstract MovementState toMovementState();
    }
}


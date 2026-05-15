package com.somefrills.features.farming.autofarmer;

/**
 * Diagonal pattern farming (sugar cane, sunflower, moonflower, rose).
 * Cycles through: Backward → Forward → Backward
 */
public class DiagonalMovement implements MovementStrategy {
    private State currentState = State.BACKWARD;

    public DiagonalMovement() {
    }

    public MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = currentState.next();
    }

    private enum State {
        BACKWARD {
            @Override
            public State next() {
                return LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(false, true, false, false);
            }
        },
        LEFT {
            @Override
            public State next() {
                return BACKWARD;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(false, false, true, false);
            }
        };

        protected abstract State next();

        protected abstract MovementState toMovementState();
    }
}


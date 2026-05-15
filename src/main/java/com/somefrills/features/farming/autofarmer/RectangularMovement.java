package com.somefrills.features.farming.autofarmer;

/**
 * Rectangular pattern farming (melon, pumpkin, wheat, carrot, potato, nether wart).
 * Cycles through: Forward (with forward always pressed) → Left (with forward always pressed) → repeat
 */
public class RectangularMovement implements MovementStrategy {
    private State currentState = State.RIGHT;

    public RectangularMovement() {
    }

    public MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = currentState.next();
    }

    private enum State {
        RIGHT {
            @Override
            public State next() {
                return LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(true, false, false, true);
            }
        },
        LEFT {
            @Override
            public State next() {
                return RIGHT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(true, false, true, false);
            }
        };

        protected abstract State next();

        protected abstract MovementState toMovementState();
    }
}


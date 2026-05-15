package com.somefrills.features.farming.autofarmer;

/**
 * Mushroom pattern farming - always moving forward with lateral adjustments.
 * Cycles: Forward+Right → Forward only → Forward+Left → repeat
 */
public class MushroomMovement implements MovementStrategy {
    private State currentState = State.FORWARD_RIGHT;

    public MushroomMovement() {
    }

    public MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    public void nextState() {
        currentState = currentState.next();
    }

    private enum State {
        FORWARD_RIGHT {
            @Override
            public State next() {
                return FORWARD_ONLY;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(true, false, false, true);
            }
        },
        FORWARD_ONLY {
            @Override
            public State next() {
                return FORWARD_LEFT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(true, false, false, false);
            }
        },
        FORWARD_LEFT {
            @Override
            public State next() {
                return FORWARD_RIGHT;
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


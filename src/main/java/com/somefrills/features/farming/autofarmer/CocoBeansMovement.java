package com.somefrills.features.farming.autofarmer;

import org.jspecify.annotations.NonNull;

/**
 * Coco beans pattern farming.
 * Cycles: Forward → Right → Backward → Right → Forward (loop)
 */
public class CocoBeansMovement implements MovementStrategy {
    private FarmingState currentState = FarmingState.FORWARD;

    public CocoBeansMovement() {
    }

    @NonNull
    @Override
    public MovementState getCurrentState() {
        return currentState.toMovementState();
    }

    @Override
    public void nextState() {
        currentState = (FarmingState) currentState.next();
    }

    private enum FarmingState implements MovementStrategy.State {
        FORWARD {
            @Override
            public FarmingState next() {
                return RIGHT;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.FORWARD);
            }
        },
        RIGHT {
            @Override
            public FarmingState next() {
                return BACKWARD;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.RIGHT);
            }
        },
        BACKWARD {
            @Override
            public FarmingState next() {
                return RIGHT_2;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.BACKWARD);
            }
        },
        RIGHT_2 {
            @Override
            public FarmingState next() {
                return FORWARD;
            }

            @Override
            public MovementState toMovementState() {
                return new MovementState(MovementState.RIGHT);
            }
        }
    }
}




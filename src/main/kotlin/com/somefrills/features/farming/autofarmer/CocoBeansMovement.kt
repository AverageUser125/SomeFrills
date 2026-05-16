package com.somefrills.features.farming.autofarmer

/**
 * Coco beans pattern farming.
 * Cycles: Forward → Right → Backward → Right → Forward (loop)
 */
class CocoBeansMovement : MovementStrategy() {
    private var state: FarmingState = FarmingState.FORWARD

    override fun getState(): MovementState {
        return state.toMovementState()
    }

    override fun nextState() {
        state = state.next() as FarmingState
    }

    private enum class FarmingState : State {
        FORWARD {
            override fun next() = RIGHT
            override fun toMovementState() = MovementState(MovementState.FORWARD)
        },
        RIGHT {
            override fun next() = BACKWARD
            override fun toMovementState() = MovementState(MovementState.RIGHT)
        },
        BACKWARD {
            override fun next() = RIGHT_2
            override fun toMovementState() = MovementState(MovementState.BACKWARD)
        },
        RIGHT_2 {
            override fun next() = FORWARD
            override fun toMovementState() = MovementState(MovementState.RIGHT)
        };
    }
}
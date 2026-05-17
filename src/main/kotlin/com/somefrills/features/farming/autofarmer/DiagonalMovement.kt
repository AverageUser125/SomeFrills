package com.somefrills.features.farming.autofarmer

/**
 * Diagonal pattern farming (sugar cane, sunflower, moonflower, rose).
 * Cycles through: Backward+Left → Forward → repeat
 */
class DiagonalMovement : MovementStrategy() {

    private var state: FarmingState = FarmingState.BACKWARD

    override fun getState(): MovementState {
        return state.toMovementState()
    }

    override fun nextState() {
        state = state.next() as FarmingState
    }

    private enum class FarmingState : State {

        BACKWARD {
            override fun next() = LEFT

            override fun toMovementState(): MovementState {
                return MovementState(
                    MovementState.BACKWARD or MovementState.LEFT
                )
            }
        },

        LEFT {
            override fun next() = BACKWARD

            override fun toMovementState(): MovementState {
                return MovementState(MovementState.FORWARD)
            }
        };
    }
}
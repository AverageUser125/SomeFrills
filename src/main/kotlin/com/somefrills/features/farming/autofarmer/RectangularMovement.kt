package com.somefrills.features.farming.autofarmer

/**
 * Rectangular pattern farming (melon, pumpkin, wheat, carrot, potato, nether wart).
 * Cycles through: Forward+Right → Forward+Left → repeat
 */
class RectangularMovement : MovementStrategy() {

    private var state: FarmingState = FarmingState.RIGHT

    override fun getState(): MovementState {
        return state.toMovementState()
    }

    override fun nextState() {
        state = state.next() as FarmingState
    }

    private enum class FarmingState : State {
        RIGHT {
            override fun next() = LEFT

            override fun toMovementState(): MovementState {
                return MovementState(
                    MovementState.FORWARD or MovementState.RIGHT
                )
            }
        },

        LEFT {
            override fun next() = RIGHT

            override fun toMovementState(): MovementState {
                return MovementState(
                    MovementState.FORWARD or MovementState.LEFT
                )
            }
        };
    }
}
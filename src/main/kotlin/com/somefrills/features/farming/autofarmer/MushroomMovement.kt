package com.somefrills.features.farming.autofarmer

/**
 * Mushroom pattern farming - always moving forward with lateral adjustments.
 * Cycles: Forward+Right → Forward only → Forward+Left → repeat
 */
class MushroomMovement : MovementStrategy() {

    private var state: FarmingState = FarmingState.FORWARD_RIGHT

    override fun getState(): MovementState {
        return state.toMovementState()
    }

    override fun nextState() {
        state = state.next() as FarmingState
    }

    private enum class FarmingState : State {

        FORWARD_RIGHT {
            override fun next() = FORWARD_ONLY

            override fun toMovementState(): MovementState {
                return MovementState(
                    MovementState.FORWARD or MovementState.RIGHT
                )
            }
        },

        FORWARD_ONLY {
            override fun next() = FORWARD_LEFT

            override fun toMovementState(): MovementState {
                return MovementState(MovementState.FORWARD)
            }
        },

        FORWARD_LEFT {
            override fun next() = FORWARD_RIGHT

            override fun toMovementState(): MovementState {
                return MovementState(
                    MovementState.FORWARD or MovementState.LEFT
                )
            }
        }
    }
}
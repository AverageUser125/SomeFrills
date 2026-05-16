package com.somefrills.features.farming.autofarmer

/**
 * Interface for movement strategies in auto farming.
 * Each strategy manages its own internal state and cycles through movement patterns.
 */
abstract class MovementStrategy {
    abstract fun getState(): MovementState

    /**
     * Advance to the next state in the movement pattern.
     */
    abstract fun nextState()

    /**
     * Common interface for internal state enums in strategies.
     * Allows strategies to have custom logic for state transitions and movement outputs.
     */
    interface State {
        /**
         * Get the next state in the cycle.
         */
        fun next(): State

        /**
         * Convert this state to a movement state.
         */
        fun toMovementState(): MovementState
    }
}


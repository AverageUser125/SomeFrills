package com.somefrills.features.farming.autofarmer;

import org.jspecify.annotations.NonNull;

/**
 * Interface for movement strategies in auto farming.
 * Each strategy manages its own internal state and cycles through movement patterns.
 */
public interface MovementStrategy {
    /**
     * Get the current movement state.
     */
    @NonNull
    MovementState getCurrentState();

    /**
     * Advance to the next state in the movement pattern.
     */
    void nextState();

    /**
     * Common interface for internal state enums in strategies.
     * Allows strategies to have custom logic for state transitions and movement outputs.
     */
    interface State {
        /**
         * Get the next state in the cycle.
         */
        State next();

        /**
         * Convert this state to a movement state.
         */
        MovementState toMovementState();
    }
}


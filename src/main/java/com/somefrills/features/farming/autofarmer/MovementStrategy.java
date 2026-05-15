package com.somefrills.features.farming.autofarmer;

/**
 * Interface for movement strategies in auto farming.
 * Each strategy manages its own internal state and cycles through movement patterns.
 */
public interface MovementStrategy {
    /**
     * Get the current movement state.
     */
    MovementState getCurrentState();

    /**
     * Advance to the next state in the movement pattern.
     */
    void nextState();
}


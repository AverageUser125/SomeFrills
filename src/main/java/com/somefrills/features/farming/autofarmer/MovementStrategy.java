package com.somefrills.features.farming.autofarmer;

/**
 * Defines the behavior for a movement strategy in auto farming.
 * Each strategy determines which keys to press on each game tick.
 */
public interface MovementStrategy {
    /**
     * Called once per game tick to determine which keys should be held.
     *
     * @return a MovementInput representing which keys to press
     */
    MovementInput getMovement();

    /**
     * Called when the feature is disabled to reset state.
     */
    void reset();

    /**
     * Represents the input state for a single tick.
     */
    class MovementInput {
        public boolean attack;      // Always true for farming
        public boolean forward;
        public boolean backward;
        public boolean left;
        public boolean right;

        public MovementInput(boolean forward, boolean backward, boolean left, boolean right) {
            this.attack = true; // Always attack
            this.forward = forward;
            this.backward = backward;
            this.left = left;
            this.right = right;
        }

        public static MovementInput noMovement() {
            return new MovementInput(false, false, false, false);
        }

        public static MovementInput forwardOnly() {
            return new MovementInput(true, false, false, false);
        }
    }
}


package com.somefrills.features.farming.autofarmer;

/**
 * Represents a farming state with associated movement input.
 * All farming states have attack always pressed.
 */
public class MovementState {
    public boolean forward;
    public boolean backward;
    public boolean left;
    public boolean right;

    public MovementState(boolean forward, boolean backward, boolean left, boolean right) {
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
    }

    public static MovementState noMovement() {
        return new MovementState(false, false, false, false);
    }

    public static MovementState forwardOnly() {
        return new MovementState(true, false, false, false);
    }
}


package com.somefrills.features.farming.autofarmer;

/**
 * Represents a farming state with associated movement input using bitwise flags.
 * All farming states have attack always pressed.
 */
public class MovementState {
    // Direction flags using bitwise operations
    public static final int NOT_ATTACK = 1;
    public static final int SPRINT = 1 << 1;
    public static final int FORWARD = 1 << 2;
    public static final int BACKWARD = 1 << 3;
    public static final int LEFT = 1 << 4;
    public static final int RIGHT = 1 << 5;
    public int flags;

    public MovementState() {
        flags = 0;
    }

    public MovementState(int flags) {
        this.flags = flags;
    }

    public boolean isForward() {
        return (flags & FORWARD) != 0;
    }

    public boolean isBackward() {
        return (flags & BACKWARD) != 0;
    }

    public boolean isLeft() {
        return (flags & LEFT) != 0;
    }

    public boolean isRight() {
        return (flags & RIGHT) != 0;
    }

    public boolean isSprinting() {
        return (flags & SPRINT) != 0;
    }

    public boolean isAttacking() {
        return (flags & NOT_ATTACK) == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MovementState other)) return false;
        return this.flags == other.flags;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(flags);
    }
}
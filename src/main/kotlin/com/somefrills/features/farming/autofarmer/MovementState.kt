package com.somefrills.features.farming.autofarmer

/**
 * Represents a farming state with associated movement input using bitwise flags.
 * All farming states have attack always pressed.
 */
class MovementState {
    var flags: Int

    constructor() {
        flags = 0
    }

    constructor(flags: Int) {
        this.flags = flags
    }

    val isForward: Boolean
        get() = (flags and FORWARD) != 0

    val isBackward: Boolean
        get() = (flags and BACKWARD) != 0

    val isLeft: Boolean
        get() = (flags and LEFT) != 0

    val isRight: Boolean
        get() = (flags and RIGHT) != 0

    val isSprinting: Boolean
        get() = (flags and SPRINT) != 0

    val isAttacking: Boolean
        get() = (flags and NOT_ATTACK) == 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MovementState) return false
        return this.flags == other.flags
    }

    override fun hashCode(): Int {
        return Integer.hashCode(flags)
    }

    companion object {
        // Direction flags using bitwise operations
        const val NOT_ATTACK = 1
        const val SPRINT = 1 shl 1
        const val FORWARD = 1 shl 2
        const val BACKWARD = 1 shl 3
        const val LEFT = 1 shl 4
        const val RIGHT = 1 shl 5
    }
}
package com.somefrills.utils

import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import kotlin.math.abs

object MathUtils {
    fun isNearlyEqual(a: Double, b: Double, eps: Double = 1e-9): Boolean {
        return abs(a - b) < eps
    }

    fun isNearlyEqual(a: Float, b: Float, eps: Float = 1e-5f): Boolean {
        return abs(a - b) < eps
    }

    fun setVector(vec: Vector3d, v: Vec3): Vector3d {
        vec.x = v.x
        vec.y = v.y
        vec.z = v.z
        return vec
    }
}

// ========== Double Extension Functions ==========

fun Double.isNearlyEqual(other: Double, eps: Double = 1e-9): Boolean {
    return abs(this - other) < eps
}

// ========== Float Extension Functions ==========

fun Float.isNearlyEqual(other: Float, eps: Float = 1e-5f): Boolean {
    return abs(this - other) < eps
}

// ========== Vector3d Extension Functions ==========

fun Vector3d.set(v: Vec3): Vector3d {
    return MathUtils.setVector(this, v)
}

fun Vector3d.isNearlyEqual(other: Vector3d, eps: Double = 1e-9): Boolean {
    return x.isNearlyEqual(other.x, eps) && y.isNearlyEqual(other.y, eps) && z.isNearlyEqual(other.z, eps)
}

// ========== Vec3 Extension Functions ==========

fun Vec3.isNearlyEqual(other: Vec3, eps: Double = 1e-9): Boolean {
    return x.isNearlyEqual(other.x, eps) && y.isNearlyEqual(other.y, eps) && z.isNearlyEqual(other.z, eps)
}

fun Vec3.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

// ========== Vector3d Operator Extensions ==========

operator fun Vector3d.plus(other: Vec3): Vector3d {
    return Vector3d(x + other.x, y + other.y, z + other.z)
}

operator fun Vector3d.minus(other: Vec3): Vector3d {
    return Vector3d(x - other.x, y - other.y, z - other.z)
}

operator fun Vector3d.times(scalar: Double): Vector3d {
    return Vector3d(x * scalar, y * scalar, z * scalar)
}

operator fun Vector3d.times(scalar: Float): Vector3d {
    return Vector3d(x * scalar, y * scalar, z * scalar)
}

fun Vector3d.dot(other: Vector3d): Double {
    return x * other.x + y * other.y + z * other.z
}

fun Vector3d.cross(other: Vector3d): Vector3d {
    return Vector3d(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
}


package com.somefrills.utils

import java.util.*
import kotlin.math.abs
import kotlin.math.floor

object NumberUtils {
    private val SUFFIXES = arrayOf("k", "m", "b", "t")

    fun parseCompact(input: String): Int {
        if (input.isBlank()) return 0
        val s = input.trim().lowercase(Locale.ROOT).replace(",", "")
        for (i in SUFFIXES.indices) {
            val suffix = SUFFIXES[i]
            if (s.endsWith(suffix)) {
                val numberPart = s.substring(0, s.length - suffix.length).trim()
                val value = numberPart.toDouble()
                val multiplier = Math.pow(1000.0, (i + 1).toDouble())
                return (value * multiplier).toInt()
            }
        }
        return s.toInt()
    }

    fun ticksToTimeString(ticks: Long): String {
        if (ticks < 20) return "0s"
        val builder = StringBuilder()
        var current = ticks
        val units = arrayOf("h", "m", "s")
        val durations = intArrayOf(72000, 1200, 20)
        for (i in 0..2) {
            var amount = 0
            while (current >= durations[i]) {
                amount++
                current -= durations[i]
            }
            if (amount > 0) {
                builder.append(amount).append(units[i])
            }
        }
        return builder.toString()
    }

    @JvmStatic
    fun formatCompact(num: Int): String {
        if (num < 1000) return toString()
        var value = num.toDouble()
        var suffixIndex = -1
        while (value >= 1000 && suffixIndex < 3) {
            value /= 1000.0
            suffixIndex++
        }
        val format = if (value >= 10 || value == floor(value)) "%.0f%s" else "%.1f%s"
        return String.format(format, value, arrayOf("k", "m", "b", "t")[suffixIndex])
    }

    @JvmStatic
    fun formatCompact(num: Double): String {
        if (num < 1000) {
            return if (num == floor(num)) String.format("%.0f", num) else String.format("%.1f", num)
        }
        var value = num
        var suffixIndex = -1
        while (value >= 1000 && suffixIndex < 3) {
            value /= 1000.0
            suffixIndex++
        }
        val format = if (value >= 10 || value == floor(value)) "%.0f%s" else "%.1f%s"
        return String.format(format, value, arrayOf("k", "m", "b", "t")[suffixIndex])
    }
}

// ========== Int Extension Functions ==========

fun Int.formatCompact(): String {
    return NumberUtils.formatCompact(this)
}

fun Int.difference(other: Int): Int = abs(abs(this) - abs(other))

// ========== Double Extension Functions ==========

fun Double.formatCompact(): String {
    return NumberUtils.formatCompact(this)
}

fun Double.formatSeparator(): String {
    return String.format(Locale.ENGLISH, "%,.1f", this)
}

// ========== Float Extension Functions ==========

fun Float.formatCompact(): String = toDouble().formatCompact()


fun Float.formatSeparator(): String = toDouble().formatSeparator()

// ========== Long Extension Functions ==========

fun Long.formatSeparator(): String {
    return String.format(Locale.ENGLISH, "%,d", this)
}

fun Long.ticksToTime(): String = NumberUtils.ticksToTimeString(this)



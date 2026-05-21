package com.somefrills.utils
import at.hannibal2.skyhanni.utils.compat.value
import com.google.common.base.Splitter
import com.somefrills.misc.RenderColor
import com.somefrills.misc.RenderColor.Companion.fromFormatting
import com.somefrills.misc.RenderColor.Companion.fromHex
import net.minecraft.text.PlainTextContent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Formatting
import org.apache.commons.lang3.StringUtils
import java.text.DecimalFormat
import java.util.*
import java.util.function.Predicate
import kotlin.text.isNotEmpty
import kotlin.text.startsWith
import kotlin.text.substring


object TextUtils {
    fun format(string: String, vararg values: Any?): String {
        val builder = StringBuilder()
        for ((index, section) in Splitter.on("{}").split(string).withIndex()) {
            builder.append(section)
            if (index < values.size) {
                builder.append(values[index])
            }
        }
        return builder.toString()
    }

    fun colorToString(hex: Int): String {
        for (f in Formatting.entries) {
            if (f.isColor && f.colorValue == hex) {
                return StringUtils.capitalize(f.name)
            }
        }
        return String.format("#%06X", hex)
    }

    @JvmStatic
    fun capitalizeType(type: String): String {
        if (type.isEmpty()) return type
        val parts = type.split("_")
        return parts.joinToString(" ") { part ->
            if (part.isNotEmpty()) part[0].uppercase() + part.substring(1) else ""
        }
    }

    fun parseRenderColor(colorStr: String): RenderColor? {
        if (colorStr.isEmpty()) {
            return null
        }

        // Try hex format
        var hexStr: String = colorStr
        if (hexStr.startsWith("#")) {
            hexStr = hexStr.substring(1)
        }
        if (hexStr.length == 6) {
            try {
                val hex = hexStr.toInt(16)
                return fromHex(hex)
            } catch (ignored: NumberFormatException) {
            }
        }

        // Try formatting color
        val formatting = colorStr.parseColor()
        if (formatting != null) {
            return fromFormatting(formatting)
        }

        // Try RGB format (space-separated)
        val parts: Array<String> = colorStr.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size == 3) {
            try {
                val r = parts[0].toInt()
                val g = parts[1].toInt()
                val b = parts[2].toInt()
                if (r in 0..255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                    return RenderColor(r, g, b, 255)
                }
            } catch (ignored: NumberFormatException) {
            }
        }

        return null
    }

    fun colorToString(color: RenderColor): String {
        return colorToString(color.hex)
    }

    @JvmStatic
    fun toPlain(content: Text): String {
        return content.toPlain()
    }

    @JvmStatic
    fun stripPrefix(s1: String, s2: String): String {
        return if (s1.startsWith(s2)) s1.substring(s2.length) else s1
    }
}

// ========== String Extension Functions ==========

fun String.humanize(): String {
    if (isEmpty()) return ""
    val withSpaces = replace('_', ' ').replace('-', ' ')
    val out = StringBuilder()
    var prev = ' '
    for (i in withSpaces.indices) {
        val c = withSpaces[i]
        if (i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(prev) || Character.isDigit(prev))) {
            out.append(' ')
        }
        out.append(c)
        prev = c
    }
    val res = out.toString().trim()
    return if (res.isEmpty()) res else Character.toUpperCase(res[0]) + res.substring(1)
}

fun String.toLower(): String = lowercase(Locale.ROOT)

fun String.toUpper(): String = uppercase(Locale.ROOT)

fun String.toID(): String = toUpper().replace("'s", "").replace(" ", "_")

fun String.parseColor(): Formatting? {
    val normalized = lowercase(Locale.ROOT).replace(" ", "_")
    return when (normalized) {
        "black" -> Formatting.BLACK
        "dark_blue", "navy" -> Formatting.DARK_BLUE
        "dark_green" -> Formatting.DARK_GREEN
        "dark_aqua", "cyan" -> Formatting.DARK_AQUA
        "dark_red" -> Formatting.DARK_RED
        "dark_purple", "purple" -> Formatting.DARK_PURPLE
        "gold", "orange" -> Formatting.GOLD
        "gray", "grey" -> Formatting.GRAY
        "dark_gray", "dark_grey" -> Formatting.DARK_GRAY
        "blue" -> Formatting.BLUE
        "green", "lime" -> Formatting.GREEN
        "aqua", "light_aqua" -> Formatting.AQUA
        "red" -> Formatting.RED
        "light_purple", "pink" -> Formatting.LIGHT_PURPLE
        "yellow" -> Formatting.YELLOW
        "white" -> Formatting.WHITE
        else -> try {
            val f = Formatting.valueOf(uppercase(Locale.ROOT))
            if (f.isColor) f else null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

private val textColorLUT = Formatting.entries
    .mapNotNull { formatting -> formatting.colorValue?.let { it to formatting } }
    .toMap()

fun Text?.formattedTextCompatLessResets(): String = this.formattedTextCompat(noExtraResets = true)
fun Text?.formattedTextCompatLeadingWhite(): String = this.formattedTextCompat(leadingWhite = true)
fun Text?.formattedTextCompatLeadingWhiteLessResets(): String =
    this.formattedTextCompat(noExtraResets = true, leadingWhite = true)

@JvmOverloads
@Suppress("unused")
fun Text?.formattedTextCompat(noExtraResets: Boolean = false, leadingWhite: Boolean = false): String {
    this ?: return ""
    return computeFormattedTextCompat(noExtraResets, leadingWhite)
}

private fun Text?.computeFormattedTextCompat(noExtraResets: Boolean, leadingWhite: Boolean): String {
    this ?: return ""
    val sb = StringBuilder(50)
    var wasFormatted = false
    for (component in iterator()) {
        val chatStyle = component.style.chatStyle()
        if (chatStyle.isNotEmpty() && (leadingWhite || (wasFormatted && (sb.length != 2 || sb[0] != '§' || sb[1] != 'r')) || chatStyle != "§f")) {
            sb.append(chatStyle)
            wasFormatted = true
        }
        sb.append(component.unformattedTextForChatCompat())
        if (!noExtraResets) {
            sb.append("§r")
            wasFormatted = true
        } else if (component == Text.empty()) {
            sb.append("§r")
            wasFormatted = true
        }
    }
    return sb.removeSuffix("§r").removePrefix("§r").toString()
}

fun Text.unformattedTextForChatCompat(): String {
    return computeUnformattedTextCompat()
}

private fun Text.computeUnformattedTextCompat(): String {
    if (this.content is TranslatableTextContent) {
        return this.string
    }
    return (this.content as? PlainTextContent)?.string().orEmpty()
}
fun Text.iterator(): Sequence<Text> {
    return sequenceOf(this) + siblings.asSequence().flatMap { it.iterator() } // TODO: in theory we want to properly inherit styles here
}

fun Style.chatStyle() = buildString {
    color?.let { append(it.toChatFormatting()?.toString() ?: "<${it.hexCode}>") }
    if (isBold) append("§l")
    if (isItalic) append("§o")
    if (isUnderlined) append("§n")
    if (isStrikethrough) append("§m")
    if (isObfuscated) append("§k")
}

fun TextColor.toChatFormatting(): Formatting? {
    return textColorLUT[this.rgb]
}

fun String.stripPrefix(prefix: String): String =
    TextUtils.stripPrefix(this, prefix)

fun String.uppercaseFirst(replaceUnderscores: Boolean = false): String {
    val parts = if (replaceUnderscores) replace("_", " ").split(Regex("\\s"))
    else split(Regex("\\s"))

    return parts
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word ->
            if (word.isNotEmpty()) word[0].uppercase() + word.substring(1) else ""
        }
        .trim()
}

fun String.wrapByDelimiter(maxLen: Int, delimiter: String): String {
    if (isEmpty()) return this
    if (maxLen <= 0) throw IllegalArgumentException("maxLen must be > 0")

    val parts = split(Regex("\\s*${Regex.escape(delimiter)}\\s*"))
    val result = StringBuilder()
    var lineLen = 0
    val sep = "$delimiter "

    for (part in parts) {
        if (part.isEmpty()) continue
        when {
            lineLen == 0 -> {
                result.append(part)
                lineLen = part.length
            }
            lineLen + sep.length + part.length <= maxLen -> {
                result.append(sep).append(part)
                lineLen += sep.length + part.length
            }
            else -> {
                result.append("\n").append(part)
                lineLen = part.length
            }
        }
    }
    return result.toString()
}

fun String.capitalize(): String = StringUtils.capitalize(this)

fun String.parseRoman(): Int {
    var result = 0
    for (i in indices) {
        val number = romanToInt(this[i])
        if (number == 0) return 0

        if (i != length - 1) {
            val nextNumber = romanToInt(this[i + 1])
            result += if (number < nextNumber) -number else number
        } else {
            result += number
        }
    }
    return result
}

private fun romanToInt(roman: Char): Int = when (Character.toUpperCase(roman)) {
    'I' -> 1
    'V' -> 5
    'X' -> 10
    'L' -> 50
    'C' -> 100
    'D' -> 500
    'M' -> 1000
    else -> 0
}

// ========== Text Extension Functions ==========

fun Text.toPlain(): String = Formatting.strip(string)!!

fun Text.getStyle(predicate: Predicate<String>): Optional<Style> {
    return visit({ textStyle, textString ->
        if (predicate.test(textString)) Optional.of(textStyle) else Optional.empty()
    }, Style.EMPTY)
}

// ========== Style Extension Functions ==========

fun Style.hasColor(color: Formatting): Boolean {
    return color.colorValue != null && hasColor(color.colorValue!!)
}

fun Style.hasColor(hex: Int): Boolean {
    return color != null && color!!.rgb == hex
}

// ========== Double & Float Extension Functions ==========

fun Double.formatDecimal(places: Int = 2): String {
    return DecimalFormat("0." + "0".repeat(places)).format(this)
}

fun Float.formatDecimal(places: Int = 2): String = toDouble().formatDecimal(places)

// ========== Int Extension Functions ==========

fun Int.colorToString(): String = TextUtils.colorToString(this)

// ========== Calendar Extension Functions ==========

fun Calendar.toDateString(): String {
    return TextUtils.format("{} {}",
        getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, Locale.getDefault()).format(time)
    )
}


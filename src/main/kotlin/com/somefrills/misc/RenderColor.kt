package com.somefrills.misc

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.util.Formatting
import net.minecraft.util.math.ColorHelper
import java.io.IOException
import kotlin.math.sqrt

class RenderColor {
    @JvmField
    var r: Float = 0f

    @JvmField
    var g: Float = 0f

    @JvmField
    var b: Float = 0f

    @JvmField
    var a: Float = 0f

    @JvmField
    var hex: Int = 0

    @JvmField
    var argb: Int = 0

    constructor(r: Int, g: Int, b: Int, a: Int) {
        this.r = Math.clamp(r.toLong(), 0, 255).toFloat() / 255
        this.g = Math.clamp(g.toLong(), 0, 255).toFloat() / 255
        this.b = Math.clamp(b.toLong(), 0, 255).toFloat() / 255
        this.a = Math.clamp(a.toLong(), 0, 255).toFloat() / 255
        this.hex = (Math.clamp(r.toLong(), 0, 255) shl 16) + (Math.clamp(g.toLong(), 0, 255) shl 8) + Math.clamp(
            b.toLong(),
            0,
            255
        )
        this.argb = ColorHelper.getArgb(
            Math.clamp(a.toLong(), 0, 255),
            Math.clamp(r.toLong(), 0, 255),
            Math.clamp(g.toLong(), 0, 255),
            Math.clamp(b.toLong(), 0, 255)
        )
    }

    constructor(r: Float, g: Float, b: Float, a: Float) {
        this.r = Math.clamp(r, 0.0f, 1.0f)
        this.g = Math.clamp(g, 0.0f, 1.0f)
        this.b = Math.clamp(b, 0.0f, 1.0f)
        this.a = Math.clamp(a, 0.0f, 1.0f)
        // compute integer components and hex/argb in a consistent manner
        val ri = (this.r * 255f).toInt()
        val gi = (this.g * 255f).toInt()
        val bi = (this.b * 255f).toInt()
        val ai = (this.a * 255f).toInt()
        this.hex = (ri shl 16) or (gi shl 8) or bi // RGB hex
        this.argb = ColorHelper.getArgb(ai, ri, gi, bi)
    }

    constructor(color: RenderColor) {
        set(color)
    }

    fun withRed(red: Float): RenderColor {
        return RenderColor(red, this.g, this.b, this.a)
    }

    fun withGreen(green: Float): RenderColor {
        return RenderColor(this.r, green, this.b, this.a)
    }

    fun withBlue(blue: Float): RenderColor {
        return RenderColor(this.r, this.g, blue, this.a)
    }

    fun withAlpha(alpha: Float): RenderColor {
        return RenderColor(this.r, this.g, this.b, alpha)
    }

    fun distance(formatColor: RenderColor): Float {
        val dr = this.r - formatColor.r
        val dg = this.g - formatColor.g
        val db = this.b - formatColor.b
        return sqrt((dr * dr + dg * dg + db * db).toDouble()).toFloat()
    }

    fun set(color: RenderColor) {
        this.r = color.r
        this.g = color.g
        this.b = color.b
        this.a = color.a
        this.hex = color.hex
        this.argb = color.argb
    }

    class RenderColorTypeAdapter : TypeAdapter<RenderColor?>() {
        @Throws(IOException::class)
        override fun write(out: JsonWriter?, value: RenderColor?) {
            if (value == null) {
                out!!.value(-1)
                return
            }
            out!!.value(value.argb.toLong())
        }

        @Throws(IOException::class)
        override fun read(`in`: JsonReader): RenderColor {
            val v = `in`.nextInt()
            if (v == -1) return white
            return fromArgb(v)
        }
    }

    companion object {
        @JvmField
        val white: RenderColor = fromHex(0xffffff)
        val green: RenderColor = fromHex(0x55ff55)
        val red: RenderColor = fromHex(0xff5555)
        val black: RenderColor = fromHex(0x000000)

        @JvmStatic
        fun fromHex(hex: Int): RenderColor {
            return RenderColor((hex shr 16) and 0xFF, (hex shr 8) and 0xFF, hex and 0xFF, 255)
        }

        fun fromArgb(hex: Int): RenderColor {
            return RenderColor((hex shr 16) and 0xFF, (hex shr 8) and 0xFF, hex and 0xFF, (hex shr 24) and 0xFF)
        }

        fun fromHex(hex: Int, alpha: Float): RenderColor {
            return RenderColor((hex shr 16) and 0xFF, (hex shr 8) and 0xFF, hex and 0xFF, (255 * alpha).toInt())
        }

        fun fromFloat(r: Float, g: Float, b: Float, a: Float): RenderColor {
            return RenderColor(r, g, b, a)
        }

        fun ofArgb(argb: Int): RenderColor {
            return RenderColor(
                ((argb shr 16) and 0xFF) / 255.0f,
                ((argb shr 8) and 0xFF) / 255.0f,
                (argb and 0xFF) / 255.0f,
                ((argb shr 24) and 0xFF) / 255.0f
            )
        }

        @JvmStatic
        fun fromChroma(colour: ChromaColour?): RenderColor {
            if (colour == null) {
                return white
            }
            return ofArgb(colour.getEffectiveColourRGB()).withAlpha(colour.alpha / 255.0f)
        }

        @JvmStatic
        fun fromFormatting(formatting: Formatting): RenderColor {
            if (formatting == null) {
                return white
            }
            val colorValue = formatting.colorValue
            if (colorValue == null || colorValue == -1) {
                return white
            }
            return fromHex(colorValue)
        }
    }
}
package com.somefrills.misc;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

import java.io.IOException;

public class RenderColor {
    public static final RenderColor white = RenderColor.fromHex(0xffffff);
    public static final RenderColor green = RenderColor.fromHex(0x55ff55);
    public static final RenderColor red = RenderColor.fromHex(0xff5555);
    public static final RenderColor black = RenderColor.fromHex(0x000000);

    public float r;
    public float g;
    public float b;
    public float a;
    public int hex;
    public int argb;

    public RenderColor(int r, int g, int b, int a) {
        this.r = (float) Math.clamp(r, 0, 255) / 255;
        this.g = (float) Math.clamp(g, 0, 255) / 255;
        this.b = (float) Math.clamp(b, 0, 255) / 255;
        this.a = (float) Math.clamp(a, 0, 255) / 255;
        this.hex = (Math.clamp(r, 0, 255) << 16) + (Math.clamp(g, 0, 255) << 8) + Math.clamp(b, 0, 255);
        this.argb = ColorHelper.getArgb(Math.clamp(a, 0, 255), Math.clamp(r, 0, 255), Math.clamp(g, 0, 255), Math.clamp(b, 0, 255));
    }

    public RenderColor(float r, float g, float b, float a) {
        this.r = Math.clamp(r, 0.0f, 1.0f);
        this.g = Math.clamp(g, 0.0f, 1.0f);
        this.b = Math.clamp(b, 0.0f, 1.0f);
        this.a = Math.clamp(a, 0.0f, 1.0f);
        // compute integer components and hex/argb in a consistent manner
        int ri = (int) (this.r * 255f);
        int gi = (int) (this.g * 255f);
        int bi = (int) (this.b * 255f);
        int ai = (int) (this.a * 255f);
        this.hex = (ri << 16) | (gi << 8) | bi; // RGB hex
        this.argb = ColorHelper.getArgb(ai, ri, gi, bi);
    }

    public static RenderColor fromHex(int hex) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, 255);
    }

    public static RenderColor fromArgb(int hex) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, (hex >> 24) & 0xFF);
    }

    public static RenderColor fromHex(int hex, float alpha) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, (int) (255 * alpha));
    }

    public static RenderColor fromFloat(float r, float g, float b, float a) {
        return new RenderColor(r, g, b, a);
    }

    public static RenderColor ofArgb(int argb) {
        return new RenderColor(
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f,
                ((argb >> 24) & 0xFF) / 255.0f
        );
    }

    public static RenderColor fromChroma(ChromaColour colour) {
        if (colour == null) {
            return RenderColor.white;
        }
        return RenderColor.ofArgb(colour.getEffectiveColourRGB()).withAlpha(colour.getAlpha() / 255.0f);
    }

    public static RenderColor fromFormatting(Formatting formatting) {
        if (formatting == null) {
            return RenderColor.white;
        }
        Integer colorValue = formatting.getColorValue();
        if (colorValue == null || colorValue == -1) {
            return RenderColor.white;
        }
        return RenderColor.fromHex(colorValue);
    }

    public RenderColor withRed(float red) {
        return new RenderColor(red, this.g, this.b, this.a);
    }

    public RenderColor withGreen(float green) {
        return new RenderColor(this.r, green, this.b, this.a);
    }

    public RenderColor withBlue(float blue) {
        return new RenderColor(this.r, this.g, blue, this.a);
    }

    public RenderColor withAlpha(float alpha) {
        return new RenderColor(this.r, this.g, this.b, alpha);
    }

    public float distance(RenderColor formatColor) {
        float dr = this.r - formatColor.r;
        float dg = this.g - formatColor.g;
        float db = this.b - formatColor.b;
        return (float) Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public void set(RenderColor color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.hex = color.hex;
        this.argb = color.argb;
    }

    public static class RenderColorTypeAdapter extends TypeAdapter<RenderColor> {
        @Override
        public void write(JsonWriter out, RenderColor value) throws IOException {
            out.value(value.argb);
        }

        @Override
        public RenderColor read(JsonReader in) throws IOException {
            int v = in.nextInt();
            if (v == -1) return RenderColor.white;
            return RenderColor.fromArgb(v);
        }
    }
}
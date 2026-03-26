package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Rendering;
import net.minecraft.client.MinecraftClient;
import com.somefrills.misc.Utils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import java.util.function.Consumer;

public class ColorSquare extends ClickableWidget implements IWidget {

    private float hue;
    private float selectionSaturation = 1.0f;
    private float selectionValue = 1.0f;

    private boolean dragging = false;

    private NativeImage image;
    private NativeImageBackedTexture texture;
    private Identifier textureId;
    private boolean dirty = true;
    private Consumer<Integer> selectionListener = null;
    private java.util.function.Consumer<RenderColor> selectionColorListener = null;

    public ColorSquare(int x, int y, int width, int height, RenderColor startColor) {
        super(x, y, width, height, Text.empty());
        float[] hsv = rgbToHsv(startColor.r, startColor.g, startColor.b);
        this.hue = hsv[0];
        this.selectionSaturation = hsv[1];
        this.selectionValue = hsv[2];
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        if (dirty || texture == null) rebuildTexture(w, h);

        int x = getX();
        int y = getY();

        // Draw cached SV square
        context.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0, 0, w, h, w, h);
        Rendering.drawBorder(context, x, y, w, h, 0xFF000000);

        // Draw selection marker (adapts to vertical/horizontal)
        int selX, selY;
        if (w >= h) { // horizontal
            selX = x + Math.round(selectionSaturation * (w - 1));
            selY = y + Math.round((1.0f - selectionValue) * (h - 1));
        } else { // vertical
            selX = x + Math.round(selectionValue * (w - 1));
            selY = y + Math.round((1.0f - selectionSaturation) * (h - 1));
        }

        selX = Math.max(x, Math.min(x + w - 1, selX));
        selY = Math.max(y, Math.min(y + h - 1, selY));

        context.fill(selX - 3, selY - 3, selX + 4, selY + 4, 0xFF000000);
        context.fill(selX - 2, selY - 2, selX + 3, selY + 3, 0xFFFFFFFF);
        context.fill(selX - 1, selY - 1, selX + 2, selY + 2, 0xFF000000);
    }

    @Override
    public boolean mouseClicked(Click event, boolean bl) {
        try {
            var pos = Utils.getMousePos();
            if (!this.isMouseOver(pos.x, pos.y)) return false;
            dragging = true;
            updateSelection(pos.x, pos.y);
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click event, double dx, double dy) {
        if (!dragging) return false;
        try {
            var pos = Utils.getMousePos();
            updateSelection(pos.x, pos.y);
        } catch (Throwable ignored) {
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click event) {
        dragging = false;
        return true;
    }

    private void updateSelection(double mouseX, double mouseY) {
        int x = getX();
        int y = getY();
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        float denomW = Math.max(1, w - 1);
        float denomH = Math.max(1, h - 1);
        float s = (float) ((mouseX - x) / denomW);
        float v = 1.0f - (float) ((mouseY - y) / denomH);

        if (w < h) { // vertical
            float temp = s;
            s = v;
            v = temp;
        }

        s = Math.max(0f, Math.min(1f, s));
        v = Math.max(0f, Math.min(1f, v));

        setSelection(s, v);
        int selected = getSelectedColor();
        if (selectionListener != null) selectionListener.accept(selected);
        if (selectionColorListener != null) selectionColorListener.accept(RenderColor.fromArgb(selected));
    }

    public void setHuePercentage(float hue) {
        this.hue = hue - (float) Math.floor(hue);
        dirty = true;
    }

    public float getHue() { return hue; }

    public void setSelection(float saturation, float value) {
        this.selectionSaturation = Math.max(0f, Math.min(1f, saturation));
        this.selectionValue = Math.max(0f, Math.min(1f, value));
        dirty = true;
    }

    public float getSelectionSaturation() { return selectionSaturation; }
    public float getSelectionValue() { return selectionValue; }

    public int getSelectedColor() {
        return hsvToRgb(hue, selectionSaturation, selectionValue);
    }

    public void setSelectionListener(Consumer<Integer> listener) {
        this.selectionListener = listener;
    }

    public void setSelectionColorListener(java.util.function.Consumer<RenderColor> listener) {
        this.selectionColorListener = listener;
    }

    // =========================
    // Manual HSV <-> RGB
    // =========================

    public static int hsvToRgb(float h, float s, float v) {
        h = h - (float)Math.floor(h);
        float r, g, b;
        float i = (float)Math.floor(h * 6f);
        float f = h * 6f - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        switch ((int)i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            case 5 -> { r = v; g = p; b = q; }
            default -> r = g = b = 0;
        }

        int ri = (int)(r * 255f);
        int gi = (int)(g * 255f);
        int bi = (int)(b * 255f);
        return (0xFF << 24) | (ri << 16) | (gi << 8) | bi;
    }

    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float v = max;
        float delta = max - min;
        float s = (max == 0f) ? 0f : (delta / max);
        float hDeg;

        if (delta == 0f) {
            hDeg = 0f; // undefined hue for greys, return 0
        } else if (max == r) {
            float hh = (g - b) / delta;
            if (hh < 0f) hh += 6f;
            hDeg = hh * 60f;
        } else if (max == g) {
            hDeg = ((b - r) / delta + 2f) * 60f;
        } else {
            hDeg = ((r - g) / delta + 4f) * 60f;
        }
        // normalize to 0..1
        float h = (hDeg % 360f) / 360f;
        return new float[]{h, s, v};
    }

    // =========================
    // Texture caching
    // =========================

    private void rebuildTexture(int w, int h) {
        if (image != null) image.close();
        if (texture != null) texture.close();

        image = new NativeImage(w, h, false);
        for (int px = 0; px < w; px++) {
            float s = (float) px / (w - 1);
            for (int py = 0; py < h; py++) {
                float v = 1.0f - (float) py / (h - 1);
                int argb = hsvToRgb(hue, s, v); // ARGB
                // Write ARGB directly so the texture uses the same R/G/B ordering as our ARGB ints.
                image.setColor(px, py, argb);
            }
        }

        textureId = Identifier.of("somefrills", "color_square_" + hashCode());
        texture = new NativeImageBackedTexture(() -> "color_square_" + hashCode(), image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);

        dirty = false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public void dispose() {
        if (texture != null) texture.close();
        if (image != null) image.close();
    }
}
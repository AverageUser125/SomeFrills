package com.somefrills.hud;

import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import com.somefrills.config.SettingColor;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import static com.somefrills.Main.mc;

/**
 * A color picker with a saturation/value square, hue strip, RGBA and hex inputs.
 * Click inside the SV box or on the hue to pick a color; inputs update live.
 */
public class ColorPickerScreen extends AbstractScreen {
    private final AbstractScreen previous;
    private final SettingColor setting;

    // layout
    private int svX, svY, svSize;
    private int hueX, hueY, hueW, hueH;
    private int inputsX, inputsY;

    // color state (HSV + alpha)
    private float hue = 0f; // 0..360
    private float sat = 1f; // 0..1
    private float val = 1f; // 0..1

    private EditBox hexBox;
    private EditBox rBox, gBox, bBox, aBox;

    public ColorPickerScreen(SettingColor setting, AbstractScreen previous) {
        super(Component.literal("Color Picker"));
        this.previous = previous;
        this.setting = setting;
    }

    @Override
    protected void init() {
        super.init();
        setBackground(new BlurredBackground());

        // layout
        svSize = Math.min(200, this.width - 260);
        svX = 10;
        svY = 10;
        hueW = 12;
        hueX = svX + svSize + 8;
        hueY = svY;
        hueH = svSize;
        inputsX = hueX + hueW + 12;
        inputsY = svY;

        // load initial state from setting
        RenderColor rc = setting.value();
        float[] hsv = rgbToHsv(rc.r, rc.g, rc.b);
        this.hue = hsv[0] * 360f;
        this.sat = hsv[1];
        this.val = hsv[2];

        // create inputs
        int y = inputsY;
        int w = 50;
        rBox = new EditBox(this.font, inputsX, y, w, 20, Component.literal("R"));
        gBox = new EditBox(this.font, inputsX + (w + 6), y, w, 20, Component.literal("G"));
        bBox = new EditBox(this.font, inputsX + 2 * (w + 6), y, w, 20, Component.literal("B"));
        aBox = new EditBox(this.font, inputsX + 3 * (w + 6), y, w, 20, Component.literal("A"));

        hexBox = new EditBox(this.font, inputsX, y + 28, w * 3 + 12, 20, Component.literal("Hex"));

        updateUiFromSetting();

        // responders - update setting live when user edits numeric inputs
        rBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> {
            int c = clamp(i, 0, 255);
            RenderColor cur = setting.value();
            setting.set(new RenderColor(c, (int) (cur.g * 255), (int) (cur.b * 255), (int) (cur.a * 255)));
            updateUiFromSetting();
        }));
        gBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> {
            int c = clamp(i, 0, 255);
            RenderColor cur = setting.value();
            setting.set(new RenderColor((int) (cur.r * 255), c, (int) (cur.b * 255), (int) (cur.a * 255)));
            updateUiFromSetting();
        }));
        bBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> {
            int c = clamp(i, 0, 255);
            RenderColor cur = setting.value();
            setting.set(new RenderColor((int) (cur.r * 255), (int) (cur.g * 255), c, (int) (cur.a * 255)));
            updateUiFromSetting();
        }));
        aBox.setResponder(v -> Utils.parseInt(v).ifPresent(i -> {
            int c = clamp(i, 0, 255);
            RenderColor cur = setting.value();
            setting.set(new RenderColor((int) (cur.r * 255), (int) (cur.g * 255), (int) (cur.b * 255), c));
            updateUiFromSetting();
        }));

        hexBox.setResponder(v -> {
            if (v == null) return;
            String s = v.trim().toLowerCase().replace("#", "");
            if (s.startsWith("0x")) s = s.substring(2);
            try {
                int parsed = (int) Long.parseLong(s, 16);
                int argb;
                if (s.length() == 6) argb = (0xFF << 24) | parsed; // assume RGB
                else if (s.length() == 8) argb = parsed; // assume ARGB
                else return;
                setting.set(RenderColor.fromArgb(argb));
                updateUiFromSetting();
            } catch (NumberFormatException ignored) {
            }
        });

        this.addRenderableWidget(rBox);
        this.addRenderableWidget(gBox);
        this.addRenderableWidget(bBox);
        this.addRenderableWidget(aBox);
        this.addRenderableWidget(hexBox);

        com.daqem.uilib.gui.widget.ButtonWidget back = new com.daqem.uilib.gui.widget.ButtonWidget(this.width / 2 - 50, this.height - 30, 100, 20, Component.literal("Back"), b -> {
            com.somefrills.config.Config.save();
            mc.setScreen(this.previous);
        });
        this.addRenderableWidget(back);
    }

    private static int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    private void updateUiFromSetting() {
        RenderColor cur = setting.value();
        float[] hsv = rgbToHsv(cur.r, cur.g, cur.b);
        this.hue = hsv[0] * 360f;
        this.sat = hsv[1];
        this.val = hsv[2];

        rBox.setValue(String.valueOf((int) (cur.r * 255)));
        gBox.setValue(String.valueOf((int) (cur.g * 255)));
        bBox.setValue(String.valueOf((int) (cur.b * 255)));
        aBox.setValue(String.valueOf((int) (cur.a * 255)));
        hexBox.setValue(String.format("0x%08X", setting.value().argb));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // draw SV box (per-pixel loop) - s horizontally, v vertically
        for (int sx = 0; sx < svSize; sx++) {
            float s = (float) sx / (svSize - 1);
            for (int sy = 0; sy < svSize; sy++) {
                float v = 1.0f - ((float) sy / (svSize - 1));
                float[] rgb = hsvToRgb(hue / 360f, s, v);
                int col = ARGBFromFloats(rgb[0], rgb[1], rgb[2], setting.value().a);
                graphics.fill(svX + sx, svY + sy, svX + sx + 1, svY + sy + 1, col);
            }
        }

        // draw hue strip
        for (int hy = 0; hy < hueH; hy++) {
            float h = (float) hy / (hueH - 1);
            float[] rgb = hsvToRgb(h, 1f, 1f);
            int col = ARGBFromFloats(rgb[0], rgb[1], rgb[2], setting.value().a);
            graphics.fill(hueX, hueY + hy, hueX + hueW, hueY + hy + 1, col);
        }

        // draw selectors
        int selX = svX + Math.round(sat * (svSize - 1));
        int selY = svY + Math.round((1.0f - val) * (svSize - 1));
        graphics.fill(selX - 2, selY - 2, selX + 3, selY + 3, 0xffffffff);
        graphics.fill(selX - 1, selY - 1, selX + 2, selY + 2, 0xff000000);

        int hueSelY = hueY + Math.round((hue / 360f) * (hueH - 1));
        graphics.fill(hueX - 2, hueSelY - 1, hueX + hueW + 2, hueSelY + 2, 0xffffffff);

        // preview box
        int previewX = inputsX;
        int previewY = inputsY + 70;
        graphics.fill(previewX, previewY, previewX + 50, previewY + 50, setting.value().argb);

        // labels (use known positions)
        graphics.drawString(this.font, "R", rBox.getX() - 10, rBox.getY() + 5, 0xffffffff);
        graphics.drawString(this.font, "G", gBox.getX() - 10, gBox.getY() + 5, 0xffffffff);
        graphics.drawString(this.font, "B", bBox.getX() - 10, bBox.getY() + 5, 0xffffffff);
        graphics.drawString(this.font, "A", aBox.getX() - 10, aBox.getY() + 5, 0xffffffff);
        graphics.drawString(this.font, "Hex", hexBox.getX() - 36, hexBox.getY() + 5, 0xffffffff);

        // render children/widgets
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private static int ARGBFromFloats(float r, float g, float b, float aFloat) {
        int a = (int) (aFloat * 255);
        int rr = (int) (r * 255);
        int gg = (int) (g * 255);
        int bb = (int) (b * 255);
        return ((a & 0xFF) << 24) | ((rr & 0xFF) << 16) | ((gg & 0xFF) << 8) | (bb & 0xFF);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        if (s == 0f) return new float[]{v, v, v};
        float hh = h * 6f;
        int i = (int) Math.floor(hh);
        float f = hh - i;
        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float t = v * (1f - s * (1f - f));
        return switch (i % 6) {
            case 0 -> new float[]{v, t, p};
            case 1 -> new float[]{q, v, p};
            case 2 -> new float[]{p, v, t};
            case 3 -> new float[]{p, q, v};
            case 4 -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }

    private static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h = 0f;
        if (delta == 0f) h = 0f;
        else if (max == r) h = ((g - b) / delta) % 6f;
        else if (max == g) h = ((b - r) / delta) + 2f;
        else h = ((r - g) / delta) + 4f;
        h /= 6f;
        if (h < 0) h += 1f;
        float s = max == 0 ? 0f : (delta / max);
        float v = max;
        return new float[]{h, s, v};
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // SV box
        if (mouseX >= svX && mouseX < svX + svSize && mouseY >= svY && mouseY < svY + svSize) {
            float s = (float) (mouseX - svX) / (svSize - 1);
            float v = 1.0f - (float) (mouseY - svY) / (svSize - 1);
            this.sat = clampFloat(s, 0f, 1f);
            this.val = clampFloat(v, 0f, 1f);
            applyHsvToSetting();
            return true;
        }
        // hue strip
        if (mouseX >= hueX && mouseX < hueX + hueW && mouseY >= hueY && mouseY < hueY + hueH) {
            float h = (float) (mouseY - hueY) / (hueH - 1);
            this.hue = clampFloat(h, 0f, 1f) * 360f;
            applyHsvToSetting();
            return true;
        }
        return true;
        //return super.mouseClicked(mouseX, mouseY, button);
    }

    private static float clampFloat(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }

    private void applyHsvToSetting() {
        float[] rgb = hsvToRgb(hue / 360f, sat, val);
        RenderColor rc = new RenderColor(rgb[0], rgb[1], rgb[2], setting.value().a);
        setting.set(rc);
        updateUiFromSetting();
    }

    @Override
    public void onClose() {
        mc.setScreen(this.previous);
    }
}

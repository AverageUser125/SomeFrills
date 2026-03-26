package com.somefrills.hud;

import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.*;
import com.somefrills.config.FeatureRegistry.FeatureInfo;
import com.somefrills.hud.components.FlatSlider;
import com.somefrills.hud.components.FlatTextbox;
import com.somefrills.hud.components.KeybindButton;
import com.somefrills.hud.components.ToggleButton;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import static com.somefrills.Main.mc;
import com.somefrills.events.InputEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.lwjgl.glfw.GLFW;
import org.joml.Vector2d;
import java.lang.reflect.Field;
import java.util.*;

public class ClickGui extends AbstractScreen {
    private final List<CategoryData> categories = new ArrayList<>();

    // transient layout info used for click detection
    private final Map<FeatureInfo, Rect> featureBounds = new HashMap<>();
    // forwarded mouse button used by the 2-arg/1-arg UI hooks
    private static int forwardedMouseButton = -1;

    public ClickGui() {
        super(Component.literal("SomeFrills - Click GUI"));
    }

    @Override
    protected void init() {
        super.init();

        // Build categories by grouping features by their package segment
        Map<String, List<FeatureInfo>> byCategory = new TreeMap<>();
        for (FeatureInfo info : FeatureRegistry.getFeatures()) {
            String pkg = info.clazz.getPackage() != null ? info.clazz.getPackage().getName() : "";
            String[] parts = pkg.split("\\.");
            String cat = parts.length > 0 ? parts[parts.length - 1] : "misc";
            if ("features".equals(cat) && parts.length > 1) cat = parts[parts.length - 2];
            cat = humanize(cat);
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(info);
        }

        for (Map.Entry<String, List<FeatureInfo>> e : byCategory.entrySet()) {
            this.categories.add(new CategoryData(e.getKey(), e.getValue()));
        }

        // create search box (vanilla EditBox works with ui-lib)
        int sbWidth = 200;
        EditBox searchBox = new EditBox(this.font, 10, 10, sbWidth, 20, Component.literal("Search"));
        searchBox.setValue("");
        searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(searchBox);

        // initial filter
        this.refreshSearchResults("");
    }

    private void onSearchChanged(String value) {
        this.refreshSearchResults(value);
    }

    private String humanize(String in) {
        if (in == null || in.isEmpty()) return "";
        String withSpaces = in.replace('_', ' ').replace('-', ' ');
        StringBuilder out = new StringBuilder();
        char prev = ' ';
        for (int i = 0; i < withSpaces.length(); i++) {
            char c = withSpaces.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(prev) || Character.isDigit(prev))) out.append(' ');
            out.append(c);
            prev = c;
        }
        String res = out.toString().trim();
        if (res.isEmpty()) return res;
        return Character.toUpperCase(res.charAt(0)) + res.substring(1);
    }

    private boolean matchSearch(String text, String search) {
        if (text == null) return false;
        if (search == null || search.isEmpty()) return true;
        return Utils.toLower(text).replaceAll(" ", "").contains(Utils.toLower(search).replaceAll(" ", ""));
    }

    private void refreshSearchResults(String value) {
        for (CategoryData category : this.categories) {
            List<FeatureInfo> features = new ArrayList<>(category.features);
            if (value != null && !value.isEmpty()) {
                features.removeIf(info -> {
                    String name = humanize(info.featureInstance.key());
                    String tooltip = info.featureInstance.key();
                    if (matchSearch(name, value) || matchSearch(tooltip, value)) return false;
                    for (Map.Entry<Field, SettingGeneric> entry : info.settings.entrySet()) {
                        Field f = entry.getKey();
                        String fieldName = humanize(f.getName());
                        String desc = info.descriptions.getOrDefault(f, "");
                        if (matchSearch(fieldName, value) || matchSearch(desc, value)) return false;
                    }
                    return true;
                });
            }
            category.filteredFeatures.clear();
            category.filteredFeatures.addAll(features);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        int left = 10;
        int top = 40;
        int gap = 8;

        // draw categories horizontally across the top; each category is a column with features listed beneath
        featureBounds.clear();
        int colWidth = 200;
        int titleHeight = 12;
        int featHeight = 20; // feature button height
        int featGap = 6;

        for (int ci = 0; ci < this.categories.size(); ci++) {
            CategoryData c = this.categories.get(ci);
            int x = left + ci * (colWidth + gap);
            // draw category title
            String txt = c.name + " (" + c.filteredFeatures.size() + "/" + c.features.size() + ")";
            graphics.drawString(this.font, txt, x, top, 0xff5ca0bf);

            // draw each feature as a button-like rectangle beneath the title
            int fy = top + titleHeight + featGap;
            for (FeatureInfo info : c.filteredFeatures) {
                String name = humanize(info.featureInstance.key());
                boolean active = info.featureInstance.isActive();

                // solid background color depends on enabled state (fully opaque alpha)
                int bg = active ? 0xff3fb5ff : 0xff0a2b66; // enabled: light blue, disabled: dark blue
                // draw filled body (no separate outline) with full opacity
                graphics.fill(x, fy, x + colWidth, fy + featHeight, bg);

                // draw feature name with a little left padding and vertical centering
                int textX = x + 6;
                int textY = fy + (featHeight - this.font.lineHeight) / 2;
                int textColor = active ? 0xffe8f7ff : 0xffffffff;
                graphics.drawString(this.font, name, textX, textY, textColor);

                // store clickable bounds
                featureBounds.put(info, new Rect(x, fy, colWidth, featHeight));

                fy += featHeight + featGap;
            }
        }

        // status / hint
        int h = this.height;
        graphics.drawString(this.font, "Left click = toggle, Right click = open settings (if available)", 10, h - 20, 0xffffffff);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // Debug: log click coordinates to help verify this method is invoked at runtime
        com.somefrills.misc.Utils.infoFormat("ClickGui.mouseClicked: x={} y={} button={}", mouseX, mouseY, button);
        // feature clicks (we render all categories and their features in a single column)
        for (Map.Entry<FeatureInfo, Rect> e : featureBounds.entrySet()) {
            Rect r = e.getValue();
            if (mouseX >= r.x && mouseX <= r.x + r.w && mouseY >= r.y && mouseY <= r.y + r.h) {
                FeatureInfo info = e.getKey();
                if (button == 0) { // left click -> toggle
                    boolean newState = !info.featureInstance.isActive();
                    info.featureInstance.setActive(newState);
                    if (newState) FeatureRegistry.subscribeFeature(info.featureInstance);
                    else FeatureRegistry.unsubscribeFeature(info.featureInstance);
                    return;
                } else if (button == 1) { // right click -> open settings screen
                    // Do not open a settings screen for features that have no settings
                    if (info.settings.isEmpty()) return;
                    SettingsScreen screen = new SettingsScreen(info, this);
                    mc.setScreen(screen);
                    return;
                }
            }
        }
    }

    /**
     * Overload that matches the UI framework's expected signature (mouseClicked(x,y)).
     * Delegates to the 3-arg variant using the last forwarded button value.
     */
    public void mouseClicked(double mouseX, double mouseY) {
        // delegate to 3-arg handler using forwarded button
        this.mouseClicked(mouseX, mouseY, forwardedMouseButton);
    }

    // accept releases forwarded from the input hook (no-op here)
    public void mouseReleased(double mouseX, double mouseY, int button) { }

    /**
     * Overload matching UI framework's mouseReleased(int) signature.
     */
    public void mouseReleased(int button) {
        Vector2d pos = Utils.getMousePos();
        this.mouseReleased(pos.x, pos.y, button);
    }

    @EventHandler(priority = EventPriority.LOW)
    private static void onInput(InputEvent event) {
        if (mc == null || mc.screen == null) return;
        if (event.isCancelled()) return;
        try {
            // Mouse presses: set forwarded mouse button and call the 2-arg mouseClicked
            if (event.isMouse && event.action == GLFW.GLFW_PRESS) {
                Vector2d mousePos = Utils.getMousePos();
                double mx = mousePos.x;
                double my = mousePos.y;
                forwardedMouseButton = event.key;
                Object screenObj = mc.screen;
                if (screenObj instanceof SettingsScreen ss) {
                    try { ss.mouseClicked(mx, my); } catch (Throwable ignored) {}
                } else if (screenObj instanceof ClickGui cg) {
                    try { cg.mouseClicked(mx, my); } catch (Throwable ignored) {}
                }
                return;
            }

            // Mouse releases: call the 1-arg mouseReleased with the button and clear forwarded state
            if (event.isMouse && event.action == GLFW.GLFW_RELEASE) {
                int btn = event.key;
                Object screenObj = mc.screen;
                if (screenObj instanceof SettingsScreen ss) {
                    try { ss.mouseReleased(btn); } catch (Throwable ignored) {}
                } else if (screenObj instanceof ClickGui cg) {
                    try { cg.mouseReleased(btn); } catch (Throwable ignored) {}
                }
                forwardedMouseButton = -1;
                return;
            }

            // Keyboard presses: forward to SettingsScreen to support KeybindButton binding
            if (event.isKeyboard && event.action == GLFW.GLFW_PRESS) {
                Object screenObj = mc.screen;
                if (screenObj instanceof SettingsScreen ss) {
                    // Only forward key events if ESC was pressed or a keybind button is actively binding
                    boolean shouldForward = event.key == GLFW.GLFW_KEY_ESCAPE;
                    if (!shouldForward) {
                        if (ss.hasActiveKeybindBinding()) shouldForward = true;
                    }
                    if (shouldForward) {
                        ss.handleKeyEvent(event.key, event.action);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public void onClose() {
        // persist config
        com.somefrills.config.Config.save();
        super.onClose();
    }

    public static class CategoryData {
        public final String name;
        public final List<FeatureInfo> features;
        public final List<FeatureInfo> filteredFeatures = new ArrayList<>();

        public CategoryData(String name, List<FeatureInfo> features) {
            this.name = name;
            this.features = features;
            this.filteredFeatures.addAll(features);
        }
    }

    private static class Rect { int x, y, w, h; Rect(int x,int y,int w,int h){this.x=x;this.y=y;this.w=w;this.h=h;} }


    private static class SettingsScreen extends AbstractScreen {
        private final FeatureRegistry.FeatureInfo info;
        private final AbstractScreen previous;
        // no persistent widget tracking needed
        private final List<KeybindButton> keybindButtons = new ArrayList<>();
        // UI rows for labels + controls so we can render labels and tooltips
        private final List<Row> rows = new ArrayList<>();
        private int pressedRow = -1;

        private static class Row {
            int labelX, labelY, labelW, labelH;
            int ctrlX, ctrlY, ctrlW, ctrlH;
            Component label;
            String desc;
            Runnable onClick;
            Object control;
            Row(int lx,int ly,int lw,int lh,int cx,int cy,int cw,int ch, Component label, String desc) {
                this.labelX = lx; this.labelY = ly; this.labelW = lw; this.labelH = lh;
                this.ctrlX = cx; this.ctrlY = cy; this.ctrlW = cw; this.ctrlH = ch;
                this.label = label; this.desc = desc;
            }
        }

        public SettingsScreen(FeatureRegistry.FeatureInfo info, AbstractScreen previous) {
            super(net.minecraft.network.chat.Component.literal("Settings - " + info.featureInstance.key()));
            this.info = info;
            this.previous = previous;
        }

        @Override
        protected void init() {
            super.init();
            int labelX = 10;
            int labelW = 100;
            int ctrlX = labelX + labelW + 10; // control placed to the right of label
            int y = 10;
            for (Map.Entry<java.lang.reflect.Field, SettingGeneric> entry : this.info.settings.entrySet()) {
                SettingGeneric setting = entry.getValue();
                java.lang.reflect.Field field = entry.getKey();
                String desc = this.info.descriptions.getOrDefault(field, "");
                Component labelComp = Component.literal(humanizeLocal(field.getName()));

                if (setting.getClass().equals(SettingBool.class)) {
                    SettingBool sb = (SettingBool) setting;
                    ToggleButton t = new ToggleButton(sb.value());
                    ButtonWidget btn = t.createButton(ctrlX, y, 80, 20);
                    t.addListener(sb::set);
                    this.addRenderableWidget(btn);
                    Row rr = new Row(labelX, y, labelW, 20, ctrlX, y, 80, 20, labelComp, desc);
                    rr.control = btn; // let the ButtonWidget handle clicks itself
                    rows.add(rr);
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingEnum.class)) {
                    try {
                        SettingEnum<?> se = (SettingEnum<?>) setting;
                        Object[] vals = se.values;
                        int startIdx = 0;
                        for (int i = 0; i < vals.length; i++) if (vals[i].equals(se.value())) { startIdx = i; break; }
                        final int[] idx = new int[] { startIdx };
                        ButtonWidget eb = new ButtonWidget(ctrlX, y, 100, 20, Component.literal(vals[idx[0]].toString()), btn -> {
                            idx[0] = (idx[0] + 1) % vals.length;
                            Object newVal = vals[idx[0]];
                            try { se.set(newVal); } catch (Throwable ignored) {}
                            btn.setMessage(Component.literal(newVal.toString()));
                        });
                        this.addRenderableWidget(eb);
                        Row rr = new Row(labelX, y, labelW, 20, ctrlX, y, 100, 20, labelComp, desc);
                        rr.control = eb;
                        // clicking the enum button cycles the value; the ButtonWidget already handles it
                        rows.add(rr);
                    } catch (Throwable ignored) {}
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingDouble.class) || setting.getClass().equals(SettingInt.class) || setting.getClass().equals(SettingIntSlider.class)) {
                    FlatSlider slider = new FlatSlider(0xff888888, 0xffffffff);
                    if (setting.getClass().equals(SettingDouble.class)) {
                        SettingDouble sd = (SettingDouble) setting;
                        // sensible defaults for doubles
                        slider.min(0);
                        slider.max(100);
                        slider.stepSize(0.1);
                        slider.value(sd.value());
                        slider.onChanged(sd::set);
                    } else if (setting.getClass().equals(SettingIntSlider.class)) {
                        // integer slider with explicit bounds
                        SettingIntSlider sis = (SettingIntSlider) setting;
                        slider.min(sis.min());
                        slider.max(sis.max());
                        slider.stepSize(1);
                        slider.value(sis.value());
                        slider.onChanged(d -> sis.set((int) Math.round(d)));
                    } else {
                        // plain integer setting — use reasonable defaults
                        SettingInt si = (SettingInt) setting;
                        slider.min(0);
                        slider.max(100);
                        slider.stepSize(1);
                        slider.value(si.value());
                        slider.onChanged(d -> si.set((int) Math.round(d)));
                    }
                    EditBox eb = slider.getEditBoxAt(ctrlX, y);
                    this.addRenderableWidget(eb);
                    Row rr = new Row(labelX, y, labelW, 20, ctrlX, y, 80, 20, labelComp, desc);
                    rr.control = eb;
                    rows.add(rr);
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingKeybind.class)) {
                    SettingKeybind sk = (SettingKeybind) setting;
                    KeybindButton kb = new KeybindButton();
                    ButtonWidget b = kb.createButton(ctrlX, y, 100, 20);
                    kb.setBoundKey(sk.value());
                    kb.onBound().subscribe(sk::set);
                    keybindButtons.add(kb);
                    this.addRenderableWidget(b);
                    Row rr = new Row(labelX, y, labelW, 20, ctrlX, y, 100, 20, labelComp, desc);
                    rr.control = b;
                    rows.add(rr);
                    y += 24; continue;
                }

                if (setting.getClass().equals(SettingString.class)) {
                    SettingString ss = (SettingString) setting;
                    FlatTextbox tb = new FlatTextbox(200);
                    tb.setValue(ss.value());
                    tb.onChanged(ss::set);
                    EditBox tbox = tb.getEditBoxAt(ctrlX, y);
                    this.addRenderableWidget(tbox);
                    Row rr = new Row(labelX, y, labelW, 20, ctrlX, y, 200, 20, labelComp, desc);
                    rr.control = tbox;
                    rows.add(rr);
                    y += 24; continue;
                }

                if(setting.getClass().equals(SettingColor.class)) {
                    ButtonWidget colorBtn = new ButtonWidget(ctrlX, y, 80, 20, Component.literal("Pick Color"), btn -> {
                        var newScreen = new ColorPickerScreen((SettingColor) setting,this);
                        mc.setScreen(newScreen);
                    });
                    this.addRenderableWidget(colorBtn);
                    Row rr = new Row(labelX, y, labelW, 20, ctrlX, y, 80, 20, labelComp, desc);
                    rr.control = colorBtn;
                    rows.add(rr);
                    y += 24; continue;
                }
                // fallback: plain label describing the setting (no editor available)
                y += 18;
            }
            // add Save & Back
            ButtonWidget save = new ButtonWidget(this.width / 2 - 50, this.height - 30, 100, 20, net.minecraft.network.chat.Component.literal("Save & Back"), b -> {
                com.somefrills.config.Config.save();
                mc.setScreen(previous);
            });
            this.addRenderableWidget(save);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.render(graphics, mouseX, mouseY, delta);
            // Draw labels for each row and show tooltip when hovering either the label or the control
            for (Row r : rows) {
                graphics.drawString(this.font, r.label, r.labelX, r.labelY + (20 - this.font.lineHeight) / 2, 0xffffffff);
                boolean overLabel = mouseX >= r.labelX && mouseX <= r.labelX + r.labelW && mouseY >= r.labelY && mouseY <= r.labelY + r.labelH;
                boolean overCtrl = mouseX >= r.ctrlX && mouseX <= r.ctrlX + r.ctrlW && mouseY >= r.ctrlY && mouseY <= r.ctrlY + r.ctrlH;
                if ((overLabel || overCtrl) && r.desc != null && !r.desc.isEmpty()) {
                    // Tooltip sizing
                    int tw = this.font.width(r.desc) + 6;
                    int th = this.font.lineHeight + 4;

                    // Center the tooltip over the control's horizontal span
                    int ctrlCenterX = r.ctrlX + r.ctrlW / 2;
                    int tx = ctrlCenterX - tw / 2;

                    // Prefer placing above the control if there's space, otherwise below
                    int padding = 6;
                    int tyAbove = r.ctrlY - th - padding;
                    int tyBelow = r.ctrlY + r.ctrlH + padding;
                    int ty = tyAbove >= 8 ? tyAbove : tyBelow;

                    // Clamp to screen bounds
                    int screenW = this.width;
                    int screenH = this.height;
                    if (tx + tw > screenW - 8) tx = Math.max(8, screenW - 8 - tw);
                    if (tx < 8) tx = 8;
                    if (ty + th > screenH - 8) ty = Math.max(8, screenH - 8 - th);
                    if (ty < 8) ty = 8;

                    graphics.fill(tx - 3, ty - 3, tx + tw, ty + th, 0xcc000000);
                    graphics.drawString(this.font, Component.literal(r.desc), tx, ty, 0xffffffff);
                }
            }
        }

        public void mouseClicked(double mouseX, double mouseY, int button) {
            // detect which row (if any) was pressed down. If the row has a real widget child
            // (ButtonWidget / EditBox) we must not intercept: let the widget receive the event.
            for (int i = 0; i < rows.size(); i++) {
                Row r = rows.get(i);
                if (mouseX >= r.ctrlX && mouseX <= r.ctrlX + r.ctrlW && mouseY >= r.ctrlY && mouseY <= r.ctrlY + r.ctrlH) {
                    // If a widget is present, do not capture the click here
                    if (r.control instanceof ButtonWidget || r.control instanceof EditBox) {
                        pressedRow = -1;
                        // Allow the widget to handle the press; do not intercept.
                        return;
                    }
                    pressedRow = i;
                    return;
                }
            }
            pressedRow = -1;
        }

        public void mouseReleased(double mouseX, double mouseY, int button) {
            if (pressedRow >= 0 && pressedRow < rows.size()) {
                Row r = rows.get(pressedRow);
                if (mouseX >= r.ctrlX && mouseX <= r.ctrlX + r.ctrlW && mouseY >= r.ctrlY && mouseY <= r.ctrlY + r.ctrlH) {
                    if (r.control == null && r.onClick != null) {
                        try { r.onClick.run(); } catch (Throwable ignored) {}
                        pressedRow = -1;
                        return;
                    }
                    // if control is a widget, allow the widget to handle release (do not intercept)
                    if (r.control instanceof ButtonWidget || r.control instanceof EditBox) {
                        pressedRow = -1;
                        return;
                    }
                }
            }
            pressedRow = -1;
        }

        // 2-arg/1-arg overloads used by the global input forwarder (match ClickGui)
        public void mouseClicked(double mouseX, double mouseY) {
            this.mouseClicked(mouseX, mouseY, forwardedMouseButton);
        }

        public void mouseReleased(int button) {
            Vector2d pos = Utils.getMousePos();
            this.mouseReleased(pos.x, pos.y, button);
        }

        private String humanizeLocal(String in) {
            if (in == null || in.isEmpty()) return "";
            String withSpaces = in.replace('_', ' ').replace('-', ' ');
            StringBuilder out = new StringBuilder();
            char prev = ' ';
            for (int i = 0; i < withSpaces.length(); i++) {
                char c = withSpaces.charAt(i);
                if (i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(prev) || Character.isDigit(prev))) out.append(' ');
                out.append(c);
                prev = c;
            }
            String res = out.toString().trim();
            if (res.isEmpty()) return res;
            return Character.toUpperCase(res.charAt(0)) + res.substring(1);
        }

        // Handle a forwarded key event. ESC closes the settings screen; if any keybind
        // widget is awaiting input, the key will be bound. This method intentionally
        // returns void because callers in this class do not use a boolean return value.
        public void handleKeyEvent(int key, int action) {
            // ESC closes the settings screen
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                mc.setScreen(previous);
                return;
            }
            // If any keybind button is awaiting binding, bind this key
            for (KeybindButton kb : keybindButtons) {
                if (kb.isBinding) {
                    kb.bind(key);
                    return;
                }
            }
        }

        // Public accessor used by ClickGui to decide whether to forward key events
        public boolean hasActiveKeybindBinding() {
            for (KeybindButton kb : keybindButtons) if (kb.isBinding) return true;
            return false;
        }

        // capitalize removed: no longer needed
    }
}




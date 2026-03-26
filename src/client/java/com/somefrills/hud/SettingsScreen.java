package com.somefrills.hud;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import com.daqem.uilib.gui.widget.ButtonWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.somefrills.config.*;
import com.somefrills.hud.components.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.Main.mc;

public class SettingsScreen extends AbstractScreen {
    private final FeatureRegistry.FeatureInfo info;

    public SettingsScreen(FeatureRegistry.FeatureInfo info) {
        super(Text.literal(info.name + " Settings"));
        this.info = info;
    }

    @Override
    protected void init() {
        super.init();
        setBackground(new BlurredBackground());
        List<SettingWidget> widgets = new ArrayList<>();
        int marginLeft = 10;
        int marginTop = 10;
        int labelWidth = 300;
        int controlWidth = 200;
        int rowHeight = 22;
        int rowGap = 6;
        int colGap = 12;

        int x = marginLeft;
        int y = marginTop;

        int columnWidth = labelWidth + 6 + controlWidth;

        for (FeatureRegistry.SettingInfo entry : info.settings) {
            var setting = entry.settingInstance();
            // wrap column if we would overflow the screen height
            if (y + rowHeight > this.height - 40) {
                x += columnWidth + colGap;
                y = marginTop;
            }
            IWidget w = getWidget(x, y, controlWidth, rowHeight, setting);
            if (w == null) continue;
            SettingWidget sw = new SettingWidget(x, y, labelWidth, rowHeight, entry.name(), entry.description(), w);
            widgets.add(sw);
            y += rowHeight + rowGap;
        }
        addWidgets(widgets);
    }

    private IWidget getWidget(int x, int y, int width, int height, SettingGeneric setting) {
        var clazz = setting.getClass();
        if (clazz.equals(SettingBool.class)) {
            return new ToggleButton(x, y, width, height, (SettingBool) setting);
        }
        if (clazz.equals(SettingKeybind.class)) {
            return new KeybindButton(x, y, width, height, (SettingKeybind) setting);
        }
        if(clazz.equals(SettingEnum.class)) {
            return new EnumButton<>(x, y, width, height, (SettingEnum<?>) setting);
        }
        if (clazz.equals(SettingInt.class)) {
            return new NumberInt(x, y, width, height, (SettingInt) setting);
        }
        if (clazz.equals(SettingDouble.class)) {
            return new NumberDouble(x, y, width, height, (SettingDouble) setting);
        }
        if (clazz.equals(SettingColor.class)) {
            var s = (SettingColor) setting;
            return new ButtonWidget(x, y, width, height, Text.literal("Edit Color"), button -> {
                mc.setScreen(new ColorPickerScreen(s, this));
            });
        }
        if (clazz.equals(SettingBlockPosList.class)) {
            // TODO
        }
        if (clazz.equals(SettingString.class)) {
            return new StringButton(x, y, width, height, (SettingString) setting);
        }
        if (clazz.equals(SettingIntSlider.class)) {
            return new SliderInt(x, y, 70, width - 70, height, (SettingIntSlider) setting);
        }
        if (clazz.equals(SettingJson.class)) {
            // TODO
        }
        return null;
    }

    @Override
    public void close() {
        Config.save();
        mc.setScreen(new ClickGui());
    }
}

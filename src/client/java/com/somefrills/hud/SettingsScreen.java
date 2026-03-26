package com.somefrills.hud;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.daqem.uilib.gui.widget.ScrollContainer2DWidget;
import com.somefrills.config.*;
import com.somefrills.hud.components.*;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.Main.mc;

public class SettingsScreen extends AbstractScreen {
    private final FeatureRegistry.FeatureInfo info;
    private ScrollContainer2DWidget scrollContainer;

    public SettingsScreen(FeatureRegistry.FeatureInfo info) {
        super(Component.literal(info.name + " Settings"));
        this.info = info;
    }

    @Override
    protected void init() {
        List<SettingWidget> widgets = new ArrayList<>();
        int marginLeft = 10;
        int marginTop = 10;
        int labelWidth = 140;
        int controlWidth = 160;
        int rowHeight = 22;
        int rowGap = 6;
        int colGap = 12;

        int x = marginLeft;
        int y = marginTop;

        int columnWidth = labelWidth + 6 + controlWidth;

        for (FeatureRegistry.SettingInfo entry : info.settings) {
            var setting = entry.settingInstance;
            // wrap column if we would overflow the screen height
            if (y + rowHeight > this.height - 40) {
                x += columnWidth + colGap;
                y = marginTop;
            }
            IWidget w = getWidget(x, y, controlWidth, rowHeight, setting);
            if (w == null) continue;
            SettingWidget sw = new SettingWidget(x, y, labelWidth, rowHeight, entry.name, entry.description, w);
            widgets.add(sw);
            // add the label/control container to the screen so it renders and receives input
            this.addRenderableWidget(sw);
            y += rowHeight + rowGap;
        }
    }

    private static IWidget getWidget(int x, int y, int width, int height, SettingGeneric setting) {
        var clazz = setting.getClass();
        if (clazz.equals(SettingBool.class)) {
            var s = (SettingBool) setting;
            return new ToggleButton(x, y, width, height, s.value());
        }
        if(clazz.equals(SettingKeybind.class)) {
            var s = (SettingKeybind) setting;
            return new KeybindButton(x, y, width, height, s.value());
        }  if(clazz.equals(SettingInt.class)) {
            var s = (SettingInt) setting;
            return new NumberInt(x, y, width, height, s.value());
        }  if(clazz.equals(SettingDouble.class)) {
            var s = (SettingDouble) setting;
            return new NumberDouble(x, y, width, height, s.value());
        }  if(clazz.equals(SettingColor.class)) {
            // TODO
            //var s = (SettingColor) setting;
            //addRenderableWidget(new ColorPicker(x, y, width, height, s.value()));
        }  if(clazz.equals(SettingEnum.class)) {
            // TODO
        }  if(clazz.equals(SettingBlockPosList.class)) {
            // TODO
        } if(clazz.equals(SettingString.class)) {
            var s = (SettingString) setting;
            return new EditBoxWidget(mc.font, x, y, width, height, Component.literal(s.value()));
        }
        if(clazz.equals(SettingJson.class)) {
            // TODO
        }
        return null;
    }
}

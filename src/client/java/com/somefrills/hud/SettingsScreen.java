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
        int startY = 10;
        for(FeatureRegistry.SettingInfo entry : info.settings) {
            var setting = entry.settingInstance;
            IWidget widget = getWidget(0, 0, 100, 20, setting);
            if(widget == null) continue;
            widgets.add(new SettingWidget(0, startY, 100, 20, entry.name, entry.description, widget));
            startY += 30;
        }
        addWidgets(widgets);
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

package com.somefrills.config;

import com.somefrills.misc.RenderColor;

public class SettingColor extends SettingGeneric {
    public SettingColor(RenderColor defaultValue) {
        super(defaultValue);
    }


    public RenderColor value() {
        int argb = this.get().getAsInt();
        return RenderColor.fromArgb(argb);
    }
}
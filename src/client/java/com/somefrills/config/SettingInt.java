package com.somefrills.config;

public class SettingInt extends SettingGeneric {

    public SettingInt(int defaultValue) {
        super(defaultValue);
    }


    public int value() {
        return this.get().getAsInt();
    }
}
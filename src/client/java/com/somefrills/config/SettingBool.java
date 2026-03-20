package com.somefrills.config;

public class SettingBool extends SettingGeneric {
    public SettingBool(boolean defaultValue) {
        super(defaultValue);
    }


    public boolean value() {
        return this.get().getAsBoolean();
    }
}
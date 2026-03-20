package com.somefrills.config;

public class SettingString extends SettingGeneric {
    public SettingString(String defaultValue) {
        super(defaultValue);
    }


    public String value() {
        return this.get().getAsString();
    }
}
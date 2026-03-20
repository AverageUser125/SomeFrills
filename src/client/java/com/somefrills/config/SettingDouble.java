package com.somefrills.config;

public class SettingDouble extends SettingGeneric {
    public SettingDouble(double defaultValue) {
        super(defaultValue);
    }


    public double value() {
        return this.get().getAsDouble();
    }
}
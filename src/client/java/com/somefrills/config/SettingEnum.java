package com.somefrills.config;

public class SettingEnum<T extends Enum<T>> extends SettingGeneric {
    public T[] values;


    public SettingEnum(T defaultValue, Class<T> values) {
        super(defaultValue);
        this.values = values.getEnumConstants();
    }

    public T value() {
        String name = this.get().getAsString();
        for (T v : this.values) {
            if (v.name().equals(name)) return v;
        }
        return this.values[0];
    }
}
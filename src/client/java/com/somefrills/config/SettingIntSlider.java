package com.somefrills.config;

/**
 * Integer setting with explicit min/max bounds for UI sliders.
 * Stores a plain integer in config like other settings; the bounds are used by the GUI.
 */
public class SettingIntSlider extends SettingInt {
    private final int min;
    private final int max;

    public SettingIntSlider(int defaultValue, int min, int max) {
        super(defaultValue);
        this.min = min;
        this.max = max;
    }

    public int min() {
        return this.min;
    }

    public int max() {
        return this.max;
    }
}


package com.somefrills.features.core;

import com.somefrills.misc.KeybindManager;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ToggleFeature extends AbstractFeature {
    private boolean keybindActive;
    public ToggleFeature(Property<Boolean> enabledProperty, Property<Integer> keybindProperty) {
        super(enabledProperty);
        keybindActive = false;
        // If the property is changed, KeybindManager will automatically update the keybind, so we don't need to do anything here
        KeybindManager.register(keybindProperty, () -> {
            keybindActive = !keybindActive;
            sync();
        });
    }

    @Override
    protected boolean evaluate() {
        return keybindActive;
    }
}

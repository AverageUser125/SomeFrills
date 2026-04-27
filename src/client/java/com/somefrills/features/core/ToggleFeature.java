package com.somefrills.features.core;

import com.somefrills.misc.KeybindManager;
import io.github.notenoughupdates.moulconfig.observer.Property;

public abstract class ToggleFeature extends AbstractFeature {
    private final Property<Integer> keybindProperty;

    private KeybindManager.Subscription sub = null;
    private boolean keybindActive = false;

    public ToggleFeature(Property<Boolean> enabledProperty,
                         Property<Integer> keybindProperty) {
        super(enabledProperty);
        this.keybindProperty = keybindProperty;
    }

    @Override
    protected boolean evaluate() {
        return keybindActive;
    }

    @Override
    public final void toggle() {
        if (!isEnabled()) return;

        keybindActive = !keybindActive;
        sync();
    }

    @Override
    protected final void onEnable() {
        if (sub != null) {
            sub.unregister();
            sub = null;
        }
        sub = KeybindManager.register(keybindProperty, this::toggle);
    }

    @Override
    protected final void onDisable() {
        if (sub != null) {
            sub.unregister();
            sub = null;
        }
        keybindActive = false;
    }
}
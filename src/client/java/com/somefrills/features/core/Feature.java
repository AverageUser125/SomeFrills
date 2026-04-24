package com.somefrills.features.core;

import com.somefrills.Main;
import io.github.notenoughupdates.moulconfig.observer.Property;

/**
 * Keep Feature clean / minimal.
 * No subscription-state pollution added.
 */
public abstract class Feature {

    private boolean active = false;

    public Feature(Property<Boolean> enabledProperty) {
        bind(enabledProperty);
    }

    private void bind(Property<Boolean> enabledProperty) {
        enabledProperty.addObserver(this::onPropertyChanged);
        onToggle(enabledProperty.get());
    }

    private void onPropertyChanged(boolean oldValue, boolean newValue) {
        onToggle(newValue);
    }

    /**
     * Backwards compatible hook.
     */
    protected void onToggle(boolean enabled) {
        if (active == enabled) return;

        active = enabled;

        if (enabled) {
            Main.eventBus.subscribe(this);
            onEnable();
        } else {
            Main.eventBus.unsubscribe(this);
            onDisable();
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public boolean isActive() {
        return active;
    }

    protected void setActive(boolean active) {
        this.active = active;
    }
}
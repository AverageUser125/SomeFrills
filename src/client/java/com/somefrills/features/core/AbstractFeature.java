package com.somefrills.features.core;

import io.github.notenoughupdates.moulconfig.observer.Property;

import static com.somefrills.Main.eventBus;

public abstract class AbstractFeature {

    private final Property<Boolean> enabledProperty;
    private boolean active;

    protected AbstractFeature(Property<Boolean> enabledProperty) {
        this.enabledProperty = enabledProperty;

        enabledProperty.addObserver((o, n) -> evaluate());
        evaluate();
    }

    protected final boolean isEnabled() {
        return enabledProperty.get();
    }

    protected final void setActive(boolean value) {
        if (value == isActive()) return;
        active = value;

        if (value) {
            eventBus.subscribe(this);
            onEnable();
        } else {
            eventBus.unsubscribe(this);
            onDisable();
        }
    }

    public final void toggle() {
        setActive(!isActive());
    }

    public final boolean isActive() {
        return active;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected abstract void evaluate();
}
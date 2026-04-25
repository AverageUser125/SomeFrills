package com.somefrills.features.core;

import io.github.notenoughupdates.moulconfig.observer.Property;

import static com.somefrills.Main.eventBus;

// Invariant: the state "enabled = false" and "active = true" cannot happen
public abstract class AbstractFeature {

    private final Property<Boolean> enabledProperty;
    private boolean active;

    protected AbstractFeature(Property<Boolean> enabledProperty) {
        this.enabledProperty = enabledProperty;
        active = false;

        enabledProperty.addObserver((o, n) -> sync());
        if (enabledProperty.get()) {
            onEnable();
            if (evaluate()) {
                active = true;
                eventBus.subscribe(this);
            }
        } else {
            onDisable();
        }
    }

    public final boolean isEnabled() {
        return enabledProperty.get();
    }

    public final boolean isActive() {
        return active;
    }

    public void setEnabled(boolean enabled) {
        enabledProperty.set(enabled);
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    private void setActive(boolean value) {
        if (!isEnabled() && value) {
            throw new IllegalStateException("Cannot activate disabled feature: " + getClass().getSimpleName());
        }
        if (value == active) return;

        active = value;

        if (value) {
            onEnable();
            eventBus.subscribe(this);
        } else {
            eventBus.unsubscribe(this);
            onDisable();
        }
    }

    protected final void sync() {
        if (!isEnabled()) {
            setActive(false);
            return;
        }

        setActive(evaluate());
    }

    /**
     * Subclasses only describe conditions.
     */
    protected abstract boolean evaluate();

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
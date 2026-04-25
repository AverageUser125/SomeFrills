package com.somefrills.features.core;

import io.github.notenoughupdates.moulconfig.observer.Property;

import static com.somefrills.Main.eventBus;

// Invariant: the state "enabled = false" and "active = true" cannot happen
public abstract class AbstractFeature {

    private final Property<Boolean> enabledProperty;
    private boolean active;

    protected AbstractFeature(Property<Boolean> enabledProperty) {
        this.enabledProperty = enabledProperty;

        enabledProperty.addObserver((o, n) -> sync());
        sync();
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
            eventBus.subscribe(this);
            onEnable();
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
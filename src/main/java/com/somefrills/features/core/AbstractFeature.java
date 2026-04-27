package com.somefrills.features.core;

import io.github.notenoughupdates.moulconfig.observer.Property;

import static com.somefrills.Main.eventBus;

// Invariant: enabled = false && active = true cannot happen
public abstract class AbstractFeature {

    private final Property<Boolean> enabledProperty;
    private boolean active;

    protected AbstractFeature(Property<Boolean> enabledProperty) {
        this.enabledProperty = enabledProperty;
        this.active = false;
    }

    /**
     * Step 2 initialization.
     * Must be called once after construction.
     */
    final void initialize() {
        enabledProperty.addObserver((o, n) -> {
            if (n) {
                onEnable();
            } else {
                onDisable();
            }
            sync();
        });
        setEnabled(isEnabled());
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
        if (!isEnabled() && value) return;

        if (value == active) return;
        active = value;

        if (value) {
            onActivate();
            eventBus.subscribe(this);
        } else {
            eventBus.unsubscribe(this);
            onDeactivate();
        }
    }

    protected final void sync() {
        if (!isEnabled()) {
            setActive(false);
            return;
        }

        setActive(evaluate());
    }

    protected abstract boolean evaluate();

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected void onActivate() {
    }

    protected void onDeactivate() {
    }
}
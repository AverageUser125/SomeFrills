package com.somefrills.config;

import com.somefrills.Main;
import com.somefrills.config.solvers.SolverCategory.ExperimentSolverConfig;
import io.github.notenoughupdates.moulconfig.observer.Property;

public abstract class Feature {

    private boolean active = false;

    public Feature(Property<Boolean> enabledProperty) {
        bind(enabledProperty);
    }

    private void bind(Property<Boolean> enabledProperty) {
        enabledProperty.addObserver(this::onToggle);
        onToggle(enabledProperty.get());
    }

    private void onToggle(boolean oldValue, boolean newValue) {
        if(oldValue != active) throw new IllegalStateException("Feature state was changed outside of onToggle! This is not allowed.");
        onToggle(newValue);
    }

    private void onToggle(boolean enabled) {
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
    protected void onEnable() {}
    protected void onDisable() {}

    public boolean isActive() {
        return active;
    }

}
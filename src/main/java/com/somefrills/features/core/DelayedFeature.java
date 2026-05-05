package com.somefrills.features.core;

import com.somefrills.events.ServerJoinEvent;
import com.somefrills.misc.DelayedRun;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class DelayedFeature extends AbstractFeature {
    private boolean active = false;

    protected DelayedFeature(Property<Boolean> enabledProperty) {
        super(enabledProperty);
    }

    @Override
    protected final void onEnable() {
        EventSubscriptions.register(this, ServerJoinEvent.class, event -> {
            active = false;
            sync();
            DelayedRun.runDelayed(() -> {
                active = true;
                sync();
            }, 1000);
        });
    }

    @Override
    protected final void onDisable() {
        EventSubscriptions.unregister(this);
    }

    @Override
    protected boolean evaluate() {
        return active;
    }
}

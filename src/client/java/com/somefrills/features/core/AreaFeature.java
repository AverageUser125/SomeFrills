package com.somefrills.features.core;

import com.somefrills.Main;
import com.somefrills.events.AreaChangeEvent;
import com.somefrills.misc.Area;
import io.github.notenoughupdates.moulconfig.observer.Property;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.eventBus;

public abstract class AreaFeature extends AbstractFeature {

    private AreaDelegator delegator;
    private boolean inArea;

    public AreaFeature(Property<Boolean> enabledProperty) {
        super(enabledProperty);
    }

    protected abstract boolean checkArea(Area area);

    @Override
    protected final void evaluate() {
        setActive(isEnabled() && inArea);
    }

    @Override
    protected void onEnable() {
        // enabled → start listening to area
        if (delegator == null) {
            delegator = new AreaDelegator();
            eventBus.subscribe(delegator);
        }
    }

    @Override
    protected void onDisable() {
        // disabled → stop listening completely
        if (delegator != null) {
            eventBus.unsubscribe(delegator);
            delegator = null;
        }

        inArea = false;
        setActive(false);
    }

    private final class AreaDelegator {

        @EventHandler
        private void onAreaChange(AreaChangeEvent event) {
            inArea = checkArea(event.area);
            evaluate();
        }
    }
}
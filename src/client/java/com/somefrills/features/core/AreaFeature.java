package com.somefrills.features.core;

import com.somefrills.events.AreaChangeEvent;
import com.somefrills.misc.Area;
import com.somefrills.misc.SkyblockData;
import io.github.notenoughupdates.moulconfig.observer.Property;
import meteordevelopment.orbit.EventHandler;

import static com.somefrills.Main.eventBus;

public abstract class AreaFeature extends AbstractFeature {

    private AreaDelegator delegator = null;
    private boolean inArea = false;

    public AreaFeature(Property<Boolean> enabledProperty) {
        super(enabledProperty);
    }

    protected abstract boolean checkArea(Area area);

    @Override
    protected final boolean evaluate() {
        return inArea;
    }

    @Override
    protected void onEnable() {
        // enabled → start listening to area
        if (delegator == null) {
            delegator = new AreaDelegator();
            eventBus.subscribe(delegator);
        }
        inArea = checkArea(SkyblockData.getArea());
    }

    @Override
    protected void onDisable() {
        // disabled → stop listening completely
        if (delegator != null) {
            eventBus.unsubscribe(delegator);
            delegator = null;
        }

        inArea = false;
    }

    private final class AreaDelegator {
        @EventHandler
        private void onAreaChange(AreaChangeEvent event) {
            boolean isInArea = checkArea(event.area);
            if (isInArea != inArea) {
                inArea = isInArea;
                sync();
            }
        }
    }
}
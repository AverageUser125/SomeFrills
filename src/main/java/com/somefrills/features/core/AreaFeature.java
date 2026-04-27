package com.somefrills.features.core;

import com.somefrills.events.AreaChangeEvent;
import com.somefrills.misc.Area;
import com.somefrills.misc.SkyblockData;
import io.github.notenoughupdates.moulconfig.observer.Property;

public abstract class AreaFeature extends AbstractFeature {
    public AreaFeature(Property<Boolean> enabledProperty) {
        super(enabledProperty);
    }

    protected abstract boolean checkArea(Area area);

    @Override
    protected final boolean evaluate() {
        return checkArea(SkyblockData.getArea());
    }

    @Override
    protected final void onEnable() {
        EventSubscriptions.register(this, AreaChangeEvent.class);
    }

    @Override
    protected final void onDisable() {
        EventSubscriptions.unregister(this);
    }
}
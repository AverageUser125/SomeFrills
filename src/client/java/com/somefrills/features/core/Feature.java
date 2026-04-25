package com.somefrills.features.core;

import io.github.notenoughupdates.moulconfig.observer.Property;

public abstract class Feature extends AbstractFeature {

    public Feature(Property<Boolean> enabledProperty) {
        super(enabledProperty);
    }

    @Override
    protected final boolean evaluate() {
        return true;
    }
}
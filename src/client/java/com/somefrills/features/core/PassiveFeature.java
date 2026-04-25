package com.somefrills.features.core;

import io.github.notenoughupdates.moulconfig.observer.Property;

public class PassiveFeature extends AbstractFeature {
    protected PassiveFeature() {
        super(Property.of(false));
    }

    @Override
    protected boolean evaluate() {
        return false;
    }
}

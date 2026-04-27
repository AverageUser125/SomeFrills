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

    @Override
    protected final void onActivate() {
    }

    @Override
    protected final void onDeactivate() {
    }

    @Override
    protected final void onEnable() {
    }

    @Override
    protected final void onDisable() {
    }

}

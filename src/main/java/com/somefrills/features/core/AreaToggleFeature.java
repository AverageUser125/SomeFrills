package com.somefrills.features.core;

import com.somefrills.events.AreaChangeEvent;
import com.somefrills.misc.Area;
import com.somefrills.misc.KeybindManager;
import com.somefrills.misc.SkyblockData;
import io.github.notenoughupdates.moulconfig.observer.Property;

/**
 * A feature that combines area checking (like AreaFeature) with keybind toggling (like ToggleFeature).
 * Only becomes active when BOTH the player is in the correct area AND the keybind is toggled on.
 */
public abstract class AreaToggleFeature extends AbstractFeature {
    private final Property<Integer> keybindProperty;
    private KeybindManager.Subscription keybindSub = null;
    private boolean keybindActive = false;

    public AreaToggleFeature(Property<Boolean> enabledProperty, Property<Integer> keybindProperty) {
        super(enabledProperty);
        this.keybindProperty = keybindProperty;
    }

    /**
     * Check if the current area is the correct one for this feature.
     */
    protected abstract boolean checkArea(Area area);

    /**
     * Both area check AND keybind toggle must be true for the feature to be active.
     */
    @Override
    protected final boolean evaluate() {
        return keybindActive && checkArea(SkyblockData.getArea());
    }

    @Override
    protected final void onEnable() {
        if (keybindSub != null) {
            keybindSub.unregister();
            keybindSub = null;
        }
        keybindSub = KeybindManager.register(keybindProperty, this::toggleActive);
        EventSubscriptions.register(this, AreaChangeEvent.class);
    }

    @Override
    protected final void onDisable() {
        if (keybindSub != null) {
            keybindSub.unregister();
            keybindSub = null;
        }
        keybindActive = false;
        EventSubscriptions.unregister(this);
    }

    protected void toggleActive() {
        if (!isEnabled()) return;
        keybindActive = !keybindActive;
        sync();
    }
}


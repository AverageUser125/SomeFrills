package com.somefrills.misc;

import com.somefrills.events.InputEvent;
import io.github.notenoughupdates.moulconfig.observer.Observer;
import io.github.notenoughupdates.moulconfig.observer.Property;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;
import java.util.List;

public class KeybindManager {
    private static final List<Keybind> keybinds = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onInput(InputEvent event) {
        // TODO: fix holding the key triggering the keybind multiple times per second
        // FIXME: Using Input here from an InputEvent is a bit jank
        // TOCONSIDER: should this call Input or just access event.key directly?
        for (Keybind keybind : keybinds) {
            if (Input.isKeyPressed(keybind.key())) {
                keybind.trigger();
            }
        }
    }

    private static void register(Keybind keybind) {
        keybinds.add(keybind);
    }

    private static void unregister(Keybind keybind) {
        keybinds.remove(keybind);
    }

    public static void register(int keybind, Runnable o) {
        register(new Keybind(keybind, o));
    }

    public static void register(Property<Integer> property, Runnable o) {
        Keybind keybind = new Keybind(property.get(), o);
        property.addObserver(new Observer<>() {
            private Keybind currentKeybind = keybind;
            @Override
            public void observeChange(Integer oldValue, Integer newValue) {
                if (currentKeybind != null) {
                    unregister(currentKeybind);
                }
                currentKeybind = new Keybind(newValue, o);
                register(currentKeybind);
            }
        });

        register(keybind);
    }


    private static class Keybind {
        private final int key;
        private final Runnable callback;

        public Keybind(int key, Runnable callback) {
            this.key = key;
            this.callback = callback;
        }

        public int key() {
            return key;
        }

        public void trigger() {
            callback.run();
        }
    }
}

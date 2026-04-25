package com.somefrills.misc;

import io.github.notenoughupdates.moulconfig.observer.Observer;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.Main.mc;

public class KeybindManager {
    private static final List<Keybind> keybinds = new ArrayList<>();

    public static void onKeyPressed(int key) {
        if (mc.currentScreen != null) return; // don't trigger keybinds while in a GUI
        for (Keybind keybind : keybinds) {
            if (keybind.key == key) {
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
        public final int key;
        public final Runnable callback;

        public Keybind(int key, Runnable callback) {
            this.key = key;
            this.callback = callback;
        }

        public void trigger() {
            callback.run();
        }
    }
}

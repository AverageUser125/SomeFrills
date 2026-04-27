package com.somefrills.misc;

import com.somefrills.mixin.BaseObservableAccessor;
import io.github.notenoughupdates.moulconfig.observer.Observer;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.Main.mc;

public class KeybindManager {
    private static final List<Keybind> keybinds = new ArrayList<>();

    public interface Subscription {
        void unregister();
    }

    public static void onKeyPressed(int key) {
        if (mc.currentScreen != null) return;

        for (Keybind keybind : new ArrayList<>(keybinds)) {
            if (keybind.key == key) {
                keybind.trigger();
            }
        }
    }

    private static Subscription register(Keybind keybind) {
        keybinds.add(keybind);
        return () -> keybinds.remove(keybind);
    }

    public static Subscription register(int key, Runnable action) {
        return register(new Keybind(key, action));
    }

    @SuppressWarnings("unchecked")
    public static Subscription register(Property<Integer> property, Runnable action) {
        Keybind initial = new Keybind(property.get(), action);
        Subscription[] current = new Subscription[]{register(initial)};

        var observer = new Observer<Integer>() {
            @Override
            public void observeChange(Integer oldValue, Integer newValue) {
                Subscription old = current[0];
                current[0] = register(new Keybind(newValue, action));
                if (old != null) old.unregister();
            }
        };
        property.addObserver(observer);

        return () -> {
            ((BaseObservableAccessor<Integer>) property).getObservers().remove(observer);
            current[0].unregister();
        };
    }

    private static class Keybind {
        final int key;
        final Runnable callback;

        Keybind(int key, Runnable callback) {
            this.key = key;
            this.callback = callback;
        }

        void trigger() {
            callback.run();
        }
    }
}
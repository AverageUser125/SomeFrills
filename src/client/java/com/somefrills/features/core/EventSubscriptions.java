package com.somefrills.features.core;

import com.somefrills.Main;
import meteordevelopment.orbit.listeners.IListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class EventSubscriptions {
    private EventSubscriptions() {}

    private static final Map<Object, IListener> listeners = new HashMap<>();

    /**
     * Register callback for event type.
     */
    public static <T> void register(Object owner, Class<T> eventClass, Consumer<T> action) {
        unregister(owner);

        IListener listener = new IListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void call(Object event) {
                action.accept((T) event);
            }

            @Override
            public Class<?> getTarget() {
                return eventClass;
            }

            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            @Deprecated
            public boolean isStatic() {
                return false;
            }
        };

        listeners.put(owner, listener);
        Main.eventBus.subscribe(listener);
    }

    /**
     * Convenience overload for no event usage.
     */
    public static <T> void register(Object owner, Class<T> eventClass, Runnable action) {
        register(owner, eventClass, e -> action.run());
    }

    public static <T> void register(AbstractFeature feature, Class<T> eventClass) {
        register(feature, eventClass, e -> feature.sync());
    }

    public static void unregister(Object owner) {
        IListener listener = listeners.remove(owner);
        if (listener != null) {
            Main.eventBus.unsubscribe(listener);
        }
    }

    public static void clear() {
        for (IListener listener : listeners.values()) {
            Main.eventBus.unsubscribe(listener);
        }
        listeners.clear();
    }
}
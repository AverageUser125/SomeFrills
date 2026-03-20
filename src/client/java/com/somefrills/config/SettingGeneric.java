package com.somefrills.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;

public class SettingGeneric {
    private String key;
    private String parent;
    private final JsonElement defaultValue;
    private JsonElement value;
    private int hash = 0;

    // Only default constructor: keys/parents are assigned by the registry using reflection
    public SettingGeneric(Object defaultValue) {
        this.key = "";
        this.parent = "";
        this.defaultValue = this.parse(defaultValue);
        this.value = this.get();
    }

    public JsonElement parse(Object value) {
        return switch (value) {
            case Boolean bool -> new JsonPrimitive(bool);
            case String string -> new JsonPrimitive(string);
            case Number number -> new JsonPrimitive(number);
            case RenderColor color -> new JsonPrimitive(color.argb);
            case JsonObject jsonObject -> jsonObject;
            case Enum<?> enumValue -> new JsonPrimitive(enumValue.name());
            default ->
                    throw new IllegalStateException(Utils.format("Unexpected value ({}) in {} {} setting class!", value, this.key, this.parent));
        };
    }

    public void update() {
        if (this.value == null || this.hash != Config.getHash()) {
            if (Config.get().has(this.parent)) {
                JsonObject data = Config.get().getAsJsonObject(this.parent);
                this.value = data.has(this.key) ? data.get(this.key) : this.defaultValue;
            } else {
                this.value = this.defaultValue;
            }
            this.hash = Config.getHash();
        }
    }

    public JsonElement get() {
        this.update();
        return this.value;
    }

    public void set(JsonElement value, boolean recompute) {
        if (!Config.get().has(this.parent)) {
            Config.get().add(this.parent, new JsonObject());
        }
        this.value = value;
        Config.get().get(this.parent).getAsJsonObject().add(this.key, this.value);
        if (recompute) {
            Config.computeHash();
        }
    }

    public void set(JsonElement value) {
        this.set(value, true);
    }

    public void set(Object value) {
        this.set(this.parse(value));
    }

    public void reset() {
        this.set(this.defaultValue);
    }

    // helper methods for reflection-based registry
    public void overrideKey(String newKey) {
        this.key = newKey;
    }

    public void overrideParent(String newParent) {
        this.parent = newParent;
    }

    public String getKey() {
        return this.key;
    }

    public String getParent() {
        return this.parent;
    }
}
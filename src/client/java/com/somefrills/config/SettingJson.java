package com.somefrills.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SettingJson extends SettingGeneric {
    public SettingJson(JsonObject defaultValue) {
        super(defaultValue);
    }

    public JsonObject value() {
        JsonElement e = this.get();
        return e == null || e.isJsonNull() ? null : e.getAsJsonObject();
    }

    public void edit(java.util.function.Consumer<JsonObject> editor) {
        JsonObject obj = value();
        if (obj == null) obj = new JsonObject();
        editor.accept(obj);
        this.set(obj);
    }
}
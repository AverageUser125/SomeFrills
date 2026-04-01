package com.somefrills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.LegacyStringChromaColourTypeAdapter;
import io.github.notenoughupdates.moulconfig.managed.DataMapper;
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory;
import org.jspecify.annotations.NonNull;

public class ConfigMapper implements DataMapper<FrillsConfig> {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new PropertyTypeAdapterFactory())
            .registerTypeAdapter(ChromaColour.class, new LegacyStringChromaColourTypeAdapter(true)
            ).create();

    @Override
    public @NonNull String serialize(FrillsConfig frillsConfig) {
        return gson.toJson(frillsConfig);
    }

    @Override
    public FrillsConfig createDefault() {
        return new FrillsConfig();
    }

    @Override
    public FrillsConfig deserialize(@NonNull String s) {
        return gson.fromJson(s, FrillsConfig.class);
    }
}

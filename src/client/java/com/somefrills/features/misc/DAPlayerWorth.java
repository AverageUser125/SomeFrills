package com.somefrills.features.misc;

import com.google.gson.GsonBuilder;
import com.somefrills.Main;
import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.misc.HypixelApiClient;
import com.somefrills.misc.LobbyFinancialUtils;
import com.somefrills.misc.Utils;

import static com.somefrills.Main.mc;

public class DAPlayerWorth extends Feature {
    public DAPlayerWorth() {
        super(FrillsConfig.instance.misc.daPlayerWorth.enabled);
    }

    /**
     * Start fetching and displaying player financial data
     */
    public static void startFetching() {
        HypixelApiClient.fetchPlayerProfile(mc.player.getUuid()).thenAccept(profile -> {
            Main.LOGGER.info("[DEBUG] Raw API Response:\n{}", profile.toString());
            Main.LOGGER.info("[DEBUG] Response (pretty):\n{}", new GsonBuilder().setPrettyPrinting().create().toJson(profile));
        });
    }
}

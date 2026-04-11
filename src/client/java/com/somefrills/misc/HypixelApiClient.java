package com.somefrills.misc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.somefrills.Main;
import net.minecraft.util.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles API calls to fetch SkyBlock player profile data from Hypixel API
 */
public class HypixelApiClient {
    private static final String API_BASE_URL = "https://api.hypixel.net";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();


    /**
     * Fetches a player's SkyBlock profile data asynchronously
     *
     * @param uuid      The player's UUID (without dashes)
     * @param profileId The specific profile ID to fetch (optional, can be null for active profile)
     * @return CompletableFuture containing the profile JSON, or empty if API call fails
     */
    public static CompletableFuture<JsonObject> fetchPlayerProfile(UUID uuid, String profileId) {
        return CompletableFuture.supplyAsync(() -> {
            String apiKey = KeyManager.getKey("hypixel");
            if (apiKey == null || apiKey.isEmpty()) {
                Main.LOGGER.warn("[HypixelApiClient] Hypixel API key not set for UUID: {}", uuid);
                return new JsonObject();
            }

            try {
                String uuidString = uuid.toString().replace("-", "");
                String url = String.format("%s/skyblock/profiles?uuid=%s", API_BASE_URL, uuidString);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("API-KEY", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    Main.LOGGER.warn("[HypixelApiClient] HTTP {} response for UUID: {} - {}", response.statusCode(), uuid, response.body());
                    return new JsonObject();
                }

                JsonObject responseJson = GSON.fromJson(response.body(), JsonObject.class);

                if (!responseJson.has("profiles") || responseJson.get("profiles").isJsonNull()) {
                    Main.LOGGER.warn("[HypixelApiClient] No 'profiles' field in response for UUID: {}", uuid);
                    return new JsonObject();
                }

                var profiles = responseJson.getAsJsonArray("profiles");
                if (profiles.isEmpty()) {
                    Main.LOGGER.warn("[HypixelApiClient] No profiles found for UUID: {}", uuid);
                    return new JsonObject();
                }

                JsonObject profile = profiles.get(0).getAsJsonObject();

                if (profileId != null && !profileId.isEmpty()) {
                    for (int i = 0; i < profiles.size(); i++) {
                        JsonObject p = profiles.get(i).getAsJsonObject();
                        if (p.has("profile_id") && p.get("profile_id").getAsString().equals(profileId)) {
                            profile = p;
                            break;
                        }
                    }
                }

                return profile;
            } catch (Exception e) {
                Main.LOGGER.warn("[HypixelApiClient] Exception fetching profile for UUID {}: {}", uuid, e.getMessage());
                return new JsonObject();
            }
        }, Util.getIoWorkerExecutor());
    }

    @SuppressWarnings("unchecked")
    public static CompletableFuture<JsonObject>[] fetchPlayerProfiles(UUID... uuids) {
        CompletableFuture<JsonObject>[] futures = new CompletableFuture[uuids.length];

        for (int i = 0; i < uuids.length; i++) {
            futures[i] = fetchPlayerProfile(uuids[i], null);
        }

        return futures;
    }
}



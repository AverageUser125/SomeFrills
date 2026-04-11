package com.somefrills.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.somefrills.Main;
import net.minecraft.util.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Handles API calls to fetch SkyBlock player profile data from Hypixel API
 */
public class HypixelApiClient {
    private static final String API_BASE_URL = "https://api.hypixel.net/v2/skyblock";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static volatile boolean fatalError = false;
    private static volatile String fatalErrorMessage = "";


    /**
     * Fetches a player's SkyBlock profile data asynchronously
     *
     * @param uuid      The player's UUID (without dashes)
     * @param profileId The specific profile ID to fetch (optional, can be null for active profile)
     * @return CompletableFuture containing the profile JSON, or empty if API call fails
     */
    public static CompletableFuture<JsonObject> fetchPlayerProfile(UUID uuid, String profileId) {
        return CompletableFuture.supplyAsync(() -> {
            // Check if fatal error already occurred
            if (fatalError) {
                Main.LOGGER.info("[HypixelApiClient] Skipping request for UUID {} - Fatal error: {}", uuid, fatalErrorMessage);
                return new JsonObject();
            }

            String apiKey = KeyManager.getKey("hypixel");
            if (apiKey == null || apiKey.isEmpty()) {
                Main.LOGGER.warn("[HypixelApiClient] Hypixel API key not set for UUID: {}", uuid);
                return new JsonObject();
            }

            try {
                String uuidString = uuid.toString().replace("-", "");
                String url = String.format("%s/profiles?uuid=%s", API_BASE_URL, uuidString);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("API-KEY", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    // Check for fatal errors
                    if (response.statusCode() == 404 || response.body().contains("deprecated")) {
                        fatalError = true;
                        fatalErrorMessage = "API endpoint deprecated (404)";
                        Main.LOGGER.error("[HypixelApiClient] FATAL ERROR: {}", fatalErrorMessage);
                        Main.LOGGER.error("[HypixelApiClient] Stopping all requests. Please update the mod or use a different API.");
                        Utils.infoFormat("§c[SomeFrills] FATAL ERROR: API endpoint deprecated. The Hypixel API endpoint is no longer available.");
                        return new JsonObject();
                    }

                    Main.LOGGER.warn("[HypixelApiClient] HTTP {} response for UUID: {} - {}", response.statusCode(), uuid, response.body());
                    return new JsonObject();
                }

                JsonObject responseJson = GSON.fromJson(response.body(), JsonObject.class);

                // DEBUG: Log the raw response
                Main.LOGGER.info("[HypixelApiClient] DEBUG Raw Response:\n{}", new GsonBuilder().setPrettyPrinting().create().toJson(responseJson));

                // Check if request was successful
                if (!responseJson.has("success") || !responseJson.get("success").getAsBoolean()) {
                    Main.LOGGER.warn("[HypixelApiClient] API returned success: false for UUID: {}", uuid);
                    return new JsonObject();
                }

                // No profiles field = player has no profile or profile is private
                if (!responseJson.has("profiles") || responseJson.get("profiles").isJsonNull()) {
                    Main.LOGGER.info("[HypixelApiClient] No profiles found for UUID: {} (private or no profile)", uuid);
                    return new JsonObject();
                }

                var profiles = responseJson.getAsJsonArray("profiles");
                if (profiles.isEmpty()) {
                    Main.LOGGER.info("[HypixelApiClient] Profiles array is empty for UUID: {}", uuid);
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

    /**
     * Check if a fatal error has occurred
     */
    public static boolean hasFatalError() {
        return fatalError;
    }

    /**
     * Get the fatal error message
     */
    public static String getFatalErrorMessage() {
        return fatalErrorMessage;
    }

    /**
     * Reset fatal error state (for testing or manual reset)
     */
    public static void resetFatalError() {
        fatalError = false;
        fatalErrorMessage = "";
        Main.LOGGER.info("[HypixelApiClient] Fatal error state reset");
    }

    @SuppressWarnings("unchecked")
    public static CompletableFuture<JsonObject>[] fetchPlayerProfiles(UUID... uuids) {
        CompletableFuture<JsonObject>[] futures = new CompletableFuture[uuids.length];

        for (int i = 0; i < uuids.length; i++) {
            futures[i] = fetchPlayerProfile(uuids[i], null);
        }

        return futures;
    }

    public static CompletableFuture<JsonObject> fetchPlayerProfile(UUID first) {
        return fetchPlayerProfile(first, null);
    }
}

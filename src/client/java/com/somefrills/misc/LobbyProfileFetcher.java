package com.somefrills.misc;

import com.google.gson.JsonObject;
import com.somefrills.Main;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Fetches and aggregates SkyBlock profile data (purse and bank) for multiple players.
 * This class handles the retrieval of profile information from the Hypixel SkyBlock API.
 */
public class LobbyProfileFetcher {

    /**
     * Represents aggregated financial data for a player
     */
    public static class PlayerFinancials {
        public final UUID uuid;
        public final String playerName;
        public final long purse;
        public final long totalBank;
        public final long totalWealth;

        public PlayerFinancials(UUID uuid, String playerName, long purse, long totalBank) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.purse = purse;
            this.totalBank = totalBank;
            this.totalWealth = purse + totalBank;
        }

        @Override
        public String toString() {
            return String.format("%s: Purse=%,d, Bank=%,d, Total=%,d",
                    playerName, purse, totalBank, totalWealth);
        }
    }

    /**
     * Fetches financial data for all players in the lobby
     *
     * @param playerUuids List of player UUIDs to fetch data for
     * @return CompletableFuture containing a map of UUID to PlayerFinancials
     */
    public static CompletableFuture<Map<UUID, PlayerFinancials>> fetchLobbyFinancials(List<UUID> playerUuids) {
        List<CompletableFuture<PlayerFinancials>> futures = playerUuids.stream()
                .map(LobbyProfileFetcher::fetchPlayerFinancials)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(a -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(f -> f.uuid, f -> f))
                );
    }

    /**
     * Fetches financial data for a single player
     *
     * @param uuid Player UUID
     * @return CompletableFuture containing the player's financial data, or null if fetch fails
     */
    public static CompletableFuture<PlayerFinancials> fetchPlayerFinancials(UUID uuid) {
        return HypixelApiClient.fetchPlayerProfile(uuid, null)
                .thenApply(profileJson -> {
                    if (profileJson.entrySet().isEmpty()) {
                        return null; // API call failed
                    }

                    String playerName = uuid.toString(); // Default to UUID if name not available
                    return extractFinancialsFromProfile(profileJson, uuid, playerName);
                });
    }

    /**
     * Extracts purse and bank totals from a player's profile JSON data
     *
     * @param profileJson The player's profile data from the API
     * @param playerUuid  The player's UUID
     * @param playerName  The player's name
     * @return PlayerFinancials containing the extracted data
     */
    public static PlayerFinancials extractFinancialsFromProfile(JsonObject profileJson, UUID playerUuid, String playerName) {
        long purse = 0;
        long totalBank = 0;

        try {
            // Get the members object
            if (!profileJson.has("members")) {
                return new PlayerFinancials(playerUuid, playerName, purse, totalBank);
            }

            JsonObject members = profileJson.getAsJsonObject("members");
            String uuidString = playerUuid.toString().replace("-", "");

            // Get the member data for this UUID
            if (!members.has(uuidString)) {
                return new PlayerFinancials(playerUuid, playerName, purse, totalBank);
            }

            JsonObject member = members.getAsJsonObject(uuidString);

            // Extract purse from currencies.coin_purse
            if (member.has("currencies")) {
                JsonObject currencies = member.getAsJsonObject("currencies");
                if (currencies.has("coin_purse")) {
                    purse = currencies.get("coin_purse").getAsLong();
                }
            }

            // Extract profile bank from profile.banking.balance (at profile level)
            if (profileJson.has("banking")) {
                JsonObject banking = profileJson.getAsJsonObject("banking");
                if (banking.has("balance")) {
                    totalBank += banking.get("balance").getAsLong();
                }
            }

            // Extract solo bank from profile.bank_account (at member level)
            if (member.has("profile")) {
                JsonObject profile = member.getAsJsonObject("profile");
                if (profile.has("bank_account")) {
                    totalBank += profile.get("bank_account").getAsLong();
                }
            }
        } catch (Exception e) {
            Main.LOGGER.debug("[LobbyProfileFetcher] Error extracting financials: {}", e.getMessage());
        }

        return new PlayerFinancials(playerUuid, playerName, purse, totalBank);
    }

    /**
     * Calculates total wealth statistics for a lobby
     *
     * @param financials Map of player financials
     * @return Summary statistics
     */
    public static String getLobbyFinancialsSummary(Map<UUID, PlayerFinancials> financials) {
        if (financials.isEmpty()) {
            return "No financial data available";
        }

        long totalPurse = financials.values().stream().mapToLong(f -> f.purse).sum();
        long totalBank = financials.values().stream().mapToLong(f -> f.totalBank).sum();
        long totalWealth = financials.values().stream().mapToLong(f -> f.totalWealth).sum();
        double averageWealth = (double) totalWealth / financials.size();

        return String.format(
                "Lobby Stats: %d Players | Total Purse: %,d | Total Bank: %,d | Total Wealth: %,d | Avg Wealth: %,.0f",
                financials.size(), totalPurse, totalBank, totalWealth, averageWealth
        );
    }
}



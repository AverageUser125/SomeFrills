package com.somefrills.misc;

import com.somefrills.Main;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.somefrills.Main.mc;

/**
 * Utility class for retrieving financial data from all players in the current lobby.
 * Supports both batch fetching and streaming progressive results as data arrives.
 */
public class LobbyFinancialUtils {

    /**
     * Gets all player UUIDs currently visible in the player list
     *
     * @return List of UUIDs of players in the lobby
     */
    public static List<UUID> getLobbyPlayerUuids() {
        List<UUID> uuids = new ArrayList<>();

        if (mc.inGameHud == null) {
            Main.LOGGER.info("[LobbyFinancialUtils] inGameHud is null");
            return uuids;
        }

        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();
        if (playerListHud == null) {
            Main.LOGGER.info("[LobbyFinancialUtils] playerListHud is null");
            return uuids;
        }

        for (PlayerListEntry entry : playerListHud.collectPlayerEntries()) {
            if (entry != null && entry.getProfile() != null) {
                uuids.add(entry.getProfile().id());
            }
        }
        // TODO: filter mc.player.getUuid() out of the list
        Main.LOGGER.info("[LobbyFinancialUtils] Found {} players in lobby", uuids.size());
        return uuids;
    }

    /**
     * Gets all player UUIDs and names currently visible in the player list
     *
     * @return Map of UUID to player name
     */
    public static Map<UUID, String> getLobbyPlayersWithNames() {
        Map<UUID, String> players = new HashMap<>();

        if (mc.inGameHud == null) return players;

        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();
        if (playerListHud == null) return players;

        for (PlayerListEntry entry : playerListHud.collectPlayerEntries()) {
            if (entry != null && entry.getProfile() != null) {
                players.put(entry.getProfile().id(), entry.getProfile().name());
            }
        }

        return players;
    }

    /**
     * Gets individual futures for each lobby player's financial data.
     * Results arrive progressively as each request completes, allowing real-time updates.
     *
     * @return List of CompletableFutures, each containing a player's financial data
     */
    public static List<CompletableFuture<LobbyProfileFetcher.PlayerFinancials>> streamLobbyFinancials() {
        Main.LOGGER.info("[LobbyFinancialUtils] Starting to stream lobby financials");
        List<UUID> lobbyUuids = getLobbyPlayerUuids();
        Map<UUID, String> playerNames = getLobbyPlayersWithNames();

        Main.LOGGER.info("[LobbyFinancialUtils] Creating futures for {} players", lobbyUuids.size());
        return lobbyUuids.stream()
                .map(uuid -> LobbyProfileFetcher.fetchPlayerFinancials(uuid)
                        .thenApply(data -> {
                            if (data == null) {
                                Main.LOGGER.warn("[LobbyFinancialUtils] Failed to fetch data for UUID: {}", uuid);
                                return null;
                            }
                            String name = playerNames.getOrDefault(uuid, data.playerName);
                            Main.LOGGER.info("[LobbyFinancialUtils] Received data for: {} (wealth: {})", name, data.totalWealth);
                            return new LobbyProfileFetcher.PlayerFinancials(uuid, name, data.purse, data.totalBank);
                        })
                        .exceptionally(ex -> {
                            Main.LOGGER.warn("[LobbyFinancialUtils] Exception fetching data for UUID {}: {}", uuid, ex.getMessage());
                            return null;
                        })
                )
                .collect(Collectors.toList());
    }

    /**
     * Fetches lobby financials with a callback for each result as it arrives.
     * Allows you to process results progressively without waiting for all to complete.
     *
     * @param onResult   Consumer called with each completed PlayerFinancials
     * @param onComplete Runnable called when all requests are finished
     * @return CompletableFuture that completes when all processing is done
     */
    public static CompletableFuture<Void> streamLobbyFinancialsWithCallback(
            Consumer<LobbyProfileFetcher.PlayerFinancials> onResult,
            Runnable onComplete) {

        Main.LOGGER.info("[LobbyFinancialUtils] Setting up streaming with callbacks");
        List<CompletableFuture<LobbyProfileFetcher.PlayerFinancials>> futures = streamLobbyFinancials();
        Main.LOGGER.info("[LobbyFinancialUtils] Created {} futures", futures.size());

        // Attach callbacks to each future as it completes
        futures.forEach(future ->
                future.thenAccept(data -> {
                    if (data != null) {
                        Main.LOGGER.info("[LobbyFinancialUtils] Processing result for: {}", data.playerName);
                        onResult.accept(data);
                    }
                })
        );

        // Return a future that completes when all are done
        @SuppressWarnings("unchecked")
        CompletableFuture<Void> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    Main.LOGGER.info("[LobbyFinancialUtils] All financials streaming completed");
                    onComplete.run();
                });
        return result;
    }

    /**
     * Similar to streamLobbyFinancialsWithCallback but without requiring a completion callback.
     *
     * @param onResult Consumer called with each completed PlayerFinancials
     * @return CompletableFuture that completes when all requests are finished
     */
    public static CompletableFuture<Void> streamLobbyFinancials(
            Consumer<LobbyProfileFetcher.PlayerFinancials> onResult) {
        Main.LOGGER.info("[LobbyFinancialUtils] Starting streaming lobby financials");
        return streamLobbyFinancialsWithCallback(onResult, () ->
            Main.LOGGER.info("[LobbyFinancialUtils] Streaming completed"));
    }

    /**
     * Fetches financial data for all players in the lobby
     *
     * @return CompletableFuture containing map of UUID to PlayerFinancials
     */
    public static CompletableFuture<Map<UUID, LobbyProfileFetcher.PlayerFinancials>> fetchAllLobbyFinancials() {
        List<UUID> lobbyUuids = getLobbyPlayerUuids();
        Map<UUID, String> playerNames = getLobbyPlayersWithNames();

        // Fetch all financials
        return LobbyProfileFetcher.fetchLobbyFinancials(lobbyUuids)
                .thenApply(financials -> {
                    // Enhance with player names if available
                    Map<UUID, LobbyProfileFetcher.PlayerFinancials> enhanced = new HashMap<>();

                    for (var entry : financials.entrySet()) {
                        UUID uuid = entry.getKey();
                        LobbyProfileFetcher.PlayerFinancials data = entry.getValue();
                        String name = playerNames.getOrDefault(uuid, data.playerName);

                        enhanced.put(uuid, new LobbyProfileFetcher.PlayerFinancials(
                                uuid, name, data.purse, data.totalBank
                        ));
                    }

                    return enhanced;
                });
    }

    /**
     * Fetches and sorts financial data by total wealth
     *
     * @return CompletableFuture containing list of PlayerFinancials sorted by wealth (highest first)
     */
    public static CompletableFuture<List<LobbyProfileFetcher.PlayerFinancials>> fetchAndSortByWealth() {
        return fetchAllLobbyFinancials()
                .thenApply(financials -> financials.values().stream()
                        .sorted((a, b) -> Long.compare(b.totalWealth, a.totalWealth))
                        .collect(Collectors.toList())
                );
    }

    /**
     * Fetches and sorts financial data by purse amount
     *
     * @return CompletableFuture containing list of PlayerFinancials sorted by purse (highest first)
     */
    public static CompletableFuture<List<LobbyProfileFetcher.PlayerFinancials>> fetchAndSortByPurse() {
        return fetchAllLobbyFinancials()
                .thenApply(financials -> financials.values().stream()
                        .sorted((a, b) -> Long.compare(b.purse, a.purse))
                        .collect(Collectors.toList())
                );
    }

    /**
     * Fetches and sorts financial data by bank amount
     *
     * @return CompletableFuture containing list of PlayerFinancials sorted by bank (highest first)
     */
    public static CompletableFuture<List<LobbyProfileFetcher.PlayerFinancials>> fetchAndSortByBank() {
        return fetchAllLobbyFinancials()
                .thenApply(financials -> financials.values().stream()
                        .sorted((a, b) -> Long.compare(b.totalBank, a.totalBank))
                        .collect(Collectors.toList())
                );
    }
}





package com.somefrills.misc;

/**
 * Example usage of the streaming financial data fetcher
 */
public class StreamingFinancialsExample {

    /**
     * Example 1: Simple streaming with callback - results appear as they arrive
     */
    public static void exampleStreamWithCallback() {
        LobbyFinancialUtils.streamLobbyFinancials(financials -> {
            // This callback fires each time a player's data arrives
            System.out.println("Got data for: " + financials.playerName);
            System.out.println("  Purse: " + financials.purse);
            System.out.println("  Bank: " + financials.totalBank);
            System.out.println("  Total: " + financials.totalWealth);
            // You can render/update UI here as each player's data arrives
        });
    }

    /**
     * Example 2: Streaming with both callbacks
     */
    public static void exampleStreamWithCallbackAndCompletion() {
        LobbyFinancialUtils.streamLobbyFinancialsWithCallback(
                financials -> {
                    // Process each result as it arrives
                    System.out.println("Player data received: " + financials.playerName);
                },
                () -> {
                    // Called when all requests are done
                    System.out.println("All lobby data fetched!");
                }
        );
    }

    /**
     * Example 3: Get list of futures to handle manually
     * This gives you more control - you can do whatever you want with each future
     */
    public static void exampleManualFutureHandling() {
        var futures = LobbyFinancialUtils.streamLobbyFinancials();

        futures.forEach(future -> {
            future.thenAccept(financials -> {
                if (financials != null) {
                    System.out.println("Processing: " + financials.playerName);
                    // Do whatever you want with each result
                }
            });
        });
    }

    /**
     * Example 4: React to results in real-time for UI rendering
     * This is ideal for displaying a leaderboard that fills in as data arrives
     */
    public static void exampleRealTimeUI() {
        LobbyFinancialUtils.streamLobbyFinancials(financials -> {
            // Each time data arrives, update your rendering list
            // This gives you a smooth progressive loading experience
            MyRenderingModule.addPlayerToDisplay(
                    financials.playerName,
                    financials.purse,
                    financials.totalBank,
                    financials.totalWealth
            );
        });
    }

    /**
     * How to understand the streaming:
     * <p>
     * Traditional approach (wait for everything):
     * Request 1 ---[WAIT]--- Response 1
     * Request 2 ---[WAIT]--- Response 2
     * Request 3 ---[WAIT]--- Response 3
     * All complete -> Process all together
     * <p>
     * Streaming approach (show as results arrive):
     * Request 1 --- Response 1 -> Show immediately
     * Request 2 --- Response 2 -> Show immediately
     * Request 3 --- Response 3 -> Show immediately
     * Much better UX!
     */

    // Placeholder for your rendering code
    static class MyRenderingModule {
        static void addPlayerToDisplay(String name, long purse, long bank, long total) {
            // Your rendering code here
        }
    }
}


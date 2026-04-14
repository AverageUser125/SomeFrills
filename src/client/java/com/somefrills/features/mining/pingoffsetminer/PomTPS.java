package com.somefrills.features.mining.pingoffsetminer;


import java.util.concurrent.ConcurrentLinkedQueue;

public class PomTPS {
    private final ConcurrentLinkedQueue<Float> tickIntervals = new ConcurrentLinkedQueue<>();
    private long lastUpdate = -1;

    public void addLatency(long realTime) {
        long timeElapsed = realTime - lastUpdate;

        if (timeElapsed < 100) return;

        tickIntervals.add((float) timeElapsed);

        int MAX = 20;
        while (tickIntervals.size() > MAX) {
            tickIntervals.poll();
        }

        lastUpdate = realTime;
    }

    public double getAverageLatency() {
        if (tickIntervals.isEmpty()) return 20.0;

        double averageTicks = tickIntervals.stream()
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(1000.0);

        double tps = 20000.0 / averageTicks;

        return Math.min(20.0, tps);
    }

    public void gameJoin() {
        tickIntervals.clear();
        lastUpdate = System.currentTimeMillis();
    }

}
package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.PingOffsetMinerConfig;
import com.somefrills.events.*;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PingOffsetMiner extends Feature {
    private final PingOffsetMinerConfig config;
    private final PomTPS pomTPS = new PomTPS();
    private final PomPing pomPing = new PomPing();
    private final MiningStats miningStats = new MiningStats();

    public PingOffsetMiner() {
        super(FrillsConfig.instance.mining.pingOffsetMiner.enabled);
        config = FrillsConfig.instance.mining.pingOffsetMiner;
    }

    @EventHandler
    public void onTabUpdate(TabListUpdateEvent event) {
        for (String line : event.lines) {
            double speed = extractSpeedValue(line);
            if (speed != -1) {
                double tps = pomTPS.getAverageLatency();
                double ping = pomPing.getAverageLatency();
                double offset = (20.0 - tps) * config.tpsOffsetMultiplier + ping * config.pingOffsetMultiplier;
                double adjustedSpeed = speed + offset;
                miningStats.setSpeed(speed);
                // Check for ability in tab
                if (line.contains("speed boost: available!")) {
                    miningStats.setBoost(false);
                }
            }
        }
    }

    @EventHandler
    public void onServerJoin(ServerJoinEvent event) {
        pomTPS.gameJoin();
        pomPing.clear();
        miningStats.reset();
    }

    @EventHandler
    public void onHeldSlot(OnHeldSlotEvent event) {
        miningStats.setItem(event.itemStack);
    }

    @EventHandler
    public void onChatMsg(ChatMsgEvent event) {
        if (config.ability) {
            String plain = event.messagePlain;
            if (plain.contains("you used your mining speed boost pickaxe ability!")) {
                miningStats.setBoost(true);
            }
        }
    }

    @EventHandler
    public void onPacket(ReceivePacketEvent event) {
        var packet = event.packet;
        if (packet instanceof PingResultS2CPacket(long startTime)) {
            long delta = System.currentTimeMillis() - startTime;
            pomPing.addLatency(delta);
            return;
        }
        if (packet instanceof WorldTimeUpdateS2CPacket) {
            long time = System.currentTimeMillis();
            pomTPS.addLatency(time);
        }
    }

    private double extractSpeedValue(String displayName) {
        try {
            String[] parts = displayName.split("⸕");
            if (parts.length > 1) {
                return Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    public static class MiningStats {
        private ItemStack item = ItemStack.EMPTY;
        private double speed = -1;
        private boolean boost = false;
        private int cooldown = -1;

        private boolean isTool(ItemStack stack) {
            if (stack.isEmpty()) return false;
            List<String> tooltip = Utils.getToolTip(stack);
            for (String text : tooltip.reversed()) {
                if (text.contains(" DRILL ") || text.contains(" GAUNTLET ") || text.contains(" PICKAXE ")) {
                    return true;
                }
            }
            return false;
        }

        private int getCooldown(ItemStack stack) {
            List<String> tooltip = Utils.getToolTip(stack);
            boolean found = false;
            for (String line : tooltip) {
                String text = Utils.toPlain(line);
                if (!found && text.contains("Ability: Mining Speed")) {
                    found = true;
                    continue;
                }
                if (found) {
                    try {
                        String[] parts = text.split("\\s+");
                        for (String part : parts) {
                            if (part.matches("[0-9.]+s")) {
                                return (int) Double.parseDouble(part.replace("s", ""));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            return -1;
        }

        public void setItem(ItemStack newItem) {
            if (isTool(newItem)) {
                this.item = newItem;
                this.cooldown = getCooldown(newItem);
            } else {
                this.item = ItemStack.EMPTY;
                this.cooldown = -1;
            }
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public void setBoost(boolean boost) {
            this.boost = boost;
        }

        public void reset() {
            this.item = ItemStack.EMPTY;
            this.speed = -1;
            this.boost = false;
            this.cooldown = -1;
        }

        public double getSpeed() {
            return speed;
        }

        public boolean getBoost() {
            return boost;
        }

        public int getCooldown() {
            return cooldown;
        }
    }

    public static class PomTPS {
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

    public static class PomPing {


        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();

        final int MAX_LATENCIES = 20;

        public void addLatency(long latency) {
            latencies.add(latency);
            while (latencies.size() > MAX_LATENCIES) {
                latencies.poll();
            }
        }

        public long getAverageLatency() {
            List<Long> latencyList = latencies.stream().toList();
            long sum = latencyList.stream().mapToLong(Long::longValue).sum();
            return latencyList.isEmpty() ? 0 : sum / latencyList.size();
        }

        public void clear() {
            latencies.clear();
        }
    }

}

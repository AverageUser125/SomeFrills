package com.somefrills.features.mining.pingoffsetminer;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.PingOffsetMinerConfig;
import com.somefrills.events.*;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import static com.somefrills.Main.mc;

public class PingOffsetMiner extends Feature {
    private final PingOffsetMinerConfig config;
    private final PomTPS pomTPS = new PomTPS();
    private final PomPing pomPing = new PomPing();
    private final MiningStats miningStats = new MiningStats();
    private final PomBlock pomBlock = new PomBlock();

    private double lastDetectedSpeed = -1;
    private int cooldownTicks = 0;

    public PingOffsetMiner() {
        super(FrillsConfig.instance.mining.pingOffsetMiner.enabled);
        config = FrillsConfig.instance.mining.pingOffsetMiner;
    }

    @EventHandler
    public void onTabUpdate(TabListUpdateEvent event) {
        for (String line : event.lines) {
            double speed = extractSpeedValue(line);
            if (speed != -1) {
                lastDetectedSpeed = speed;
                miningStats.setSpeed(speed);

                // Log speed if logging enabled
                if (config.logging) {
                    double tps = pomTPS.getAverageLatency();
                    long ping = pomPing.getAverageLatency();
                    double offset = (20.0 - tps) * config.tpsOffsetMultiplier + ping * config.pingOffsetMultiplier;
                    Utils.infoFormat("Speed: {} | TPS: {} | Ping: {}ms | Offset: {}",
                        Utils.formatDecimal(speed, 1),
                        Utils.formatDecimal(tps, 2),
                        ping,
                        Utils.formatDecimal(offset, 1)
                    );
                }

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
        lastDetectedSpeed = -1;
    }

    @EventHandler
    public void onHeldSlot(OnHeldSlotEvent event) {
        miningStats.setItem(event.itemStack);
    }

    @EventHandler
    public void onChatMsg(ChatMsgEvent event) {
        if (!config.ability) return;
        String plain = Formatting.strip(event.messagePlain);
        if (plain.contains("you used your mining speed boost pickaxe ability")) {
            miningStats.setBoost(true);
            if (config.logging) {
                Utils.info("Mining speed ability used!");
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

    @EventHandler
    public void onWorldTick(WorldTickEvent event) {
        if (!config.active) return;

        // Update block targeting
        pomBlock.setBlock();

        // Update cooldown
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        // Check if speed was found
        if (lastDetectedSpeed == -1 && config.shouldWarn) {
            Utils.infoFormat("§cMining speed not detected");
        }
    }


    public double getTicksToBreak(String blockName) {
        double speed = getCalculatedSpeed();
        if (speed == -1) return -1;
        int hardness = SpeedCalc.blockHardness.getOrDefault(blockName, -1);
        return SpeedCalc.getTicksToBreak(hardness, speed);
    }

    public double getTicksToBreakBlock(net.minecraft.block.Block block) {
        String blockName = SpeedCalc.getBlockName(block);
        return getTicksToBreak(blockName);
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

    private double getCalculatedSpeed() {
        if (config.debug) {
            return config.speed;
        }

        double baseSpeed = lastDetectedSpeed;
        if (baseSpeed == -1) return -1;

        if (config.extra && miningStats.getSpeed() > -1) {
            baseSpeed += config.extraVal;
        }

        double tps = pomTPS.getAverageLatency();
        long ping = pomPing.getAverageLatency();
        double offset = (20.0 - tps) * config.tpsOffsetMultiplier + ping * config.pingOffsetMultiplier;

        return baseSpeed + offset;
    }


    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (pomBlock.isEmpty()) return;

        BlockPos blockPos = pomBlock.getBlockPos();
        if (blockPos == null) return;

        // FIXME: we should use the blockstate
        if (pomBlock.getShape() == null) return;

        // Get colors from config
        var cfg = FrillsConfig.instance.mining.pingOffsetMiner;
        RenderColor lineColor = RenderColor.fromChroma(cfg.line.color);
        RenderColor highlightColor = RenderColor.fromChroma(cfg.highlight.color);

        // Draw the block's voxel shape
        for (Box box : pomBlock.getShape().getBoundingBoxes()) {
            Box offsetBox = box.offset(pomBlock.getBlockPos());

            // Draw highlight (filled)
            if (cfg.highlight.active) {
                event.drawFilled(offsetBox, true, highlightColor);
            }

            // Draw outline
            if (cfg.line.active) {
                event.drawOutline(offsetBox, true, lineColor);
            }
        }
    }

}

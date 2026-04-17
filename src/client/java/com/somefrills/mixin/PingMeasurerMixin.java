package com.somefrills.mixin;

import com.somefrills.Main;
import com.somefrills.events.PingEvent;
import com.somefrills.events.ReceivePacketEvent;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PingMeasurer.class)
public class PingMeasurerMixin {
    /**
     * @author Naor
     * @reason Just cause
     */
    @Overwrite
    public void onPingResult(PingResultS2CPacket packet) {
        long delta = System.currentTimeMillis() - packet.startTime();;
        Main.eventBus.post(new PingEvent(delta));
    }
}

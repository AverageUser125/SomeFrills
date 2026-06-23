package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.somefrills.events.HudRenderEvent;
import com.somefrills.events.HudTickEvent;
import com.somefrills.features.misc.Freecam;
import com.somefrills.mixininterface.TitleRendering;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

@Mixin(Gui.class)
public abstract class GuiMixin implements TitleRendering {
    @Unique
    private static int titleTicks = 0;

    @Shadow
    public abstract Font getFont();

    @Override
    public boolean somefrills$isRenderingTitle() {
        return titleTicks > 0;
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void onTickHud(CallbackInfo ci) {
        if (titleTicks > 0) {
            titleTicks--;
        }
        eventBus.post(new HudTickEvent());
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!mc.options.hideGui) {
            eventBus.post(new HudRenderEvent(graphics, this.getFont(), deltaTracker));
        }
    }

    @ModifyExpressionValue(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean alwaysRenderCrosshairInFreecam(boolean firstPerson) {
        return Freecam.INSTANCE.isActive() || firstPerson;
    }
}

package com.somefrills.mixin;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.InputEvent;
import com.somefrills.features.core.Features;
import com.somefrills.features.farming.SpaceFarmer;
import com.somefrills.features.misc.SaveCursorPosition;
import kotlin.Pair;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    private double x;
    @Shadow
    private double y;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(input, action)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "lockCursor", at = @At("HEAD"))
    public void onLockCursor(CallbackInfo ci) {
        Features.get(SaveCursorPosition.class).saveCursorOriginal(x, y);
    }

    @Inject(method = "lockCursor", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;getWindow()Lnet/minecraft/client/util/Window;",
            ordinal = 2
    ))
    public void onLockCursorAfter(CallbackInfo ci) {
        Features.get(SaveCursorPosition.class).saveCursorMiddle(x, y);
    }

    @Inject(method = "unlockCursor", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;getWindow()Lnet/minecraft/client/util/Window;",
            ordinal = 2
    ))

    public void onUnlockCursor(CallbackInfo ci) {
        Pair<Double, Double> cursorPosition = Features.get(SaveCursorPosition.class).loadCursor(this.x, this.y);
        if (cursorPosition == null) return;
        this.x = cursorPosition.getFirst();
        this.y = cursorPosition.getSecond();
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onMouseMove(double timeDelta, CallbackInfo ci) {
        if (FrillsConfig.instance.farming.spaceFarmerEnabled.get() && SpaceFarmer.spaceHeld && mc.options.attackKey.isPressed()) {
            ci.cancel();
        }
    }
}

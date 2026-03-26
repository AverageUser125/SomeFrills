package com.somefrills.mixin;

import com.somefrills.events.InputEvent;
import com.somefrills.features.farming.SpaceFarmer;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        if (eventBus.post(new InputEvent(input, action)).isCancelled()) {
            ci.cancel();
        }
    }
    /*
    @Inject(method = "onCursorPos", at = @At("TAIL"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
    }
     */

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onMouseMove(double timeDelta, CallbackInfo ci) {
        if (SpaceFarmer.instance.isActive() && SpaceFarmer.spaceHeld && mc.options.attackKey.isPressed()) {
            ci.cancel();
        }
    }
}

package com.somefrills.mixin;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.MouseClickEvent;
import com.somefrills.events.MouseScrollEvent;
import com.somefrills.features.core.Features;
import com.somefrills.features.farming.SpaceFarmer;
import com.somefrills.features.misc.SaveCursorPosition;
import com.somefrills.misc.Input;
import com.somefrills.misc.KeyAction;
import kotlin.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    private double x;
    @Shadow
    private double y;

    @Shadow
    public abstract double getScaledX(Window window);

    @Shadow
    public abstract double getScaledY(Window window);

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseInput mouseInput, int action, CallbackInfo ci) {
        Input.setButtonState(mouseInput.button(), action != GLFW_RELEASE);

        Click click = new Click(getScaledX(client.getWindow()), getScaledY(client.getWindow()), mouseInput);
        if (eventBus.post(new MouseClickEvent(click, KeyAction.get(action))).isCancelled()) {
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

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        if (eventBus.post(new MouseScrollEvent(vertical)).isCancelled()) info.cancel();
    }
}

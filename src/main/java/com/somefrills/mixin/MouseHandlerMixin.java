package com.somefrills.mixin;

import com.somefrills.events.MouseClickEvent;
import com.somefrills.events.MouseScrollEvent;
import com.somefrills.features.misc.SaveCursorPosition;
import com.somefrills.misc.KeyAction;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Shadow
    public abstract double getScaledXPos(Window window);

    @Shadow
    public abstract double getScaledYPos(Window window);

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo mouseInput, int action, CallbackInfo ci) {
        if (mouseInput == null) {
            ci.cancel();
            return;
        }

        try {
            //Input.setButtonState(mouseInput.button(), action != GLFW_RELEASE);

            MouseButtonEvent click = new Click(getScaledXPos(minecraft.getWindow()), getScaledYPos(minecraft.getWindow()), mouseInput);
            if (eventBus.post(new MouseClickEvent(click, KeyAction.get(action))).isCancelled()) {
                ci.cancel();
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "grabMouse", at = @At("HEAD"))
    public void onLockCursor(CallbackInfo ci) {
        SaveCursorPosition.saveCursorOriginal(xpos, ypos);
    }

    @Inject(method = "grabMouse", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;",
            ordinal = 2
    ))
    public void onLockCursorAfter(CallbackInfo ci) {
        SaveCursorPosition.saveCursorMiddle(xpos, ypos);
    }

    @Inject(method = "releaseMouse", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;",
            ordinal = 2
    ))

    public void onUnlockCursor(CallbackInfo ci) {
        Pair<Double, Double> cursorPosition = SaveCursorPosition.loadCursor(this.xpos, this.ypos);
        if (cursorPosition == null) return;
        this.xpos = cursorPosition.getFirst();
        this.ypos = cursorPosition.getSecond();
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        if (eventBus.post(new MouseScrollEvent(vertical)).isCancelled()) info.cancel();
    }
}

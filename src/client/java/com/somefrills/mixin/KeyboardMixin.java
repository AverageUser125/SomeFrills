package com.somefrills.mixin;

import com.somefrills.events.InputEvent;
import com.somefrills.misc.Input;
import com.somefrills.misc.KeyAction;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        int modifiers = input.modifiers();
        if (input.key() == GLFW.GLFW_KEY_UNKNOWN) return;
        // on Linux/X11 the modifier is not active when the key is pressed and still active when the key is released
        // https://github.com/glfw/glfw/issues/1630
        if (action == GLFW.GLFW_PRESS) {
            modifiers |= Input.getModifier(input.key());
        } else if (action == GLFW.GLFW_RELEASE) {
            modifiers &= ~Input.getModifier(input.key());
        }

        Input.setKeyState(input.key(), action != GLFW.GLFW_RELEASE);
        if (eventBus.post(new InputEvent(new KeyInput(input.key(), input.scancode(), modifiers), KeyAction.get(action))).isCancelled()) {
            ci.cancel();
        }
    }
}

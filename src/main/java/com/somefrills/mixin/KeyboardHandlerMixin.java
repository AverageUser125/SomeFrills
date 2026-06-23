package com.somefrills.mixin;

import com.somefrills.events.KeyDownEvent;
import com.somefrills.events.KeyUpEvent;
import com.somefrills.misc.KeybindManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_UNKNOWN) return;

        if (action == 0) if(new KeyUpEvent(key).post()) ci.cancel();
        if (action == 1) {
            if(new KeyDownEvent(key).post()) {
                ci.cancel();
                return;
            }
            KeybindManager.onKeyPressed(key);
            // on 1.21 it takes like 1 full second before the key press event will get posted so im doing it here
            // new KeyPressEvent(key).post();
        }
        //if (action == 2) if(new KeyPressEvent(key).post()) ci.cancel();
    }
}

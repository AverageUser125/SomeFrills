package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.somefrills.features.core.Features;
import com.somefrills.features.tweaks.NoRender;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
    @ModifyExpressionValue(method = "getGlyph", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isObfuscated()Z"))
    private boolean onRenderObfuscatedStyle(boolean original) {
        if (!Features.isInitialized()) {
            return original;
        }
        return !NoRender.getConfig().noObfuscation && original;
    }
}

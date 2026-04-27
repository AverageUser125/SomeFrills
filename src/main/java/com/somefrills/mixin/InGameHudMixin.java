package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.somefrills.events.HudRenderEvent;
import com.somefrills.events.HudTickEvent;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import com.somefrills.misc.RenderColor;
import com.somefrills.mixininterface.TitleRendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;
import static com.somefrills.Main.mc;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin implements TitleRendering {
    @Unique
    private static String titleString = "";
    @Unique
    private static int titleTicks = 0;
    @Unique
    private static int titleOffset = 0;
    @Unique
    private static float titleScale = 1;
    @Unique
    private static int titleColor = 0xFFFFFFFF;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Override
    public void somefrills$setRenderTitle(String title, int stayTicks, int yOffset, float scale, RenderColor color) {
        titleString = title;
        titleTicks = stayTicks;
        titleOffset = yOffset;
        titleScale = scale;
        titleColor = color.argb;
    }

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

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (mc.options.hudHidden) return;
        eventBus.post(new HudRenderEvent(context, this.getTextRenderer(), tickCounter));
        if (titleTicks > 0) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate((float) (context.getScaledWindowWidth() / 2), (float) (context.getScaledWindowHeight() / 2));
            context.getMatrices().scale(titleScale, titleScale);
            TextRenderer textRenderer = mc.inGameHud.getTextRenderer();
            Text title = Text.of(titleString);
            int width = textRenderer.getWidth(title);
            context.drawTextWithBackground(textRenderer, title, -width / 2, titleOffset, width, titleColor);
            context.getMatrices().popMatrix();
        }

    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"))
    private boolean alwaysRenderCrosshairInFreecam(boolean firstPerson) {
        return Features.get(Freecam.class).isActive() || firstPerson;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;<init>(Lnet/minecraft/client/MinecraftClient;)V"))
    private void onInit(MinecraftClient client, CallbackInfo ci) {
        //HudManager.registerElements();
    }
}

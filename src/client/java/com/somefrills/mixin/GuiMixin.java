package com.somefrills.mixin;

import com.somefrills.events.HudRenderEvent;
import com.somefrills.events.HudTickEvent;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.TitleRendering;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
    private static String titleString;
    @Unique
    private static int titleTicks = 0;
    @Unique
    private static int titleOffset;
    @Unique
    private static float titleScale;
    @Unique
    private static int titleColor;

    @Shadow
    public abstract Font getFont();

    @Override
    public void somefrills_mod$setRenderTitle(String title, int stayTicks, int yOffset, float scale, RenderColor color) {
        titleString = title;
        titleTicks = stayTicks;
        titleOffset = yOffset;
        titleScale = scale;
        titleColor = color.argb;
    }

    @Override
    public boolean somefrills_mod$isRenderingTitle() {
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
    private void onRender(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (!mc.options.hideGui) {
            eventBus.post(new HudRenderEvent(context, this.getFont(), tickCounter));
            if (titleTicks > 0) {
                context.pose().pushMatrix();
                context.pose().translate((float) (context.guiWidth() / 2), (float) (context.guiHeight() / 2));
                context.pose().scale(titleScale, titleScale);
                Font textRenderer = mc.gui.getFont();
                Component title = Component.nullToEmpty(titleString);
                int width = textRenderer.width(title);
                context.drawStringWithBackdrop(textRenderer, title, -width / 2, titleOffset, width, titleColor);
                context.pose().popMatrix();
            }
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;<init>(Lnet/minecraft/client/Minecraft;)V"))
    private void onInit(Minecraft client, CallbackInfo ci) {
        //HudManager.registerElements();
    }
}

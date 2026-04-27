package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.ScreenRenderEvent;
import com.somefrills.events.SlotClickEvent;
import com.somefrills.events.TooltipRenderEvent;
import com.somefrills.features.tweaks.MiddleClickOverride;
import com.somefrills.misc.SlotOptions;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.somefrills.Main.eventBus;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    @Final
    protected T handler;
    @Shadow
    protected int y;
    @Shadow
    protected int x;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", ordinal = 1))
    private void onClickSlotRedirect(HandledScreen<?> instance, Slot slot, int slotId, int button, SlotActionType actionType, Operation<Void> original) {
        if (MiddleClickOverride.shouldOverride(slot, button, actionType)) {
            instance.onMouseClick(slot, slotId, GLFW.GLFW_MOUSE_BUTTON_3, SlotActionType.CLONE);
        } else {
            original.call(instance, slot, slotId, button, actionType);
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (eventBus.post(new SlotClickEvent(slot, slotId, button, actionType, this.title.getString(), this.handler)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("TAIL"))
    private void onClickSlotTail(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (SlotOptions.isSpoofed(slot)) {
            this.handler.setCursorStack(ItemStack.EMPTY); // prevents the real item from showing at the cursor
        }
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"))
    private List<Text> onGetTooltipFromItem(List<Text> original, @Local ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            eventBus.post(new TooltipRenderEvent(original, itemStack, Utils.getCustomData(itemStack), this.getTitle().getString()));
        }
        return original;
    }

    @ModifyExpressionValue(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawStack(ItemStack original, DrawContext context, Slot slot) {
        if (SlotOptions.isSpoofed(slot)) {
            return SlotOptions.getSpoofed(slot);
        }
        return original;
    }

    @ModifyArg(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"), index = 4)
    private @Nullable String onDrawStackCount(@Nullable String stackCountText, @Local(argsOnly = true) Slot slot) {
        if (SlotOptions.hasCount(slot)) {
            return SlotOptions.getCount(slot);
        }
        return stackCountText;
    }

    @ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        if (SlotOptions.isSpoofed(focusedSlot)) {
            return SlotOptions.getSpoofed(focusedSlot);
        }
        return original;
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onRenderSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (SlotOptions.hasBackground(slot)) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, SlotOptions.getBackground(slot).argb);
        }
    }

    @Inject(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlightBack(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void onBeforeHighlightRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        eventBus.post(new ScreenRenderEvent.Before(context, mouseX, mouseY, deltaTicks, this.title.getString(), this.handler, this.focusedSlot));
    }

    @SuppressWarnings("mapping")
    @Inject(method = "renderMain", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    private void onAfterRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        eventBus.post(new ScreenRenderEvent.After(context, mouseX, mouseY, delta, this.title.getString(), this.handler, this.focusedSlot));
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isInCreativeMode()Z"))
    private boolean onMiddleClick(boolean original) {
        if (FrillsConfig.instance.tweaks.middleClickFixEnabled) {
            return true;
        }
        return original;
    }
}

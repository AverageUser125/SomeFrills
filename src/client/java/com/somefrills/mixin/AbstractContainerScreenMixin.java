package com.somefrills.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.events.ScreenRenderEvent;
import com.somefrills.events.SlotClickEvent;
import com.somefrills.events.TooltipRenderEvent;
import com.somefrills.features.tweaks.MiddleClickFix;
import com.somefrills.features.tweaks.MiddleClickOverride;
import com.somefrills.misc.SlotOptions;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    @Final
    protected T menu;
    @Shadow
    protected int topPos;
    @Shadow
    protected int leftPos;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 1))
    private void onClickSlotRedirect(AbstractContainerScreen<?> instance, Slot slot, int slotId, int button, ClickType actionType, Operation<Void> original) {
        if (MiddleClickOverride.shouldOverride(slot, button, actionType)) {
            instance.slotClicked(slot, slotId, GLFW.GLFW_MOUSE_BUTTON_3, ClickType.CLONE);
        } else {
            original.call(instance, slot, slotId, button, actionType);
        }
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (eventBus.post(new SlotClickEvent(slot, slotId, button, actionType, this.title.getString(), this.menu)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At("TAIL"))
    private void onClickSlotTail(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (SlotOptions.isSpoofed(slot)) {
            this.menu.setCarried(ItemStack.EMPTY); // prevents the real item from showing at the cursor
        }
    }

    @ModifyExpressionValue(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getTooltipFromContainerItem(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private List<Component> onGetTooltipFromItem(List<Component> original, @Local ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            eventBus.post(new TooltipRenderEvent(original, itemStack, Utils.getCustomData(itemStack), this.getTitle().getString()));
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onDrawStack(ItemStack original, GuiGraphics context, Slot slot) {
        if (SlotOptions.isSpoofed(slot)) {
            return SlotOptions.getSpoofed(slot);
        }
        return original;
    }

    @ModifyArg(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"), index = 4)
    private @Nullable String onDrawStackCount(@Nullable String stackCountText, @Local(argsOnly = true) Slot slot) {
        if (SlotOptions.hasCount(slot)) {
            return SlotOptions.getCount(slot);
        }
        return stackCountText;
    }

    @ModifyExpressionValue(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        if (SlotOptions.isSpoofed(hoveredSlot)) {
            return SlotOptions.getSpoofed(hoveredSlot);
        }
        return original;
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onRenderSlot(GuiGraphics context, Slot slot, CallbackInfo ci) {
        if (SlotOptions.hasBackground(slot)) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, SlotOptions.getBackground(slot).argb);
        }
    }

    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlightBack(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void onBeforeHighlightRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        eventBus.post(new ScreenRenderEvent.Before(context, mouseX, mouseY, deltaTicks, this.title.getString(), this.menu, this.hoveredSlot));
    }

    @SuppressWarnings("mapping")
    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    private void onAfterRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        eventBus.post(new ScreenRenderEvent.After(context, mouseX, mouseY, delta, this.title.getString(), this.menu, this.hoveredSlot));
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInCreativeMode()Z"))
    private boolean onMiddleClick(boolean original) {
        if (MiddleClickFix.instance.isActive()) {
            return true;
        }
        return original;
    }
}

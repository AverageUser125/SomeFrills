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
import com.somefrills.utils.ItemStackUtils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;

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

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    @Final
    protected T menu;


    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }


    @WrapOperation(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V"
            )
    )
    private void onClickSlotRedirect(
            AbstractContainerScreen<?> instance,
            Slot slot,
            int slotId,
            int button,
            ContainerInput actionType,
            Operation<Void> original
    ) {
        if (MiddleClickOverride.shouldOverride(slot, button, actionType)) {
            instance.slotClicked(
                    slot,
                    slotId,
                    GLFW.GLFW_MOUSE_BUTTON_3,
                    ContainerInput.CLONE
            );
        } else {
            original.call(instance, slot, slotId, button, actionType);
        }
    }


    @Inject(
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onClickSlot(
            Slot slot,
            int slotId,
            int button,
            ContainerInput actionType,
            CallbackInfo ci
    ) {
        if (new SlotClickEvent(
                        slot,
                        slotId,
                        button,
                        actionType,
                        title.getString(),
                        menu
                ).post().isCancelled()) {
            ci.cancel();
        }
    }


    @Inject(
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
            at = @At("TAIL")
    )
    private void onClickSlotTail(
            Slot slot,
            int slotId,
            int button,
            ContainerInput actionType,
            CallbackInfo ci
    ) {
        if (SlotOptions.isSpoofed(slot)) {
            menu.setCarried(ItemStack.EMPTY);
        }
    }


    @ModifyExpressionValue(
            method = "extractTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getTooltipFromContainerItem(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            )
    )
    private List<Component> onGetTooltipFromItem(
            List<Component> original,
            @Local ItemStack itemStack
    ) {
        if (!itemStack.isEmpty()) {
            new TooltipRenderEvent(
                    original,
                    itemStack,
                    ItemStackUtils.getCustomData(itemStack),
                    title.getString()
            ).post();
        }

        return original;
    }


    @ModifyExpressionValue(
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack onDrawStack(ItemStack original) {
        if (SlotOptions.isSpoofed(hoveredSlot)) {
            return SlotOptions.getSpoofed(hoveredSlot);
        }

        return original;
    }


    @ModifyExpressionValue(
            method = "extractTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack onDrawSpoofedTooltip(ItemStack original) {
        if (SlotOptions.isSpoofed(hoveredSlot)) {
            return SlotOptions.getSpoofed(hoveredSlot);
        }

        return original;
    }


    @ModifyArg(
            method = "extractSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            ),
            index = 4
    )
    private String onDrawStackCount(
            @Nullable String count,
            @Local(argsOnly = true) Slot slot
    ) {
        return SlotOptions.hasCount(slot)
                ? SlotOptions.getCount(slot)
                : count;
    }


    @Inject(
            method = "extractSlot",
            at = @At("HEAD")
    )
    private void onRenderSlot(
            GuiGraphicsExtractor graphics,
            Slot slot,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        if (SlotOptions.hasBackground(slot)) {
            graphics.fill(
                    slot.x,
                    slot.y,
                    slot.x + 16,
                    slot.y + 16,
                    SlotOptions.getBackground(slot).argb
            );
        }
    }


    @Inject(
            method = "extractContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlotHighlightBack(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"
            )
    )
    private void beforeRender(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
            new ScreenRenderEvent.Before(
                    graphics,
                    mouseX,
                    mouseY,
                    delta,
                    title.getString(),
                    menu,
                    hoveredSlot
            ).post();
    }


    @Inject(
            method = "extractContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"
            )
    )
    private void afterRender(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
            new ScreenRenderEvent.After(
                    graphics,
                    mouseX,
                    mouseY,
                    delta,
                    title.getString(),
                    menu,
                    hoveredSlot
            ).post();
    }


    @ModifyExpressionValue(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"
            )
    )
    private boolean onMiddleClick(boolean original) {
        return MiddleClickFix.INSTANCE.isActive()
                || original;
    }
}
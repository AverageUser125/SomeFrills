package com.somefrills.mixin;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.PlaceBlockEvent;
import com.somefrills.features.tweaks.NoAbilityPlace;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.somefrills.Main.eventBus;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow
    protected abstract BlockState getPlacementState(ItemPlacementContext context);

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"),
            cancellable = true)
    private void onPlaceBlock(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getWorld().isClient()) return;

        if (eventBus.post(new PlaceBlockEvent(context, state.getBlock())).isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"), cancellable = true)
    private void beforeGetSoundGroup(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (NoAbilityPlace.hasAbility(context)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @ModifyVariable(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            ordinal = 1,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
            )
    )
    private BlockState modifyState(BlockState state, ItemPlacementContext context) {
        var noGhostBlocksConfig = FrillsConfig.instance.tweaks.noGhostBlocks;
        if (noGhostBlocksConfig.enabled.get() && noGhostBlocksConfig.placing) {
            return getPlacementState(context);
        }

        return state;
    }
}
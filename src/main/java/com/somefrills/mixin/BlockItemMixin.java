package com.somefrills.mixin;

import com.somefrills.events.PlaceBlockEvent;
import com.somefrills.features.tweaks.NoAbilityPlace;
import com.somefrills.features.tweaks.NoGhostBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
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
    protected abstract BlockState getPlacementState(BlockPlaceContext context);

    @Inject(method = "placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"),
            cancellable = true)
    private void onPlaceBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getWorld().isClient()) return;

        if (eventBus.post(new PlaceBlockEvent(context, state.getBlock())).isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundGroup()Lnet/minecraft/world/level/block/SoundType;"), cancellable = true)
    private void beforeGetSoundGroup(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (NoAbilityPlace.hasAbility(context)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @ModifyVariable(
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
            ordinal = 1,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isOf(Lnet/minecraft/world/level/block/Block;)Z"
            )
    )
    private BlockState modifyState(BlockState state, BlockPlaceContext context) {
        var noGhostBlocksConfig = NoGhostBlocks.getConfig();
        if (noGhostBlocksConfig.enabled.get() && noGhostBlocksConfig.placing) {
            return getPlacementState(context);
        }

        return state;
    }
}
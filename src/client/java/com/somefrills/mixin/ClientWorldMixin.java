package com.somefrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.somefrills.events.EntityRemovedEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.somefrills.Main.eventBus;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V"))
    private void onBeforeRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity ent) {
        eventBus.post(new EntityRemovedEvent(ent, removalReason));
    }
}
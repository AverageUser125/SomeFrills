package com.somefrills.mixin.skyhanni;

import at.hannibal2.skyhanni.events.MobEvent;
import at.hannibal2.skyhanni.features.dungeon.DungeonApi;
import at.hannibal2.skyhanni.features.fishing.SeaCreatureFeatures;
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraApi;
import at.hannibal2.skyhanni.utils.SkyBlockUtils;
import com.somefrills.config.FrillsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SeaCreatureFeatures.class)
public class SeaCreatureFeaturesMixin {
    @Inject(method = "onMobSpawn", at = @At("HEAD"))
    public void onMobSpawn(MobEvent.Spawn.SkyblockMob event, CallbackInfo ci) {
        if (!FrillsConfig.instance.fishing.rareSeaCreatureAlert.get()) return;
        if (!(SkyBlockUtils.INSTANCE.getInSkyBlock() && !DungeonApi.INSTANCE.inDungeon() && !KuudraApi.INSTANCE.getInKuudra())) return;
        SeaCreatureFeatures.INSTANCE.onSkyblockMobFirstSeen(new MobEvent.FirstSeen.SkyblockMob(event.getMob()));
    }
}
package com.somefrills.features.mining;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.CorpseEvent;
import com.somefrills.features.mining.CorpseApi.Corpse;
import com.somefrills.features.misc.PartyApi;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class CorpseAnnouncer extends Feature {
    public CorpseAnnouncer() {
        super(FrillsConfig.instance.mining.corpseAnnouncer.enabled);
    }

    @EventHandler
    public void onCorpse(CorpseEvent event) {
        if (!isActive()) return;
        if (!PartyApi.isInParty()) return;
        String message = "/pc " + corpseToMessage(event.corpse);
        Utils.sendMessage(message);
    }

    private static String corpseToMessage(Corpse corpse) {
        String corpseType = Utils.capitalizeType(corpse.getType().name());
        String location = locationToString(corpse.getEntity().getBlockPos());
        return location + " | (" + corpseType + " Corpse)";
    }

    private static String locationToString(BlockPos pos) {
        return "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ();
    }
}
package com.somefrills.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Freecam;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class FreecamCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("freecam")
                .executes(ctx -> {
                    Features.get(Freecam.class).toggle();
                    return 1;
                });
    }
}

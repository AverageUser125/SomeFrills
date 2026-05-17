package com.somefrills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.somefrills.features.misc.Freecam
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object FreecamCommand {
    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return LiteralArgumentBuilder.literal<FabricClientCommandSource>("freecam")
            .executes { ctx: CommandContext<FabricClientCommandSource> ->
                Freecam.toggle()
                1
            }
    }
}
package com.somefrills.commands

import at.hannibal2.skyhanni.utils.DelayedRun
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.NumberUtils
import me.owdding.skyocean.features.item.search.screen.ItemSearchScreen
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.item.sources.ItemSources.entries
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object TotalNetworthCommand {
    fun getBuilder(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal("totalnetworth").executes { ctx ->
            ChatUtils.info("Starting calculations...")
            Thread {
                val total = getTotalPrice()
                DelayedRun.runOrNextTick {
                    val commaNumber = NumberUtils.formatComma(total)
                    ChatUtils.infoFormat("Total networth: {}", commaNumber)
                }
            }.start()
            1
        }
    }

    fun getTotalPrice(): Long {
        val items = ItemSources.getAllItems()
        var totalPrice = 0L;
        for (item in items) {
            totalPrice += item.price
        }
        return totalPrice;
    }
}

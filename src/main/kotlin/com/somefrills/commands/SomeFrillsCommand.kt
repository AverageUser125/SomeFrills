package com.somefrills.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.somefrills.utils.ChatUtils
import com.somefrills.utils.GuiUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandBuildContext
import java.util.function.Supplier

object SomeFrillsCommand {
    val commands: Array<ModCommand> = arrayOf<ModCommand>(
        ModCommand(
            "settings",
            "Opens the settings GUI."
        ) {
            ClientCommands.literal("settings")
                .executes { obj: CommandContext<FabricClientCommandSource> -> executeSettings(obj) }
        },
        ModCommand(
            "glowplayer",
            "Manage glowing players."
        ) { GlowPlayerCommand.getBuilder() },
        ModCommand(
            "glowmob",
            "Manage glowing mobs/entities."
        ) { GlowMobCommand.getBuilder() },
        ModCommand(
            "npclocator",
            "Track NPC locations."
        ) { NpcLocatorCommand.getBuilder("npclocator") },

        ModCommand(
            "locatenpc",
            "Alias for npclocator."
        ) { NpcLocatorCommand.getBuilder("locatenpc") },
        ModCommand(
            "freecam",
            "Toggle freecam mode."
        ) { FreecamCommand.getBuilder() },
        ModCommand(
            "glowblock",
            "Manage glowing blocks."
        ) { GlowBlockCommand.getBuilder() }
    )

    /**
     * Checks if a command with the given name already exists at the top level of the dispatcher.
     */
    private fun commandExists(
        dispatcher: CommandDispatcher<FabricClientCommandSource?>,
        commandName: String?
    ): Boolean {
        val root: CommandNode<FabricClientCommandSource?> = dispatcher.getRoot()
        return root.getChild(commandName) != null
    }

    /**
     * Executes the settings command.
     */
    private fun executeSettings(context: CommandContext<FabricClientCommandSource>): Int {
        GuiUtils.showGui()
        return Command.SINGLE_SUCCESS
    }

    fun init(dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandBuildContext) {
        val helpArg =
            ClientCommands.literal("help")
                .executes(Command { context: CommandContext<FabricClientCommandSource> ->
                    ChatUtils.info("§7Printing command list...")
                    for (command in commands) {
                        ChatUtils.infoFormat("§l{}§r§7: {}", command.command, command.description)
                    }
                    Command.SINGLE_SUCCESS
                })

        val commandMain =
            ClientCommands.literal("somefrills")
                .executes(Command { context: CommandContext<FabricClientCommandSource?>? ->
                    GuiUtils.showGui()
                    Command.SINGLE_SUCCESS
                })

        val commandShort =
            ClientCommands.literal("sf")
                .executes(Command { context: CommandContext<FabricClientCommandSource?>? ->
                    GuiUtils.showGui()
                    Command.SINGLE_SUCCESS
                })

        commandMain.then(helpArg)
        commandShort.then(helpArg)

        // Add all commands as subcommands to both main and short aliases
        for (command in commands) {
            commandMain.then(command.builder())
            commandShort.then(command.builder())
        }

        dispatcher.register(commandMain)
        dispatcher.register(commandShort)

        // Register top-level aliases for each command (if not already registered)
        for (command in commands) {
            @Suppress("UNCHECKED_CAST")
            if (!commandExists(dispatcher as CommandDispatcher<FabricClientCommandSource?>, command.command)) {
                dispatcher.register(command.builder())
            }
        }
    }

    class ModCommand(
        var command: String?, var description: String,
        private val factory: Supplier<LiteralArgumentBuilder<FabricClientCommandSource>>
    ) {
        private var cached: LiteralArgumentBuilder<FabricClientCommandSource>? = null

        fun builder(): LiteralArgumentBuilder<FabricClientCommandSource> {
            if (cached == null) {
                cached = factory.get()
            }
            return cached!!
        }
    }
}
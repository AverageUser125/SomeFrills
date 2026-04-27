package com.somefrills.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SomeFrillsCommand {
    public static final ModCommand[] commands = {
            new ModCommand("settings", "Opens the settings GUI.",
                    ClientCommandManager.literal("settings")
                            .executes(SomeFrillsCommand::executeSettings)
            ),
            new ModCommand("glowplayer", "Manage glowing players.",
                    GlowPlayerCommand.getBuilder()
            ),
            new ModCommand("glowmob", "Manage glowing mobs/entities.",
                    GlowMobCommand.getBuilder()
            ),
            new ModCommand("npclocator", "Track NPC locations.",
                    NpcLocatorCommand.getBuilder("npclocator")
            ),
            new ModCommand("locatenpc", "Alias for npclocator.",
                    NpcLocatorCommand.getBuilder("locatenpc")
            ),
            new ModCommand("freecam", "Toggle freecam mode.",
                    FreecamCommand.getBuilder()
            ),
            new ModCommand("glowblock", "Manage glowing blocks.",
                    GlowBlockCommand.getBuilder()
            )
    };

    /**
     * Checks if a command with the given name already exists at the top level of the dispatcher.
     */
    private static boolean commandExists(CommandDispatcher<FabricClientCommandSource> dispatcher, String commandName) {
        CommandNode<FabricClientCommandSource> root = dispatcher.getRoot();
        return root.getChild(commandName) != null;
    }

    /**
     * Executes the settings command.
     */
    private static int executeSettings(CommandContext<FabricClientCommandSource> context) {
        Utils.showGui();
        return SINGLE_SUCCESS;
    }

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        LiteralArgumentBuilder<FabricClientCommandSource> helpArg =
                ClientCommandManager.literal("help").executes(context -> {
                    Utils.info("§7Printing command list...");
                    for (ModCommand command : commands) {
                        Utils.info("§l" + command.command + "§r§7: " + command.description);
                    }
                    return SINGLE_SUCCESS;
                });

        LiteralArgumentBuilder<FabricClientCommandSource> commandMain =
                ClientCommandManager.literal("somefrills").executes(context -> {
                    Utils.showGui();
                    return SINGLE_SUCCESS;
                });

        LiteralArgumentBuilder<FabricClientCommandSource> commandShort =
                ClientCommandManager.literal("sf").executes(context -> {
                    Utils.showGui();
                    return SINGLE_SUCCESS;
                });

        commandMain.then(helpArg);
        commandShort.then(helpArg);

        // Add all commands as subcommands to both main and short aliases
        for (ModCommand command : commands) {
            commandMain.then(command.builder);
            commandShort.then(command.builder);
        }

        dispatcher.register(commandMain);
        dispatcher.register(commandShort);

        // Register top-level aliases for each command (if not already registered)
        for (ModCommand command : commands) {
            if (!commandExists(dispatcher, command.command)) {
                dispatcher.register(command.builder);
            }
        }
    }

    public static class ModCommand {
        public String command;
        public String description;
        public LiteralArgumentBuilder<FabricClientCommandSource> builder;

        public ModCommand(String command, String description, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
            this.command = command;
            this.description = description;
            this.builder = builder;
        }
    }
}

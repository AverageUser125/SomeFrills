package com.somefrills.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.somefrills.misc.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SomeFrillsCommand {
    public static final ModCommand[] commands = {
            new ModCommand("settings", "Opens the settings GUI.",
                    ClientCommandManager.literal("settings")
                            .executes(context -> {
                                Utils.showGui();
                                return SINGLE_SUCCESS;
                            })
            )
    };
    private static final LiteralArgumentBuilder<FabricClientCommandSource> queueCommandBuilder =
            ClientCommandManager.literal("queue")
                    .executes(context -> SINGLE_SUCCESS);

    /**
     * Checks if a command with the given name already exists at the top level of the dispatcher.
     */
    private static boolean commandExists(CommandDispatcher<FabricClientCommandSource> dispatcher, String commandName) {
        CommandNode<FabricClientCommandSource> root = dispatcher.getRoot();
        return root.getChild(commandName) != null;
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

        for (ModCommand command : commands) {
            commandMain.then(command.builder);
            commandShort.then(command.builder);
        }

        // (Optional) register queue command if you plan to use it
        commandMain.then(queueCommandBuilder);
        commandShort.then(queueCommandBuilder);

        // Register glowplayer as a subcommand and also as a top-level alias
        commandMain.then(GlowPlayerCommand.getBuilder());
        commandShort.then(GlowPlayerCommand.getBuilder());

        // Register glowmob as a subcommand and also as a top-level alias
        commandMain.then(GlowMobCommand.getBuilder());
        commandShort.then(GlowMobCommand.getBuilder());

        // Register npclocator as a subcommand and also as a top-level alias
        commandMain.then(NpcLocatorCommand.getBuilder());
        commandShort.then(NpcLocatorCommand.getBuilder());

        dispatcher.register(commandMain);
        dispatcher.register(commandShort);

        // Top-level registration for /glowplayer
        if (!commandExists(dispatcher, "glowplayer")) {
            dispatcher.register(GlowPlayerCommand.getBuilder());
        }

        // Top-level registration for /entityhighlight
        if (!commandExists(dispatcher, "entityhighlight")) {
            dispatcher.register(GlowMobCommand.getBuilder());
        }

        // Top-level registration for /npclocator
        if (!commandExists(dispatcher, "npclocator")) {
            dispatcher.register(NpcLocatorCommand.getBuilder());
        }

        // Register locatenpc alias as a subcommand and also as a top-level alias
        var locateNpcAlias = ClientCommandManager.literal("locatenpc").redirect(NpcLocatorCommand.getBuilder().build());
        commandMain.then(locateNpcAlias);
        commandShort.then(locateNpcAlias);

        // Alias for /locatenpc
        if (!commandExists(dispatcher, "locatenpc")) {
            dispatcher.register(locateNpcAlias);
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

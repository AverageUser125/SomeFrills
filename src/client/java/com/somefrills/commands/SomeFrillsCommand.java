package com.somefrills.commands;

import com.somefrills.config.Config;
import com.somefrills.features.farming.Rewarp;
import com.somefrills.hud.ClickGui;
import com.somefrills.misc.Utils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SomeFrillsCommand {
    public static final ModCommand[] commands = {
            new ModCommand("settings", "Opens the settings GUI.",
                    ClientCommandManager.literal("settings")
                            .executes(context -> {
                                Utils.setScreen(new ClickGui());
                                return SINGLE_SUCCESS;
                            })
                            .then(ClientCommandManager.literal("load").executes(context -> {
                                Config.load();
                                Utils.info("§aLoaded latest settings from the configuration file.");
                                return SINGLE_SUCCESS;
                            }))
                            .then(ClientCommandManager.literal("save").executes(context -> {
                                Config.save();
                                Utils.info("§aSaved your current settings to the configuration file.");
                                return SINGLE_SUCCESS;
                            }))
            ),
            new ModCommand("warp", "Manage simple saved waypoints.",
                    ClientCommandManager.literal("warp")
                            .then(ClientCommandManager.literal("add").executes(context -> {
                                Rewarp.addWaypoint();
                                return SINGLE_SUCCESS;
                            }))
                            .then(ClientCommandManager.literal("remove").executes(context -> {
                                Rewarp.removeLastWaypoint();
                                return SINGLE_SUCCESS;
                            }))
                            .then(ClientCommandManager.literal("clear").executes(context -> {
                                Rewarp.clearWaypoints();
                                return SINGLE_SUCCESS;
                            }))
            )
    };
    private static final LiteralArgumentBuilder<FabricClientCommandSource> queueCommandBuilder =
            ClientCommandManager.literal("queue")
                    .executes(context -> SINGLE_SUCCESS);

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
                    Utils.setScreen(new ClickGui());
                    return SINGLE_SUCCESS;
                });

        LiteralArgumentBuilder<FabricClientCommandSource> commandShort =
                ClientCommandManager.literal("sf").executes(context -> {
                    Utils.setScreen(new ClickGui());
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
        dispatcher.register(commandMain);
        dispatcher.register(commandShort);

        // Top-level registration for /glowplayer
        dispatcher.register(GlowPlayerCommand.getBuilder());
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

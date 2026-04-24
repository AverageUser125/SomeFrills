package com.somefrills;

import com.mojang.brigadier.CommandDispatcher;
import com.somefrills.commands.SomeFrillsCommand;
import com.somefrills.config.FrillsConfig;
import com.somefrills.config.about.ConfigVersionDisplay;
import com.somefrills.config.about.GuiOptionEditorUpdateCheck;
import com.somefrills.events.*;
import com.somefrills.features.core.Features;
import com.somefrills.features.misc.Aliases;
import com.somefrills.features.misc.glowmob.MatchInfo;
import com.somefrills.misc.*;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfigBuilder;
import kotlin.Unit;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main implements ClientModInitializer {
    // Mod id - keep a local constant instead of relying on loader internals
    public static final String MOD_ID = "somefrills";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftClient mc;
    public static IEventBus eventBus = new EventBus();
    public static ManagedConfig<FrillsConfig> config;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        SomeFrillsCommand.init(dispatcher);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onGameStop(GameStopEvent event) {
        Main.config.saveToFile();
    }

    @Override
    public void onInitializeClient() {
        long start = Util.getMeasuringTimeMs();
        mc = MinecraftClient.getInstance();

        ClientCommandRegistrationCallback.EVENT.register(Main::registerCommands);

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String msg = Utils.toPlain(message);

            if (overlay) {
                return !eventBus.post(new OverlayMsgEvent(message, msg)).isCancelled();
            }

            boolean cancelled = eventBus.post(new ChatMsgEvent(message, msg)).isCancelled();
            if (msg.startsWith("Party > ") && msg.contains(": ")) {
                int nameStart = msg.contains("]") && msg.indexOf("]") < msg.indexOf(":") ? msg.indexOf("]") : msg.indexOf(">");
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":", 2);
                String author = clean[0].trim(), content = clean[1].trim();
                cancelled = eventBus.post(new PartyChatMsgEvent(content, author)).isCancelled() || cancelled;
            }
            return !cancelled;
        });

        // Post ClientDisconnectEvent on Fabric disconnect and save config
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> eventBus.post(new ClientDisconnectEvent()));

        ClientSendMessageEvents.MODIFY_COMMAND.register(Aliases::convertCommand);
        var file = FabricLoader.getInstance().getConfigDir().resolve("somefrills.json").toFile();
        var builder = new ManagedConfigBuilder<>(file, FrillsConfig.class);
        builder.customProcessor(ConfigVersionDisplay.class, (option, annotation) -> new GuiOptionEditorUpdateCheck(option));
        builder.setLoadFailed((cfg, ex) -> LOGGER.error("Failed to load config file {}: {}", cfg.getFile().getName(), ex.getMessage()));
        builder.setSaveFailed((cfg, ex) -> LOGGER.error("Failed to save config file {}: {}", cfg.getFile().getName(), ex.getMessage()));
        builder.jsonMapper(
                mapper -> {
                    mapper.getGsonBuilder().
                            registerTypeAdapter(MatchInfo.class, new MatchInfo.MatchInfoTypeAdapter())
                            .registerTypeAdapter(RenderColor.class, new RenderColor.RenderColorTypeAdapter())
                            .disableHtmlEscaping()
                            .disableJdkUnsafe();
                    return Unit.INSTANCE;
                }
        );

        config = new ManagedConfig<>(builder);
        FrillsConfig.instance = config.getInstance();
        eventBus.registerLambdaFactory("com.somefrills",
                (lookupInMethod, klass) -> (MethodHandles.Lookup)
                        lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        eventBus.subscribe(SkyblockData.class);
        eventBus.subscribe(EntityCache.class);
        eventBus.subscribe(KeybindManager.class);
        eventBus.subscribe(Main.class);
        Features.init();

        eventBus.post(new GameStartEvent());

        LOGGER.info("It's time to get real, SomeFrills mod initialized in {}ms.", Util.getMeasuringTimeMs() - start);
    }
}

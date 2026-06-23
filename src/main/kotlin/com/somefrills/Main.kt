package com.somefrills

import com.somefrills.commands.SomeFrillsCommand
import com.somefrills.config.FrillsConfig
import com.somefrills.config.FrillsMod
import com.somefrills.config.about.ConfigVersionDisplay
import com.somefrills.config.about.GuiOptionEditorUpdateCheck
import com.somefrills.events.*
import com.somefrills.features.core.Features
import com.somefrills.features.misc.Aliases
import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.misc.*
import com.somefrills.utils.toPlain
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import io.github.notenoughupdates.moulconfig.managed.ManagedConfigBuilder
import meteordevelopment.orbit.EventBus
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import meteordevelopment.orbit.IEventBus
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

object Main : ClientModInitializer {

    const val MOD_ID = "somefrills"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmField
    val mc: Minecraft = Minecraft.getInstance()

    @JvmField
    val eventBus: IEventBus = EventBus()

    lateinit var config: ManagedConfig<FrillsConfig>

    @EventHandler(priority = EventPriority.LOWEST)
    @JvmStatic
    fun onGameStop(event: GameStopEvent) {
        config.saveToFile()
    }

    override fun onInitializeClient() {
        val start = System.currentTimeMillis()

        ClientCommandRegistrationCallback.EVENT.register(SomeFrillsCommand::init)

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            val msg = message.toPlain()

            if (overlay) {
                return@register !eventBus.post(
                    OverlayMsgEvent(message, msg)
                ).isCancelled
            }

            var cancelled = eventBus.post(
                ChatMsgEvent(message, msg)
            ).isCancelled

            if (msg.startsWith("Party > ") && msg.contains(": ")) {
                val nameStart =
                    if (msg.contains("]") && msg.indexOf("]") < msg.indexOf(":")) {
                        msg.indexOf("]")
                    } else {
                        msg.indexOf(">")
                    }

                val clean = msg
                    .replace(msg.substring(0, nameStart + 1), "")
                    .split(":", limit = 2)

                val author = clean[0].trim()
                val content = clean[1].trim()

                cancelled = eventBus.post(
                    PartyChatMsgEvent(content, author)
                ).isCancelled || cancelled
            }

            !cancelled
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            eventBus.post(ClientDisconnectEvent())
        }

        val file = FabricLoader.getInstance()
            .configDir
            .resolve("somefrills.json")
            .toFile()

        val builder = ManagedConfigBuilder(
            file,
            FrillsConfig::class.java
        ).apply {

            customProcessor<ConfigVersionDisplay> { option, _ ->
                GuiOptionEditorUpdateCheck(option)
            }

            loadFailed = java.util.function.BiConsumer { cfg, ex ->
                LOGGER.error(
                    "Failed to load config file {}: {}",
                    cfg.file.name,
                    ex.message
                )
            }

            saveFailed = java.util.function.BiConsumer { cfg, ex ->
                LOGGER.error(
                    "Failed to save config file {}: {}",
                    cfg.file.name,
                    ex.message
                )
            }

            jsonMapper {
                gsonBuilder
                    .registerTypeAdapter(
                        MatchInfo::class.java,
                        MatchInfo.MatchInfoTypeAdapter()
                    )
                    .registerTypeAdapter(
                        RenderColor::class.java,
                        RenderColor.RenderColorTypeAdapter()
                    )
                    .disableHtmlEscaping()
                    .disableJdkUnsafe()
            }
        }

        config = ManagedConfig(builder)
        FrillsMod.bind(config.instance)

        eventBus.registerLambdaFactory("com.somefrills") { lookupInMethod, klass ->
            lookupInMethod.invoke(
                null,
                klass,
                MethodHandles.lookup()
            ) as MethodHandles.Lookup
        }

        eventBus.subscribe(SkyblockData::class.java)
        eventBus.subscribe(EntityCache::class.java)
        eventBus.subscribe(Main::class.java)

        Features.init()

        eventBus.post(GameStartEvent())

        ClientSendMessageEvents.MODIFY_COMMAND.register(
            Aliases::convertCommand
        )

        LOGGER.info(
            "It's time to get real, SomeFrills mod initialized in {}ms.",
            System.currentTimeMillis() - start
        )
    }
}

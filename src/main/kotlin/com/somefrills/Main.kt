package com.somefrills

import com.somefrills.commands.SomeFrillsCommand
import com.somefrills.config.FrillsConfig
import com.somefrills.config.FrillsMod
import com.somefrills.config.about.ConfigVersionDisplay
import com.somefrills.config.about.GuiOptionEditorUpdateCheck
import com.somefrills.events.*
import com.somefrills.events.core.EventHandle
import com.somefrills.events.core.EventPriority
import com.somefrills.events.core.FrillsEvents
import com.somefrills.features.core.AbstractFeature
import com.somefrills.features.misc.Aliases
import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.misc.*
import com.somefrills.modules.FrillsFeature
import com.somefrills.modules.LoadedModules
import com.somefrills.utils.toPlain
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import io.github.notenoughupdates.moulconfig.managed.ManagedConfigBuilder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@FrillsFeature
object Main : ClientModInitializer {

    const val MOD_ID = "somefrills"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmField
    val mc: Minecraft = Minecraft.getInstance()

    @JvmField
    var totalTicks: Int = 0

    lateinit var config: ManagedConfig<FrillsConfig>

    @EventHandle(priority = EventPriority.LOWEST)
    fun onGameStop(event: GameStopEvent) {
        config.saveToFile()
    }

    override fun onInitializeClient() {
        val start = System.currentTimeMillis()

        ClientCommandRegistrationCallback.EVENT.register(SomeFrillsCommand::init)
        ClientTickEvents.END_CLIENT_TICK.register {
            if (mc.player == null) return@register
            if (mc.level == null) return@register
            totalTicks++
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            val msg = message.toPlain()

            if (overlay) {
                return@register !OverlayMsgEvent(message, msg).post().isCancelled
            }

            var cancelled = ChatMsgEvent(message, msg).post().isCancelled

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

                cancelled = PartyChatMsgEvent(content, author).post().isCancelled || cancelled
            }

            !cancelled
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            ClientDisconnectEvent().post()
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

        /*
        eventBus.registerLambdaFactory("com.somefrills") { lookupInMethod, klass ->
            lookupInMethod.invoke(
                null,
                klass,
                MethodHandles.lookup()
            ) as MethodHandles.Lookup
        }
         */

        val modules = LoadedModules.modules

        modules.forEach {
            if (it is AbstractFeature) {
                it.initialize()
            } else {
                FrillsEvents.register(it)
            }
        }

        GameStartEvent().post()

        ClientSendMessageEvents.MODIFY_COMMAND.register(
            Aliases::convertCommand
        )

        LOGGER.info(
            "It's time to get real, SomeFrills mod initialized in {}ms.",
            System.currentTimeMillis() - start
        )
    }

    @JvmStatic
    fun isInitialized(): Boolean {
        return ::config.isInitialized
    }
}

package com.somefrills.features.update

import com.google.gson.JsonElement
import com.somefrills.Main
import com.somefrills.Main.LOGGER
import com.somefrills.Main.mc
import com.somefrills.misc.Utils
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateTarget
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function
import kotlin.concurrent.Volatile

object UpdateManager {
    private val context = UpdateContext(
        CustomGithubReleaseUpdateSource("AverageUser125", "SomeFrills"),
        UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager::class.java),
        object : CurrentVersion {
            override fun display(): String {
                return currentVersion
            }

            override fun isOlderThan(element: JsonElement): Boolean {
                if (!element.isJsonPrimitive) return false
                try {
                    val asString = element.asString
                    val currentParsed = parseSemanticVersion(currentVersion)
                    val latestParsed = parseSemanticVersion(asString)
                    return currentParsed < latestParsed
                } catch (e: Exception) {
                    LOGGER.warn("Failed to compare versions", e)
                    return false
                }
            }
        },
        Main.MOD_ID
    )

    @Volatile
    private var activePromise: CompletableFuture<*>? = null

    @Volatile
    var updateState: UpdateState = UpdateState.NONE
        private set

    @Volatile
    private var potentialUpdate: PotentialUpdate? = null

    val currentVersion: String
        get() = FabricLoader.getInstance()
            .getModContainer(Main.MOD_ID)
            .map(Function { mod: ModContainer ->
                mod.metadata.version.friendlyString
            })
            .orElse("unknown") ?: "unknown"

    val latestVersion: String
        get() {
            return potentialUpdate?.update?.getVersionNumber()
                ?.asString ?: currentVersion
        }

    val isUpdateAvailable: Boolean
        get() = updateState == UpdateState.AVAILABLE

    fun reset() {
        updateState = UpdateState.NONE
        potentialUpdate = null
        cancelActivePromise()
        LOGGER.debug("Update state reset")
    }

    @JvmOverloads
    fun checkUpdate(autoQueue: Boolean = false) {
        if (updateState != UpdateState.NONE && updateState != UpdateState.AVAILABLE) {
            LOGGER.info("Trying to perform update check while another update is already in progress")
            return
        }

        if (updateState == UpdateState.AVAILABLE) {
            updateState = UpdateState.NONE
            LOGGER.info("Resetting update state to force download")
        }

        LOGGER.info("Starting update check (autoQueue: {})", autoQueue)
        activePromise = context.checkUpdate("full").thenAcceptAsync(Consumer thenAcceptAsync@{ update: PotentialUpdate? ->
            LOGGER.info("Update check completed")
            if (updateState != UpdateState.NONE) {
                LOGGER.info("This appears to be the second update check. Ignoring this one")
                return@thenAcceptAsync
            }

            potentialUpdate = update
            if (update!!.isUpdateAvailable()) {
                updateState = UpdateState.AVAILABLE
                val versionName = update.update.getVersionName()
                LOGGER.info("Update available: {}", versionName)
                Utils.infoFormat("Update available: {}", versionName)

                if (autoQueue) {
                    LOGGER.info("Auto-queuing update")
                    Utils.infoFormat("Auto-queuing update")
                    queueUpdate()
                }
            } else {
                LOGGER.info("No update available")
            }
        }, mc).exceptionally(Function { e: Throwable? ->
            LOGGER.error("[SomeFrills] Failed to check for updates", e)
            null
        })
    }

    fun queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            LOGGER.info("Trying to enqueue an update while another one is already downloaded or none is present")
            return
        }

        if (potentialUpdate == null) {
            LOGGER.error("Cannot queue update: potentialUpdate is null")
            return
        }

        updateState = UpdateState.QUEUED
        LOGGER.info("Queuing update download")
        Utils.infoFormat("Queuing update download")
        activePromise = CompletableFuture.supplyAsync<Any?> {
            LOGGER.info("Update download started")
            Utils.infoFormat("Update download started")
            try {
                potentialUpdate!!.prepareUpdate()
            } catch (e: IOException) {
                LOGGER.error("Failed to download update", e)
                Utils.infoFormat("Failed to download update: {}", e.message)
                updateState = UpdateState.AVAILABLE
            } catch (e: NullPointerException) {
                LOGGER.error("Update was cleared while downloading", e)
                updateState = UpdateState.AVAILABLE
            }
            null
        }.thenAcceptAsync(Consumer thenAcceptAsync@{ _: Any? ->
            if (potentialUpdate == null) {
                LOGGER.error("Update was cleared before installation")
                return@thenAcceptAsync
            }
            LOGGER.info("Update download completed")
            Utils.infoFormat("Update download completed. Will be installed on next restart.")
            updateState = UpdateState.DOWNLOADED
            potentialUpdate!!.executePreparedUpdate()
        }, mc)
    }

    private fun cancelActivePromise() {
        if (activePromise != null) {
            activePromise!!.cancel(true)
            activePromise = null
        }
    }

    private fun parseSemanticVersion(version: String): Int {
        if (version.isEmpty()) return 0

        val numbers: Array<String> = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (numbers.size < 3) return 0

        val major = parseInt(numbers[0])
        val minor = parseInt(numbers[1])
        val patch = parseInt(numbers[2])
        return major * 1000 + minor * 100 + patch
    }

    private fun parseInt(str: String): Int {
        return try {
            str.toInt()
        } catch (_: NumberFormatException) {
            0
        }
    }

    enum class UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
    }
}

package com.somefrills.features.solvers

import at.hannibal2.skyhanni.api.ExperimentationTableApi
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.somefrills.config.FrillsMod
import com.somefrills.events.InventoryUpdateEvent
import com.somefrills.events.PlaySoundEvent
import com.somefrills.events.ScreenCloseEvent
import com.somefrills.events.SlotClickEvent
import com.somefrills.events.TickEventPost
import com.somefrills.features.core.AreaFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.utils.*
import meteordevelopment.orbit.EventHandler
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot


@FrillsFeature
object ExperimentSolver : AreaFeature(FrillsMod.config.solvers.experimentSolver.enabled) {
    private val config get() = FrillsMod.config.solvers.experimentSolver

    // Auto-click tracking
    private var pendingClickSlot: Slot? = null
    private var lastAutoClickTime: Long = 0

    private enum class HelperPhase {
        READ,
        REPLICATE
    }

    private const val ROUND_STATUS_SLOT = 4
    private const val PHASE_STATUS_SLOT = 49
    private val hypixelChronomatronData: MutableList<LorenzColor> = mutableListOf()
    private val userChronomatronProgress: MutableList<LorenzColor> = mutableListOf()
    private val hypixelUltrasequencerData: MutableList<Int> = mutableListOf()
    private val userUltrasequencerProgress: MutableList<Int> = mutableListOf()
    private val ultrasequencerDyeMap: MutableMap<Int, ItemStack> = mutableMapOf()

    private var chronHasBeenEmpty: Boolean = true
    private var lastChronomatronSound: SimpleTimeMark = SimpleTimeMark.farPast()
    private var currentAddonPhase: HelperPhase? = null
    private var chronomatronSequenceIndex: Int = 0
    var currentChronomatronRound: Int = 0
        private set
    var currentUltraSequencerRound: Int = 0
        private set

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §7Round: §e1
     * REGEX-TEST: §7Round: §e2
     */
    private val roundItemPattern by ExperimentationTableApi.patternGroup.pattern(
        "addons.round-item",
        "§7Round: §e(?<round>\\d+)",
    )

    /**
     * REGEX-TEST: §7Timer: §a3s
     * REGEX-TEST: §7Timer: §a10s
     */
    private val replicatePhaseItemPattern by ExperimentationTableApi.patternGroup.pattern(
        "addons.replicate-phase-item",
        "§7Timer: §a\\d+s",
    )

    private val readPhaseItemPattern by ExperimentationTableApi.patternGroup.pattern(
        "addons.read-phase-item",
        "§aRemember the pattern!",
    )

    /**
     * REGEX-TEST: minecraft:stained_hardened_clay
     * REGEX-TEST: minecraft:orange_terracotta
     */
    private val nextChronomatronItemPattern by ExperimentationTableApi.patternGroup.pattern(
        "addons.chronomatron.read-item",
        "(?:minecraft:)?(?:stained_hardened_clay|\\w+_terracotta)",
    )
    // </editor-fold>

    @EventHandler
    fun resetAddonsData(@Suppress("UNUSED_PARAMETER") event: ScreenCloseEvent) {
        hypixelChronomatronData.clear()
        userChronomatronProgress.clear()
        hypixelUltrasequencerData.clear()
        userUltrasequencerProgress.clear()
        currentChronomatronRound = 0
        currentUltraSequencerRound = 0
        chronomatronSequenceIndex = 0
        lastChronomatronSound = SimpleTimeMark.farPast()
        currentAddonPhase = null
        chronHasBeenEmpty = true
        pendingClickSlot = null
    }

    @EventHandler
    fun onTick(@Suppress("UNUSED_PARAMETER") event: TickEventPost) {
        if (!config.enabled.get() || currentAddonPhase != HelperPhase.REPLICATE) return
        if (!ExperimentationTableApi.inAddon) return

        // Scan for next clickable slot if we don't have one queued
        if (pendingClickSlot == null) {
            if (ExperimentationTableApi.inChronomatron) scanChronomatron()
            if (ExperimentationTableApi.inUltrasequencer) scanUltrasequencer()
        }

        // Execute pending click if delay passed
        val slot = pendingClickSlot ?: return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAutoClickTime >= config.clickDelay) {
            ContainerUtils.clickSlot(slot)
            lastAutoClickTime = currentTime
            pendingClickSlot = null
        }
    }

    private fun scanChronomatron() {
        if (userChronomatronProgress.size >= hypixelChronomatronData.size) return
        val expectedColor = hypixelChronomatronData[userChronomatronProgress.size]

        val inventoryItems = ContainerUtils.currentlyOpenContainer?.slots ?: return
        for (slot in inventoryItems) {
            val item = slot.item
            if (item == null || item.isEmpty) continue
            val slotColor = item.getLorenzColorOrNull()
            if (slotColor == expectedColor) {
                pendingClickSlot = slot
                return
            }
        }
    }

    private fun scanUltrasequencer() {
        if (userUltrasequencerProgress.size >= hypixelUltrasequencerData.size) return
        val expectedSlotIndex = hypixelUltrasequencerData[userUltrasequencerProgress.size]

        val inventoryItems = ContainerUtils.currentlyOpenContainer?.slots ?: return
        for (slot in inventoryItems) {
            val item = slot.item
            if (slot.index == expectedSlotIndex && item != null && !item.isEmpty) {
                pendingClickSlot = slot
                return
            }
        }
    }

    private fun ItemStack.getLorenzColorOrNull(): LorenzColor? = when (hoverName.string.removeColor()) {
        "Green" -> LorenzColor.DARK_GREEN
        "Lime" -> LorenzColor.GREEN
        "Pink" -> LorenzColor.LIGHT_PURPLE
        "Cyan" -> LorenzColor.DARK_AQUA
        "Orange" -> LorenzColor.GOLD
        "Purple" -> LorenzColor.DARK_PURPLE
        else -> try {
            LorenzColor.valueOf(hoverName.formattedTextCompatLeadingWhiteLessResets().removeColor().uppercase())
        } catch (ignored: IllegalArgumentException) {
            null
        }
    }

    // <editor-fold desc="Slot click stuff">
    @EventHandler
    fun onSlotClick(event: SlotClickEvent) {
        if (event.slot == null || event.slot.item == null || !ExperimentationTableApi.inAddon) return
        if (currentAddonPhase != HelperPhase.REPLICATE) return
        event.handleChronomatronClick()
        event.handleUltrasequencerClick()
    }

    private fun SlotClickEvent.handleChronomatronClick() {
        if (!ExperimentationTableApi.inChronomatron || slot == null) return
        if (userChronomatronProgress.size == hypixelChronomatronData.size) return
        val clickedColor = slot.item?.getLorenzColorOrNull()?.takeIf {
            it == hypixelChronomatronData[userChronomatronProgress.size]
        } ?: run {
            return
        }
        userChronomatronProgress.add(clickedColor)
    }

    private fun SlotClickEvent.handleUltrasequencerClick() {
        if (!ExperimentationTableApi.inUltrasequencer || slot == null) return
        if (userUltrasequencerProgress.size == hypixelUltrasequencerData.size) return
        val clickedSlot = slot.index.takeIf {
            val expectedSlot = hypixelUltrasequencerData[userUltrasequencerProgress.size]
            it == expectedSlot
        } ?: run {
            return
        }
        userUltrasequencerProgress.add(clickedSlot)
    }
    // </editor-fold>

    // <editor-fold desc="Inventory Update reading logic">
    @EventHandler
    fun onPlaySound(event: PlaySoundEvent) {
        if (!ExperimentationTableApi.inChronomatron) return
        // This sound indicates when the player has finished a round in chronomatron
        if (event.soundName != "entity.player.levelup" || event.pitch != 1.7619047f || event.volume != 0.7f) return
        lastChronomatronSound = SimpleTimeMark.now()
    }

    @EventHandler
    fun onInventoryUpdated(@Suppress("UNUSED_PARAMETER") event: InventoryUpdateEvent) {
        if (!ExperimentationTableApi.inAddon) return
        val oldAddonPhase = currentAddonPhase
        currentAddonPhase = readPhaseOrNull() ?: return

        if (ExperimentationTableApi.inChronomatron) readNextChronomatron(oldAddonPhase)
        if (ExperimentationTableApi.inUltrasequencer) readUltrasequencer()
    }

    private fun readPhaseOrNull(): HelperPhase? {
        val inventoryItems = ContainerUtils.currentlyOpenContainer?.slots ?: return null
        val phaseItemName = inventoryItems[PHASE_STATUS_SLOT]?.item?.hoverName?.formattedTextCompatLeadingWhiteLessResets() ?: return null
        return when {
            replicatePhaseItemPattern.matches(phaseItemName) -> HelperPhase.REPLICATE
            readPhaseItemPattern.matches(phaseItemName) -> HelperPhase.READ
            else -> null
        }
    }

    private fun readChronomatronRoundOrNull(): Int? {
        val inventoryItems = ContainerUtils.currentlyOpenContainer?.slots ?: return null
        val roundItemName = inventoryItems[ROUND_STATUS_SLOT]?.item?.hoverName?.formattedTextCompatLeadingWhiteLessResets() ?: return null
        return roundItemPattern.matchGroup(roundItemName, "round")?.formatIntOrNull()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun readNextChronomatron(oldPhase: HelperPhase? = null) {
        currentChronomatronRound = readChronomatronRoundOrNull() ?: return
        val hypixelSizeNow = hypixelChronomatronData.size
        val userSizeNow = userChronomatronProgress.size

        val inventoryItems = ContainerUtils.currentlyOpenContainer?.slots ?: return
        val activeColors = inventoryItems.filter {
            nextChronomatronItemPattern.matches(it.item?.item?.getIdentifierString())
        }.mapNotNull { it.item?.getLorenzColorOrNull() }.distinct()

        chronHasBeenEmpty = if (activeColors.isEmpty()) true
        else if (!chronHasBeenEmpty) return
        else false

        val clickedColor = activeColors.firstOrNull { itemColor ->
            val expectedColor = hypixelChronomatronData.getOrNull(chronomatronSequenceIndex)
            expectedColor == null || itemColor == expectedColor
        } ?: return

        val shouldReadLastReplicate = oldPhase == HelperPhase.READ || hypixelSizeNow < currentChronomatronRound
        val isReadingReady = oldPhase == null || oldPhase == HelperPhase.READ
        val shouldNotReadYet = when (currentAddonPhase) {
            HelperPhase.REPLICATE -> !shouldReadLastReplicate
            HelperPhase.READ -> !isReadingReady
            // User hasn't progressed enough or the last sound was too long ago
            else -> userSizeNow < hypixelSizeNow || lastChronomatronSound.isFarPast() && chronomatronSequenceIndex != 0
        }
        if (shouldNotReadYet) return

        // Only record if we're exactly at the next slot, otherwise increment the index
        if (chronomatronSequenceIndex == hypixelSizeNow) {
            hypixelChronomatronData.add(clickedColor)
            lastChronomatronSound = SimpleTimeMark.farPast()
            chronomatronSequenceIndex = 0
            userChronomatronProgress.clear()
        } else chronomatronSequenceIndex++
    }

    private data class UltraSequencerSlot(
        val sequenceNumber: Int,
        val slotIndex: Int,
        val itemStack: ItemStack,
    )

    private fun readUltrasequencer() {
        val inventoryItems = ContainerUtils.currentlyOpenContainer?.slots ?: return
        val orderedUltrasequencerSlots = inventoryItems.filter {
            it.item?.hoverName.formattedTextCompatLeadingWhiteLessResets().trim().isNotEmpty()
        }.mapNotNull { slot ->
            val stack = slot.item ?: return@mapNotNull null
            val sequenceNumber = stack.hoverName.string.removeColor().toIntOrNull() ?: return@mapNotNull null
            currentUltraSequencerRound = maxOf(currentUltraSequencerRound, sequenceNumber)
            if (sequenceNumber !in ultrasequencerDyeMap) ultrasequencerDyeMap[sequenceNumber] = stack
            UltraSequencerSlot(
                sequenceNumber = sequenceNumber,
                slotIndex = slot.index,
                itemStack = stack,
            )
        }.sortedBy { it.sequenceNumber }

        val isOld = currentUltraSequencerRound != orderedUltrasequencerSlots.size
        val alreadyKnown = hypixelChronomatronData.size == orderedUltrasequencerSlots.size
        if (isOld || alreadyKnown) return

        hypixelUltrasequencerData.clear()
        userUltrasequencerProgress.clear()
        hypixelUltrasequencerData.addAll(orderedUltrasequencerSlots.map { it.slotIndex })
    }
    // </editor-fold>
    override fun checkArea(area: Area): Boolean {
        return area == Area.PRIVATE_ISLAND
    }
}
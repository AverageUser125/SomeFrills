package com.somefrills.features.solvers

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.TickEventPost
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Utils
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

@FrillsFeature
object ExperimentSolver : Feature(FrillsMod.config.solvers.experimentSolver.enabled) {
    private val config get() = FrillsMod.config.solvers.experimentSolver

    private val ultrasequencerOrder: MutableMap<Int, Int> = HashMap()
    private val chronomatronOrder: MutableList<Int> = ArrayList()
    private var lastClickTime: Long = 0
    private var hasAdded = false
    private var lastAdded = 0
    private var clicks = 0

    val experimentType: ExperimentType
        get() {
            val screen = mc.currentScreen ?: return ExperimentType.None
            if (Utils.isOnPrivateIsland() && screen is GenericContainerScreen) {
                val title: String = screen.getTitle().string
                if (title.startsWith("Chronomatron (")) return ExperimentType.Chronomatron
                if (title.startsWith("Ultrasequencer (")) return ExperimentType.Ultrasequencer
                if (title.startsWith("Superpairs (")) return ExperimentType.Superpairs
            }
            return ExperimentType.None
        }

    @EventHandler
    private fun onTick(event: TickEventPost) {
        val player = mc.player ?: return
        val handler = player.currentScreenHandler ?: return

        val type: ExperimentType = experimentType
        if (type == ExperimentType.None) {
            reset()
            return
        }
        // Determine which experiment type
        if (config.chronomatron.enabled && type == ExperimentType.Chronomatron) {
            solveChronomatron(handler)
        } else if (config.ultrasequencer.enabled && type == ExperimentType.Ultrasequencer) {
            solveUltraSequencer(handler)
        }
    }

    private fun solveChronomatron(handler: ScreenHandler) {
        val invSlots: MutableList<Slot> = handler.slots
        val maxChronomatron = config.chronomatron.closeThreshold

        // Check if slot 49 is glowstone AND last added slot is not enchanted (click registered)
        val slot49 = invSlots.get(49)
        val lastAddedSlot = invSlots.get(lastAdded)
        val slot49IsGlowstone = slot49.stack != null && isGlowstone(slot49)
        val lastAddedNotEnchanted = lastAddedSlot.stack != null && !isEnchanted(lastAddedSlot.stack)

        if (slot49IsGlowstone && lastAddedNotEnchanted) {
            if (config.chronomatron.shouldClose && chronomatronOrder.size > maxChronomatron) {
                mc.player?.closeHandledScreen()
            }
            hasAdded = false
        }

        // Detect new item entering: slot 49 is clock
        if (!hasAdded && slot49.stack != null && isClock(slot49)) {
            for (i in 11..56) { // Scan the colored glass/terracotta area (slots 11-56)
                if (!isValidChronoSlot(i)) continue
                val s = invSlots.get(i)
                val stack = s.stack
                if (stack == null || stack.isEmpty) continue
                if (!isEnchanted(stack)) continue
                if (!isTerracotta(stack)) continue
                chronomatronOrder.add(i)
            }

            if (!chronomatronOrder.isEmpty()) {
                lastAdded = chronomatronOrder.last()
                hasAdded = true
                clicks = 0
            }
        }

        // Perform clicking: slot 49 is clock AND we have items to click
        if (hasAdded && slot49.stack != null && isClock(slot49)
            && chronomatronOrder.size > clicks && System.currentTimeMillis() - lastClickTime > config.clickDelay
        ) {
            val slotToClick = chronomatronOrder.get(clicks)
            sendClickPacket(handler, slotToClick)
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }

    private fun solveUltraSequencer(handler: ScreenHandler) {
        val invSlots: MutableList<Slot> = handler.slots
        val maxUltraSequencer = config.ultrasequencer.closeThreshold

        // Reset when slot 49 becomes clock (new round)
        val slot49 = invSlots.get(49)
        if (slot49.stack != null && isClock(slot49)) {
            hasAdded = false
        }

        // Detect and rebuild map when slot 49 becomes glowstone
        if (!hasAdded && slot49.stack != null && isGlowstone(slot49)) {
            ultrasequencerOrder.clear()

            for (i in invSlots.indices) {
                val s = invSlots.get(i)
                if (s.stack != null && !s.stack.isEmpty) {
                    val stackSize = s.stack.count
                    if (isDye(s)) {
                        val idx = stackSize - 1
                        ultrasequencerOrder.put(idx, i)
                    }
                }
            }

            hasAdded = true
            clicks = 0

            if (ultrasequencerOrder.size > maxUltraSequencer && config.ultrasequencer.shouldClose) {
                val player = MinecraftClient.getInstance().player
                if (player != null) {
                    player.closeHandledScreen()
                }
            }
        }

        // Perform clicking: slot 49 is clock AND we have dyes to click
        if (slot49.stack != null && isClock(slot49)
            && ultrasequencerOrder.containsKey(clicks) && System.currentTimeMillis() - lastClickTime > config.clickDelay
        ) {
            val slotToClick = ultrasequencerOrder.get(clicks)
            if (slotToClick != null) {
                sendClickPacket(handler, slotToClick)
            }
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }

    private fun sendClickPacket(handler: ScreenHandler, slotIdx: Int) {
        val client = MinecraftClient.getInstance()
        if (client.interactionManager == null || client.player == null) {
            return
        }

        client.interactionManager!!.clickSlot(
            handler.syncId,
            slotIdx,
            0,
            SlotActionType.PICKUP,
            client.player
        )
    }

    private fun isEnchanted(stack: ItemStack?): Boolean {
        if (stack == null || stack.isEmpty) return false
        return stack.hasEnchantments() || stack.hasGlint()
    }

    private fun reset() {
        ultrasequencerOrder.clear()
        chronomatronOrder.clear()
        hasAdded = false
        lastAdded = 0
        clicks = 0
    }

    enum class ExperimentType {
        Chronomatron, Ultrasequencer, Superpairs, None
    }

    private fun isValidChronoSlot(idx: Int): Boolean {
        return (11 <= idx && idx <= 19) || (30 <= idx && idx <= 38)
    }

    private fun isGlowstone(stack: ItemStack): Boolean {
        return stack.item == Items.GLOWSTONE
    }

    private fun isClock(stack: ItemStack): Boolean {
        return stack.item == Items.CLOCK
    }

    private fun isGlowstone(s: Slot): Boolean {
        return isGlowstone(s.stack)
    }

    private fun isClock(s: Slot): Boolean {
        return isClock(s.stack)
    }

    private fun isDye(stack: ItemStack): Boolean {
        val item = stack.item
        return item is DyeItem
                || item == Items.INK_SAC
                || item == Items.BONE_MEAL
                || item == Items.LAPIS_LAZULI
                || item == Items.COCOA_BEANS
    }

    private fun isDye(s: Slot): Boolean {
        return isDye(s.stack)
    }

    private fun isTerracotta(stack: ItemStack): Boolean {
        return stack.item.toString().endsWith("terracotta")
    }

    private fun isTerracotta(s: Slot): Boolean {
        return isTerracotta(s.stack)
    }

}
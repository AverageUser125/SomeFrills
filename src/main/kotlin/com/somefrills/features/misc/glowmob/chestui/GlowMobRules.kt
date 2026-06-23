package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.config.misc.GlowMobConfig.GlowMobRule
import com.somefrills.features.misc.glowmob.GlowMob
import com.somefrills.features.misc.glowmob.MatchInfo.GearFlag
import com.somefrills.misc.MyMapColor
import com.somefrills.misc.RenderColor.Companion.fromHex
import com.somefrills.utils.GuiUtils
import com.somefrills.utils.TextUtils
import com.somefrills.utils.plainCustomName
import com.somefrills.utils.setCustomName
import com.somefrills.utils.wrapByDelimiter
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import org.lwjgl.glfw.GLFW
import java.util.*

class GlowMobRules : ChestUI("GlowMob Rules") {
    private val allRules: MutableList<GlowMobRule> = GlowMob.rules
    private var session: RuleEditSession? = null

    init {
        addAddon(PagingAddon())
        rebuild()
    }

    override fun build() {
        for (i in allRules.indices) {
            val rule = allRules[i]
            val stack = ItemStack(if (rule.enabled()) Items.GREEN_TERRACOTTA else Items.RED_TERRACOTTA)
            // Display the rule index (1-based) in the menu
            val displayId = "Rule " + (i + 1)
            stack.setCustomName(
                colorStyle(if (rule.enabled()) ChatFormatting.GREEN else ChatFormatting.RED)!!.withItalic(false),
                displayId
            )
            stack.set(DataComponents.LORE, getRuleLore(rule))
            addItem(stack)
        }

        val createButton = ItemStack(Items.YELLOW_TERRACOTTA)
        createButton.setCustomName(colorStyle(ChatFormatting.GREEN)!!.withItalic(false), "Create Rule")

        val lore: MutableList<Component> = ArrayList<Component>()
        lore.add(
            Component.literal("Setup a new ").setStyle(colorStyle(ChatFormatting.GRAY))
                .append(Component.literal("Glow").setStyle(colorStyle(ChatFormatting.RED)))
                .append(Component.literal(" rule.").setStyle(colorStyle(ChatFormatting.GRAY)))
        )
        lore.add(Component.literal(""))
        lore.add(Component.literal("Click to create!").setStyle(colorStyle(ChatFormatting.YELLOW)))
        createButton.set(DataComponents.LORE, ItemLore(lore, lore))

        addItem(createButton)
    }

    private fun getRuleLore(rule: GlowMobRule): ItemLore {
        val matchInfo = rule.info()

        val lines: MutableList<Component> = ArrayList<Component>()

        lines.add(Component.literal("Conditions: ").setStyle(colorStyle(ChatFormatting.GREEN)))

        if (!matchInfo.type.isEmpty()) {
            val typeLines: Array<String> =
                matchInfo.type.toString().wrapByDelimiter(20, ",").split("\n".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            lines.add(
                Component.literal("Type: ").setStyle(colorStyle(ChatFormatting.GRAY))
                    .append(Component.literal(typeLines[0]).setStyle(colorStyle(ChatFormatting.YELLOW)))
            )
            for (i in 1..<typeLines.size) {
                lines.add(Component.literal(typeLines[i]).setStyle(colorStyle(ChatFormatting.YELLOW)))
            }
        }

        if (!matchInfo.name.isEmpty()) {
            lines.add(
                Component.literal("Name: ").setStyle(colorStyle(ChatFormatting.GRAY))
                    .append(Component.literal(matchInfo.name).setStyle(colorStyle(ChatFormatting.YELLOW)))
            )
        }

        if (matchInfo.area != null) {
            lines.add(
                Component.literal("Island: ").setStyle(colorStyle(ChatFormatting.GRAY))
                    .append(Component.literal(matchInfo.area!!.displayName).setStyle(colorStyle(matchInfo.area!!.colorHex)))
            )
        }

        if (!matchInfo.gear.isEmpty()) {
            val gearStr = matchInfo.gear.joinToString { obj: GearFlag -> obj.name }
            lines.add(
                Component.literal("Gear: ").setStyle(colorStyle(ChatFormatting.GRAY))
                    .append(Component.literal(gearStr).setStyle(colorStyle(ChatFormatting.YELLOW)))
            )
        }

        lines.add(Component.literal(""))
        lines.add(Component.literal("Color: ").setStyle(colorStyle(ChatFormatting.YELLOW)))

        val colorDisplay = TextUtils.colorToString(rule.color())
        lines.add(Component.literal(capitalize(colorDisplay)).setStyle(colorStyle(rule.color().hex)))

        lines.add(Component.literal(""))
        lines.add(
            Component.literal("Enabled: ").setStyle(colorStyle(ChatFormatting.YELLOW))
                .append(
                    Component.literal(if (rule.enabled()) "On" else "Off")
                        .setStyle(colorStyle(if (rule.enabled()) ChatFormatting.GREEN else ChatFormatting.RED))
                )
        )

        lines.add(Component.literal("Right-click to toggle!").setStyle(colorStyle(ChatFormatting.YELLOW)))
        lines.add(Component.literal("Left-click to configure!").setStyle(colorStyle(ChatFormatting.YELLOW)))

        return ItemLore(lines, lines)
    }

    private fun colorStyle(color: ChatFormatting): Style {
        val colorValue = color.color
        if (colorValue == null) {
            return Style.EMPTY
        }
        return colorStyle(colorValue)
    }

    private fun colorStyle(colorHex: Int): Style {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorHex))
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack == null || stack.isEmpty) return

        val itemName = stack.plainCustomName
        if (itemName.isEmpty()) return

        if (itemName == "Create Rule") {
            createNewRule()
            return
        }
        // Expecting display name like "Rule {index}"; parse index and use index-based lookup
        var parsedId: Int? = null
        if (itemName.startsWith("Rule ")) {
            try {
                parsedId = itemName.substring(5).trim { it <= ' ' }.toInt()
            } catch (ignored: NumberFormatException) {
            }
        } else {
            try {
                parsedId = itemName.trim { it <= ' ' }.toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        if (parsedId == null) return

        if (parsedId < 1 || parsedId > allRules.size) return
        val rule = allRules[parsedId - 1]

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            GlowMob.toggleRule(parsedId)
            rebuild()
            return
        }
        // both middle-click and left-click will open the editor for the rule
        openRuleEditor(rule)
    }

    override fun onReturn() {
        val session = session ?: run {
            rebuild()
            return
        }

        // Check if revert was requested
        if (session.menu.isRevertRequested) {
            // Restore working copy from original
            if (!session.isNew) {
                session.workingCopy = GlowMobRule(session.original!!)
                session.workingCopy.recompilePredicate()
            } else {
                // For new rules, just clear the working copy
                session.workingCopy = GlowMobRule()
            }
        }

        if (session.isNew) {
            if (!session.workingCopy.info().isEmpty) {
                GlowMob.addRule(session.workingCopy)
            }
        } else {
            if (session.workingCopy.info().isEmpty) {
                GlowMob.removeRule(session.original!!)
            } else {
                GlowMob.replaceRule(session.original!!, session.workingCopy)
            }
        }

        this.session = null
        super.onReturn()
    }

    private fun openRuleEditor(rule: GlowMobRule?) {
        session = RuleEditSession(rule)
        session!!.menu = GlowMobEditMenu(this, session!!.workingCopy, false)
        GuiUtils.setScreen(session!!.menu)
    }

    private fun createNewRule() {
        session = RuleEditSession(null)
        session!!.workingCopy = GlowMobRule(session!!.workingCopy.info(), fromHex(MyMapColor.WHITE.hex))
        session!!.workingCopy.toggle() // new rules start as enabled by default
        session!!.menu = GlowMobEditMenu(this, session!!.workingCopy, true)
        GuiUtils.setScreen(session!!.menu)
    }

    private class RuleEditSession(// null if creating new
        var original: GlowMobRule?
    ) {
        var workingCopy: GlowMobRule
        lateinit var menu: GlowMobEditMenu // reference to track revert flag

        init {
            if (original == null) {
                this.workingCopy = GlowMobRule()
            } else {
                // editing existing rule: create a copy so edits can be committed or discarded
                this.workingCopy = GlowMobRule(original!!)
                this.workingCopy.recompilePredicate() // ensure predicate is recompiled for the copy, since it will be modified
            }
        }

        val isNew: Boolean
            get() = original == null
    }

    companion object {
        private fun capitalize(str: String): String {
            if (str.isEmpty()) return str
            return str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
        }
    }
}


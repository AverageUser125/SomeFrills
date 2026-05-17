package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.config.misc.GlowMobConfig.GlowMobRule
import com.somefrills.features.core.Features.get
import com.somefrills.features.misc.glowmob.GlowMob
import com.somefrills.features.misc.glowmob.MatchInfo.GearFlag
import com.somefrills.misc.MyMapColor
import com.somefrills.misc.RenderColor.Companion.fromHex
import com.somefrills.misc.Utils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW
import java.util.*
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.NumberFormatException

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
            Utils.setCustomName(
                stack,
                colorStyle(if (rule.enabled()) Formatting.GREEN else Formatting.RED)!!.withItalic(false),
                displayId
            )
            stack.set<LoreComponent?>(DataComponentTypes.LORE, getRuleLore(rule))
            addItem(stack)
        }

        val createButton = ItemStack(Items.YELLOW_TERRACOTTA)
        Utils.setCustomName(createButton, colorStyle(Formatting.GREEN)!!.withItalic(false), "Create Rule")

        val lore: MutableList<Text> = ArrayList<Text>()
        lore.add(
            Text.literal("Setup a new ").setStyle(colorStyle(Formatting.GRAY))
                .append(Text.literal("Glow").setStyle(colorStyle(Formatting.RED)))
                .append(Text.literal(" rule.").setStyle(colorStyle(Formatting.GRAY)))
        )
        lore.add(Text.literal(""))
        lore.add(Text.literal("Click to create!").setStyle(colorStyle(Formatting.YELLOW)))
        createButton.set<LoreComponent?>(DataComponentTypes.LORE, LoreComponent(lore, lore))

        addItem(createButton)
    }

    private fun getRuleLore(rule: GlowMobRule): LoreComponent {
        val matchInfo = rule.info()

        val lines: MutableList<Text?> = ArrayList<Text?>()

        lines.add(Text.literal("Conditions: ").setStyle(colorStyle(Formatting.GREEN)))

        if (!matchInfo.type.isEmpty()) {
            val typeLines: Array<String> =
                Utils.wrapByDelimiter(matchInfo.type.toString(), 20, ",").split("\n".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            lines.add(
                Text.literal("Type: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(typeLines[0]).setStyle(colorStyle(Formatting.YELLOW)))
            )
            for (i in 1..<typeLines.size) {
                lines.add(Text.literal(typeLines[i]).setStyle(colorStyle(Formatting.YELLOW)))
            }
        }

        if (!matchInfo.name.isEmpty()) {
            lines.add(
                Text.literal("Name: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(matchInfo.name).setStyle(colorStyle(Formatting.YELLOW)))
            )
        }

        if (matchInfo.area != null) {
            lines.add(
                Text.literal("Island: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(matchInfo.area!!.displayName).setStyle(colorStyle(matchInfo.area!!.colorHex)))
            )
        }

        if (!matchInfo.gear.isEmpty()) {
            val gearStr = matchInfo.gear.joinToString { obj: GearFlag -> obj.name }
            lines.add(
                Text.literal("Gear: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(gearStr).setStyle(colorStyle(Formatting.YELLOW)))
            )
        }

        lines.add(Text.literal(""))
        lines.add(Text.literal("Color: ").setStyle(colorStyle(Formatting.YELLOW)))

        val colorDisplay = Utils.colorToString(rule.color())
        lines.add(Text.literal(capitalize(colorDisplay)).setStyle(colorStyle(rule.color().hex)))

        lines.add(Text.literal(""))
        lines.add(
            Text.literal("Enabled: ").setStyle(colorStyle(Formatting.YELLOW))
                .append(
                    Text.literal(if (rule.enabled()) "On" else "Off")
                        .setStyle(colorStyle(if (rule.enabled()) Formatting.GREEN else Formatting.RED))
                )
        )

        lines.add(Text.literal("Right-click to toggle!").setStyle(colorStyle(Formatting.YELLOW)))
        lines.add(Text.literal("Left-click to configure!").setStyle(colorStyle(Formatting.YELLOW)))

        return LoreComponent(lines, lines)
    }

    private fun colorStyle(color: Formatting): Style? {
        val colorValue = color.getColorValue()
        if (colorValue == null) {
            return Style.EMPTY
        }
        return colorStyle(colorValue)
    }

    private fun colorStyle(colorHex: Int): Style? {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorHex))
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack == null || stack.isEmpty) return

        val itemName = Utils.getPlainCustomName(stack)
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
            get<GlowMob>(GlowMob::class.java).toggleRule(parsedId)
            rebuild()
            return
        }
        // both middle-click and left-click will open the editor for the rule
        openRuleEditor(rule)
    }

    override fun onReturn() {
        val glowMob = get(GlowMob::class.java)
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
                glowMob.addRule(session.workingCopy)
            }
        } else {
            if (session.workingCopy.info().isEmpty) {
                glowMob.removeRule(session.original!!)
            } else {
                glowMob.replaceRule(session.original!!, session.workingCopy)
            }
        }

        this.session = null
        super.onReturn()
    }

    private fun openRuleEditor(rule: GlowMobRule?) {
        session = RuleEditSession(rule)
        session!!.menu = GlowMobEditMenu(this, session!!.workingCopy, false)
        Utils.setScreen(session!!.menu)
    }

    private fun createNewRule() {
        session = RuleEditSession(null)
        session!!.workingCopy = GlowMobRule(session!!.workingCopy.info(), fromHex(MyMapColor.WHITE.hex))
        session!!.workingCopy.toggle() // new rules start as enabled by default
        session!!.menu = GlowMobEditMenu(this, session!!.workingCopy, true)
        Utils.setScreen(session!!.menu)
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


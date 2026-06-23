package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.config.misc.GlowMobConfig.GlowMobRule
import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.features.misc.glowmob.MatchInfo.GearFlag
import com.somefrills.misc.MyMapColor.Companion.getClosest
import com.somefrills.utils.GuiUtils
import com.somefrills.utils.NumberUtils
import com.somefrills.utils.TextUtils
import com.somefrills.utils.formatCompact
import com.somefrills.utils.plainCustomName
import com.somefrills.utils.setCustomName
import com.somefrills.utils.wrapByDelimiter
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import java.util.function.Consumer

class GlowMobEditMenu(previousMenu: ChestUI?, private val rule: GlowMobRule, private val isNewRule: Boolean) :
    ChestUI("GlowMob Edit Menu", previousMenu) {
    private val info: MatchInfo = rule.info()
    var isRevertRequested: Boolean = false
        private set

    init {
        rebuild()
    }

    override fun build() {
        addItem(
            createChoiceItem(
                getClosest(rule.color())!!.item, "Color",
                TextUtils.colorToString(rule.color()),
                rule.color().hex,
                "Sets the glow color",
                "Click to change color"
            )
        )

        addItem(
            createChoiceItem(
                Items.CREEPER_SPAWN_EGG, "Entity",
                info.type.toString().wrapByDelimiter(20, ","),
                "Filters by mob type (zombie, creeper, etc.)",
                "Leave empty to match all types",
                "Click to choose entities"
            )
        )

        addItem(
            createChoiceItem(
                Items.NAME_TAG, "Name",
                info.name,
                "Matches custom names from armor stands",
                "Searches nearby name tags above mobs",
                "Click to set name filter"
            )
        )

        // Area may be unset; avoid forcing a null color with !! which causes NPEs
        val areaDisplay = info.area?.displayName
        val areaColor = info.area?.colorHex ?: ChatFormatting.YELLOW.color!!
        addItem(
            createChoiceItem(
                Items.CARVED_PUMPKIN, "Area",
                areaDisplay,
                areaColor,
                "Only glows mobs in specific areas",
                "Leave empty for all locations",
                "Click to select area"
            )
        )

        addItem(createGearChoiceItem())

        addItem(
            createChoiceItem(
                Items.ENCHANTED_BOOK, "Max HP",
                if (info.maxHp > 0) info.maxHp.formatCompact() else null,
                "Only glows mobs with this exact max health",
                "Leave empty for any health",
                "Click to set max HP filter"
            )
        )

        // Add revert/cancel button
        if (!isNewRule) {
            val revert = ItemStack(Items.REDSTONE)
            revert.setCustomName(colorStyle(ChatFormatting.GOLD), "Revert Changes")
            val revertLore: MutableList<Component> = ArrayList()
            revertLore.add(Component.literal("Restore to original").setStyle(colorStyle(ChatFormatting.GRAY)))
            revertLore.add(Component.literal("").setStyle(colorStyle(ChatFormatting.GRAY)))
            revertLore.add(Component.literal("Click to revert").setStyle(colorStyle(ChatFormatting.YELLOW)))
            revert.set(DataComponents.LORE, ItemLore(revertLore, revertLore))
            revert.remove(DataComponents.ATTRIBUTE_MODIFIERS)
            inventory.setItem(INV_SIZE - 9 + 3, revert)
        } else {
            val cancel = ItemStack(Items.BARRIER)
            cancel.setCustomName(colorStyle(ChatFormatting.RED), "Cancel")
            val cancelLore: MutableList<Component> = ArrayList()
            cancelLore.add(Component.literal("Discard changes").setStyle(colorStyle(ChatFormatting.GRAY)))
            cancelLore.add(Component.literal("").setStyle(colorStyle(ChatFormatting.GRAY)))
            cancelLore.add(Component.literal("Click to cancel").setStyle(colorStyle(ChatFormatting.YELLOW)))
            cancel.set(DataComponents.LORE, ItemLore(cancelLore, cancelLore))
            cancel.remove(DataComponents.ATTRIBUTE_MODIFIERS)
            inventory.setItem(INV_SIZE - 9 + 3, cancel)
        }

        val delete = ItemStack(Items.CAULDRON)
        delete.setCustomName(colorStyle(ChatFormatting.RED), "Delete")
        inventory.setItem(INV_SIZE - 9 + 5, delete)
    }

    private fun createGearChoiceItem(): ItemStack {
        val text: String
        val color: Int

        val gear: MutableSet<GearFlag> = info.gear

        // NONE
        if (gear.isEmpty()) {
            text = "(Unset)"
            color = ChatFormatting.RED.color!!
            return createChoiceItem(
                Items.IRON_CHESTPLATE,
                "Gear",
                text,
                color,
                "Filters by equipped armor",
                "Leave empty for any armor state",
                "Click to toggle armor filter"
            )
        }

        // NAKED overrides everything
        if (gear.contains(GearFlag.NAKED)) {
            text = "Naked"
            return createChoiceItem(
                Items.CHAINMAIL_CHESTPLATE,
                "Gear",
                text,
                "Filters by equipped armor",
                "Only matches mobs with no armor",
                "Click to toggle armor filter"
            )
        }

        // NORMAL ARMOR SELECTION
        val parts: MutableList<String> = ArrayList()

        if (gear.contains(GearFlag.CHEST)) parts.add("Chestplate")
        if (gear.contains(GearFlag.LEGS)) parts.add("Leggings")
        if (gear.contains(GearFlag.FEET)) parts.add("Boots")
        if (gear.contains(GearFlag.HEAD)) parts.add("Helmet")

        text = parts.joinToString(", ")

        color = (if (parts.isEmpty())
            ChatFormatting.RED.color
        else
            ChatFormatting.GREEN.color)!!

        return createChoiceItem(
            Items.IRON_CHESTPLATE,
            "Gear",
            text,
            color,
            "Filters by equipped armor",
            "Matches mobs wearing selected pieces",
            "Click to toggle armor filter"
        )
    }

    private fun createChoiceItem(
        item: Item,
        label: String,
        chosen: String?,
        vararg descriptions: String
    ): ItemStack {
        return createChoiceItem(item, label, chosen, ChatFormatting.YELLOW.color!!, *descriptions)
    }

    private fun createChoiceItem(
        item: Item,
        label: String,
        chosen: String?,
        chosenColor: Int,
        vararg descriptions: String
    ): ItemStack {
        val stack = ItemStack(item)
        stack.setCustomName(colorStyle(ChatFormatting.GREEN).withItalic(false), label)

        val lore: MutableList<Component> = ArrayList()

        if (chosen.isNullOrEmpty()) {
            lore.add(
                Component.literal("Chosen: ").setStyle(colorStyle(ChatFormatting.GRAY))
                    .append(Component.literal("(Unset)").setStyle(colorStyle(ChatFormatting.DARK_GRAY)))
            )
        } else {
            val lines: Array<String> =
                chosen.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            lore.add(
                Component.literal("Chosen: ").setStyle(colorStyle(ChatFormatting.GRAY))
                    .append(Component.literal(lines[0]).setStyle(colorStyle(chosenColor)))
            )
            for (i in 1..<lines.size) {
                lore.add(Component.literal(lines[i]).setStyle(colorStyle(chosenColor)))
            }
        }

        lore.add(Component.literal(""))

        // Add descriptions with proper ChatFormatting
        if (descriptions.isNotEmpty()) {
            for (i in descriptions.indices) {
                val desc: String = descriptions[i]
                if (i == descriptions.size - 1) {
                    // Last line: action text in YELLOW
                    lore.add(Component.literal(desc).setStyle(colorStyle(ChatFormatting.YELLOW)))
                } else {
                    // Middle lines: help text in GRAY
                    lore.add(Component.literal(desc).setStyle(colorStyle(ChatFormatting.GRAY)))
                }
            }
        }

        stack.set(DataComponents.LORE, ItemLore(lore, lore))
        stack.remove(DataComponents.ATTRIBUTE_MODIFIERS)
        return stack
    }

    private fun colorStyle(color: ChatFormatting): Style {
        val colorValue = color.color ?: return Style.EMPTY
        return colorStyle(colorValue)
    }

    private fun colorStyle(colorHex: Int): Style {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorHex))
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack == null || stack.isEmpty) return

        val itemName = stack.plainCustomName
        when (itemName) {
            "Entity" -> GuiUtils.setScreen(EntityTypesMenu(this, info))
            "Area" -> GuiUtils.setScreen(AreaSelectionMenu(this, info))
            "Gear" -> GuiUtils.setScreen(ArmorSelectionMenu(this, info))
            "Color" -> GuiUtils.setScreen(ColorSelectionMenu(this, rule.color()))
            "Name" -> SignGui.open(
                arrayOf("Set Name Filter", info.name)
            ) { lines: Array<String> ->
                // concat all lines except first one, IGNORE THE FIRST LINE
                // note: lines is a String[]
                val nameBuilder = StringBuilder()
                for (i in 1..<lines.size) {
                    if (i > 1) nameBuilder.append(" ")
                    nameBuilder.append(lines[i])
                }
                info.name = nameBuilder.toString().trim { it <= ' ' }
                rebuild()
                GuiUtils.setScreen(this)
            }

            "Max HP" -> SignGui.open(
                arrayOf(
                    "Enter Max Hp",
                    if (info.maxHp > 0) info.maxHp.formatCompact() else ""
                ), Consumer open@{ lines: Array<String> ->
                    if (lines.size < 2) return@open
                    val input = lines[1].trim { it <= ' ' }

                    // If empty, clear the filter
                    if (input.isEmpty()) {
                        info.maxHp = 0
                    } else {
                        try {
                            info.maxHp = NumberUtils.parseCompact(input)
                        } catch (e: NumberFormatException) {
                            // Invalid input - don't update
                            GuiUtils.setScreen(this)
                            return@open
                        }
                    }
                    rebuild()
                    GuiUtils.setScreen(this)
                })

            "Revert Changes", "Cancel" -> {
                this.isRevertRequested = true
                onClose()
            }

            "Delete" -> {
                info.clear()
                onClose()
            }
        }
    }
}

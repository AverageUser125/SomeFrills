package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.config.misc.GlowMobConfig.GlowMobRule
import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.features.misc.glowmob.MatchInfo.GearFlag
import com.somefrills.misc.MyMapColor.Companion.getClosest
import com.somefrills.misc.Utils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
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
                Utils.colorToString(rule.color()),
                rule.color().hex,
                "Sets the glow color",
                "Click to change color"
            )
        )

        addItem(
            createChoiceItem(
                Items.CREEPER_SPAWN_EGG, "Entity",
                Utils.wrapByDelimiter(info.type.toString(), 20, ","),
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

        addItem(
            createChoiceItem(
                Items.CARVED_PUMPKIN, "Area",
                if (info.area != null) info.area!!.displayName else null,
                (if (info.area != null) info.area!!.colorHex else null)!!,
                "Only glows mobs in specific areas",
                "Leave empty for all locations",
                "Click to select area"
            )
        )

        addItem(createGearChoiceItem())

        addItem(
            createChoiceItem(
                Items.ENCHANTED_BOOK, "Max HP",
                if (info.maxHp > 0) Utils.formatCompact(info.maxHp) else null,
                "Only glows mobs with this exact max health",
                "Leave empty for any health",
                "Click to set max HP filter"
            )
        )

        // Add revert/cancel button
        if (!isNewRule) {
            val revert = ItemStack(Items.REDSTONE)
            Utils.setCustomName(revert, colorStyle(Formatting.GOLD), "Revert Changes")
            val revertLore: MutableList<Text?> = ArrayList<Text?>()
            revertLore.add(Text.literal("Restore to original").setStyle(colorStyle(Formatting.GRAY)))
            revertLore.add(Text.literal("").setStyle(colorStyle(Formatting.GRAY)))
            revertLore.add(Text.literal("Click to revert").setStyle(colorStyle(Formatting.YELLOW)))
            revert.set<LoreComponent?>(DataComponentTypes.LORE, LoreComponent(revertLore, revertLore))
            revert.remove<AttributeModifiersComponent?>(DataComponentTypes.ATTRIBUTE_MODIFIERS)
            inventory.setStack(INV_SIZE - 9 + 3, revert)
        } else {
            val cancel = ItemStack(Items.BARRIER)
            Utils.setCustomName(cancel, colorStyle(Formatting.RED), "Cancel")
            val cancelLore: MutableList<Text?> = ArrayList<Text?>()
            cancelLore.add(Text.literal("Discard changes").setStyle(colorStyle(Formatting.GRAY)))
            cancelLore.add(Text.literal("").setStyle(colorStyle(Formatting.GRAY)))
            cancelLore.add(Text.literal("Click to cancel").setStyle(colorStyle(Formatting.YELLOW)))
            cancel.set<LoreComponent?>(DataComponentTypes.LORE, LoreComponent(cancelLore, cancelLore))
            cancel.remove<AttributeModifiersComponent?>(DataComponentTypes.ATTRIBUTE_MODIFIERS)
            inventory.setStack(INV_SIZE - 9 + 3, cancel)
        }

        val delete = ItemStack(Items.CAULDRON)
        Utils.setCustomName(delete, colorStyle(Formatting.RED), "Delete")
        inventory.setStack(INV_SIZE - 9 + 5, delete)
    }

    private fun createGearChoiceItem(): ItemStack {
        val text: String
        val color: Int

        val gear: MutableSet<GearFlag> = info.gear

        // NONE
        if (gear.isEmpty()) {
            text = "(Unset)"
            color = Formatting.RED.colorValue!!
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
            Formatting.RED.colorValue
        else
            Formatting.GREEN.colorValue)!!

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
        return createChoiceItem(item, label, chosen, Formatting.YELLOW.colorValue!!, *descriptions)
    }

    private fun createChoiceItem(
        item: Item,
        label: String,
        chosen: String?,
        chosenColor: Int,
        vararg descriptions: String
    ): ItemStack {
        val stack = ItemStack(item)
        Utils.setCustomName(stack, colorStyle(Formatting.GREEN)!!.withItalic(false), label)

        val lore: MutableList<Text> = ArrayList()

        if (chosen.isNullOrEmpty()) {
            lore.add(
                Text.literal("Chosen: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal("(Unset)").setStyle(colorStyle(Formatting.DARK_GRAY)))
            )
        } else {
            val lines: Array<String> =
                chosen.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            lore.add(
                Text.literal("Chosen: ").setStyle(colorStyle(Formatting.GRAY))
                    .append(Text.literal(lines[0]).setStyle(colorStyle(chosenColor)))
            )
            for (i in 1..<lines.size) {
                lore.add(Text.literal(lines[i]).setStyle(colorStyle(chosenColor)))
            }
        }

        lore.add(Text.literal(""))

        // Add descriptions with proper formatting
        if (descriptions.isNotEmpty()) {
            for (i in descriptions.indices) {
                val desc: String = descriptions[i]
                if (i == descriptions.size - 1) {
                    // Last line: action text in YELLOW
                    lore.add(Text.literal(desc).setStyle(colorStyle(Formatting.YELLOW)))
                } else {
                    // Middle lines: help text in GRAY
                    lore.add(Text.literal(desc).setStyle(colorStyle(Formatting.GRAY)))
                }
            }
        }

        stack.set(DataComponentTypes.LORE, LoreComponent(lore, lore))
        stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS)
        return stack
    }

    private fun colorStyle(color: Formatting): Style? {
        val colorValue = color.colorValue ?: return Style.EMPTY
        return colorStyle(colorValue)
    }

    private fun colorStyle(colorHex: Int): Style? {
        return Style.EMPTY.withColor(TextColor.fromRgb(colorHex))
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack == null || stack.isEmpty) return

        val itemName = Utils.getPlainCustomName(stack)
        when (itemName) {
            "Entity" -> Utils.setScreen(EntityTypesMenu(this, info))
            "Area" -> Utils.setScreen(AreaSelectionMenu(this, info))
            "Gear" -> Utils.setScreen(ArmorSelectionMenu(this, info))
            "Color" -> Utils.setScreen(ColorSelectionMenu(this, rule.color()))
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
                Utils.setScreen(this)
            }

            "Max HP" -> SignGui.open(
                arrayOf(
                    "Enter Max Hp",
                    if (info.maxHp > 0) Utils.formatCompact(info.maxHp) else ""
                ), Consumer open@{ lines: Array<String> ->
                    if (lines.size < 2) return@open
                    val input = lines[1].trim { it <= ' ' }

                    // If empty, clear the filter
                    if (input.isEmpty()) {
                        info.maxHp = 0
                    } else {
                        try {
                            info.maxHp = Utils.parseCompact(input)
                        } catch (e: NumberFormatException) {
                            // Invalid input - don't update
                            Utils.setScreen(this)
                            return@open
                        }
                    }
                    rebuild()
                    Utils.setScreen(this)
                })

            "Revert Changes", "Cancel" -> {
                this.isRevertRequested = true
                close()
            }

            "Delete" -> {
                info.clear()
                close()
            }
        }
    }
}

package com.somefrills.config.misc

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class MiscCategory {
    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Glow Player", desc = "Make players glow through walls")
    var glowPlayer: GlowPlayerConfig = GlowPlayerConfig()

    class GlowPlayerConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Make players glow through walls")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Mob Glow", desc = "Highlight entities based on name, type, or both")
    var glowMob: GlowMobConfig = GlowMobConfig()

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "NPC Locator", desc = "Locate and track NPCs")
    var npcLocator: NpcLocatorConfig = NpcLocatorConfig()

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Command Aliases", desc = "Add aliases for commonly used commands")
    var commandAliases: CommandAliasesConfig = CommandAliasesConfig()

    class CommandAliasesConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Add aliases for commonly used commands")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Chat Filter", desc = "Filter out unwanted chat messages")
    var chatFilter: ChatFilterConfig = ChatFilterConfig()

    class ChatFilterConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Filter out unwanted chat messages")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Save Cursor Position", desc = "Save and restore cursor position in chests")
    var saveCursorPosition: SaveCursorPositionConfig = SaveCursorPositionConfig()

    class SaveCursorPositionConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Save and restore cursor position in chests")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(true)

        @JvmField
        @Expose
        @ConfigOption(name = "Only ChestUI", desc = "Only save cursor position when in ChestUI")
        @ConfigEditorBoolean
        var onlyChestUI: Boolean = true
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Glow Block", desc = "Highlight blocks based on name, type, or both")
    var glowBlock: GlowBlockConfig = GlowBlockConfig()

    class GlowBlockConfig {
        @JvmField
        @Expose
        @ConfigOption(name = "Enabled", desc = "Highlight blocks based on name, type, or both")
        @ConfigEditorBoolean
        var enabled: Property<Boolean> = Property.of(false)
    }

    @JvmField
    @Expose
    @Accordion
    @ConfigOption(name = "Freecam", desc = "Enable freecam mode to move your camera independently of your player")
    var freecam: FreecamConfig = FreecamConfig()
}
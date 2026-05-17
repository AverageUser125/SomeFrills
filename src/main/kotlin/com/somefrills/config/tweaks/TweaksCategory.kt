package com.somefrills.config.tweaks

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class TweaksCategory {
    @JvmField
    @Expose
    @ConfigOption(name = "Middle Click Override", desc = "Replace left click with middle click where possible")
    @ConfigEditorBoolean
    var middleClickOverrideEnabled: Property<Boolean> = Property.of(true)

    @JvmField
    @Expose
    @ConfigOption(
        name = "Item Count Fix",
        desc = "Fix the issue where item counts in the inventory are not updated correctly"
    )
    @ConfigEditorBoolean
    var itemCountFix: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "No Pearl Cooldown", desc = "Remove the cooldown after using an ender pearl")
    @ConfigEditorBoolean
    var noPearlCooldown: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(
        name = "Break Reset Fix",
        desc = "Fix the issue where breaking a block and then switching to another item resets your break progress"
    )
    @ConfigEditorBoolean
    var breakResetFixEnabled: Property<Boolean> = Property.of(true)

    @JvmField
    @Expose
    @ConfigOption(
        name = "Double Use Fix",
        desc = "Fix the issue where using an item (like a bucket) and then switching to another item causes the use action to trigger again"
    )
    @ConfigEditorBoolean
    var doubleUseFixEnabled: Property<Boolean> = Property.of(true)

    @JvmField
    @Expose
    @ConfigOption(
        name = "No Ability Place",
        desc = "Prevent placing blocks with abilities like the grappling hook or the teleportation wand"
    )
    @ConfigEditorBoolean
    var noAbilityPlaceEnabled: Property<Boolean> = Property.of(true)

    @JvmField
    @Expose
    @ConfigOption(
        name = "Middle Click Fix",
        desc = "Fix the issue where middle clicking to swap items doesn't work in certain situations"
    )
    @ConfigEditorBoolean
    var middleClickFixEnabled: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "No Ghost Blocks", desc = "Prevent ghost blocks from being placed or broken")
    @Accordion
    var noGhostBlocks: NoGhostBlocksConfig = NoGhostBlocksConfig()

    class NoGhostBlocksConfig {
        @JvmField
        @ConfigEditorBoolean
        @Expose
        @ConfigOption(name = "Enabled", desc = "Prevent ghost blocks from being placed or broken")
        var enabled: Property<Boolean> = Property.of(false)

        @JvmField
        @Expose
        @ConfigOption(name = "Prevent Placing", desc = "Prevent placing ghost blocks")
        @ConfigEditorBoolean
        var placing: Boolean = true

        @JvmField
        @Expose
        @ConfigOption(name = "Prevent Breaking", desc = "Prevent breaking ghost blocks")
        @ConfigEditorBoolean
        var breaking: Boolean = true
    }

    @JvmField
    @Expose
    @ConfigOption(name = "Camera Tweaks", desc = "Tweaks to the camera behavior")
    @Accordion
    var cameraTweaks: CameraTweaksConfig = CameraTweaksConfig()

    class CameraTweaksConfig {
        @JvmField
        @ConfigEditorBoolean
        @Expose
        @ConfigOption(name = "Clip", desc = "Prevent the camera from clipping into blocks")
        var clip: Boolean = false
    }
}
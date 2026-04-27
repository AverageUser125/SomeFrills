package com.somefrills.config.tweaks;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class TweaksCategory {
    @Expose
    @ConfigOption(name = "Middle Click Override", desc = "Replace left click with middle click where possible")
    @ConfigEditorBoolean
    public Property<Boolean> middleClickOverrideEnabled = Property.of(true);

    @Expose
    @ConfigOption(name = "Item Count Fix", desc = "Fix the issue where item counts in the inventory are not updated correctly")
    @ConfigEditorBoolean
    public boolean itemCountFix = true;

    @Expose
    @ConfigOption(name = "No Pearl Cooldown", desc = "Remove the cooldown after using an ender pearl")
    @ConfigEditorBoolean
    public boolean noPearlCooldown = true;


    @Expose
    @ConfigOption(name = "Break Reset Fix", desc = "Fix the issue where breaking a block and then switching to another item resets your break progress")
    @ConfigEditorBoolean
    public Property<Boolean> breakResetFixEnabled = Property.of(true);

    @Expose
    @ConfigOption(name = "Double Use Fix", desc = "Fix the issue where using an item (like a bucket) and then switching to another item causes the use action to trigger again")
    @ConfigEditorBoolean
    public Property<Boolean> doubleUseFixEnabled = Property.of(true);

    @Expose
    @ConfigOption(name = "No Ability Place", desc = "Prevent placing blocks with abilities like the grappling hook or the teleportation wand")
    @ConfigEditorBoolean
    public Property<Boolean> noAbilityPlaceEnabled = Property.of(true);

    @Expose
    @ConfigOption(name = "Middle Click Fix", desc = "Fix the issue where middle clicking to swap items doesn't work in certain situations")
    @ConfigEditorBoolean
    public boolean middleClickFixEnabled = true;

    @Expose
    @ConfigOption(name = "No Ghost Blocks", desc = "Prevent ghost blocks from being placed or broken")
    @Accordion
    public NoGhostBlocksConfig noGhostBlocks = new NoGhostBlocksConfig();

    public static class NoGhostBlocksConfig {
        @ConfigEditorBoolean
        @Expose
        @ConfigOption(name = "Enabled", desc = "Prevent ghost blocks from being placed or broken")
        public Property<Boolean> enabled = Property.of(false);

        @Expose
        @ConfigOption(name = "Prevent Placing", desc = "Prevent placing ghost blocks")
        @ConfigEditorBoolean
        public boolean placing = true;

        @Expose
        @ConfigOption(name = "Prevent Breaking", desc = "Prevent breaking ghost blocks")
        @ConfigEditorBoolean
        public boolean breaking = true;
    }

    @Expose
    @ConfigOption(name = "Camera Tweaks", desc = "Tweaks to the camera behavior")
    @Accordion
    public CameraTweaksConfig cameraTweaks = new CameraTweaksConfig();

    public static class CameraTweaksConfig {
        @ConfigEditorBoolean
        @Expose
        @ConfigOption(name = "Clip", desc = "Prevent the camera from clipping into blocks")
        public boolean clip = false;
    }

    @Expose
    @ConfigOption(name = "No Render", desc = "Prevent certain things from rendering (experimental, may cause issues)")
    @Accordion
    public NoRenderConfig noRender = new NoRenderConfig();

    public static class NoRenderConfig {
        @ConfigEditorBoolean
        @Expose
        @ConfigOption(name = "No Obfuscation", desc = "Prevent obfuscation from rendering in text")
        public boolean noObfuscation = false;
    }
}
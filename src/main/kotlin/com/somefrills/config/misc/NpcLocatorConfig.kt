package com.somefrills.config.misc

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.ChromaColour.Companion.fromStaticRGB
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property


class NpcLocatorConfig {
    @JvmField
    @Expose
    @ConfigOption(name = "Enabled", desc = "Locate and track NPCs")
    @ConfigEditorBoolean
    var enabled: Property<Boolean> = Property.of(false)

    @JvmField
    @Expose
    @ConfigOption(name = "Beacon Beam", desc = "Show beacon beam to NPCs")
    @ConfigEditorBoolean
    var beaconBeam: Boolean = false

    @JvmField
    @Expose
    @ConfigOption(name = "Tracer", desc = "Show tracer lines to NPCs")
    @ConfigEditorBoolean
    var tracer: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(name = "Outline Box", desc = "Show outline box around NPCs")
    @ConfigEditorBoolean
    var outlineBox: Boolean = false

    @JvmField
    @Expose
    @ConfigOption(name = "Color", desc = "Rendering color for NPC locator")
    @ConfigEditorColour
    var color: Property<ChromaColour> = Property.of(fromStaticRGB(255, 100, 100, 255))

    @JvmField
    @Expose
    @ConfigOption(name = "Auto Remove Waypoint", desc = "Automatically remove NPC waypoints when you get close to them")
    @ConfigEditorBoolean
    var autoRemoveWaypoint: Boolean = true

    @JvmField
    @Expose
    @ConfigOption(
        name = "Waypoint Remove Distance",
        desc = "Distance threshold for automatically removing NPC waypoints"
    )
    @ConfigEditorSlider(minValue = 1.0f, maxValue = 20.0f, minStep = 0.5f)
    var waypointRemoveDistance: Float = 6.0f
}


package com.somefrills.config.misc;


import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class NpcLocatorConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Locate and track NPCs")
    @ConfigEditorBoolean
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Beacon Beam", desc = "Show beacon beam to NPCs")
    @ConfigEditorBoolean
    public boolean beaconBeam = false;

    @Expose
    @ConfigOption(name = "Tracer", desc = "Show tracer lines to NPCs")
    @ConfigEditorBoolean
    public boolean tracer = true;

    @Expose
    @ConfigOption(name = "Outline Box", desc = "Show outline box around NPCs")
    @ConfigEditorBoolean
    public boolean outlineBox = false;

    @Expose
    @ConfigOption(name = "Color", desc = "Rendering color for NPC locator")
    @ConfigEditorColour
    public Property<ChromaColour> color = Property.of(ChromaColour.fromStaticRGB(255, 100, 100, 255));

    @Expose
    @ConfigOption(name = "Auto Remove Waypoint", desc = "Automatically remove NPC waypoints when you get close to them")
    @ConfigEditorBoolean
    public boolean autoRemoveWaypoint;

    @Expose
    @ConfigOption(name = "Waypoint Remove Distance", desc = "Distance threshold for automatically removing NPC waypoints")
    @ConfigEditorSlider(minValue = 1.0f, maxValue = 20.0f, minStep = 0.5f)
    public float waypointRemoveDistance = 6.0f;
}


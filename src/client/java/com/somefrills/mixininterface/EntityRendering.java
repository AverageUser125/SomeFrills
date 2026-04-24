package com.somefrills.mixininterface;

import com.somefrills.misc.RenderColor;

public interface EntityRendering {
    void somefrills$setGlowingColored(boolean glowing, RenderColor color);

    boolean somefrills$getGlowing();
}
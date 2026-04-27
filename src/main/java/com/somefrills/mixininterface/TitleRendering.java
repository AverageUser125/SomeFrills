package com.somefrills.mixininterface;

import com.somefrills.misc.RenderColor;

public interface TitleRendering {
    void somefrills$setRenderTitle(String title, int stayTicks, int yOffset, float scale, RenderColor color);

    boolean somefrills$isRenderingTitle();
}
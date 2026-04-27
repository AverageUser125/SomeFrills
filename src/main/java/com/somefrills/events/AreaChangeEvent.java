package com.somefrills.events;

import com.somefrills.misc.Area;

public class AreaChangeEvent {
    public Area area;

    public AreaChangeEvent(Area area) {
        this.area = area;
    }
}

package com.somefrills.events;

import java.util.List;

public class TabListUpdateEvent {
    public List<String> lines;

    public TabListUpdateEvent(List<String> lines) {
        this.lines = lines;
    }
}

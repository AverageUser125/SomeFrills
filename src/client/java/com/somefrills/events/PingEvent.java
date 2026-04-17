package com.somefrills.events;

public class PingEvent {
    public final long delta;
    public PingEvent(long delta) {
        this.delta = delta;
    }
}

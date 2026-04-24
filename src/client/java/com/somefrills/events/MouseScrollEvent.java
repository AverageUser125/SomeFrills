package com.somefrills.events;

public class MouseScrollEvent extends Cancellable {
    public double value;

    public MouseScrollEvent(double value) {
        this.value = value;
    }
}

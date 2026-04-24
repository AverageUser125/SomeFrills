package com.somefrills.events;

import com.somefrills.misc.KeyAction;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;

public class MouseClickEvent extends Cancellable {
    public Click click;
    public MouseInput input;
    public KeyAction action;

    public MouseClickEvent(Click click, KeyAction action) {
        this.click = click;
        this.input = click.buttonInfo();
        this.action = action;
    }

    public int button() {
        return this.input.button();
    }
}

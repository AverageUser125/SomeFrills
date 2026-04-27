package com.somefrills.events;

import com.somefrills.misc.KeyAction;
import net.minecraft.client.input.KeyInput;

public class InputEvent extends Cancellable {
    public int key, modifiers;
    public KeyAction action;
    public KeyInput keyInput;

    public InputEvent(KeyInput input, KeyAction action) {
        this.setCancelled(false);
        this.key = input.key();
        this.modifiers = input.modifiers();
        this.action = action;
        this.keyInput = input;
    }
}

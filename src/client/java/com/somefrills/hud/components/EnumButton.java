package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.SettingEnum;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A simple button that cycles through the values of an enum type.
 * Displays the current enum constant name and calls an optional
 * callback when the value changes.
 */
public class EnumButton<T extends Enum<T>> extends ButtonWidget {
	private final T[] values;
	private int index = 0;

	public EnumButton(int x, int y, int width, int height, SettingEnum<T> initial) {
		// initialize with the current enum value from the setting
		super(x, y, width, height, Component.literal(initial.value().name()), button -> {
			@SuppressWarnings("unchecked")
			var btn = (EnumButton<T>) button;
			btn.cycleNext();
		}, Button.DEFAULT_NARRATION);

		this.values = initial.values;
		this.index = findIndex(initial.value());
		updateMessage();
	}

	private int findIndex(T v) {
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(v)) return i;
		}
		return 0;
	}

	public T getValue() {
		return values[index];
	}

	public void setValue(T v) {
		int ni = findIndex(v);
		if (ni != this.index) {
			this.index = ni;
			updateMessage();
		}
	}

	private void cycleNext() {
		if (values.length == 0) return;
		this.index = (this.index + 1) % values.length;
		updateMessage();
	}

	private void updateMessage() {
		this.setMessage(Component.literal(values[index].name()));
	}
}

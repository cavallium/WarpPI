package it.cavallium.warppi.device.input;

import java.util.Objects;

public class InputManager {
	private final KeyboardInputDevice keyboard;
	private final TouchInputDevice touchDevice;

	public InputManager(KeyboardInputDevice keyboard, TouchInputDevice touchscreen) {
		this.keyboard = Objects.requireNonNull(keyboard);
		this.touchDevice = Objects.requireNonNull(touchscreen);
	}

	public KeyboardInputDevice getKeyboard() {
		return keyboard;
	}

	public TouchInputDevice getTouchDevice() {
		return touchDevice;
	}

	public void initialize() {
		this.keyboard.initialize();
		this.touchDevice.initialize();
	}

}

package it.cavallium.warppi.device.input;

public class InputManager {
	private final KeyboardInputDevice keyboard;
	private final TouchInputDevice touchDevice;

	public InputManager(KeyboardInputDevice keyboard, TouchInputDevice touchscreen) {
		this.keyboard = keyboard;
		this.touchDevice = touchscreen;
	}
	
	// TODO: manage the keyboard and the touchscreen here!

	public KeyboardInputDevice getKeyboard() {
		return keyboard;
	}

	public TouchInputDevice getTouchDevice() {
		return touchDevice;
	}

}

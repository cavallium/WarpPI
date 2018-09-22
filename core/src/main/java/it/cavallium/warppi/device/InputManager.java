package it.cavallium.warppi.device;

public class InputManager {
	private final Keyboard keyboard;
	private final HardwareTouchDevice touchDevice;

	public InputManager(final Keyboard k, final HardwareTouchDevice t) {
		keyboard = k;
		touchDevice = t;
	}

	public Keyboard getKeyboard() {
		return keyboard;
	}

	public HardwareTouchDevice getTouchDevice() {
		return touchDevice;
	}

}

package it.cavallium.warppi.device;

public class InputManager {
	private final Keyboard keyboard;
	private final HardwareTouchDevice touchDevice;

	public InputManager(Keyboard k, HardwareTouchDevice t) {
		this.keyboard = k;
		this.touchDevice = t;
	}

	public Keyboard getKeyboard() {
		return keyboard;
	}

	public HardwareTouchDevice getTouchDevice() {
		return touchDevice;
	}

}

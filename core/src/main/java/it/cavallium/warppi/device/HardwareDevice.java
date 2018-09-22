package it.cavallium.warppi.device;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.gui.DisplayManager;

public class HardwareDevice {
	private final DisplayManager displayManager;
	private final InputManager inputManager;

	public HardwareDevice(final DisplayManager m, final InputManager im) {
		displayManager = m;
		inputManager = im;
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}

	public InputManager getInputManager() {
		return inputManager;
	}

	public void setup(final Runnable r) {
		displayManager.initialize();
		inputManager.getKeyboard().startKeyboard();
		final Thread t = new Thread(r);
		Engine.getPlatform().setThreadDaemon(t, false);
		Engine.getPlatform().setThreadName(t, "Main thread (after setup)");
		t.start();
	}

}

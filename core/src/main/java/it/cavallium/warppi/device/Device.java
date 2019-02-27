package it.cavallium.warppi.device;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.InputManager;
import it.cavallium.warppi.gui.DisplayManager;

public class Device {
	private final DisplayManager displayManager;
	private final InputManager inputManager;

	public Device(final DisplayManager m, final InputManager im) {
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
		final Thread t = new Thread(r);
		WarpPI.getPlatform().setThreadDaemon(t, false);
		WarpPI.getPlatform().setThreadName(t, "Main thread (after setup)");
		t.start();
	}

}

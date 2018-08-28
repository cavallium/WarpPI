package org.warp.picalculator.device;

import org.warp.picalculator.PlatformUtils;
import org.warp.picalculator.gui.DisplayManager;

public class HardwareDevice {
	public static HardwareDevice INSTANCE;
	private final DisplayManager displayManager;
	private final InputManager inputManager;
	
	public HardwareDevice(DisplayManager m, InputManager im) {
		INSTANCE = this;
		displayManager = m;
		inputManager = im;
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}
	
	public InputManager getInputManager() {
		return inputManager;
	}

	public void setup(Runnable r) {
		displayManager.initialize();
	    inputManager.getKeyboard().startKeyboard();
		Thread t = new Thread(r);
		PlatformUtils.setDaemon(t, false);
		PlatformUtils.setThreadName(t, "Main thread (after setup)");
		t.start();
	}
	
}

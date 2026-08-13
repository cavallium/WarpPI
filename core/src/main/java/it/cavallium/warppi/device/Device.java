package it.cavallium.warppi.device;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.input.InputManager;
import it.cavallium.warppi.gui.DisplayManager;

public class Device {
	private final DisplayManager displayManager;
	private final InputManager inputManager;
	private final DeviceStateDevice deviceState;

	public Device(final DisplayManager m, final InputManager im, final DeviceStateDevice dm) {
		displayManager = m;
		inputManager = im;
		deviceState = dm;
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}

	public InputManager getInputManager() {
		return inputManager;
	}
	
	public DeviceStateDevice getDeviceStateDevice() {
		return deviceState;
	}

	public void setup() throws InterruptedException {
		displayManager.initialize();
		inputManager.initialize();
		deviceState.initialize();
		
		inputManager.getTouchDevice().listenTouchEvents(displayManager.getTouchEventListener());
		WarpPI.INSTANCE.reportLoadingProgress(WarpPI.STARTUP_DEVICE_READY);
	}

}

package it.cavallium.warppi.device;

import java.util.concurrent.Future;

public interface DeviceStateDevice {

	void initialize();

	void waitForExit();
	
	void powerOff();
}

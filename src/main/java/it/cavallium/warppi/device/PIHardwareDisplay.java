package it.cavallium.warppi.device;

import it.cavallium.warppi.ConsoleUtils;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.deps.DGpio;
import it.cavallium.warppi.gui.HardwareDisplay;

public class PIHardwareDisplay implements HardwareDisplay {

	@Override
	public void initialize() {}

	@Override
	public void shutdown() {}

	@Override
	public void setBrightness(double value) {
		if (StaticVars.debugOn == false) {
			DGpio.pwmWrite(12, (int) Math.ceil(value * 1024f));
//			SoftPwm.softPwmWrite(12, (int)(Math.ceil(brightness*10)));
		} else {
			ConsoleUtils.out.println(1, "Brightness: " + value);
		}
	}

}

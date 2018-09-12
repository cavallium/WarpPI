package it.cavallium.warppi.device;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.gui.HardwareDisplay;

public class PIHardwareDisplay implements HardwareDisplay {

	@Override
	public void initialize() {}

	@Override
	public void shutdown() {}

	@Override
	public void setBrightness(double value) {
		if (Engine.getPlatform().getSettings().isDebugEnabled() == false) {
			Engine.getPlatform().getGpio().pwmWrite(12, (int) Math.ceil(value * 1024f));
//			SoftPwm.softPwmWrite(12, (int)(Math.ceil(brightness*10)));
		} else {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Brightness: " + value);
		}
	}

}

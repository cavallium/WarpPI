package it.cavallium.warppi;

import java.io.IOException;

import it.cavallium.warppi.boot.WarpPI;
import it.cavallium.warppi.device.PIHardwareDisplay;
import it.cavallium.warppi.device.PIHardwareTouchDevice;
import it.cavallium.warppi.gui.CalculatorHUD;
import it.cavallium.warppi.gui.screens.KeyboardDebugScreen;

public class KeyboardTest {

	public static void main(String[] args) throws InterruptedException, Error, IOException {
		Engine.start(new KeyboardDebugScreen(), new PIHardwareDisplay(), new PIHardwareTouchDevice(false, false, false), new CalculatorHUD(), WarpPI.parseStartupArguments(args));
	}
}

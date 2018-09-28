package it.cavallium.warppi.device.chip;

import it.cavallium.warppi.Engine;

public class SerialToParallel {
	private final int RCK; //Storage register clock pin (latch pin)
	private final int SCK; //Shift register clock pin
	private final int SER; //Serial data input

	public SerialToParallel(final int RCK_pin, final int SCK_pin, final int SER_pin) {
		RCK = RCK_pin;
		SCK = SCK_pin;
		SER = SER_pin;
	}

	public void write(final boolean[] data) {
		if (data.length != 8) {
			return;
		} else {
			Engine.getPlatform().getGpio().digitalWrite(RCK, Engine.getPlatform().getGpio().valueLow());

			for (int i = 7; i >= 0; i--) {
				Engine.getPlatform().getGpio().digitalWrite(SCK, Engine.getPlatform().getGpio().valueLow());
				Engine.getPlatform().getGpio().digitalWrite(SER, data[i]);
				Engine.getPlatform().getGpio().digitalWrite(SCK, Engine.getPlatform().getGpio().valueHigh());
			}

			Engine.getPlatform().getGpio().digitalWrite(RCK, Engine.getPlatform().getGpio().valueHigh());
		}
	}
}

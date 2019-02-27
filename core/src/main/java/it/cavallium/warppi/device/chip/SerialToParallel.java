package it.cavallium.warppi.device.chip;

import it.cavallium.warppi.WarpPI;

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
			WarpPI.getPlatform().getGpio().digitalWrite(RCK, WarpPI.getPlatform().getGpio().valueLow());

			for (int i = 7; i >= 0; i--) {
				WarpPI.getPlatform().getGpio().digitalWrite(SCK, WarpPI.getPlatform().getGpio().valueLow());
				WarpPI.getPlatform().getGpio().digitalWrite(SER, data[i]);
				WarpPI.getPlatform().getGpio().digitalWrite(SCK, WarpPI.getPlatform().getGpio().valueHigh());
			}

			WarpPI.getPlatform().getGpio().digitalWrite(RCK, WarpPI.getPlatform().getGpio().valueHigh());
		}
	}
}

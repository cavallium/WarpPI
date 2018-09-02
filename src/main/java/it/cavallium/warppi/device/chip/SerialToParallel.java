package it.cavallium.warppi.device.chip;

import it.cavallium.warppi.deps.DGpio;

public class SerialToParallel {
	private final int RCK; //Storage register clock pin (latch pin)
	private final int SCK; //Shift register clock pin
	private final int SER; //Serial data input

	public SerialToParallel(int RCK_pin, int SCK_pin, int SER_pin) {
		RCK = RCK_pin;
		SCK = SCK_pin;
		SER = SER_pin;
	}

	public void write(boolean[] data) {
		if (data.length != 8) {
			return;
		} else {
			DGpio.digitalWrite(RCK, DGpio.LOW);

			for (int i = 7; i >= 0; i--) {
				DGpio.digitalWrite(SCK, DGpio.LOW);
				DGpio.digitalWrite(SER, data[i]);
				DGpio.digitalWrite(SCK, DGpio.HIGH);
			}

			DGpio.digitalWrite(RCK, DGpio.HIGH);
		}
	}
}

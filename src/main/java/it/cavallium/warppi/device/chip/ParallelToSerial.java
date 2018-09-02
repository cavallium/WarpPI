package it.cavallium.warppi.device.chip;

import it.cavallium.warppi.deps.DGpio;

public class ParallelToSerial {

	private final int SH_LD;
	private final int CLK_INH;
	private final int QH;
	private final int CLK;

	public ParallelToSerial(int SH_LD_pin, int CLK_INH_pin, int QH_pin, int CLK_pin) {
		SH_LD = SH_LD_pin;
		CLK_INH = CLK_INH_pin;
		QH = QH_pin;
		CLK = CLK_pin;
	}

	public boolean[] read() {
		final boolean[] data = new boolean[8];
		DGpio.digitalWrite(CLK_INH, DGpio.HIGH);
		DGpio.digitalWrite(SH_LD, DGpio.LOW);
		DGpio.delayMicroseconds(1);
		DGpio.digitalWrite(SH_LD, DGpio.HIGH);
		DGpio.digitalWrite(CLK_INH, DGpio.LOW);

		for (int i = 7; i >= 0; i--) {
			DGpio.digitalWrite(CLK, DGpio.HIGH);
			DGpio.digitalWrite(CLK, DGpio.LOW);
			data[i] = DGpio.digitalRead(QH) == DGpio.HIGH ? true : false;
		}

		return data;
	}
}

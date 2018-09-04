package it.cavallium.warppi.device.chip;

import it.cavallium.warppi.Engine;

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
		Engine.getPlatform().getGpio().digitalWrite(CLK_INH, Engine.getPlatform().getGpio().valueHigh());
		Engine.getPlatform().getGpio().digitalWrite(SH_LD, Engine.getPlatform().getGpio().valueLow());
		Engine.getPlatform().getGpio().delayMicroseconds(1);
		Engine.getPlatform().getGpio().digitalWrite(SH_LD, Engine.getPlatform().getGpio().valueHigh());
		Engine.getPlatform().getGpio().digitalWrite(CLK_INH, Engine.getPlatform().getGpio().valueLow());

		for (int i = 7; i >= 0; i--) {
			Engine.getPlatform().getGpio().digitalWrite(CLK, Engine.getPlatform().getGpio().valueHigh());
			Engine.getPlatform().getGpio().digitalWrite(CLK, Engine.getPlatform().getGpio().valueLow());
			data[i] = Engine.getPlatform().getGpio().digitalRead(QH) == Engine.getPlatform().getGpio().valueHigh() ? true : false;
		}

		return data;
	}
}

package it.cavallium.warppi.device.chip;

import it.cavallium.warppi.WarpPI;

public class ParallelToSerial {

	private final int SH_LD;
	private final int CLK_INH;
	private final int QH;
	private final int CLK;

	public ParallelToSerial(final int SH_LD_pin, final int CLK_INH_pin, final int QH_pin, final int CLK_pin) {
		SH_LD = SH_LD_pin;
		CLK_INH = CLK_INH_pin;
		QH = QH_pin;
		CLK = CLK_pin;
	}

	public boolean[] read() {
		final boolean[] data = new boolean[8];
		WarpPI.getPlatform().getGpio().digitalWrite(CLK_INH, WarpPI.getPlatform().getGpio().valueHigh());
		WarpPI.getPlatform().getGpio().digitalWrite(SH_LD, WarpPI.getPlatform().getGpio().valueLow());
		WarpPI.getPlatform().getGpio().delayMicroseconds(1);
		WarpPI.getPlatform().getGpio().digitalWrite(SH_LD, WarpPI.getPlatform().getGpio().valueHigh());
		WarpPI.getPlatform().getGpio().digitalWrite(CLK_INH, WarpPI.getPlatform().getGpio().valueLow());

		for (int i = 7; i >= 0; i--) {
			WarpPI.getPlatform().getGpio().digitalWrite(CLK, WarpPI.getPlatform().getGpio().valueHigh());
			WarpPI.getPlatform().getGpio().digitalWrite(CLK, WarpPI.getPlatform().getGpio().valueLow());
			data[i] = WarpPI.getPlatform().getGpio().digitalRead(QH) == WarpPI.getPlatform().getGpio().valueHigh() ? true : false;
		}

		return data;
	}
}

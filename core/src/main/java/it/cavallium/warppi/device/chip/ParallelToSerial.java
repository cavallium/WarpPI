package it.cavallium.warppi.device.chip;

import java.util.Objects;
import java.util.function.IntConsumer;

import it.cavallium.warppi.Platform.Gpio.DigitalInput;
import it.cavallium.warppi.Platform.Gpio.DigitalOutput;

public final class ParallelToSerial {

	private final DigitalOutput shiftLoad;
	private final DigitalOutput clockInhibit;
	private final DigitalInput serialData;
	private final DigitalOutput clock;
	private final IntConsumer delayMicroseconds;

	public ParallelToSerial(final DigitalOutput shiftLoad, final DigitalOutput clockInhibit,
			final DigitalInput serialData, final DigitalOutput clock, final IntConsumer delayMicroseconds) {
		this.shiftLoad = Objects.requireNonNull(shiftLoad);
		this.clockInhibit = Objects.requireNonNull(clockInhibit);
		this.serialData = Objects.requireNonNull(serialData);
		this.clock = Objects.requireNonNull(clock);
		this.delayMicroseconds = Objects.requireNonNull(delayMicroseconds);
	}

	/**
	 * Loads all parallel inputs, then samples QH before each rising clock edge.
	 * Bit seven is input H and bit zero is input A.
	 */
	public int readByte() {
		clockInhibit.set(true);
		clock.set(false);
		shiftLoad.set(false);
		delayMicroseconds.accept(1);
		shiftLoad.set(true);
		clockInhibit.set(false);

		int data = 0;
		try {
			for (int bit = 7; bit >= 0; bit--) {
				if (serialData.isHigh())
					data |= 1 << bit;
				if (bit > 0) {
					clock.set(true);
					clock.set(false);
				}
			}
			return data;
		} finally {
			clockInhibit.set(true);
			clock.set(false);
		}
	}
}

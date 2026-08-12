package it.cavallium.warppi.device.chip;

import java.util.Objects;

import it.cavallium.warppi.Platform.Gpio.DigitalOutput;

public final class SerialToParallel {
	private final DigitalOutput latchClock;
	private final DigitalOutput shiftClock;
	private final DigitalOutput serialData;

	public SerialToParallel(final DigitalOutput latchClock, final DigitalOutput shiftClock,
			final DigitalOutput serialData) {
		this.latchClock = Objects.requireNonNull(latchClock);
		this.shiftClock = Objects.requireNonNull(shiftClock);
		this.serialData = Objects.requireNonNull(serialData);
	}

	/**
	 * Writes the low eight bits to the parallel outputs without exposing partially
	 * shifted values. Bit zero is output QA and bit seven is output QH.
	 */
	public void writeByte(final int data) {
		latchClock.set(false);
		for (int bit = 7; bit >= 0; bit--) {
			shiftClock.set(false);
			serialData.set((data & 1 << bit) != 0);
			shiftClock.set(true);
		}
		shiftClock.set(false);
		latchClock.set(true);
	}
}

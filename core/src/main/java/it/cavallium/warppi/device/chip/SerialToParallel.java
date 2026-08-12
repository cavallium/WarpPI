package it.cavallium.warppi.device.chip;

import java.util.Objects;

import it.cavallium.warppi.Platform.Gpio;
import it.cavallium.warppi.WarpPI;

public class SerialToParallel {
	private final Gpio gpio;
	private final int latchClock;
	private final int shiftClock;
	private final int serialData;
	private final int high;
	private final int low;

	public SerialToParallel(final int latchClockPin, final int shiftClockPin, final int serialDataPin) {
		this(WarpPI.getPlatform().getGpio(), latchClockPin, shiftClockPin, serialDataPin);
	}

	public SerialToParallel(final Gpio gpio, final int latchClockPin, final int shiftClockPin,
			final int serialDataPin) {
		this.gpio = Objects.requireNonNull(gpio);
		this.latchClock = latchClockPin;
		this.shiftClock = shiftClockPin;
		this.serialData = serialDataPin;
		this.high = gpio.valueHigh();
		this.low = gpio.valueLow();
	}

	/**
	 * Writes the low eight bits to the parallel outputs without exposing partially
	 * shifted values. Bit zero is output QA and bit seven is output QH.
	 */
	public void writeByte(final int data) {
		gpio.digitalWrite(latchClock, low);
		for (int bit = 7; bit >= 0; bit--) {
			gpio.digitalWrite(shiftClock, low);
			gpio.digitalWrite(serialData, (data & 1 << bit) != 0);
			gpio.digitalWrite(shiftClock, high);
		}
		gpio.digitalWrite(shiftClock, low);
		gpio.digitalWrite(latchClock, high);
	}

	/**
	 * @deprecated Prefer {@link #writeByte(int)} to avoid allocating a boolean
	 *             array for each transfer.
	 */
	@Deprecated
	public void write(final boolean[] data) {
		Objects.requireNonNull(data);
		if (data.length != Byte.SIZE)
			throw new IllegalArgumentException("Expected exactly eight output bits");

		int value = 0;
		for (int bit = 0; bit < Byte.SIZE; bit++)
			if (data[bit])
				value |= 1 << bit;
		writeByte(value);
	}
}

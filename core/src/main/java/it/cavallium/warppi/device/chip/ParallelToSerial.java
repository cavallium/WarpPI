package it.cavallium.warppi.device.chip;

import java.util.Objects;

import it.cavallium.warppi.Platform.Gpio;
import it.cavallium.warppi.WarpPI;

public class ParallelToSerial {

	private final Gpio gpio;
	private final int shiftLoad;
	private final int clockInhibit;
	private final int serialData;
	private final int clock;
	private final int high;
	private final int low;

	public ParallelToSerial(final int shiftLoadPin, final int clockInhibitPin, final int serialDataPin,
			final int clockPin) {
		this(WarpPI.getPlatform().getGpio(), shiftLoadPin, clockInhibitPin, serialDataPin, clockPin);
	}

	public ParallelToSerial(final Gpio gpio, final int shiftLoadPin, final int clockInhibitPin,
			final int serialDataPin, final int clockPin) {
		this.gpio = Objects.requireNonNull(gpio);
		this.shiftLoad = shiftLoadPin;
		this.clockInhibit = clockInhibitPin;
		this.serialData = serialDataPin;
		this.clock = clockPin;
		this.high = gpio.valueHigh();
		this.low = gpio.valueLow();
	}

	/**
	 * Loads all parallel inputs, then samples QH before each rising clock edge.
	 * Bit seven is input H and bit zero is input A.
	 */
	public int readByte() {
		gpio.digitalWrite(clockInhibit, high);
		gpio.digitalWrite(clock, low);
		gpio.digitalWrite(shiftLoad, low);
		gpio.delayMicroseconds(1);
		gpio.digitalWrite(shiftLoad, high);
		gpio.digitalWrite(clockInhibit, low);

		int data = 0;
		try {
			for (int bit = 7; bit >= 0; bit--) {
				if (gpio.digitalRead(serialData) == high)
					data |= 1 << bit;
				if (bit > 0) {
					gpio.digitalWrite(clock, high);
					gpio.digitalWrite(clock, low);
				}
			}
			return data;
		} finally {
			gpio.digitalWrite(clockInhibit, high);
			gpio.digitalWrite(clock, low);
		}
	}

	/**
	 * @deprecated Prefer {@link #readByte()} to avoid allocating a boolean array
	 *             for each transfer.
	 */
	@Deprecated
	public boolean[] read() {
		final int value = readByte();
		final boolean[] data = new boolean[Byte.SIZE];
		for (int bit = 0; bit < Byte.SIZE; bit++)
			data[bit] = (value & 1 << bit) != 0;
		return data;
	}
}

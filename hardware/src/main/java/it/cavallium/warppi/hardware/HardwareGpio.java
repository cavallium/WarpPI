package it.cavallium.warppi.hardware;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

import it.cavallium.warppi.Platform.Gpio;

/**
 * Direct BCM2835 GPIO-register backend. WarpPI must have exclusive ownership
 * of every line requested through this class.
 */
public final class HardwareGpio implements Gpio {
	static final int GPIO_LINE_COUNT = 54;
	static final int GPFSEL0 = 0x00;
	static final int GPSET0 = 0x1c;
	static final int GPCLR0 = 0x28;
	static final int GPLEV0 = 0x34;

	private static final int LINES_PER_FUNCTION_SELECT_REGISTER = 10;
	private static final int BITS_PER_FUNCTION = 3;
	private static final int GPIO_FUNCTION_INPUT = 0;
	private static final int GPIO_FUNCTION_OUTPUT = 1;
	private static final long SPIN_THRESHOLD_NANOS = 50_000L;
	private static final long MAX_OUTPUT_MASK = (1L << GPIO_LINE_COUNT) - 1;

	@FunctionalInterface
	interface RegisterFactory {
		GpioRegisters open() throws IOException;
	}

	private final RegisterFactory registerFactory;
	private final DigitalInput[] inputLines = new DigitalInput[GPIO_LINE_COUNT];
	private final DigitalOutput[] outputLines = new DigitalOutput[GPIO_LINE_COUNT];
	private volatile GpioRegisters registers;
	private long outputMask;

	public HardwareGpio() {
		this(LinuxMemoryMappedRegisters::openGpio);
	}

	HardwareGpio(final RegisterFactory registerFactory) {
		this.registerFactory = Objects.requireNonNull(registerFactory);
	}

	@Override
	public synchronized void initialize() throws IOException {
		if (registers == null)
			registers = registerFactory.open();
	}

	@Override
	public synchronized DigitalInput input(final int bcmPin) {
		validateBcmPin(bcmPin);
		final GpioRegisters currentRegisters = requireRegisters();
		if (outputLines[bcmPin] != null)
			throw new IllegalStateException("BCM GPIO " + bcmPin + " is already configured as output");
		if (inputLines[bcmPin] == null) {
			configureFunction(currentRegisters, bcmPin, GPIO_FUNCTION_INPUT);
			final int register = bankRegister(GPLEV0, bcmPin);
			final int mask = bankMask(bcmPin);
			inputLines[bcmPin] = () -> (currentRegisters.read(register) & mask) != 0;
		}
		return inputLines[bcmPin];
	}

	@Override
	public synchronized DigitalOutput output(final int bcmPin, final boolean initialHigh) {
		validateBcmPin(bcmPin);
		final GpioRegisters currentRegisters = requireRegisters();
		if (inputLines[bcmPin] != null)
			throw new IllegalStateException("BCM GPIO " + bcmPin + " is already configured as input");
		if (outputLines[bcmPin] == null) {
			final int setRegister = bankRegister(GPSET0, bcmPin);
			final int clearRegister = bankRegister(GPCLR0, bcmPin);
			final int mask = bankMask(bcmPin);
			writeLevel(currentRegisters, setRegister, clearRegister, mask, initialHigh);
			configureFunction(currentRegisters, bcmPin, GPIO_FUNCTION_OUTPUT);
			outputLines[bcmPin] = high ->
					writeLevel(currentRegisters, setRegister, clearRegister, mask, high);
			outputMask |= lineMask(bcmPin);
		} else {
			outputLines[bcmPin].set(initialHigh);
		}
		return outputLines[bcmPin];
	}

	@Override
	public void delayMicroseconds(final int microseconds) {
		if (microseconds < 0)
			throw new IllegalArgumentException("microseconds must not be negative");
		if (microseconds == 0)
			return;

		final long delayNanos = microseconds * 1_000L;
		final long start = System.nanoTime();
		if (delayNanos > SPIN_THRESHOLD_NANOS)
			LockSupport.parkNanos(delayNanos - SPIN_THRESHOLD_NANOS);
		while (System.nanoTime() - start < delayNanos)
			Thread.onSpinWait();
	}

	@Override
	public synchronized void close() {
		final GpioRegisters currentRegisters = registers;
		if (currentRegisters == null)
			return;
		try {
			clearConfiguredOutputs(currentRegisters);
			for (int bcmPin = 0; bcmPin < GPIO_LINE_COUNT; bcmPin++)
				if ((outputMask & lineMask(bcmPin)) != 0)
					configureFunction(currentRegisters, bcmPin, GPIO_FUNCTION_INPUT);
		} finally {
			registers = null;
			outputMask = 0;
			Arrays.fill(inputLines, null);
			Arrays.fill(outputLines, null);
			currentRegisters.close();
		}
	}

	private GpioRegisters requireRegisters() {
		final GpioRegisters result = registers;
		if (result == null)
			throw new IllegalStateException("GPIO has not been initialized");
		return result;
	}

	private void clearConfiguredOutputs(final GpioRegisters currentRegisters) {
		final long configuredOutputs = outputMask & MAX_OUTPUT_MASK;
		final int bank0Mask = (int) configuredOutputs;
		final int bank1Mask = (int) (configuredOutputs >>> Integer.SIZE);
		if (bank0Mask != 0)
			currentRegisters.write(GPCLR0, bank0Mask);
		if (bank1Mask != 0)
			currentRegisters.write(GPCLR0 + Integer.BYTES, bank1Mask);
	}

	private static void configureFunction(final GpioRegisters registers, final int bcmPin, final int function) {
		final int register = GPFSEL0
				+ bcmPin / LINES_PER_FUNCTION_SELECT_REGISTER * Integer.BYTES;
		final int shift = bcmPin % LINES_PER_FUNCTION_SELECT_REGISTER * BITS_PER_FUNCTION;
		final int functionMask = 0b111 << shift;
		final int currentValue = registers.read(register);
		registers.write(register, (currentValue & ~functionMask) | (function << shift));
	}

	private static void writeLevel(final GpioRegisters registers, final int setRegister,
			final int clearRegister, final int mask, final boolean high) {
		registers.write(high ? setRegister : clearRegister, mask);
	}

	private static int bankRegister(final int bank0Register, final int bcmPin) {
		return bank0Register + bcmPin / Integer.SIZE * Integer.BYTES;
	}

	private static int bankMask(final int bcmPin) {
		return 1 << (bcmPin % Integer.SIZE);
	}

	private static long lineMask(final int bcmPin) {
		return 1L << bcmPin;
	}

	private static void validateBcmPin(final int bcmPin) {
		if (bcmPin < 0 || bcmPin >= GPIO_LINE_COUNT)
			throw new IllegalArgumentException("BCM GPIO must be between 0 and " + (GPIO_LINE_COUNT - 1));
	}
}

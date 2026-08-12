package it.cavallium.warppi.hardware;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

import it.cavallium.warppi.Platform.Gpio;

public final class HardwareGpio implements Gpio, AutoCloseable {
	private static final int INPUT = 0;
	private static final int OUTPUT = 1;
	private static final int PWM_OUTPUT = 2;
	private static final int LOW = 0;
	private static final int HIGH = 1;
	private static final String UNKNOWN_BOARD = "unknown";
	private static final int HEADER_PIN_COUNT = 40;

	private final DigitalInput[] inputs = new DigitalInput[HEADER_PIN_COUNT + 1];
	private final DigitalOutput[] outputs = new DigitalOutput[HEADER_PIN_COUNT + 1];
	private volatile Context context;

	@Override
	public int valueOutput() {
		return OUTPUT;
	}

	@Override
	public int valuePwmOutput() {
		return PWM_OUTPUT;
	}

	@Override
	public int valueInput() {
		return INPUT;
	}

	@Override
	public int valueHigh() {
		return HIGH;
	}

	@Override
	public int valueLow() {
		return LOW;
	}

	@Override
	public Object valueUnknownBoardType() {
		return UNKNOWN_BOARD;
	}

	@Override
	public synchronized void wiringPiSetupPhys() {
		if (context == null)
			context = Pi4J.newAutoContext();
	}

	@Override
	public synchronized void pinMode(final int physicalPin, final int type) {
		final Context pi4j = requireContext();
		final int bcmPin = physicalToBcm(physicalPin);
		if (type == OUTPUT) {
			if (inputs[physicalPin] != null)
				throw new IllegalStateException("Physical pin " + physicalPin + " is already configured as input");
			if (outputs[physicalPin] == null) {
				outputs[physicalPin] = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
						.id("warppi-output-" + physicalPin)
						.name("WarpPI physical pin " + physicalPin)
						.address(bcmPin)
						.provider("gpiod-digital-output")
						.initial(DigitalState.LOW)
						.shutdown(DigitalState.LOW));
			}
		} else if (type == INPUT) {
			if (outputs[physicalPin] != null)
				throw new IllegalStateException("Physical pin " + physicalPin + " is already configured as output");
			if (inputs[physicalPin] == null) {
				inputs[physicalPin] = pi4j.create(DigitalInput.newConfigBuilder(pi4j)
						.id("warppi-input-" + physicalPin)
						.name("WarpPI physical pin " + physicalPin)
						.address(bcmPin)
						.provider("gpiod-digital-input"));
			}
		} else if (type == PWM_OUTPUT) {
			throw new UnsupportedOperationException("The GpioD provider does not support PWM");
		} else {
			throw new IllegalArgumentException("Unknown GPIO mode " + type);
		}
	}

	@Override
	public void digitalWrite(final int physicalPin, final int value) {
		if (value == HIGH)
			requireOutput(physicalPin).high();
		else if (value == LOW)
			requireOutput(physicalPin).low();
		else
			throw new IllegalArgumentException("Digital value must be LOW or HIGH");
	}

	@Override
	public void digitalWrite(final int physicalPin, final boolean value) {
		requireOutput(physicalPin).state(value ? DigitalState.HIGH : DigitalState.LOW);
	}

	@Override
	public void pwmWrite(final int physicalPin, final int value) {
		throw new UnsupportedOperationException("The GpioD provider does not support PWM");
	}

	@Override
	public void delayMicroseconds(final int microseconds) {
		if (microseconds < 0)
			throw new IllegalArgumentException("microseconds must not be negative");
		if (microseconds > 0)
			LockSupport.parkNanos(microseconds * 1_000L);
	}

	@Override
	public int digitalRead(final int physicalPin) {
		return requireInput(physicalPin).state() == DigitalState.HIGH ? HIGH : LOW;
	}

	@Override
	public Object getBoardType() {
		return context == null ? UNKNOWN_BOARD : "raspberry-pi";
	}

	@Override
	public synchronized void close() {
		final Context currentContext = context;
		if (currentContext == null)
			return;
		context = null;
		Arrays.fill(inputs, null);
		Arrays.fill(outputs, null);
		currentContext.shutdown();
	}

	static int physicalToBcm(final int physicalPin) {
		return switch (physicalPin) {
			case 3 -> 2;
			case 5 -> 3;
			case 7 -> 4;
			case 8 -> 14;
			case 10 -> 15;
			case 11 -> 17;
			case 12 -> 18;
			case 13 -> 27;
			case 15 -> 22;
			case 16 -> 23;
			case 18 -> 24;
			case 19 -> 10;
			case 21 -> 9;
			case 22 -> 25;
			case 23 -> 11;
			case 24 -> 8;
			case 26 -> 7;
			case 27 -> 0;
			case 28 -> 1;
			case 29 -> 5;
			case 31 -> 6;
			case 32 -> 12;
			case 33 -> 13;
			case 35 -> 19;
			case 36 -> 16;
			case 37 -> 26;
			case 38 -> 20;
			case 40 -> 21;
			default -> throw new IllegalArgumentException("Physical pin " + physicalPin + " is not a GPIO pin");
		};
	}

	private Context requireContext() {
		final Context result = context;
		if (result == null)
			throw new IllegalStateException("GPIO has not been initialized");
		return result;
	}

	private DigitalOutput requireOutput(final int physicalPin) {
		validateHeaderPin(physicalPin);
		final DigitalOutput output = outputs[physicalPin];
		if (output == null)
			throw new IllegalStateException("Physical pin " + physicalPin + " is not configured as output");
		return output;
	}

	private DigitalInput requireInput(final int physicalPin) {
		validateHeaderPin(physicalPin);
		final DigitalInput input = inputs[physicalPin];
		if (input == null)
			throw new IllegalStateException("Physical pin " + physicalPin + " is not configured as input");
		return input;
	}

	private static void validateHeaderPin(final int physicalPin) {
		if (physicalPin < 1 || physicalPin > HEADER_PIN_COUNT)
			throw new IllegalArgumentException("Physical pin must be between 1 and " + HEADER_PIN_COUNT);
	}
}

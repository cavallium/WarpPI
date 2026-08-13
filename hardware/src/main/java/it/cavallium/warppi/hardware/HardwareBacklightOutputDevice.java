package it.cavallium.warppi.hardware;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

import it.cavallium.warppi.device.display.BacklightOutputDevice;

/**
 * Drives the WarpPI backlight through the Raspberry Pi Zero's BCM2835 PWM0
 * peripheral on BCM GPIO18 (physical header pin 12).
 */
final class HardwareBacklightOutputDevice implements BacklightOutputDevice, AutoCloseable {
	static final int BACKLIGHT_GPIO = 18;
	static final int BACKLIGHT_GPIO_ALT5 = 2;
	static final int PWM_CONTROL = 0x00;
	static final int PWM_RANGE_1 = 0x10;
	static final int PWM_DATA_1 = 0x14;
	static final int PWM_CHANNEL_1_ENABLE = 0x0001;
	static final int PWM_RANGE = 1_024;
	static final int PWM_CLOCK_CONTROL = 0xa0;
	static final int PWM_CLOCK_DIVISOR = 0xa4;
	static final int PWM_CLOCK_BUSY = 0x0080;
	static final int PWM_CLOCK_DIVIDER = 32;
	static final int CLOCK_MANAGER_PASSWORD = 0x5a00_0000;
	static final int CLOCK_SOURCE_OSCILLATOR = 0x0001;
	static final int CLOCK_ENABLE = 0x0010;

	private static final Path PHYSICAL_MEMORY_DEVICE = Path.of("/dev/mem");
	private static final long REGISTER_PAGE_SIZE = 4_096;
	private static final long BCM2835_CLOCK_BASE = 0x2010_1000L;
	private static final long BCM2835_PWM_BASE = 0x2020_c000L;
	private static final int CLOCK_STOP_DELAY_MICROSECONDS = 110;
	private static final int PWM_RANGE_DELAY_MICROSECONDS = 10;
	private static final int CLOCK_BUSY_TIMEOUT_MICROSECONDS = 10_000;

	@FunctionalInterface
	interface RegisterFactory {
		GpioRegisters open() throws IOException;
	}

	private final HardwareGpio gpio;
	private final RegisterFactory pwmRegisterFactory;
	private final RegisterFactory clockRegisterFactory;
	private GpioRegisters pwmRegisters;
	private GpioRegisters clockRegisters;
	private int dutyCycle;
	private boolean powerEnabled = true;

	HardwareBacklightOutputDevice(final HardwareGpio gpio) {
		this(gpio,
				() -> LinuxMemoryMappedRegisters.map(
						PHYSICAL_MEMORY_DEVICE, REGISTER_PAGE_SIZE, BCM2835_PWM_BASE),
				() -> LinuxMemoryMappedRegisters.map(
						PHYSICAL_MEMORY_DEVICE, REGISTER_PAGE_SIZE, BCM2835_CLOCK_BASE));
	}

	HardwareBacklightOutputDevice(final HardwareGpio gpio,
			final RegisterFactory pwmRegisterFactory,
			final RegisterFactory clockRegisterFactory) {
		this.gpio = Objects.requireNonNull(gpio);
		this.pwmRegisterFactory = Objects.requireNonNull(pwmRegisterFactory);
		this.clockRegisterFactory = Objects.requireNonNull(clockRegisterFactory);
	}

	@Override
	public boolean supportsBrightnessControl() {
		return true;
	}

	@Override
	public synchronized void setBrightness(final double value) {
		if (!Double.isFinite(value) || value < 0d || value > 1d)
			throw new IllegalArgumentException("Brightness must be between 0 and 1");
		final int requestedDutyCycle = (int) Math.ceil(value * PWM_RANGE);
		initializeHardware();
		dutyCycle = requestedDutyCycle;
		writeDutyCycle();
	}

	@Override
	public synchronized void setPower(final boolean value) {
		initializeHardware();
		powerEnabled = value;
		writeDutyCycle();
	}

	private void initializeHardware() {
		if (pwmRegisters != null)
			return;

		GpioRegisters openedPwmRegisters = null;
		GpioRegisters openedClockRegisters = null;
		boolean gpioClaimed = false;
		try {
			openedPwmRegisters = pwmRegisterFactory.open();
			openedClockRegisters = clockRegisterFactory.open();
			openedPwmRegisters.write(PWM_CONTROL, 0);
			openedPwmRegisters.write(PWM_DATA_1, 0);
			gpio.claimAlternateFunction(BACKLIGHT_GPIO, BACKLIGHT_GPIO_ALT5);
			gpioClaimed = true;
			configurePwm(openedPwmRegisters, openedClockRegisters);
			pwmRegisters = openedPwmRegisters;
			clockRegisters = openedClockRegisters;
		} catch (final IOException failure) {
			cleanupFailedInitialization(openedPwmRegisters, openedClockRegisters, gpioClaimed, failure);
			throw new UncheckedIOException("Unable to initialize the Raspberry Pi backlight", failure);
		} catch (final RuntimeException | Error failure) {
			cleanupFailedInitialization(openedPwmRegisters, openedClockRegisters, gpioClaimed, failure);
			throw failure;
		}
	}

	private void configurePwm(final GpioRegisters pwm, final GpioRegisters clock) {
		gpio.delayMicroseconds(CLOCK_STOP_DELAY_MICROSECONDS);
		pwm.write(PWM_RANGE_1, PWM_RANGE);
		gpio.delayMicroseconds(PWM_RANGE_DELAY_MICROSECONDS);

		clock.write(PWM_CLOCK_CONTROL,
				CLOCK_MANAGER_PASSWORD | CLOCK_SOURCE_OSCILLATOR);
		gpio.delayMicroseconds(CLOCK_STOP_DELAY_MICROSECONDS);
		waitForClockToStop(clock);
		clock.write(PWM_CLOCK_DIVISOR,
				CLOCK_MANAGER_PASSWORD | (PWM_CLOCK_DIVIDER << 12));
		clock.write(PWM_CLOCK_CONTROL,
				CLOCK_MANAGER_PASSWORD | CLOCK_ENABLE | CLOCK_SOURCE_OSCILLATOR);
		pwm.write(PWM_CONTROL, PWM_CHANNEL_1_ENABLE);
	}

	private void waitForClockToStop(final GpioRegisters clock) {
		for (int waited = 0; waited < CLOCK_BUSY_TIMEOUT_MICROSECONDS; waited++) {
			if ((clock.read(PWM_CLOCK_CONTROL) & PWM_CLOCK_BUSY) == 0)
				return;
			gpio.delayMicroseconds(1);
		}
		throw new IllegalStateException("BCM2835 PWM clock did not stop");
	}

	private void writeDutyCycle() {
		pwmRegisters.write(PWM_DATA_1, powerEnabled ? dutyCycle : 0);
	}

	private void cleanupFailedInitialization(final GpioRegisters pwm,
			final GpioRegisters clock, final boolean gpioClaimed, final Throwable failure) {
		if (gpioClaimed) {
			try {
				gpio.releaseAlternateFunction(BACKLIGHT_GPIO);
			} catch (final RuntimeException cleanupFailure) {
				failure.addSuppressed(cleanupFailure);
			}
		}
		closeAfterFailure(clock, failure);
		closeAfterFailure(pwm, failure);
	}

	private static void closeAfterFailure(final GpioRegisters registers, final Throwable failure) {
		if (registers == null)
			return;
		try {
			registers.close();
		} catch (final RuntimeException cleanupFailure) {
			failure.addSuppressed(cleanupFailure);
		}
	}

	@Override
	public synchronized void close() {
		final GpioRegisters currentPwmRegisters = pwmRegisters;
		final GpioRegisters currentClockRegisters = clockRegisters;
		if (currentPwmRegisters == null)
			return;
		pwmRegisters = null;
		clockRegisters = null;

		RuntimeException failure = null;
		try {
			currentPwmRegisters.write(PWM_DATA_1, 0);
			currentPwmRegisters.write(PWM_CONTROL, 0);
			currentClockRegisters.write(PWM_CLOCK_CONTROL,
					CLOCK_MANAGER_PASSWORD | CLOCK_SOURCE_OSCILLATOR);
		} catch (final RuntimeException shutdownFailure) {
			failure = shutdownFailure;
		}
		try {
			gpio.releaseAlternateFunction(BACKLIGHT_GPIO);
		} catch (final RuntimeException shutdownFailure) {
			failure = combine(failure, shutdownFailure);
		}
		try {
			currentClockRegisters.close();
		} catch (final RuntimeException shutdownFailure) {
			failure = combine(failure, shutdownFailure);
		}
		try {
			currentPwmRegisters.close();
		} catch (final RuntimeException shutdownFailure) {
			failure = combine(failure, shutdownFailure);
		}
		if (failure != null)
			throw failure;
	}

	private static RuntimeException combine(final RuntimeException first,
			final RuntimeException next) {
		if (first == null)
			return next;
		first.addSuppressed(next);
		return first;
	}
}

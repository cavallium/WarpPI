package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class HardwareBacklightOutputDeviceTest {
	@Test
	void configuresThePiZeroHardwarePwmAndDrivesBrightness() throws IOException {
		final FakeRegisters gpioRegisters = new FakeRegisters();
		final FakeRegisters pwmRegisters = new FakeRegisters();
		final FakeRegisters clockRegisters = new FakeRegisters();
		final HardwareGpio gpio = initializedGpio(gpioRegisters);
		final HardwareBacklightOutputDevice backlight = backlight(
				gpio, pwmRegisters, clockRegisters);

		backlight.setBrightness(0.5d);

		assertEquals(HardwareBacklightOutputDevice.BACKLIGHT_GPIO_ALT5,
				function(gpioRegisters, HardwareBacklightOutputDevice.BACKLIGHT_GPIO));
		assertEquals(HardwareBacklightOutputDevice.PWM_RANGE,
				pwmRegisters.read(HardwareBacklightOutputDevice.PWM_RANGE_1));
		assertEquals(512, pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));
		assertEquals(HardwareBacklightOutputDevice.PWM_CHANNEL_1_ENABLE,
				pwmRegisters.read(HardwareBacklightOutputDevice.PWM_CONTROL));
		assertEquals(HardwareBacklightOutputDevice.CLOCK_MANAGER_PASSWORD
					| (HardwareBacklightOutputDevice.PWM_CLOCK_DIVIDER << 12),
				clockRegisters.read(HardwareBacklightOutputDevice.PWM_CLOCK_DIVISOR));
		assertEquals(HardwareBacklightOutputDevice.CLOCK_MANAGER_PASSWORD
					| HardwareBacklightOutputDevice.CLOCK_ENABLE
					| HardwareBacklightOutputDevice.CLOCK_SOURCE_OSCILLATOR,
				clockRegisters.read(HardwareBacklightOutputDevice.PWM_CLOCK_CONTROL));

		backlight.setPower(false);
		assertEquals(0, pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));
		backlight.setBrightness(0.75d);
		assertEquals(0, pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));
		backlight.setPower(true);
		assertEquals(768, pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));

		backlight.close();
		assertEquals(0, function(gpioRegisters, HardwareBacklightOutputDevice.BACKLIGHT_GPIO));
		assertEquals(0, pwmRegisters.values[HardwareBacklightOutputDevice.PWM_DATA_1 / Integer.BYTES]);
		assertEquals(0, pwmRegisters.values[HardwareBacklightOutputDevice.PWM_CONTROL / Integer.BYTES]);
		assertTrue(pwmRegisters.closed);
		assertTrue(clockRegisters.closed);
		backlight.close();
		gpio.close();
	}

	@Test
	void mapsTheFullBrightnessRangeWithTheLegacyCeilingRule() throws IOException {
		final FakeRegisters gpioRegisters = new FakeRegisters();
		final FakeRegisters pwmRegisters = new FakeRegisters();
		final HardwareGpio gpio = initializedGpio(gpioRegisters);
		try (HardwareBacklightOutputDevice backlight = backlight(
				gpio, pwmRegisters, new FakeRegisters())) {
			backlight.setBrightness(0d);
			assertEquals(0, pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));
			backlight.setBrightness(0.0001d);
			assertEquals(1, pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));
			backlight.setBrightness(1d);
			assertEquals(HardwareBacklightOutputDevice.PWM_RANGE,
					pwmRegisters.read(HardwareBacklightOutputDevice.PWM_DATA_1));
		} finally {
			gpio.close();
		}
	}

	@Test
	void rejectsInvalidBrightnessBeforeOpeningHardware() throws IOException {
		final HardwareGpio gpio = initializedGpio(new FakeRegisters());
		final int[] opens = {0};
		try (HardwareBacklightOutputDevice backlight = new HardwareBacklightOutputDevice(
				gpio,
				() -> {
					opens[0]++;
					return new FakeRegisters();
				},
				() -> {
					opens[0]++;
					return new FakeRegisters();
				})) {
			assertThrows(IllegalArgumentException.class, () -> backlight.setBrightness(-0.01d));
			assertThrows(IllegalArgumentException.class, () -> backlight.setBrightness(1.01d));
			assertThrows(IllegalArgumentException.class, () -> backlight.setBrightness(Double.NaN));
			assertEquals(0, opens[0]);
		} finally {
			gpio.close();
		}
	}

	@Test
	void cleansUpAllResourcesWhenThePwmClockDoesNotStop() throws IOException {
		final FakeRegisters gpioRegisters = new FakeRegisters();
		final FakeRegisters pwmRegisters = new FakeRegisters();
		final FakeRegisters clockRegisters = new FakeRegisters();
		clockRegisters.clockAlwaysBusy = true;
		final HardwareGpio gpio = initializedGpio(gpioRegisters);
		final HardwareBacklightOutputDevice backlight = backlight(
				gpio, pwmRegisters, clockRegisters);

		assertThrows(IllegalStateException.class, () -> backlight.setBrightness(0.5d));

		assertEquals(0, function(gpioRegisters, HardwareBacklightOutputDevice.BACKLIGHT_GPIO));
		assertTrue(pwmRegisters.closed);
		assertTrue(clockRegisters.closed);
		backlight.close();
		gpio.close();
	}

	private static HardwareBacklightOutputDevice backlight(final HardwareGpio gpio,
			final FakeRegisters pwmRegisters, final FakeRegisters clockRegisters) {
		return new HardwareBacklightOutputDevice(gpio, () -> pwmRegisters, () -> clockRegisters);
	}

	private static HardwareGpio initializedGpio(final FakeRegisters registers) throws IOException {
		final HardwareGpio gpio = new HardwareGpio(() -> registers);
		gpio.initialize();
		return gpio;
	}

	private static int function(final FakeRegisters registers, final int bcmPin) {
		final int register = HardwareGpio.GPFSEL0 + bcmPin / 10 * Integer.BYTES;
		return registers.values[register / Integer.BYTES] >>> bcmPin % 10 * 3 & 0b111;
	}

	private static final class FakeRegisters implements GpioRegisters {
		private final int[] values = new int[1_024];
		private boolean clockAlwaysBusy;
		private boolean closed;

		@Override
		public int read(final int byteOffset) {
			requireOpen();
			final int value = values[byteOffset / Integer.BYTES];
			if (clockAlwaysBusy
					&& byteOffset == HardwareBacklightOutputDevice.PWM_CLOCK_CONTROL)
				return value | HardwareBacklightOutputDevice.PWM_CLOCK_BUSY;
			return value;
		}

		@Override
		public void write(final int byteOffset, final int value) {
			requireOpen();
			values[byteOffset / Integer.BYTES] = value;
		}

		@Override
		public void close() {
			closed = true;
		}

		private void requireOpen() {
			if (closed)
				throw new IllegalStateException("registers closed");
		}
	}
}

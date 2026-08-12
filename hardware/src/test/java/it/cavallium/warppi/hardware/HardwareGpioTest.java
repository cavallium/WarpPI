package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Gpio.DigitalInput;
import it.cavallium.warppi.Platform.Gpio.DigitalOutput;

class HardwareGpioTest {
	@Test
	void configuresOutputLevelBeforeEnablingTheLine() throws IOException {
		final FakeRegisters registers = new FakeRegisters();
		try (HardwareGpio gpio = gpio(registers)) {
			final DigitalOutput output = gpio.output(19, true);

			assertEquals(new Write(HardwareGpio.GPSET0, 1 << 19), registers.writes.get(0));
			assertEquals(1, function(registers, 19));

			output.set(false);
			assertEquals(1 << 19, registers.lastWrite(HardwareGpio.GPCLR0));
			assertSame(output, gpio.output(19, true));
			assertEquals(1 << 19, registers.lastWrite(HardwareGpio.GPSET0));
		}
	}

	@Test
	void configuresAndReadsInputLines() throws IOException {
		final FakeRegisters registers = new FakeRegisters();
		registers.values[functionRegister(21) / Integer.BYTES] = -1;
		try (HardwareGpio gpio = gpio(registers)) {
			final DigitalInput input = gpio.input(21);

			assertEquals(0, function(registers, 21));
			registers.values[HardwareGpio.GPLEV0 / Integer.BYTES] = 1 << 21;
			assertTrue(input.isHigh());
			registers.values[HardwareGpio.GPLEV0 / Integer.BYTES] = 0;
			assertFalse(input.isHigh());
			assertSame(input, gpio.input(21));
		}
	}

	@Test
	void supportsTheSecondGpioRegisterBank() throws IOException {
		final FakeRegisters registers = new FakeRegisters();
		try (HardwareGpio gpio = gpio(registers)) {
			final DigitalOutput output = gpio.output(40, true);
			assertEquals(1 << 8, registers.lastWrite(HardwareGpio.GPSET0 + Integer.BYTES));

			output.set(false);
			assertEquals(1 << 8, registers.lastWrite(HardwareGpio.GPCLR0 + Integer.BYTES));
		}
	}

	@Test
	void rejectsInvalidAndConflictingLineRequests() throws IOException {
		final FakeRegisters registers = new FakeRegisters();
		try (HardwareGpio gpio = gpio(registers)) {
			assertThrows(IllegalArgumentException.class, () -> gpio.input(-1));
			assertThrows(IllegalArgumentException.class, () -> gpio.output(54, false));
			gpio.output(19, false);
			assertThrows(IllegalStateException.class, () -> gpio.input(19));
			gpio.input(21);
			assertThrows(IllegalStateException.class, () -> gpio.output(21, false));
			assertThrows(IllegalArgumentException.class, () -> gpio.delayMicroseconds(-1));
		}
	}

	@Test
	void closesOutputsLowAndReturnsThemToInputMode() throws IOException {
		final FakeRegisters registers = new FakeRegisters();
		final HardwareGpio gpio = gpio(registers);
		final DigitalOutput output = gpio.output(19, true);
		gpio.output(40, true);

		gpio.close();

		assertEquals(1 << 19, registers.lastWrite(HardwareGpio.GPCLR0));
		assertEquals(1 << 8, registers.lastWrite(HardwareGpio.GPCLR0 + Integer.BYTES));
		assertEquals(0, function(registers, 19));
		assertEquals(0, function(registers, 40));
		assertTrue(registers.closed);
		assertThrows(IllegalStateException.class, () -> output.set(true));
		gpio.close();
	}

	@Test
	void staysUninitializedWhenOpeningFails() {
		final HardwareGpio gpio = new HardwareGpio(() -> {
			throw new IOException("expected");
		});

		assertThrows(IOException.class, gpio::initialize);
		assertThrows(IllegalStateException.class, () -> gpio.output(19, false));
		gpio.close();
	}

	private static HardwareGpio gpio(final FakeRegisters registers) throws IOException {
		final HardwareGpio gpio = new HardwareGpio(() -> registers);
		gpio.initialize();
		gpio.initialize();
		return gpio;
	}

	private static int function(final FakeRegisters registers, final int bcmPin) {
		final int registerValue = registers.values[functionRegister(bcmPin) / Integer.BYTES];
		return registerValue >>> bcmPin % 10 * 3 & 0b111;
	}

	private static int functionRegister(final int bcmPin) {
		return HardwareGpio.GPFSEL0 + bcmPin / 10 * Integer.BYTES;
	}

	private record Write(int byteOffset, int value) {
	}

	private static final class FakeRegisters implements GpioRegisters {
		private final int[] values = new int[1_024];
		private final List<Write> writes = new ArrayList<>();
		private boolean closed;

		@Override
		public int read(final int byteOffset) {
			requireOpen();
			return values[byteOffset / Integer.BYTES];
		}

		@Override
		public void write(final int byteOffset, final int value) {
			requireOpen();
			values[byteOffset / Integer.BYTES] = value;
			writes.add(new Write(byteOffset, value));
		}

		@Override
		public void close() {
			closed = true;
		}

		int lastWrite(final int byteOffset) {
			for (int index = writes.size() - 1; index >= 0; index--) {
				final Write write = writes.get(index);
				if (write.byteOffset == byteOffset)
					return write.value;
			}
			throw new AssertionError("No write to offset " + byteOffset);
		}

		private void requireOpen() {
			if (closed)
				throw new IllegalStateException("registers closed");
		}
	}
}

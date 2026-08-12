package it.cavallium.warppi.device.chip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Gpio;

class ShiftRegisterTest {

	@Test
	void writesAllBitsBeforeLatchingOutputs() {
		final RecordingGpio gpio = new RecordingGpio();
		final SerialToParallel output = new SerialToParallel(gpio, 1, 2, 3);

		output.writeByte(0b1000_0001);

		assertEquals(List.of(1, 0, 0, 0, 0, 0, 0, 1), gpio.valuesWrittenTo(3));
		assertEquals(List.of(0, 1), gpio.valuesWrittenTo(1));
		assertEquals(0, gpio.lastValueWrittenTo(2));
	}

	@Test
	void rejectsBooleanArraysThatAreNotOneByte() {
		final SerialToParallel output = new SerialToParallel(new RecordingGpio(), 1, 2, 3);

		assertThrows(IllegalArgumentException.class, () -> output.write(new boolean[7]));
	}

	@Test
	void samplesTheLoadedBitBeforeEachShift() {
		final RecordingGpio gpio = new RecordingGpio();
		gpio.queueReadByte(0b1010_0101);
		final ParallelToSerial input = new ParallelToSerial(gpio, 1, 2, 3, 4);

		assertEquals(0b1010_0101, input.readByte());
		assertEquals(8, gpio.readCount);
		assertEquals(7, gpio.valuesWrittenTo(4).stream().filter(value -> value == 1).count());
		assertTrue(gpio.operations.indexOf("R3") < gpio.operations.indexOf("W4=1"));
		assertEquals(1, gpio.lastValueWrittenTo(2));
		assertEquals(0, gpio.lastValueWrittenTo(4));
	}

	private static final class RecordingGpio implements Gpio {
		private final List<String> operations = new ArrayList<>();
		private final Deque<Integer> reads = new ArrayDeque<>();
		private int readCount;

		void queueReadByte(final int value) {
			for (int bit = 7; bit >= 0; bit--)
				reads.addLast((value & 1 << bit) != 0 ? 1 : 0);
		}

		List<Integer> valuesWrittenTo(final int pin) {
			final String prefix = "W" + pin + "=";
			final List<Integer> values = new ArrayList<>();
			for (String operation : operations)
				if (operation.startsWith(prefix))
					values.add(Integer.parseInt(operation.substring(prefix.length())));
			return values;
		}

		int lastValueWrittenTo(final int pin) {
			final List<Integer> values = valuesWrittenTo(pin);
			return values.get(values.size() - 1);
		}

		@Override
		public int valueOutput() {
			return 1;
		}

		@Override
		public int valuePwmOutput() {
			return 2;
		}

		@Override
		public int valueInput() {
			return 0;
		}

		@Override
		public int valueHigh() {
			return 1;
		}

		@Override
		public int valueLow() {
			return 0;
		}

		@Override
		public Object valueUnknownBoardType() {
			return new Object();
		}

		@Override
		public void wiringPiSetupPhys() {
		}

		@Override
		public void pinMode(final int pin, final int type) {
		}

		@Override
		public void digitalWrite(final int pin, final int value) {
			operations.add("W" + pin + "=" + value);
		}

		@Override
		public void digitalWrite(final int pin, final boolean value) {
			digitalWrite(pin, value ? 1 : 0);
		}

		@Override
		public void pwmWrite(final int pin, final int value) {
		}

		@Override
		public void delayMicroseconds(final int time) {
			operations.add("D" + time);
		}

		@Override
		public int digitalRead(final int pin) {
			operations.add("R" + pin);
			readCount++;
			return reads.removeFirst();
		}

		@Override
		public Object getBoardType() {
			return new Object();
		}
	}
}

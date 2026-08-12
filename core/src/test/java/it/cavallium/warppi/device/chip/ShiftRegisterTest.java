package it.cavallium.warppi.device.chip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Gpio.DigitalInput;
import it.cavallium.warppi.Platform.Gpio.DigitalOutput;

class ShiftRegisterTest {

	@Test
	void writesAllBitsBeforeLatchingOutputs() {
		final RecordingLines lines = new RecordingLines();
		final SerialToParallel output = new SerialToParallel(lines.output(1), lines.output(2), lines.output(3));

		output.writeByte(0b1000_0001);

		assertEquals(List.of(1, 0, 0, 0, 0, 0, 0, 1), lines.valuesWrittenTo(3));
		assertEquals(List.of(0, 1), lines.valuesWrittenTo(1));
		assertEquals(0, lines.lastValueWrittenTo(2));
	}

	@Test
	void samplesTheLoadedBitBeforeEachShift() {
		final RecordingLines lines = new RecordingLines();
		lines.queueReadByte(0b1010_0101);
		final ParallelToSerial input = new ParallelToSerial(lines.output(1), lines.output(2), lines.input(3),
				lines.output(4), lines::delayMicroseconds);

		assertEquals(0b1010_0101, input.readByte());
		assertEquals(8, lines.readCount);
		assertEquals(7, lines.valuesWrittenTo(4).stream().filter(value -> value == 1).count());
		assertTrue(lines.operations.indexOf("R3") < lines.operations.indexOf("W4=1"));
		assertEquals(1, lines.lastValueWrittenTo(2));
		assertEquals(0, lines.lastValueWrittenTo(4));
		assertTrue(lines.operations.contains("D1"));
	}

	private static final class RecordingLines {
		private final List<String> operations = new ArrayList<>();
		private final Deque<Boolean> reads = new ArrayDeque<>();
		private int readCount;

		void queueReadByte(final int value) {
			for (int bit = 7; bit >= 0; bit--)
				reads.addLast((value & 1 << bit) != 0);
		}

		DigitalOutput output(final int pin) {
			return high -> operations.add("W" + pin + "=" + (high ? 1 : 0));
		}

		DigitalInput input(final int pin) {
			return () -> {
				operations.add("R" + pin);
				readCount++;
				return reads.removeFirst();
			};
		}

		void delayMicroseconds(final int microseconds) {
			operations.add("D" + microseconds);
		}

		List<Integer> valuesWrittenTo(final int pin) {
			final String prefix = "W" + pin + "=";
			final List<Integer> values = new ArrayList<>();
			for (final String operation : operations)
				if (operation.startsWith(prefix))
					values.add(Integer.parseInt(operation.substring(prefix.length())));
			return values;
		}

		int lastValueWrittenTo(final int pin) {
			final List<Integer> values = valuesWrittenTo(pin);
			return values.get(values.size() - 1);
		}
	}
}

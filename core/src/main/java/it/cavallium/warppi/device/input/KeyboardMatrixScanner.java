package it.cavallium.warppi.device.input;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import it.cavallium.warppi.device.chip.ParallelToSerial;
import it.cavallium.warppi.device.chip.SerialToParallel;

final class KeyboardMatrixScanner {
	static final int MATRIX_SIZE = Byte.SIZE;

	@FunctionalInterface
	interface TransitionListener {
		void onTransition(int row, int column, boolean pressed);
	}

	@FunctionalInterface
	interface SampleListener {
		void onSample(int column, int sampledRows);
	}

	private final IntConsumer writeColumns;
	private final IntSupplier readRows;
	private final TransitionListener listener;
	private final SampleListener sampleListener;
	private final int debounceSamples;
	private final int[] stableRowsByColumn = new int[MATRIX_SIZE];
	private final byte[] consecutiveChanges = new byte[MATRIX_SIZE * MATRIX_SIZE];

	KeyboardMatrixScanner(final SerialToParallel columns, final ParallelToSerial rows,
			final TransitionListener listener, final int debounceSamples) {
		this(columns::writeByte, rows::readByte, listener, null, debounceSamples);
	}

	KeyboardMatrixScanner(final SerialToParallel columns, final ParallelToSerial rows,
			final TransitionListener listener, final SampleListener sampleListener,
			final int debounceSamples) {
		this(columns::writeByte, rows::readByte, listener, sampleListener, debounceSamples);
	}

	KeyboardMatrixScanner(final IntConsumer writeColumns, final IntSupplier readRows,
			final TransitionListener listener, final int debounceSamples) {
		this(writeColumns, readRows, listener, null, debounceSamples);
	}

	KeyboardMatrixScanner(final IntConsumer writeColumns, final IntSupplier readRows,
			final TransitionListener listener, final SampleListener sampleListener,
			final int debounceSamples) {
		this.writeColumns = Objects.requireNonNull(writeColumns);
		this.readRows = Objects.requireNonNull(readRows);
		this.listener = Objects.requireNonNull(listener);
		this.sampleListener = sampleListener;
		if (debounceSamples < 1 || debounceSamples > Byte.MAX_VALUE)
			throw new IllegalArgumentException("debounceSamples must be between 1 and 127");
		this.debounceSamples = debounceSamples;
	}

	void scanOnce() {
		try {
			for (int column = 0; column < MATRIX_SIZE; column++) {
				writeColumns.accept(1 << column);
				final int sampledRows = readRows.getAsInt() & 0xff;
				if (sampleListener != null)
					sampleListener.onSample(column, sampledRows);
				acceptSample(column, sampledRows);
			}
		} finally {
			writeColumns.accept(0);
		}
	}

	void releaseAll() {
		for (int column = 0; column < MATRIX_SIZE; column++) {
			final int stableRows = stableRowsByColumn[column];
			stableRowsByColumn[column] = 0;
			for (int row = 0; row < MATRIX_SIZE; row++) {
				consecutiveChanges[index(row, column)] = 0;
				if ((stableRows & 1 << row) != 0)
					listener.onTransition(row, column, false);
			}
		}
	}

	private void acceptSample(final int column, final int sampledRows) {
		for (int row = 0; row < MATRIX_SIZE; row++) {
			final int mask = 1 << row;
			final boolean sampled = (sampledRows & mask) != 0;
			final boolean stable = (stableRowsByColumn[column] & mask) != 0;
			final int index = index(row, column);

			if (sampled == stable) {
				consecutiveChanges[index] = 0;
				continue;
			}

			final int samples = ++consecutiveChanges[index];
			if (samples < debounceSamples)
				continue;

			consecutiveChanges[index] = 0;
			if (sampled)
				stableRowsByColumn[column] |= mask;
			else
				stableRowsByColumn[column] &= ~mask;
			listener.onTransition(row, column, sampled);
		}
	}

	private static int index(final int row, final int column) {
		return row * MATRIX_SIZE + column;
	}
}

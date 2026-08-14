package it.cavallium.warppi.device.input;

import java.util.Arrays;

/**
 * Lock-free diagnostic state for the Raspberry keyboard matrix scanner.
 *
 * <p>The scanner has one writer and the UI may sample it from another thread.
 * Raw activity is recorded before debounce, while accepted activity is recorded
 * from the scanner's debounced transitions. This makes wiring/contact failures
 * distinguishable from mapping and debounce problems without allocating in the
 * scan loop.</p>
 */
public final class KeyboardMatrixDiagnostics {
	public static final int MATRIX_SIZE = Byte.SIZE;
	private static final int MATRIX_KEY_COUNT = MATRIX_SIZE * MATRIX_SIZE;

	private final int[] rawPressCounts = new int[MATRIX_KEY_COUNT];
	private final int[] acceptedPressCounts = new int[MATRIX_KEY_COUNT];
	private volatile int counterVersion;

	private volatile boolean scannerRunning;
	private volatile long rawMatrix;
	private volatile long acceptedMatrix;
	private volatile long observedRawMatrix;
	private volatile long observedAcceptedMatrix;
	private volatile long scanCount;
	private volatile long lastScanNanos;
	private volatile int lastRawChange = -1;
	private volatile boolean lastRawPressed;
	private volatile long lastRawChangeNanos;
	private volatile int lastAcceptedChange = -1;
	private volatile boolean lastAcceptedPressed;
	private volatile long lastAcceptedChangeNanos;

	void scanningStarted() {
		clear();
		scannerRunning = true;
	}

	void scanningStopped() {
		scannerRunning = false;
		rawMatrix = 0;
		acceptedMatrix = 0;
	}

	void recordSample(final int column, final int sampledRows) {
		validateCoordinate(column);
		long columnMask = 0;
		long sampledColumn = 0;
		for (int row = 0; row < MATRIX_SIZE; row++) {
			columnMask |= bit(row, column);
			if ((sampledRows & 1 << row) != 0)
				sampledColumn |= bit(row, column);
		}

		final long previous = rawMatrix;
		final long updated = previous & ~columnMask | sampledColumn;
		rawMatrix = updated;

		final long changes = (previous ^ updated) & columnMask;
		if (changes != 0) {
			final long now = System.nanoTime();
			for (int row = 0; row < MATRIX_SIZE; row++) {
				final long keyBit = bit(row, column);
				if ((changes & keyBit) == 0)
					continue;
				final int keyIndex = index(row, column);
				final boolean pressed = (updated & keyBit) != 0;
				if (pressed) {
					incrementCounter(rawPressCounts, keyIndex);
					observedRawMatrix |= keyBit;
				}
				lastRawChange = keyIndex;
				lastRawPressed = pressed;
				lastRawChangeNanos = now;
			}
		}

		if (column == MATRIX_SIZE - 1) {
			scanCount++;
			lastScanNanos = System.nanoTime();
		}
	}

	void recordTransition(final int row, final int column, final boolean pressed) {
		validatePosition(row, column);
		final long keyBit = bit(row, column);
		if (pressed) {
			acceptedMatrix |= keyBit;
			observedAcceptedMatrix |= keyBit;
			incrementCounter(acceptedPressCounts, index(row, column));
		} else {
			acceptedMatrix &= ~keyBit;
		}
		lastAcceptedChange = index(row, column);
		lastAcceptedPressed = pressed;
		lastAcceptedChangeNanos = System.nanoTime();
	}

	/** Clears accumulated evidence while preserving the current held-key state. */
	public void clear() {
		observedRawMatrix = 0;
		observedAcceptedMatrix = 0;
		scanCount = 0;
		lastScanNanos = 0;
		lastRawChange = -1;
		lastRawPressed = false;
		lastRawChangeNanos = 0;
		lastAcceptedChange = -1;
		lastAcceptedPressed = false;
		lastAcceptedChangeNanos = 0;
		counterVersion++;
		Arrays.fill(rawPressCounts, 0);
		Arrays.fill(acceptedPressCounts, 0);
		counterVersion++;
	}

	public Snapshot snapshot() {
		int version;
		int[] rawCounts;
		int[] acceptedCounts;
		while (true) {
			version = counterVersion;
			if ((version & 1) != 0)
				continue;
			rawCounts = rawPressCounts.clone();
			acceptedCounts = acceptedPressCounts.clone();
			if (version == counterVersion)
				break;
		}
		return new Snapshot(scannerRunning, rawMatrix, acceptedMatrix, observedRawMatrix,
				observedAcceptedMatrix, scanCount, lastScanNanos, lastRawChange,
				lastRawPressed, lastRawChangeNanos, lastAcceptedChange,
				lastAcceptedPressed, lastAcceptedChangeNanos, rawCounts, acceptedCounts,
				System.nanoTime());
	}

	private void incrementCounter(final int[] counters, final int keyIndex) {
		counterVersion++;
		if (counters[keyIndex] != Integer.MAX_VALUE)
			counters[keyIndex]++;
		counterVersion++;
	}

	private static long bit(final int row, final int column) {
		return 1L << index(row, column);
	}

	private static int index(final int row, final int column) {
		return row * MATRIX_SIZE + column;
	}

	private static void validatePosition(final int row, final int column) {
		validateCoordinate(row);
		validateCoordinate(column);
	}

	private static void validateCoordinate(final int coordinate) {
		if (coordinate < 0 || coordinate >= MATRIX_SIZE)
			throw new IllegalArgumentException("Matrix coordinates must be between 0 and 7");
	}

	/** Immutable point-in-time diagnostic view. */
	public record Snapshot(boolean scannerRunning, long rawMatrix, long acceptedMatrix,
			long observedRawMatrix, long observedAcceptedMatrix, long scanCount,
			long lastScanNanos, int lastRawChange, boolean lastRawPressed,
			long lastRawChangeNanos, int lastAcceptedChange, boolean lastAcceptedPressed,
			long lastAcceptedChangeNanos, int[] rawPressCounts, int[] acceptedPressCounts,
			long capturedNanos) {
		public Snapshot {
			rawPressCounts = rawPressCounts.clone();
			acceptedPressCounts = acceptedPressCounts.clone();
		}

		@Override
		public int[] rawPressCounts() {
			return rawPressCounts.clone();
		}

		@Override
		public int[] acceptedPressCounts() {
			return acceptedPressCounts.clone();
		}

		public boolean isRawPressed(final int row, final int column) {
			return contains(rawMatrix, row, column);
		}

		public boolean isAcceptedPressed(final int row, final int column) {
			return contains(acceptedMatrix, row, column);
		}

		public boolean hasRawActivity(final int row, final int column) {
			return contains(observedRawMatrix, row, column);
		}

		public boolean hasAcceptedActivity(final int row, final int column) {
			return contains(observedAcceptedMatrix, row, column);
		}

		public int rawPressCount(final int row, final int column) {
			validatePosition(row, column);
			return rawPressCounts[index(row, column)];
		}

		public int acceptedPressCount(final int row, final int column) {
			validatePosition(row, column);
			return acceptedPressCounts[index(row, column)];
		}

		public int lastRawRow() {
			return rowOf(lastRawChange);
		}

		public int lastRawColumn() {
			return columnOf(lastRawChange);
		}

		public int lastAcceptedRow() {
			return rowOf(lastAcceptedChange);
		}

		public int lastAcceptedColumn() {
			return columnOf(lastAcceptedChange);
		}

		private static boolean contains(final long matrix, final int row, final int column) {
			validatePosition(row, column);
			return (matrix & bit(row, column)) != 0;
		}

		private static int rowOf(final int keyIndex) {
			return keyIndex < 0 ? -1 : keyIndex / MATRIX_SIZE;
		}

		private static int columnOf(final int keyIndex) {
			return keyIndex < 0 ? -1 : keyIndex % MATRIX_SIZE;
		}
	}
}

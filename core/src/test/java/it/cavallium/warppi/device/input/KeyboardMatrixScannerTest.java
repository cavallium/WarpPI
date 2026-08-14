package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class KeyboardMatrixScannerTest {

	@Test
	void debouncesPressesAndReleasesWithoutAllocatingSamples() {
		final MatrixFixture fixture = new MatrixFixture(3);
		fixture.rows[4] = 1 << 2;

		fixture.scanner.scanOnce();
		fixture.scanner.scanOnce();
		assertTrue(fixture.events.isEmpty());

		fixture.scanner.scanOnce();
		assertEquals(List.of("2:4:true"), fixture.events);

		fixture.rows[4] = 0;
		fixture.scanner.scanOnce();
		fixture.rows[4] = 1 << 2;
		fixture.scanner.scanOnce();
		fixture.rows[4] = 0;
		fixture.scanner.scanOnce();
		fixture.scanner.scanOnce();
		assertEquals(1, fixture.events.size());

		fixture.scanner.scanOnce();
		assertEquals(List.of("2:4:true", "2:4:false"), fixture.events);
		assertEquals(0, fixture.lastColumnMask);
	}

	@Test
	void releasesStableKeysWhenScanningStops() {
		final MatrixFixture fixture = new MatrixFixture(1);
		fixture.rows[7] = 1;
		fixture.scanner.scanOnce();

		fixture.scanner.releaseAll();

		assertEquals(List.of("0:7:true", "0:7:false"), fixture.events);
	}

	@Test
	void exposesEveryRawColumnBeforeDebounce() {
		final int[] rows = new int[KeyboardMatrixScanner.MATRIX_SIZE];
		final List<String> samples = new ArrayList<>();
		final List<String> transitions = new ArrayList<>();
		final int[] selectedColumn = { -1 };
		rows[3] = 1 << 2 | 1 << 3;
		rows[5] = 1 << 1;
		rows[6] = 1 << 2 | 1 << 3;

		final KeyboardMatrixScanner scanner = new KeyboardMatrixScanner(
				mask -> selectedColumn[0] = mask == 0 ? -1 : Integer.numberOfTrailingZeros(mask),
				() -> rows[selectedColumn[0]],
				(row, column, pressed) -> transitions.add(row + ":" + column + ":" + pressed),
				(column, sampledRows) -> samples.add(column + ":" + sampledRows), 3);

		scanner.scanOnce();

		assertEquals(8, samples.size());
		assertEquals("3:12", samples.get(3));
		assertEquals("5:2", samples.get(5));
		assertEquals("6:12", samples.get(6));
		assertTrue(transitions.isEmpty());
	}

	private static final class MatrixFixture {
		private final int[] rows = new int[KeyboardMatrixScanner.MATRIX_SIZE];
		private final List<String> events = new ArrayList<>();
		private int selectedColumn = -1;
		private int lastColumnMask;
		private final KeyboardMatrixScanner scanner;

		private MatrixFixture(final int debounceSamples) {
			scanner = new KeyboardMatrixScanner(this::selectColumn, this::readRows,
					(row, column, pressed) -> events.add(row + ":" + column + ":" + pressed),
					debounceSamples);
		}

		private void selectColumn(final int mask) {
			lastColumnMask = mask;
			selectedColumn = mask == 0 ? -1 : Integer.numberOfTrailingZeros(mask);
		}

		private int readRows() {
			return rows[selectedColumn];
		}
	}
}

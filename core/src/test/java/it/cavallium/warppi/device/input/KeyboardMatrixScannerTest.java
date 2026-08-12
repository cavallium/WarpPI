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

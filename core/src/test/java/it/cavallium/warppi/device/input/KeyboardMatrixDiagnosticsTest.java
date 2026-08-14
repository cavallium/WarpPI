package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KeyboardMatrixDiagnosticsTest {

	@Test
	void separatesRawSamplesFromAcceptedDebouncedTransitions() {
		final KeyboardMatrixDiagnostics diagnostics = new KeyboardMatrixDiagnostics();
		diagnostics.scanningStarted();

		diagnostics.recordSample(5, 1 << 1);
		var snapshot = diagnostics.snapshot();
		assertTrue(snapshot.scannerRunning());
		assertTrue(snapshot.isRawPressed(1, 5));
		assertTrue(snapshot.hasRawActivity(1, 5));
		assertFalse(snapshot.isAcceptedPressed(1, 5));
		assertFalse(snapshot.hasAcceptedActivity(1, 5));
		assertEquals(1, snapshot.rawPressCount(1, 5));
		assertEquals(0, snapshot.acceptedPressCount(1, 5));
		assertEquals(1, snapshot.lastRawRow());
		assertEquals(5, snapshot.lastRawColumn());

		diagnostics.recordTransition(1, 5, true);
		snapshot = diagnostics.snapshot();
		assertTrue(snapshot.isAcceptedPressed(1, 5));
		assertTrue(snapshot.hasAcceptedActivity(1, 5));
		assertEquals(1, snapshot.acceptedPressCount(1, 5));

		diagnostics.recordSample(5, 0);
		diagnostics.recordTransition(1, 5, false);
		snapshot = diagnostics.snapshot();
		assertFalse(snapshot.isRawPressed(1, 5));
		assertFalse(snapshot.isAcceptedPressed(1, 5));
		assertTrue(snapshot.hasRawActivity(1, 5));
		assertTrue(snapshot.hasAcceptedActivity(1, 5));
	}

	@Test
	void recordsReportedSwitchCoordinatesWithoutColumnAliasing() {
		final KeyboardMatrixDiagnostics diagnostics = new KeyboardMatrixDiagnostics();
		diagnostics.scanningStarted();
		diagnostics.recordSample(3, 1 << 2 | 1 << 3); // S34, S44
		diagnostics.recordSample(4, 1 << 2); // S35
		diagnostics.recordSample(5, 1 << 1); // S26
		diagnostics.recordSample(6, 1 << 2 | 1 << 3); // S37, S47

		final var snapshot = diagnostics.snapshot();
		assertTrue(snapshot.isRawPressed(1, 5));
		assertTrue(snapshot.isRawPressed(2, 3));
		assertTrue(snapshot.isRawPressed(3, 3));
		assertTrue(snapshot.isRawPressed(2, 4));
		assertTrue(snapshot.isRawPressed(2, 6));
		assertTrue(snapshot.isRawPressed(3, 6));
		assertFalse(snapshot.isRawPressed(1, 3));
		assertFalse(snapshot.isRawPressed(3, 5));
	}

	@Test
	void countsCompleteScansAndClearsOnlyAccumulatedEvidence() {
		final KeyboardMatrixDiagnostics diagnostics = new KeyboardMatrixDiagnostics();
		diagnostics.scanningStarted();
		for (int column = 0; column < KeyboardMatrixDiagnostics.MATRIX_SIZE; column++)
			diagnostics.recordSample(column, column == 7 ? 1 : 0);
		diagnostics.recordTransition(0, 7, true);

		assertEquals(1, diagnostics.snapshot().scanCount());
		diagnostics.clear();

		final var snapshot = diagnostics.snapshot();
		assertTrue(snapshot.isRawPressed(0, 7));
		assertTrue(snapshot.isAcceptedPressed(0, 7));
		assertFalse(snapshot.hasRawActivity(0, 7));
		assertFalse(snapshot.hasAcceptedActivity(0, 7));
		assertEquals(0, snapshot.rawPressCount(0, 7));
		assertEquals(0, snapshot.acceptedPressCount(0, 7));
		assertEquals(0, snapshot.scanCount());

		diagnostics.scanningStopped();
		assertFalse(diagnostics.snapshot().scannerRunning());
		assertFalse(diagnostics.snapshot().isRawPressed(0, 7));
	}
}

package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.device.input.KeyboardMatrixDiagnostics.Snapshot;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.screens.KeyboardDebugScreen.SignalState;

class KeyboardDebugScreenTest {

	@Test
	void mapsTheReportedSwitchesToRealMatrixCells() {
		assertSwitch(1, 5, "S26");
		assertSwitch(2, 3, "S34");
		assertSwitch(3, 3, "S44");
		assertSwitch(2, 4, "S35");
		assertSwitch(2, 6, "S37");
		assertSwitch(3, 6, "S47");
		assertNull(KeyboardDebugScreen.physicalKeyAt(0, 2));
	}

	@Test
	void distinguishesUnseenRawOnlyAcceptedAndHeldSignals() {
		final long s26 = bit(1, 5);
		final long s34 = bit(2, 3);
		final long s44 = bit(3, 3);
		final int[] rawCounts = new int[64];
		final int[] acceptedCounts = new int[64];
		rawCounts[index(1, 5)] = 2;
		rawCounts[index(2, 3)] = 1;
		acceptedCounts[index(2, 3)] = 1;
		final Snapshot snapshot = new Snapshot(true, s44, s44, s26 | s34 | s44,
				s34 | s44, 20, 1, index(3, 3), true, 1, index(3, 3), true, 1,
				rawCounts, acceptedCounts, 2);

		assertEquals(SignalState.RAW_ONLY, KeyboardDebugScreen.signalState(snapshot, 1, 5));
		assertEquals(SignalState.ACCEPTED, KeyboardDebugScreen.signalState(snapshot, 2, 3));
		assertEquals(SignalState.ACCEPTED_HELD, KeyboardDebugScreen.signalState(snapshot, 3, 3));
		assertEquals(SignalState.UNSEEN, KeyboardDebugScreen.signalState(snapshot, 2, 4));
		assertEquals(SignalState.UNUSED, KeyboardDebugScreen.signalState(snapshot, 0, 2));
		assertEquals(2, KeyboardDebugScreen.acceptedPhysicalSwitchCount(snapshot));
		assertEquals(1, KeyboardDebugScreen.rawOnlyPhysicalSwitchCount(snapshot));
	}

	@Test
	void f1ClearsCountersAndIsConsumed() {
		final KeyboardDebugScreen screen = new KeyboardDebugScreen();
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.F1)));
	}

	private static void assertSwitch(final int row, final int column, final String label) {
		assertNotNull(KeyboardDebugScreen.physicalKeyAt(row, column));
		assertEquals(label, KeyboardDebugScreen.physicalKeyAt(row, column).switchLabel());
	}

	private static long bit(final int row, final int column) {
		return 1L << index(row, column);
	}

	private static int index(final int row, final int column) {
		return row * 8 + column;
	}
}

package it.cavallium.warppi.gui.graphicengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RendererFrameDiagnosticsTest {

	@Test
	void derivesDisplayValuesWithoutChangingTheRawCounters() {
		final RendererFrameDiagnostics diagnostics = new RendererFrameDiagnostics(
				true, "TEST", 7, 12, 3, 2, 1_024, 128, 2_500_000);

		assertEquals(72, diagnostics.vertices());
		assertEquals(2.5d, diagnostics.cpuSubmissionMillis(), 0.0001d);
		assertFalse(RendererFrameDiagnostics.UNAVAILABLE.available());
	}

	@Test
	void rejectsInvalidCounterSnapshots() {
		assertThrows(IllegalArgumentException.class,
				() -> new RendererFrameDiagnostics(true, "", 0, 0, 0, 0, 0, 0, 0));
		assertThrows(IllegalArgumentException.class,
				() -> new RendererFrameDiagnostics(true, "TEST", 0, -1, 0, 0, 0, 0, 0));
	}
}

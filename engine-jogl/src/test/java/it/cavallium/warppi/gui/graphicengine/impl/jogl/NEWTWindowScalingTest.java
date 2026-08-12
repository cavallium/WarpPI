package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NEWTWindowScalingTest {
	@Test
	void calculatorZoomDoesNotParticipateInWindowSizing() {
		assertEquals(new NEWTWindow.WindowUnits(480, 584),
				NEWTWindow.windowUnitsFor(480, 320, 1f, 1f, true));
	}

	@Test
	void desktopScaleCompensatesOnlyForScaleMissingFromTheJoglSurface() {
		assertEquals(2f, NEWTWindow.windowCompensation(2f, 1f));
		assertEquals(1f, NEWTWindow.windowCompensation(2f, 2f));
		assertEquals(new NEWTWindow.WindowUnits(960, 1168),
				NEWTWindow.windowUnitsFor(480, 320, 2f, 2f, true));
		assertEquals(new NEWTWindow.WindowUnits(480, 584),
				NEWTWindow.windowUnitsFor(480, 320, 1f, 1f, true));
	}

	@Test
	void calculatorZoomChangesOnlyTheDisplayViewport() {
		assertLayout(NEWTWindow.calculateSurfaceLayout(480, 584, 1f, 1f, 1f, true),
				480, 320, 480, 320, 264);
		assertLayout(NEWTWindow.calculateSurfaceLayout(960, 1168, 1f, 2f, 2f, true),
				480, 320, 480, 320, 528);
		assertLayout(NEWTWindow.calculateSurfaceLayout(480, 584, 2f, 1f, 1f, true),
				480, 320, 240, 160, 264);
		assertLayout(NEWTWindow.calculateSurfaceLayout(960, 1168, 2f, 2f, 2f, true),
				480, 320, 240, 160, 528);
	}

	@Test
	void invalidScaleCannotSilentlyCorruptTheLogicalSize() {
		assertThrows(IllegalArgumentException.class,
				() -> NEWTWindow.calculateSurfaceLayout(480, 584, 1f, 0f, 1f, true));
		assertThrows(IllegalArgumentException.class,
				() -> NEWTWindow.windowUnitsFor(480, 320, Float.NaN, 1f, true));
	}

	private static void assertLayout(final NEWTWindow.SurfaceLayout layout,
			final int windowLogicalWidth, final int windowLogicalHeight,
			final int logicalWidth, final int logicalHeight, final int keyboardSurfaceHeight) {
		assertEquals(windowLogicalWidth, layout.windowLogicalWidth());
		assertEquals(windowLogicalHeight, layout.windowLogicalHeight());
		assertEquals(logicalWidth, layout.logicalWidth());
		assertEquals(logicalHeight, layout.logicalHeight());
		assertEquals(keyboardSurfaceHeight, layout.keyboardSurfaceHeight());
	}
}

package it.cavallium.warppi.gui.graphicengine.impl.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Dimension;

import org.junit.jupiter.api.Test;

class SwingScalingTest {
	@Test
	void calculatorZoomChangesLogicalViewportWithoutChangingWindowUnits() {
		assertEquals(new Dimension(480, 584), SwingScaling.toUserSize(480, 584, 1f));
		assertEquals(480, SwingScaling.toLogicalUnits(480, 1f, 1f));
		assertEquals(240, SwingScaling.toLogicalUnits(480, 2f, 1f));
	}

	@Test
	void deviceScaleIsAppliedAfterAndOnTopOfCalculatorZoom() {
		final float compensation = SwingScaling.toolkitCompensation(2f, 2f);
		final Dimension swingUserSize = SwingScaling.toUserSize(480, 584, compensation);

		assertEquals(new Dimension(480, 584), swingUserSize);
		assertEquals(240, SwingScaling.toLogicalUnits(swingUserSize.width, 2f, compensation));
		assertEquals(4d, SwingScaling.effectivePhysicalScale(2f, compensation, 2d));
		assertEquals(960d, swingUserSize.width * 2d);
		assertEquals(1168d, swingUserSize.height * 2d);
	}

	@Test
	void missingToolkitScaleIsCompensatedExactlyOnce() {
		final float compensation = SwingScaling.toolkitCompensation(2f, 1f);

		assertEquals(2f, compensation);
		assertEquals(new Dimension(960, 1168), SwingScaling.toUserSize(480, 584, compensation));
		assertEquals(2d, SwingScaling.effectivePhysicalScale(1f, compensation, 1d));
	}

	@Test
	void invalidScaleCannotBeUsedForSizing() {
		assertThrows(IllegalArgumentException.class, () -> SwingScaling.toUserUnits(480, 0f));
		assertThrows(IllegalArgumentException.class,
				() -> SwingScaling.effectivePhysicalScale(1f, 1f, 0d));
	}
}

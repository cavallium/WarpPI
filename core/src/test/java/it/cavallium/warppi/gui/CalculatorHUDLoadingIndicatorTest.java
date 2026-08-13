package it.cavallium.warppi.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.graphicengine.impl.common.ReboundedRenderer;
import it.cavallium.warppi.gui.graphicengine.impl.nogui.NoGuiEngine;

class CalculatorHUDLoadingIndicatorTest {

	@Test
	void rendersACompactStaticHourglassWithoutUsingTheSkin() {
		final RecordingRenderer renderer = new RecordingRenderer();

		CalculatorHUD.renderLoadingIndicator(renderer, 41, 4);

		assertEquals(5, renderer.fillCount);
		assertArrayEquals(new int[] { 41, 42, 45, 42, 41 }, renderer.x);
		assertArrayEquals(new int[] { 4, 6, 8, 12, 14 }, renderer.y);
		assertArrayEquals(new int[] { 10, 8, 2, 8, 10 }, renderer.width);
		assertArrayEquals(new int[] { 2, 2, 4, 2, 2 }, renderer.height);
		assertArrayEquals(new int[] { 0, 0, 0, 0, 0 }, renderer.colors);
		assertEquals(0xFFFFFF, renderer.currentColor);
	}

	private static final class RecordingRenderer extends ReboundedRenderer {
		private final int[] colors = new int[5];
		private final int[] x = new int[5];
		private final int[] y = new int[5];
		private final int[] width = new int[5];
		private final int[] height = new int[5];
		private int currentColor;
		private int fillCount;

		private RecordingRenderer() {
			super(new NoGuiEngine().getRenderer(), 0, 0, 480, 320);
		}

		@Override
		public void glColor3i(final int red, final int green, final int blue) {
			currentColor = red << 16 | green << 8 | blue;
		}

		@Override
		public void glFillColor(final float left, final float top,
				final float fillWidth, final float fillHeight) {
			colors[fillCount] = currentColor;
			x[fillCount] = Math.round(left);
			y[fillCount] = Math.round(top);
			width[fillCount] = Math.round(fillWidth);
			height[fillCount] = Math.round(fillHeight);
			fillCount++;
		}
	}
}

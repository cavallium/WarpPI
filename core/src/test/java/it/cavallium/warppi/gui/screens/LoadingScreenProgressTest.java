package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.graphicengine.impl.common.ReboundedRenderer;
import it.cavallium.warppi.gui.graphicengine.impl.nogui.NoGuiEngine;

class LoadingScreenProgressTest {

	@Test
	void mapsProgressToTheCompactBarWidth() {
		assertEquals(0, LoadingScreen.progressBarFillWidth(-1));
		assertEquals(16, LoadingScreen.progressBarFillWidth(25));
		assertEquals(32, LoadingScreen.progressBarFillWidth(50));
		assertEquals(63, LoadingScreen.progressBarFillWidth(99));
		assertEquals(64, LoadingScreen.progressBarFillWidth(100));
		assertEquals(64, LoadingScreen.progressBarFillWidth(101));
	}

	@Test
	void rendersOnlyATrackAndTheCompletedPart() {
		final RecordingRenderer renderer = new RecordingRenderer();

		LoadingScreen.renderProgressBar(480, 320, renderer, 50);

		assertEquals(2, renderer.fillCount);
		assertEquals(0x9A9A8E, renderer.colors[0]);
		assertEquals(208, renderer.x[0]);
		assertEquals(222, renderer.y[0]);
		assertEquals(64, renderer.width[0]);
		assertEquals(3, renderer.height[0]);
		assertEquals(0, renderer.colors[1]);
		assertEquals(32, renderer.width[1]);
		assertEquals(0xFFFFFF, renderer.currentColor);
	}

	private static final class RecordingRenderer extends ReboundedRenderer {
		private final int[] colors = new int[2];
		private final int[] x = new int[2];
		private final int[] y = new int[2];
		private final int[] width = new int[2];
		private final int[] height = new int[2];
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

package it.cavallium.warppi.gui.graphicengine.impl.swing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SwingRendererClippingTest {

	private static final int RED = 0xFFFF0000;
	private static final int GREEN = 0xFF00FF00;

	private SwingRenderer renderer;

	@BeforeEach
	void createFramebuffer() {
		renderer = new SwingRenderer();
		renderer.size = new int[] { 4, 2 };
		renderer.glColor(0xFFFFFFFF);
		SwingRenderer.canvas2d = new int[8];
	}

	@Test
	void skinPixelsPastTheRightEdgeAreClippedInsteadOfWrappingToTheNextRow() {
		renderer.drawSkinPixels(new int[] { RED }, 1, 1,
				3, 0, 5, 1, 0, 0, 1, 1, false);

		assertArrayEquals(new int[] { 0, 0, 0, RED, 0, 0, 0, 0 },
				SwingRenderer.canvas2d);
	}

	@Test
	void clippingKeepsTheMatchingSourcePixelAtEveryFramebufferEdge() {
		final int[] twoByTwoSkin = { RED, GREEN, GREEN, RED };

		renderer.drawSkinPixels(twoByTwoSkin, 2, 2,
				-1, -1, 1, 1, 0, 0, 2, 2, false);
		assertArrayEquals(new int[] { RED, 0, 0, 0, 0, 0, 0, 0 },
				SwingRenderer.canvas2d);

		Arrays.fill(SwingRenderer.canvas2d, 0);
		renderer.drawSkinPixels(twoByTwoSkin, 2, 2,
				3, 1, 5, 3, 0, 0, 2, 2, false);
		assertArrayEquals(new int[] { 0, 0, 0, 0, 0, 0, 0, RED },
				SwingRenderer.canvas2d);
	}
}

package it.cavallium.warppi.gui.graphicengine.impl.swing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.DataBufferInt;

import org.junit.jupiter.api.Test;

class SwingFramebufferTest {

	@Test
	void initialFramebufferUsesTheRequestedViewportAndDisplayedImageStorage() {
		final SwingEngine engine = new SwingEngine(480, 320);
		engine.r = new SwingRenderer();

		engine.initializeFramebuffer(480, 320);

		assertArrayEquals(new int[] { 480, 320 }, engine.getSize());
		assertEquals(480, engine.g.getWidth());
		assertEquals(320, engine.g.getHeight());
		final int[] displayedPixels = ((DataBufferInt) engine.g.getRaster()
				.getDataBuffer()).getData();
		assertSame(displayedPixels, SwingRenderer.canvas2d);

		engine.r.glClearColor(0xFFF8E0B0);
		engine.r.glClear(480, 320);
		assertEquals(0xFFF8E0B0, displayedPixels[0]);
		assertEquals(0xFFF8E0B0, displayedPixels[displayedPixels.length - 1]);
	}
}

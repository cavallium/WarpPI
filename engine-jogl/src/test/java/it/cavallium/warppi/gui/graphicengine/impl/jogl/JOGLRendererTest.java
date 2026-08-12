package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.Test;

class JOGLRendererTest {

	@Test
	void reusesColorStorage() {
		final JOGLRenderer renderer = new JOGLRenderer();
		final float[] colorStorage = renderer.currentColor;
		final float[] clearColorStorage = renderer.currentClearColorARGBf;

		renderer.glColor4i(255, 128, 0, 64);
		renderer.glClearColor4i(10, 20, 30, 40);

		assertSame(colorStorage, renderer.currentColor);
		assertSame(clearColorStorage, renderer.currentClearColorARGBf);
		for (int vertex = 0; vertex < 6; vertex++) {
			final int offset = vertex * 4;
			assertEquals(1.0f, colorStorage[offset]);
			assertEquals(128 / 255f, colorStorage[offset + 1]);
			assertEquals(0.0f, colorStorage[offset + 2]);
			assertEquals(64 / 255f, colorStorage[offset + 3]);
		}
		assertArrayEquals(new float[] { 40 / 255f, 10 / 255f, 20 / 255f, 30 / 255f }, clearColorStorage);
	}

	@Test
	void writesUntexturedQuadsDirectlyIntoReusableBuffers() {
		final JOGLRenderer renderer = new JOGLRenderer();
		renderer.fbVertices = FloatBuffer.allocate(18);
		renderer.fbTextures = FloatBuffer.allocate(12);
		renderer.fbColors = FloatBuffer.allocate(24);
		renderer.glColor4f(0.1f, 0.2f, 0.3f, 0.4f);

		renderer.glFillColor(2, 3, 5, 7);

		assertEquals(1, renderer.fbElements);
		assertArrayEquals(new float[] {
				2, 10, 0, 7, 10, 0, 2, 3, 0,
				2, 3, 0, 7, 10, 0, 7, 3, 0
		}, written(renderer.fbVertices));
		assertArrayEquals(new float[] {
				0, 0, 1, 0, 0, 1,
				0, 1, 1, 0, 1, 1
		}, written(renderer.fbTextures));
		assertArrayEquals(renderer.currentColor, written(renderer.fbColors));
	}

	private static float[] written(final FloatBuffer buffer) {
		final float[] values = new float[buffer.position()];
		buffer.flip();
		buffer.get(values);
		return values;
	}
}

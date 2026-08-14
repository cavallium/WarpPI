package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;

class JOGLRendererTest {

	@Test
	void opensRootClasspathTextureWithoutLeadingSlash() throws IOException {
		final JOGLRenderer.OpenedTextureData texture = JOGLRenderer.openTexture("test.png", true);
		try {
			assertEquals(64, texture.w);
			assertEquals(64, texture.h);
		} finally {
			Files.deleteIfExists(texture.f.toPath());
		}
	}

	@Test
	void reportsMissingClasspathTextureAsIOException() {
		assertThrows(IOException.class,
				() -> JOGLRenderer.openTexture("missing-optional-debug-texture.png", true));
	}

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

	@Test
	void clearingASkinAlsoDisablesTexturing() {
		final JOGLRenderer renderer = new JOGLRenderer();
		renderer.currentTexEnabled = true;

		renderer.glClearSkin();

		assertFalse(renderer.currentTexEnabled);
	}

	@Test
	void restoresSelectedFontTextureAfterSkinClear() throws IOException {
		final JOGLRenderer renderer = new JOGLRenderer();
		final JOGLFont font = new JOGLFont("smallest");
		font.texture = new Texture(GL.GL_TEXTURE_2D);
		font.textureW = 64;
		font.textureH = 32;
		renderer.currentFont = font;

		renderer.glClearSkin();
		renderer.selectCurrentFontTexture();

		assertSame(font.texture, renderer.currentTex);
		assertEquals(64, renderer.currentTexWidth);
		assertEquals(32, renderer.currentTexHeight);
	}

	@Test
	void normalizesReverseLineCoordinates() {
		final JOGLRenderer renderer = rendererWithOneQuadBuffers();
		renderer.glColor4f(1f, 1f, 1f, 1f);

		renderer.glDrawLine(7, 10, 2, 3);

		assertArrayEquals(new float[] {
				2, 11, 0, 8, 11, 0, 2, 3, 0,
				2, 3, 0, 8, 11, 0, 8, 3, 0
		}, written(renderer.fbVertices));
	}

	@Test
	void reportsSubmittedQuadsOnlyWhenDiagnosticsAreRequested() {
		final JOGLRenderer renderer = rendererWithOneQuadBuffers();
		renderer.beginFrameDiagnostics();
		renderer.glColor4f(1f, 1f, 1f, 1f);
		renderer.glFillColor(1, 2, 3, 4);
		renderer.endFrameDiagnostics();

		final var diagnostics = renderer.getFrameDiagnostics();
		assertTrue(diagnostics.available());
		assertEquals("JOGL GL2ES1", diagnostics.backend());
		assertEquals(1, diagnostics.frameNumber());
		assertEquals(1, diagnostics.quads());
		assertEquals(6, diagnostics.vertices());
		assertEquals(128, diagnostics.maxBatchQuads());
	}

	private static JOGLRenderer rendererWithOneQuadBuffers() {
		final JOGLRenderer renderer = new JOGLRenderer();
		renderer.fbVertices = FloatBuffer.allocate(18);
		renderer.fbTextures = FloatBuffer.allocate(12);
		renderer.fbColors = FloatBuffer.allocate(24);
		return renderer;
	}

	private static float[] written(final FloatBuffer buffer) {
		final float[] values = new float[buffer.position()];
		buffer.flip();
		buffer.get(values);
		return values;
	}
}

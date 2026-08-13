package it.cavallium.warppi.gui.graphicengine.impl.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

class ClippedRendererTest {

	@Test
	void clipsSolidAndTexturedRectanglesAndAdjustsTextureCoordinates() {
		final RecordingRenderer target = new RecordingRenderer();
		final Renderer viewport = target.getClippedInstance(10, 20, 40, 30);

		viewport.glFillColor(-5, 3, 12, 8);
		viewport.glFillRect(35, 25, 10, 10, 100, 200, 20, 40);

		assertEquals(List.of(
				"fill:10.0,23.0,7.0,8.0",
				"texture:45.0,45.0,5.0,5.0:100.0,200.0,10.0,20.0"), target.events);
	}

	@Test
	void clipsLinesAndOnlyEmitsFullyVisibleTextRows() {
		final RecordingRenderer target = new RecordingRenderer();
		final Renderer viewport = target.getClippedInstance(10, 20, 16, 8);

		viewport.glDrawLine(-5, 4, 20, 4);
		viewport.glDrawStringLeft(-8, 1, "ABCDE");
		viewport.glDrawStringLeft(0, 5, "HIDDEN");

		assertEquals(List.of(
				"line:10.0,24.0,25.0,24.0",
				"text:10.0,21.0:CDE"), target.events);
	}

	@Test
	void nestedViewportsKeepTheIntersectionOfBothClipRectangles() {
		final RecordingRenderer target = new RecordingRenderer();
		final Renderer viewport = target.getClippedInstance(10, 20, 20, 12)
				.getClippedInstance(15, 5, 20, 12);

		viewport.glFillColor(0, 0, 20, 12);

		assertEquals(List.of("fill:25.0,25.0,5.0,7.0"), target.events);
	}

	private static final class RecordingRenderer implements Renderer {
		private final List<String> events = new ArrayList<>();
		private final BinaryFont font = new BinaryFont() {
			@Override public int getStringWidth(final String text) { return text.length() * 4; }
			@Override public int getCharacterWidth() { return 4; }
			@Override public int getCharacterHeight() { return 4; }
			@Override public void use(final it.cavallium.warppi.device.display.DisplayOutputDevice display) {}
			@Override public void load(final String file) {}
			@Override public boolean isInitialized() { return true; }
			@Override public void initialize(final it.cavallium.warppi.device.display.DisplayOutputDevice display) {}
			@Override public int getSkinWidth() { return 0; }
			@Override public int getSkinHeight() { return 0; }
		};

		@Override public void glFillColor(final float x, final float y, final float width, final float height) {
			events.add("fill:" + x + "," + y + "," + width + "," + height);
		}
		@Override public void glFillRect(final float x, final float y, final float width, final float height,
				final float uvX, final float uvY, final float uvWidth, final float uvHeight) {
			events.add("texture:" + x + "," + y + "," + width + "," + height
					+ ":" + uvX + "," + uvY + "," + uvWidth + "," + uvHeight);
		}
		@Override public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {
			events.add("line:" + x0 + "," + y0 + "," + x1 + "," + y1);
		}
		@Override public void glDrawStringLeft(final float x, final float y, final String text) {
			events.add("text:" + x + "," + y + ":" + text);
		}
		@Override public BinaryFont getCurrentFont() { return font; }
		@Override public void glColor3i(final int r, final int g, final int b) {}
		@Override public void glColor(final int color) {}
		@Override public void glColor4i(final int red, final int green, final int blue, final int alpha) {}
		@Override public void glColor3f(final float red, final float green, final float blue) {}
		@Override public void glColor4f(final float red, final float green, final float blue, final float alpha) {}
		@Override public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {}
		@Override public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {}
		@Override public int glGetClearColor() { return 0; }
		@Override public void glClearColor(final int color) {}
		@Override public void glClear(final int width, final int height) {}
		@Override public void glDrawCharLeft(final int x, final int y, final char character) {}
		@Override public void glDrawCharCenter(final int x, final int y, final char character) {}
		@Override public void glDrawCharRight(final int x, final int y, final char character) {}
		@Override public void glDrawStringCenter(final float x, final float y, final String text) {}
		@Override public void glDrawStringRight(final float x, final float y, final String text) {}
		@Override public void glClearSkin() {}
	}
}

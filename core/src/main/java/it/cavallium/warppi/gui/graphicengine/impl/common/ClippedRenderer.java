package it.cavallium.warppi.gui.graphicengine.impl.common;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RendererFrameDiagnostics;

/**
 * Translates local drawing coordinates and clips every primitive to a rectangular viewport.
 */
public final class ClippedRenderer implements Renderer {
	private final Renderer renderer;
	private final int dx;
	private final int dy;
	private final int width;
	private final int height;

	public ClippedRenderer(final Renderer renderer, final int dx, final int dy, final int width, final int height) {
		this.renderer = renderer;
		this.dx = dx;
		this.dy = dy;
		this.width = Math.max(0, width);
		this.height = Math.max(0, height);
	}

	@Override
	public void glColor3i(final int r, final int g, final int b) {
		renderer.glColor3i(r, g, b);
	}

	@Override
	public void glColor(final int color) {
		renderer.glColor(color);
	}

	@Override
	public void glColor4i(final int red, final int green, final int blue, final int alpha) {
		renderer.glColor4i(red, green, blue, alpha);
	}

	@Override
	public void glColor3f(final float red, final float green, final float blue) {
		renderer.glColor3f(red, green, blue);
	}

	@Override
	public void glColor4f(final float red, final float green, final float blue, final float alpha) {
		renderer.glColor4f(red, green, blue, alpha);
	}

	@Override
	public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {
		renderer.glClearColor4i(red, green, blue, alpha);
	}

	@Override
	public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {
		renderer.glClearColor4f(red, green, blue, alpha);
	}

	@Override
	public int glGetClearColor() {
		return renderer.glGetClearColor();
	}

	@Override
	public void glClearColor(final int color) {
		renderer.glClearColor(color);
	}

	@Override
	public void glClear(final int screenWidth, final int screenHeight) {
		glFillColor(0, 0, Math.min(screenWidth, width), Math.min(screenHeight, height));
	}

	@Override
	public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {
		final float[] clipped = clipLine(x0, y0, x1, y1);
		if (clipped != null) {
			renderer.glDrawLine(dx + clipped[0], dy + clipped[1], dx + clipped[2], dy + clipped[3]);
		}
	}

	@Override
	public void glFillRect(final float x, final float y, final float rectWidth, final float rectHeight,
			final float uvX, final float uvY, final float uvWidth, final float uvHeight) {
		if (rectWidth <= 0 || rectHeight <= 0) {
			return;
		}
		final float left = Math.max(0, x);
		final float top = Math.max(0, y);
		final float right = Math.min(width, x + rectWidth);
		final float bottom = Math.min(height, y + rectHeight);
		if (left >= right || top >= bottom) {
			return;
		}
		final float clippedUvX = uvX + uvWidth * (left - x) / rectWidth;
		final float clippedUvY = uvY + uvHeight * (top - y) / rectHeight;
		final float clippedUvWidth = uvWidth * (right - left) / rectWidth;
		final float clippedUvHeight = uvHeight * (bottom - top) / rectHeight;
		renderer.glFillRect(dx + left, dy + top, right - left, bottom - top,
				clippedUvX, clippedUvY, clippedUvWidth, clippedUvHeight);
	}

	@Override
	public void glFillColor(final float x, final float y, final float rectWidth, final float rectHeight) {
		if (rectWidth <= 0 || rectHeight <= 0) {
			return;
		}
		final float left = Math.max(0, x);
		final float top = Math.max(0, y);
		final float right = Math.min(width, x + rectWidth);
		final float bottom = Math.min(height, y + rectHeight);
		if (left < right && top < bottom) {
			renderer.glFillColor(dx + left, dy + top, right - left, bottom - top);
		}
	}

	@Override
	public void glDrawCharLeft(final int x, final int y, final char character) {
		glDrawStringLeft(x, y, String.valueOf(character));
	}

	@Override
	public void glDrawCharCenter(final int x, final int y, final char character) {
		glDrawStringCenter(x, y, String.valueOf(character));
	}

	@Override
	public void glDrawCharRight(final int x, final int y, final char character) {
		glDrawStringRight(x, y, String.valueOf(character));
	}

	@Override
	public void glDrawStringLeft(final float x, final float y, final String text) {
		if (text == null || text.isEmpty() || renderer.getCurrentFont() == null) {
			return;
		}
		final BinaryFont font = renderer.getCurrentFont();
		final int characterWidth = Math.max(1, font.getCharacterWidth());
		final int characterHeight = font.getCharacterHeight();
		if (y < 0 || y + characterHeight > height) {
			return;
		}
		final int firstCharacter = Math.max(0, (int) Math.ceil(-x / characterWidth));
		final int lastCharacter = Math.min(text.length(), (int) Math.floor((width - x) / characterWidth));
		if (firstCharacter >= lastCharacter) {
			return;
		}
		final String visibleText = text.substring(firstCharacter, lastCharacter);
		renderer.glDrawStringLeft(dx + x + (float) firstCharacter * characterWidth, dy + y, visibleText);
	}

	@Override
	public void glDrawStringCenter(final float x, final float y, final String text) {
		final BinaryFont font = renderer.getCurrentFont();
		if (font != null) {
			glDrawStringLeft(x - font.getStringWidth(text) / 2f, y, text);
		}
	}

	@Override
	public void glDrawStringRight(final float x, final float y, final String text) {
		final BinaryFont font = renderer.getCurrentFont();
		if (font != null) {
			glDrawStringLeft(x - font.getStringWidth(text), y, text);
		}
	}

	@Override
	public void glClearSkin() {
		renderer.glClearSkin();
	}

	@Override
	public BinaryFont getCurrentFont() {
		return renderer.getCurrentFont();
	}

	@Override
	public RendererFrameDiagnostics getFrameDiagnostics() {
		return renderer.getFrameDiagnostics();
	}

	@Override
	public Renderer getBoundedInstance(final int childX, final int childY,
			final int childWidth, final int childHeight) {
		return new ClippedRenderer(this, childX, childY, childWidth, childHeight);
	}

	@Override
	public Renderer getClippedInstance(final int childX, final int childY,
			final int childWidth, final int childHeight) {
		return new ClippedRenderer(this, childX, childY, childWidth, childHeight);
	}

	private float[] clipLine(final float x0, final float y0, final float x1, final float y1) {
		if (width <= 0 || height <= 0) {
			return null;
		}
		if (x0 == x1 && y0 == y1) {
			return outCode(x0, y0) == 0 ? new float[] { x0, y0, x1, y1 } : null;
		}
		float startX = x0;
		float startY = y0;
		float endX = x1;
		float endY = y1;
		int startCode = outCode(startX, startY);
		int endCode = outCode(endX, endY);
		while (true) {
			if ((startCode | endCode) == 0) {
				return new float[] { startX, startY, endX, endY };
			}
			if ((startCode & endCode) != 0) {
				return null;
			}
			final int code = startCode != 0 ? startCode : endCode;
			final float deltaX = endX - startX;
			final float deltaY = endY - startY;
			final float clippedX;
			final float clippedY;
			if ((code & 8) != 0) {
				clippedY = height - 1;
				clippedX = startX + deltaX * (clippedY - startY) / deltaY;
			} else if ((code & 4) != 0) {
				clippedY = 0;
				clippedX = startX + deltaX * (clippedY - startY) / deltaY;
			} else if ((code & 2) != 0) {
				clippedX = width - 1;
				clippedY = startY + deltaY * (clippedX - startX) / deltaX;
			} else {
				clippedX = 0;
				clippedY = startY + deltaY * (clippedX - startX) / deltaX;
			}
			if (code == startCode) {
				startX = clippedX;
				startY = clippedY;
				startCode = outCode(startX, startY);
			} else {
				endX = clippedX;
				endY = clippedY;
				endCode = outCode(endX, endY);
			}
		}
	}

	private int outCode(final float x, final float y) {
		int code = 0;
		if (x < 0) {
			code |= 1;
		} else if (x >= width) {
			code |= 2;
		}
		if (y < 0) {
			code |= 4;
		} else if (y >= height) {
			code |= 8;
		}
		return code;
	}
}

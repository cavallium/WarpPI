package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.awt.FontMetrics;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class SwingRenderer implements Renderer {
	public int clearcolor = 0xFFc5c2af;
	public SwingFont currentFont;
	public SwingSkin currentSkin;
	public int color = 0xFF000000;
	public int[] size = new int[] { 1, 1 };
	static int[] canvas2d = new int[1];

	public SwingRenderer() {

	}

	@Override
	public void glColor3i(final int r, final int gg, final int b) {
		glColor4i(r, gg, b, 255);
	}

	@Override
	public void glColor(final int c) {
		color = c & 0xFFFFFFFF;
	}

	@Override
	public void glClearColor(final int c) {
		clearcolor = c & 0xFFFFFFFF;
	}

	@Override
	public void glColor4i(final int red, final int green, final int blue, final int alpha) {
		color = (alpha << 24) + (red << 16) + (green << 8) + blue;
	}

	@Override
	public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {
		clearcolor = (alpha << 24) + (red << 16) + (green << 8) + blue;
	}

	@Override
	public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {
		clearcolor = ((int) (alpha * 255) << 24) + ((int) (red * 255) << 16) + ((int) (green * 255) << 8) + (int) (blue * 255);
	}

	@Override
	public void glClear(final int screenWidth, final int screenHeight) {
		for (int x = 0; x < screenWidth; x++)
			for (int y = 0; y < screenHeight; y++) {
				final int index = x + y * size[0];
				if (index >= 0 && index < SwingRenderer.canvas2d.length)
					SwingRenderer.canvas2d[index] = clearcolor;
			}
	}

	private void glDrawSkin(final int x0, final int y0, final int x1, final int y1,
			final int s0, final int t0, final int s1, final int t1,
			final boolean transparent) {
		drawSkinPixels(currentSkin.skinData, currentSkin.skinSize[0], currentSkin.skinSize[1],
				x0, y0, x1, y1, s0, t0, s1, t1, transparent);
	}

	void drawSkinPixels(final int[] skinData, final int skinWidth, final int skinHeight,
			final int x0, final int y0, final int x1, final int y1,
			final int s0, final int t0, final int s1, final int t1,
			final boolean transparent) {
		final int destinationWidth = x1 - x0;
		final int destinationHeight = y1 - y0;
		final int sourceWidth = s1 - s0;
		final int sourceHeight = t1 - t0;
		if (destinationWidth <= 0 || destinationHeight <= 0
				|| sourceWidth == 0 || sourceHeight == 0)
			return;

		final int clippedX0 = Math.max(0, x0);
		final int clippedY0 = Math.max(0, y0);
		final int clippedX1 = Math.min(size[0], x1);
		final int clippedY1 = Math.min(size[1], y1);
		if (clippedX0 >= clippedX1 || clippedY0 >= clippedY1)
			return;

		final double incrementX = Math.abs((double) destinationWidth / sourceWidth);
		final double incrementY = Math.abs((double) destinationHeight / sourceHeight);
		final boolean flippedX = sourceWidth < 0;
		final boolean flippedY = sourceHeight < 0;
		final int sourceMinX = Math.min(s0, s1);
		final int sourceMaxX = Math.max(s0, s1);
		final int sourceMinY = Math.min(t0, t1);
		final int sourceMaxY = Math.max(t0, t1);
		final int expX = incrementX < 1d ? (int) Math.round(1d / incrementX / 2d) : 0;
		final int expY = incrementY < 1d ? (int) Math.round(1d / incrementY / 2d) : 0;
		int oldColor;
		int newColor;
		for (int destinationY = clippedY0; destinationY < clippedY1; destinationY++) {
			final int textureY = (int) ((destinationY - y0) / incrementY);
			final int skinY = flippedY ? sourceMaxY - 1 - textureY : sourceMinY + textureY;
			for (int destinationX = clippedX0; destinationX < clippedX1; destinationX++) {
				final int index = destinationX + destinationY * size[0];
				if (index >= 0 && index < SwingRenderer.canvas2d.length) {
					final int textureX = (int) ((destinationX - x0) / incrementX);
					final int skinX = flippedX ? sourceMaxX - 1 - textureX : sourceMinX + textureX;
					if (expX == 0 && expY == 0) {
						newColor = getSkinColorAt(skinData, skinWidth, skinHeight,
								skinX, skinY);
					} else {
						int samples = 0;
						int alpha = 0;
						int red = 0;
						int green = 0;
						int blue = 0;
						for (int expXi = -expX; expXi <= expX; expXi++)
							for (int expYi = -expY; expYi <= expY; expYi++) {
								final int sample = getSkinColorAt(skinData, skinWidth,
										skinHeight, skinX + expXi, skinY + expYi);
								alpha += sample >>> 24;
								red += sample >> 16 & 0xFF;
								green += sample >> 8 & 0xFF;
								blue += sample & 0xFF;
								samples++;
							}
						newColor = alpha / samples << 24 | red / samples << 16
								| green / samples << 8 | blue / samples;
					}
					if (transparent) {
						oldColor = SwingRenderer.canvas2d[index];
						final float a2 = (newColor >> 24 & 0xFF) / 255f;
						final float a1 = 1f - a2;
						final int r = (int) ((oldColor >> 16 & 0xFF) * a1 + (newColor >> 16 & 0xFF) * a2);
						final int g = (int) ((oldColor >> 8 & 0xFF) * a1 + (newColor >> 8 & 0xFF) * a2);
						final int b = (int) ((oldColor & 0xFF) * a1 + (newColor & 0xFF) * a2);
						newColor = 0xFF000000 | r << 16 | g << 8 | b;
					}

					SwingRenderer.canvas2d[index] = stackColors(SwingRenderer.canvas2d[index], newColor);
				}
			}
		}
	}

	private int stackColors(final int oldColor, final int newColor) {
		final int alpha = newColor >>> 24;
		if (alpha == 0)
			return oldColor;
		if (alpha == 0xFF)
			return newColor;
		final int inverseAlpha = 0xFF - alpha;
		final int a = ((oldColor >>> 24) * inverseAlpha + alpha * alpha) / 0xFF;
		final int r = ((oldColor >> 16 & 0xFF) * inverseAlpha
				+ (newColor >> 16 & 0xFF) * alpha) / 0xFF;
		final int g = ((oldColor >> 8 & 0xFF) * inverseAlpha
				+ (newColor >> 8 & 0xFF) * alpha) / 0xFF;
		final int b = ((oldColor & 0xFF) * inverseAlpha
				+ (newColor & 0xFF) * alpha) / 0xFF;
		return a << 24 | r << 16 | g << 8 | b;
	}

	private int getSkinColorAt(final int[] skinData, final int skinWidth,
			final int skinHeight, final int skinX, final int skinY) {
		int newColor = 0;
		if (skinX >= 0 && skinX < skinWidth && skinY >= 0 && skinY < skinHeight) {
			final int skinIndex = skinX + skinY * skinWidth;
			newColor = skinData[skinIndex];
			final int a = (int) ((newColor >> 24 & 0xFF) * ((double) (color >> 24 & 0xFF) / (double) 0xFF));
			final int r = (int) ((newColor >> 16 & 0xFF) * ((double) (color >> 16 & 0xFF) / (double) 0xFF));
			final int g = (int) ((newColor >> 8 & 0xFF) * ((double) (color >> 8 & 0xFF) / (double) 0xFF));
			final int b = (int) ((newColor & 0xFF) * ((double) (color & 0xFF) / (double) 0xFF));
			newColor = a << 24 | r << 16 | g << 8 | b;
		}
		return newColor;
	}

	@Override
	public void glDrawLine(float x0, float y0, float x1, float y1) {
		final int ix0 = (int) x0;
		final int ix1 = (int) x1;
		final int iy0 = (int) y0;
		final int iy1 = (int) y1;
		if (ix0 >= size[0] || iy0 >= size[0])
			return;
		if (iy0 == iy1) {
			for (int x = 0; x <= ix1 - ix0; x++)
				if (ix0 + x < size[0] & iy0 < size[1])
					SwingRenderer.canvas2d[ix0 + x + iy0 * size[0]] = stackColors(SwingRenderer.canvas2d[ix0 + x + iy0 * size[0]], color);
		} else if (ix0 == ix1) {
			for (int y = 0; y <= iy1 - iy0; y++)
				if (ix0 < size[0] & iy0 + y < size[1])
					SwingRenderer.canvas2d[ix0 + (iy0 + y) * size[0]] = stackColors(SwingRenderer.canvas2d[ix0 + (iy0 + y) * size[0]], color);
		} else {
			final int m = (iy1 - iy0) / (ix1 - ix0);
			for (int texx = 0; texx <= ix1 - ix0; texx++)
				if (ix0 + texx < size[0] && iy0 + m * texx < size[1])
					if (ix0 + texx < size[0] & iy0 + m * texx < size[1])
						SwingRenderer.canvas2d[ix0 + texx + (iy0 + m * texx) * size[0]] = stackColors(SwingRenderer.canvas2d[ix0 + texx + (iy0 + m * texx) * size[0]], color);
		}
	}

	@Override
	public void glFillRect(final float x, final float y, final float width, final float height, final float uvX,
			final float uvY, final float uvWidth, final float uvHeight) {
		if (currentSkin != null)
			glDrawSkin((int) x, (int) y, (int) (x + width), (int) (y + height), (int) uvX, (int) uvY, (int) (uvWidth + uvX), (int) (uvHeight + uvY), true);
		else
			glFillColor(x, y, width, height);
	}

	@Override
	public void glFillColor(float x, float y, final float width, final float height) {

		final int ix = (int) x;
		final int iy = (int) y;
		final int iw = (int) width;
		final int ih = (int) height;

		int x0 = ix;
		int y0 = iy;
		int x1 = ix + iw;
		int y1 = iy + ih;
		if (ix >= size[0] || iy >= size[0])
			return;
		if (x0 < 0)
			x0 = 0;
		if (x1 >= size[0])
			x1 = size[0];
		if (y0 < 0)
			y0 = 0;
		if (y1 >= size[1])
			y1 = size[1];
		final int sizeW = size[0];
		for (int px = x0; px < x1; px++)
			for (int py = y0; py < y1; py++) {
				final int idx = px + py * sizeW;
				if (px < sizeW && idx >= 0 && idx < SwingRenderer.canvas2d.length)
					SwingRenderer.canvas2d[idx] = stackColors(SwingRenderer.canvas2d[idx], color);
			}
	}

	@Override
	public void glDrawStringLeft(float x, float y, final String textString) {

		final int ix = (int) x;
		final int iy = (int) y;

		final int[] text = currentFont.getCharIndexes(textString);
		final int[] screen = SwingRenderer.canvas2d;
		final int[] screenSize = size;
		final int screenLength = screen.length;
		int screenPos = 0;

		int currentInt;
		int currentIntBitPosition;
		int bitData;
		int cpos;
		int j;
		final int l = text.length;
		for (int i = 0; i < l; i++) {
			cpos = i * currentFont.charW;
			final int charIndex = text[i];
			for (int dy = 0; dy < currentFont.charH; dy++)
				for (int dx = 0; dx < currentFont.charW; dx++) {
					j = ix + cpos + dx;
					if (j > 0 & j < screenSize[0]) {
						final int bit = dx + dy * currentFont.charW;
						currentInt = (int) (Math.floor(bit) / RFTFont.intBits);
						currentIntBitPosition = bit - currentInt * RFTFont.intBits;
						final int charIdx = charIndex * currentFont.charIntCount + currentInt;
						if (charIdx >= 0 && charIdx < currentFont.chars32.length) {
							bitData = currentFont.chars32[charIdx] >> currentIntBitPosition & 1;
							screenPos = ix + cpos + dx + (iy + dy) * screenSize[0];
							if (bitData == 1 & screenLength > screenPos & screenPos >= 0)
								screen[screenPos] = stackColors(screen[screenPos], color);
						}
					}
				}
		}
	}

	@Override
	public void glDrawStringCenter(final float x, final float y, final String text) {
		glDrawStringLeft(x - currentFont.getStringWidth(text) / 2, y, text);
	}

	@Override
	public void glDrawStringRight(final float x, final float y, final String text) {
		glDrawStringLeft(x - currentFont.getStringWidth(text), y, text);
	}

	@Deprecated
	public int glGetFontWidth(final FontMetrics fm, final String text) {
		return fm.stringWidth(text);
	}

	@Deprecated
	public int glGetCurrentFontHeight() {
		return currentFont.charH;
	}

	@Override
	public int glGetClearColor() {
		return clearcolor;
	}

	@Override
	public BinaryFont getCurrentFont() {
		return currentFont;
	}

	@Override
	public void glColor3f(final float red, final float green, final float blue) {
		glColor3i((int) (red * 255f), (int) (green * 255f), (int) (blue * 255f));
	}

	@Override
	public void glColor4f(final float red, final float green, final float blue, final float alpha) {
		glColor4i((int) (red * 255f), (int) (green * 255f), (int) (blue * 255f), (int) (alpha * 255f));
	}

	@Override
	public void glClearSkin() {
		currentSkin = null;
	}

	@Override
	public void glDrawCharLeft(final int x, final int y, final char ch) {
		glDrawStringLeft(x, y, ch + "");
	}

	@Override
	public void glDrawCharCenter(final int x, final int y, final char ch) {
		glDrawStringCenter(x, y, ch + "");
	}

	@Override
	public void glDrawCharRight(final int x, final int y, final char ch) {
		glDrawStringRight(x, y, ch + "");
	}

}

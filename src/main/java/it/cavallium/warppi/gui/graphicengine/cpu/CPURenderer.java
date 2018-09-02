package it.cavallium.warppi.gui.graphicengine.cpu;

import java.awt.FontMetrics;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class CPURenderer implements Renderer {
	public int clearcolor = 0xFFc5c2af;
	public CPUFont currentFont;
	public CPUSkin currentSkin;
	public int color = 0xFF000000;
	public int[] size = new int[] { 1, 1 };
	static int[] canvas2d = new int[1];

	@Override
	public void glColor3i(int r, int gg, int b) {
		glColor4i(r, gg, b, 255);
	}

	@Override
	public void glColor(int c) {
		color = c & 0xFFFFFFFF;
	}

	@Override
	public void glClearColor(int c) {
		clearcolor = c & 0xFFFFFFFF;
	}

	@Override
	public void glColor4i(int red, int green, int blue, int alpha) {
		color = (alpha << 24) + (red << 16) + (green << 8) + (blue);
	}

	@Override
	public void glClearColor4i(int red, int green, int blue, int alpha) {
		clearcolor = (alpha << 24) + (red << 16) + (green << 8) + (blue);
	}

	@Override
	public void glClearColor4f(float red, float green, float blue, float alpha) {
		clearcolor = ((int) (alpha * 255) << 24) + ((int) (red * 255) << 16) + ((int) (green * 255) << 8) + ((int) (blue * 255));
	}

	@Override
	public void glClear(int screenWidth, int screenHeight) {
		for (int x = 0; x < screenWidth; x++) {
			for (int y = 0; y < screenHeight; y++) {
				final int index = x + y * size[0];
				if (index >= 0 && index < canvas2d.length) {
					canvas2d[index] = clearcolor;
				}
			}
		}
	}

	private void glDrawSkin(int x0, int y0, int x1, int y1, int s0, int t0, int s1, int t1, boolean transparent) {
		x0 += StaticVars.screenPos[0];
		y0 += StaticVars.screenPos[1];
		final double incrementX = Math.abs((double) (x1 - x0) / (double) (s1 - s0));
		final double incrementY = Math.abs((double) (y1 - y0) / (double) (t1 - t0));
		final boolean flippedX = (x1 - x0) / (s1 - s0) < 0;
		final boolean flippedY = (y1 - y0) / (t1 - t0) < 0;
		int oldColor;
		int newColor;
		final int onex = s0 <= s1 ? 1 : -1;
		final int oney = t0 <= t1 ? 1 : -1;
		int width = 0;
		int height = 0;
		if (onex == -1) {
			final int s00 = s0;
			s0 = s1;
			s1 = s00;
			width = s1 - s0;
		}
		if (oney == -1) {
			final int t00 = t0;
			t0 = t1;
			t1 = t00;
			height = t1 - t0;
		}
		if (x0 >= size[0] || y0 >= size[0]) {
			return;
		}
		if (x0 + width >= size[0]) {
			s1 = size[0] - x0 + s0;
		}
		if (y0 + height >= size[1]) {
			t1 = size[1] - y0 + t0;
		}
		if (x0 < 0) {
			if (onex == -1) {
				width += x0;
				s1 += x0 + 1;
			} else {
				s0 -= x0;
			}
			x0 = 0;
		}
		if (y0 < 0) {
			if (oney == -1) {
				height += y0;
				t1 += y0 + 1;
			} else {
				t0 -= y0;
			}
			y0 = 0;
		}
		for (double pixelX = 0; pixelX < x1 - x0; pixelX++) {
			for (double pixelY = 0; pixelY < y1 - y0; pixelY++) {
				final int index = (int) (x0 + pixelX + (y0 + pixelY) * size[0]);
				if (index >= 0 && index < canvas2d.length && pixelX < size[0]) {
					final int texx = (int) (pixelX / incrementX);
					final int texy = (int) (pixelY / incrementY);
					int expX = 0;
					int expY = 0;
					if (incrementX < 1) {
						expX = (int) Math.round(1d / incrementX / 2d);
					}
					if (incrementY < 1) {
						expY = (int) Math.round(1d / incrementY / 2d);
					}
					final int[] newColors = new int[(1 + expX * 2) * (1 + expY * 2)];
					for (int expXi = -expX; expXi <= expX; expXi++) {
						for (int expYi = -expY; expYi <= expY; expYi++) {
							final int skinIndex = (int) (s0 + (texx * (flippedX ? -1d : 1d) + (flippedX ? -(s0 - s1) - 1 : 0) + expXi) + (t0 + (texy * (flippedY ? -1d : 1d) + (flippedY ? -(t0 - t1) - 1 : 0) + expYi)) * currentSkin.skinSize[0]);
							final int idx = (expXi + expX) + (expYi + expY) * (1 + expY * 2);
							if (idx >= 0 && idx < newColors.length) {
								newColors[idx] = getSkinColorAt(currentSkin.skinData, skinIndex);
							}
						}
					}
					newColor = joinColors(newColors);
					if (transparent) {
						oldColor = canvas2d[index];
						final float a2 = (newColor >> 24 & 0xFF) / 255f;
						final float a1 = 1f - a2;
						final int r = (int) ((oldColor >> 16 & 0xFF) * a1 + (newColor >> 16 & 0xFF) * a2);
						final int g = (int) ((oldColor >> 8 & 0xFF) * a1 + (newColor >> 8 & 0xFF) * a2);
						final int b = (int) ((oldColor & 0xFF) * a1 + (newColor & 0xFF) * a2);
						newColor = 0xFF000000 | r << 16 | g << 8 | b;
					}

					canvas2d[index] = stackColors(canvas2d[index], newColor);
				}
			}
		}
	}

	private int joinColors(int[] newColors) {
		int a = 0;
		int r = 0;
		int g = 0;
		int b = 0;
		for (final int newColor : newColors) {
			a += newColor >> 24 & 0xFF;
			r += newColor >> 16 & 0xFF;
			g += newColor >> 8 & 0xFF;
			b += newColor & 0xFF;
		}
		return (a / newColors.length) << 24 | (r / newColors.length) << 16 | (g / newColors.length) << 8 | (b / newColors.length);
	}

	private int stackColors(int... color) {
		double a = 0;
		double r = 0;
		double g = 0;
		double b = 0;
		for (final int newColor : color) {
			final double alpha = (newColor >> 24 & 0xFF) / 255d;
			a = a * (1d - alpha) + (newColor >> 24 & 0xFF) * alpha;
			r = r * (1d - alpha) + (newColor >> 16 & 0xFF) * alpha;
			g = g * (1d - alpha) + (newColor >> 8 & 0xFF) * alpha;
			b = b * (1d - alpha) + (newColor & 0xFF) * alpha;
		}
		return ((int) a) << 24 | ((int) r) << 16 | ((int) g) << 8 | ((int) b);
	}

	private int getSkinColorAt(int[] skinData, int skinIndex) {
		int newColor = 0;
		if (skinIndex >= 0 && skinIndex < skinData.length) {
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
		x0 += StaticVars.screenPos[0];
		x1 += StaticVars.screenPos[0];
		y0 += StaticVars.screenPos[1];
		y1 += StaticVars.screenPos[1];
		final int ix0 = (int) x0;
		final int ix1 = (int) x1;
		final int iy0 = (int) y0;
		final int iy1 = (int) y1;
		if (ix0 >= size[0] || iy0 >= size[0]) {
			return;
		}
		if (iy0 == iy1) {
			for (int x = 0; x <= ix1 - ix0; x++) {
				if ((ix0 + x < size[0]) & (iy0 < size[1])) {
					canvas2d[ix0 + x + iy0 * size[0]] = stackColors(canvas2d[ix0 + x + iy0 * size[0]], color);
				}
			}
		} else if (ix0 == ix1) {
			for (int y = 0; y <= iy1 - iy0; y++) {
				if ((ix0 < size[0]) & (iy0 + y < size[1])) {
					canvas2d[ix0 + (iy0 + y) * size[0]] = stackColors(canvas2d[ix0 + (iy0 + y) * size[0]], color);
				}
			}
		} else {
			final int m = (iy1 - iy0) / (ix1 - ix0);
			for (int texx = 0; texx <= ix1 - ix0; texx++) {
				if (ix0 + texx < size[0] && iy0 + (m * texx) < size[1]) {
					if ((ix0 + texx < size[0]) & ((iy0 + (m * texx)) < size[1])) {
						canvas2d[(ix0 + texx) + (iy0 + (m * texx)) * size[0]] = stackColors(canvas2d[(ix0 + texx) + (iy0 + (m * texx)) * size[0]], color);
					}
				}
			}
		}
	}

	@Override
	public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth,
			float uvHeight) {
		if (currentSkin != null) {
			glDrawSkin((int) x, (int) y, (int) (x + width), (int) (y + height), (int) uvX, (int) uvY, (int) (uvWidth + uvX), (int) (uvHeight + uvY), true);
		} else {
			glFillColor(x, y, width, height);
		}
	}

	@Override
	public void glFillColor(float x, float y, float width, float height) {
		x += StaticVars.screenPos[0];
		y += StaticVars.screenPos[1];

		final int ix = (int) x;
		final int iy = (int) y;
		final int iw = (int) width;
		final int ih = (int) height;

		int x0 = ix;
		int y0 = iy;
		int x1 = ix + iw;
		int y1 = iy + ih;
		if (ix >= size[0] || iy >= size[0]) {
			return;
		}
		if (x0 < 0) {
			x0 = 0;
		}
		if (x1 >= size[0]) {
			x1 = size[0];
		}
		if (y0 < 0) {
			y0 = 0;
		}
		if (y1 >= size[1]) {
			y1 = size[1];
		}
		final int sizeW = size[0];
		for (int px = x0; px < x1; px++) {
			for (int py = y0; py < y1; py++) {
				final int idx = (px) + (py) * sizeW;
				if (px < sizeW && idx >= 0 && idx < canvas2d.length) {
					canvas2d[idx] = stackColors(canvas2d[idx], color);
				}
			}
		}
	}

	@Override
	public void glDrawStringLeft(float x, float y, String textString) {
		x += StaticVars.screenPos[0];
		y += StaticVars.screenPos[1];

		final int ix = (int) x;
		final int iy = (int) y;

		final int[] text = currentFont.getCharIndexes(textString);
		final int[] screen = canvas2d;
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
			cpos = (i * (currentFont.charW));
			final int charIndex = text[i];
			for (int dy = 0; dy < currentFont.charH; dy++) {
				for (int dx = 0; dx < currentFont.charW; dx++) {
					j = ix + cpos + dx;
					if (j > 0 & j < screenSize[0]) {
						final int bit = dx + dy * currentFont.charW;
						currentInt = (int) (Math.floor(bit) / (CPUFont.intBits));
						currentIntBitPosition = bit - (currentInt * CPUFont.intBits);
						final int charIdx = charIndex * currentFont.charIntCount + currentInt;
						if (charIdx >= 0 && charIdx < currentFont.chars32.length) {
							bitData = (currentFont.chars32[charIdx] >> currentIntBitPosition) & 1;
							screenPos = ix + cpos + dx + (iy + dy) * screenSize[0];
							if (bitData == 1 & screenLength > screenPos & screenPos >= 0) {
								screen[screenPos] = stackColors(screen[screenPos], color);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void glDrawStringCenter(float x, float y, String text) {
		glDrawStringLeft(x - (currentFont.getStringWidth(text) / 2), y, text);
	}

	@Override
	public void glDrawStringRight(float x, float y, String text) {
		glDrawStringLeft(x - currentFont.getStringWidth(text), y, text);
	}

	@Deprecated
	public int glGetFontWidth(FontMetrics fm, String text) {
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
	public void glColor3f(float red, float green, float blue) {
		glColor3i((int) (red * 255f), (int) (green * 255f), (int) (blue * 255f));
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		glColor4i((int) (red * 255f), (int) (green * 255f), (int) (blue * 255f), (int) (alpha * 255f));
	}

	@Override
	public void glClearSkin() {
		currentSkin = null;
	}

	@Override
	public void glDrawCharLeft(int x, int y, char ch) {
		glDrawStringLeft(x, y, ch + "");
	}

	@Override
	public void glDrawCharCenter(int x, int y, char ch) {
		glDrawStringCenter(x, y, ch + "");
	}

	@Override
	public void glDrawCharRight(int x, int y, char ch) {
		glDrawStringRight(x, y, ch + "");
	}

}

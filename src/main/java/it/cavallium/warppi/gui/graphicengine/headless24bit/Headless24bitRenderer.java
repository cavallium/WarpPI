package it.cavallium.warppi.gui.graphicengine.headless24bit;

import it.cavallium.warppi.gui.graphicengine.Renderer;

public class Headless24bitRenderer implements Renderer {

	Headless24bitFont currentFont;
	public int[] size = new int[] { Headless24bitEngine.C_WIDTH * Headless24bitEngine.C_MUL_X, Headless24bitEngine.C_HEIGHT * Headless24bitEngine.C_MUL_Y };
	protected int[][] fgColorMatrixSs = new int[size[0] * size[1]][3];
	protected int[][] bgColorMatrixSs = new int[size[0] * size[1]][3];

	protected char[] charmatrix = new char[Headless24bitEngine.C_WIDTH * Headless24bitEngine.C_HEIGHT];
	protected int[][] fgColorMatrix = new int[Headless24bitEngine.C_WIDTH * Headless24bitEngine.C_HEIGHT][3];
	protected int[][] bgColorMatrix = new int[Headless24bitEngine.C_WIDTH * Headless24bitEngine.C_HEIGHT][3];
	protected int[] clearColor = rgbToIntArray(0xc5, 0xc2, 0xaf);
	protected int[] curColor = new int[] { clearColor[0], clearColor[1], clearColor[2] };
	public Headless24bitSkin currentSkin;

	public static final String ANSI_PREFIX = "\u001B[";
	public static final String ansiFgColorPrefix = "38;2;";
	public static final String ansiBgColorPrefix = "48;2;";
	public static final String ansiColorSuffix = "m";

	public static final String ANSI_RESET = "\u001B[0m";
	public static final char FILL = 0xDB;
	public static final int[] TRANSPARENT = new int[] { 0, 0, 0, 1 };

	public static int[] rgbToIntArray(int r_U, int g_U, int b_U) {
		return new int[] { r_U, g_U, b_U };
	}

	@Override
	public void glColor3i(int r, int gg, int b) {
		curColor = rgbToIntArray(r, gg, b);
	}

	@Override
	public void glColor(int c) {
		curColor = rgbToIntArray(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF);
	}

	@Override
	public void glColor4i(int red, int green, int blue, int alpha) {
		curColor = rgbToIntArray(red, green, blue);
	}

	@Override
	public void glColor3f(float red, float green, float blue) {
		curColor = rgbToIntArray((int) (red * 255), (int) (green * 255), (int) (blue * 255));
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		curColor = rgbToIntArray((int) (red * 255), (int) (green * 255), (int) (blue * 255));
	}

	@Override
	public void glClearColor4i(int red, int green, int blue, int alpha) {
		clearColor = rgbToIntArray(red, green, blue);
	}

	@Override
	public void glClearColor4f(float red, float green, float blue, float alpha) {
		clearColor = rgbToIntArray((int) (red * 255), (int) (green * 255), (int) (blue * 255));
	}

	@Override
	public int glGetClearColor() {
		return clearColor[2] << 16 | clearColor[1] << 8 | clearColor[0];
	}

	@Override
	public void glClearColor(int c) {
		clearColor = rgbToIntArray(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF);
	}

	@Override
	public void glClear(int screenWidth, int screenHeight) {
		clearAll();
	}

	@Override
	public void glDrawLine(float x1, float y1, float x2, float y2) {

		final int dx = (int) Math.abs(x2 - x1);
		final int dy = (int) Math.abs(y2 - y1);

		final int sx = (x1 < x2) ? 1 : -1;
		final int sy = (y1 < y2) ? 1 : -1;

		int err = dx - dy;

		while (true) {
			if (((int) x1) >= size[0] || ((int) y1) >= size[1] || ((int) x2) >= size[0] || ((int) y2) >= size[1]) {
				break;
			}
			bgColorMatrixSs[((int) x1) + ((int) y1) * size[0]] = curColor;
			charmatrix[((int) x1 / Headless24bitEngine.C_MUL_X) + ((int) y1 / Headless24bitEngine.C_MUL_Y) * Headless24bitEngine.C_WIDTH] = ' ';

			if (x1 == x2 && y1 == y2) {
				break;
			}

			final int e2 = 2 * err;

			if (e2 > -dy) {
				err = err - dy;
				x1 = x1 + sx;
			}

			if (e2 < dx) {
				err = err + dx;
				y1 = y1 + sy;
			}
		}
	}

	@Override
	public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth,
			float uvHeight) {
		if (currentSkin != null) {
			glDrawSkin((int) (x), (int) (y), (int) (uvX), (int) (uvY), (int) ((uvWidth + uvX)), (int) ((uvHeight + uvY)), true);
		} else {
			glFillColor(x, y, width, height);
		}
	}

	@Override
	public void glFillColor(float x, float y, float width, float height) {
		final int ix = (int) x;
		final int iy = (int) y;
		final int iw = (int) width;
		final int ih = (int) height;

		int x1 = ix + iw;
		int y1 = iy + ih;
		if (ix >= size[0] || iy >= size[1]) {
			return;
		}
		if (x1 >= size[0]) {
			x1 = size[0];
		}
		if (y1 >= size[1]) {
			y1 = size[1];
		}
		final int sizeW = size[0];
		for (int px = ix; px < x1; px++) {
			for (int py = iy; py < y1; py++) {
				drawPixelAt(' ', curColor, px, py);
				bgColorMatrixSs[(px) + (py) * sizeW] = curColor;
				charmatrix[(px / Headless24bitEngine.C_MUL_X) + (py / Headless24bitEngine.C_MUL_Y) * sizeW / Headless24bitEngine.C_MUL_X] = ' ';
			}
		}
	}

	@Override
	public void glDrawCharLeft(int x, int y, char ch) {
		final int cx = x;
		final int cy = y;
		if (cx >= size[0] || cy >= size[1]) {
			return;
		}
		charmatrix[cx / Headless24bitEngine.C_MUL_X + cy / Headless24bitEngine.C_MUL_Y * Headless24bitEngine.C_WIDTH] = ch;
		fgColorMatrixSs[cx + cy * size[0]] = curColor;
	}

	@Override
	public void glDrawCharCenter(int x, int y, char ch) {
		glDrawCharLeft(x, y, ch);
	}

	@Override
	public void glDrawCharRight(int x, int y, char ch) {
		final int cx = x - 1 * Headless24bitEngine.C_MUL_X;
		final int cy = y;
		if (cx >= size[0] || cy >= size[1]) {
			return;
		}
		charmatrix[cx / Headless24bitEngine.C_MUL_X + cy / Headless24bitEngine.C_MUL_Y * Headless24bitEngine.C_WIDTH] = ch;
		fgColorMatrixSs[cx + cy * size[0]] = curColor;
	}

	@Override
	public void glDrawStringLeft(float x, float y, String text) {
		final int cx = (int) x;
		final int cy = (int) y;
		int i = 0;
		for (final char c : text.toCharArray()) {
			if (cx + i >= size[0] || cy >= size[1]) {
				break;
			}
			charmatrix[cx / Headless24bitEngine.C_MUL_X + i + cy / Headless24bitEngine.C_MUL_Y * Headless24bitEngine.C_WIDTH] = c;
			fgColorMatrixSs[cx + i + cy * size[0]] = curColor;
			i++;
		}
	}

	@Override
	public void glDrawStringCenter(float x, float y, String text) {
		final int cx = ((int) x) - (text.length() / 2) * Headless24bitEngine.C_MUL_X;
		final int cy = ((int) y);
		int i = 0;
		for (final char c : text.toCharArray()) {
			if (cx + i >= size[0] || cy >= size[1]) {
				break;
			}
			charmatrix[cx / Headless24bitEngine.C_MUL_X + i + cy / Headless24bitEngine.C_MUL_Y * Headless24bitEngine.C_WIDTH] = c;
			fgColorMatrixSs[cx + i + cy * size[0]] = curColor;
			i++;
		}
	}

	@Override
	public void glDrawStringRight(float x, float y, String text) {
		// TODO Auto-generated method stub

	}

	private void glDrawSkin(int x0, int y0, int s0, int t0, int s1, int t1, boolean transparent) {
		int[] newColor;
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
		if (x0 >= size[0] || y0 >= size[1]) {
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
		int pixelX;
		int pixelY;
		for (int texx = 0; texx < s1 - s0; texx++) {
			for (int texy = 0; texy < t1 - t0; texy++) {
				pixelX = (x0 + texx * onex + width);
				pixelY = (y0 + texy * oney + height);
				if (pixelY < size[1]) {
					if (pixelX - (pixelX % size[0]) == 0) {
						newColor = currentSkin.skinData[(s0 + texx) + (t0 + texy) * currentSkin.skinSize[0]];
						if (transparent) {
							if (newColor.length == 3 || (newColor.length == 4 && newColor[3] != 1)) {
								charmatrix[pixelX / Headless24bitEngine.C_MUL_X + pixelY / Headless24bitEngine.C_MUL_Y * Headless24bitEngine.C_WIDTH] = ' ';
								bgColorMatrixSs[pixelX + pixelY * size[0]] = newColor;
							}
						}
					}
				}
			}
		}
	}

	private void drawPixelAt(char ch, int[] color, double x, double y) {

	}

	@Override
	public void glClearSkin() {
		currentSkin = null;
	}

	protected void clearAll() {
		for (int i = 0; i < Headless24bitEngine.C_WIDTH * Headless24bitEngine.C_HEIGHT; i++) {
			charmatrix[i] = ' ';
		}
		for (int i = 0; i < size[0] * size[1]; i++) {
			bgColorMatrixSs[i] = clearColor;
			fgColorMatrixSs[i] = new int[] { 0, 0, 0 };
		}
	}

	@Override
	public Headless24bitFont getCurrentFont() {
		return currentFont;
	}

}

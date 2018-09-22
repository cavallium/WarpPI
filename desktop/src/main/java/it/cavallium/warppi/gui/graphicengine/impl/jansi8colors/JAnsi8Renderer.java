package it.cavallium.warppi.gui.graphicengine.impl.jansi8colors;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class JAnsi8Renderer implements Renderer {

	JAnsi8Font currentFont;
	protected char[] charmatrix = new char[JAnsi8Engine.C_WIDTH * JAnsi8Engine.C_HEIGHT];
	protected int[] colorMatrix = new int[JAnsi8Engine.C_WIDTH * JAnsi8Engine.C_HEIGHT];
	protected int clearColor = hexColor(0xc5, 0xc2, 0xaf);
	protected int curColor = clearColor;
	public JAnsi8Skin currentSkin;

	public static final String ANSI_PREFIX = "\u001B[";
	public static final String ansiFgColorPrefix = "3";
	public static final String ansiBgColorPrefix = "4";
	public static final String ansiColorSuffix = "m";
	public static final String[] colorANSI = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "0;1", "1;1", "2;1", "3;1", "4;1", "5;1", "6;1", "7;1" };

	public static final String ANSI_RESET = "\u001B[0m";
	public static char FILL;

	private int hexColor(final int red, final int green, final int blue) {
		final int r1 = red;
		int r2;
		final int g1 = green;
		int g2;
		final int b1 = blue;
		int b2;

		final float[] match = new float[16];

		// COLOR
		r2 = 0;
		g2 = 0;
		b2 = 0;
		match[0] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 205;
		g2 = 0;
		b2 = 0;
		match[1] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 0;
		g2 = 205;
		b2 = 0;
		match[2] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 205;
		g2 = 205;
		b2 = 0;
		match[3] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 0;
		g2 = 0;
		b2 = 238;
		match[4] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 205;
		g2 = 0;
		b2 = 205;
		match[5] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 0;
		g2 = 205;
		b2 = 205;
		match[6] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 229;
		g2 = 229;
		b2 = 229;
		match[7] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 127;
		g2 = 127;
		b2 = 127;
		match[8] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 255;
		g2 = 0;
		b2 = 0;
		match[9] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 0;
		g2 = 255;
		b2 = 0;
		match[0xa] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 255;
		g2 = 255;
		b2 = 0;
		match[0xb] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 92;
		g2 = 92;
		b2 = 255;
		match[0xc] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 255;
		g2 = 0;
		b2 = 255;
		match[0xd] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 0;
		g2 = 255;
		b2 = 255;
		match[0xe] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		// COLOR
		r2 = 255;
		g2 = 255;
		b2 = 255;
		match[0xf] = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

		int minIndex = 0;
		for (int i = 1; i < match.length; i++) {
			final float newnumber = match[i];
			if (newnumber < match[minIndex])
				minIndex = i;
		}

		return minIndex;
	}

	private int[] hexColorReverse(final int i) {
		switch (i) {
			case 0x0:
				return new int[] { 0, 0, 0 };
			case 0x1:
				return new int[] { 205, 0, 0 };
			case 0x2:
				return new int[] { 0, 205, 0 };
			case 0x3:
				return new int[] { 205, 205, 0 };
			case 0x4:
				return new int[] { 0, 0, 238 };
			case 0x5:
				return new int[] { 205, 0, 205 };
			case 0x6:
				return new int[] { 0, 205, 205 };
			case 0x7:
				return new int[] { 229, 229, 229 };
			case 0x8:
				return new int[] { 127, 127, 127 };
			case 0x9:
				return new int[] { 255, 0, 0 };
			case 0xa:
				return new int[] { 0, 255, 0 };
			case 0xb:
				return new int[] { 255, 255, 0 };
			case 0xc:
				return new int[] { 92, 92, 255 };
			case 0xd:
				return new int[] { 255, 0, 255 };
			case 0xe:
				return new int[] { 0, 255, 255 };
			default:
				return new int[] { 255, 255, 255 };
		}
	}

	private int colorUnion(final int[] col) {
		return 0xFF << 24 | col[0] << 16 | col[1] << 8 | col[2];
	}

	@Override
	public void glColor3i(final int r, final int gg, final int b) {
		curColor = hexColor(r, gg, b);
	}

	@Override
	public void glColor(final int c) {
		curColor = hexColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF);
	}

	@Override
	public void glColor4i(final int red, final int green, final int blue, final int alpha) {
		curColor = hexColor(red, green, blue);
	}

	@Override
	public void glColor3f(final float red, final float green, final float blue) {
		curColor = hexColor((int) (red * 255), (int) (green * 255), (int) (blue * 255));
	}

	@Override
	public void glColor4f(final float red, final float green, final float blue, final float alpha) {
		curColor = hexColor((int) (red * 255), (int) (green * 255), (int) (blue * 255));
	}

	@Override
	public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {
		clearColor = hexColor(red, green, blue);
	}

	@Override
	public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {
		clearColor = hexColor((int) (red * 255), (int) (green * 255), (int) (blue * 255));
	}

	@Override
	public int glGetClearColor() {
		return clearColor;
	}

	@Override
	public void glClearColor(final int c) {
		clearColor = hexColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF);
	}

	@Override
	public void glClear(final int screenWidth, final int screenHeight) {
		clearAll();
	}

	@Override
	public void glDrawLine(float x1, float y1, float x2, float y2) {
		x1 /= JAnsi8Engine.C_MUL_X;
		x2 /= JAnsi8Engine.C_MUL_X;
		y1 /= JAnsi8Engine.C_MUL_Y;
		y2 /= JAnsi8Engine.C_MUL_Y;

		final int dx = (int) Math.abs(x2 - x1);
		final int dy = (int) Math.abs(y2 - y1);

		final int sx = x1 < x2 ? 1 : -1;
		final int sy = y1 < y2 ? 1 : -1;

		int err = dx - dy;

		while (true) {
			if ((int) x1 >= JAnsi8Engine.C_WIDTH || (int) y1 >= JAnsi8Engine.C_HEIGHT || (int) x2 >= JAnsi8Engine.C_WIDTH || (int) y2 >= JAnsi8Engine.C_HEIGHT)
				break;
			final int precBG = colorMatrix[(int) x1 + (int) y1 * JAnsi8Engine.C_WIDTH] & 0xF0;
			colorMatrix[(int) x1 + (int) y1 * JAnsi8Engine.C_WIDTH] = precBG | curColor;
			if (JAnsi8Renderer.FILL == 0)
				initFill();
			charmatrix[(int) x1 + (int) y1 * JAnsi8Engine.C_WIDTH] = JAnsi8Renderer.FILL;

			if (x1 == x2 && y1 == y2)
				break;

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

	private void initFill() {
		JAnsi8Renderer.FILL = StaticVars.startupArguments.isMSDOSModeEnabled() ? 0xDB : '█';
	}

	@Override
	public void glFillRect(final float x, final float y, final float width, final float height, final float uvX,
			final float uvY, final float uvWidth, final float uvHeight) {
		if (currentSkin != null)
			glDrawSkin((int) (x / JAnsi8Engine.C_MUL_X), (int) (y / JAnsi8Engine.C_MUL_Y), (int) (uvX / JAnsi8Engine.C_MUL_X), (int) (uvY / JAnsi8Engine.C_MUL_Y), (int) ((uvWidth + uvX) / JAnsi8Engine.C_MUL_X), (int) ((uvHeight + uvY) / JAnsi8Engine.C_MUL_Y), true);
		else
			glFillColor(x, y, width, height);
	}

	@Override
	public void glFillColor(final float x, final float y, final float width, final float height) {
		if (JAnsi8Renderer.FILL == 0)
			initFill();
		glFillColor(x, y, width, height, JAnsi8Renderer.FILL, curColor);
	}

	private void glFillColor(final float x, final float y, final float width, final float height, final char character,
			final int color) {
		final int ix = (int) x / JAnsi8Engine.C_MUL_X;
		final int iy = (int) y / JAnsi8Engine.C_MUL_Y;
		final int iw = (int) width / JAnsi8Engine.C_MUL_X;
		final int ih = (int) height / JAnsi8Engine.C_MUL_Y;

		int x1 = ix + iw;
		int y1 = iy + ih;
		if (ix >= JAnsi8Engine.C_WIDTH || iy >= JAnsi8Engine.C_WIDTH)
			return;
		if (x1 >= JAnsi8Engine.C_WIDTH)
			x1 = JAnsi8Engine.C_WIDTH;
		if (y1 >= JAnsi8Engine.C_HEIGHT)
			y1 = JAnsi8Engine.C_HEIGHT;
		final int sizeW = JAnsi8Engine.C_WIDTH;
		for (int px = ix; px < x1; px++)
			for (int py = iy; py < y1; py++) {
				final int precBG = colorMatrix[px + py * sizeW] & 0xF0;
				colorMatrix[px + py * sizeW] = precBG | color;
				charmatrix[px + py * sizeW] = character;
			}
	}

	@Override
	public void glDrawCharLeft(final int x, final int y, final char ch) {
		final int cx = x / JAnsi8Engine.C_MUL_X;
		final int cy = y / JAnsi8Engine.C_MUL_Y;
		if (cx >= JAnsi8Engine.C_WIDTH || cy >= JAnsi8Engine.C_HEIGHT)
			return;
		charmatrix[cx + cy * JAnsi8Engine.C_WIDTH] = ch;
		final int precBG = colorMatrix[cx + cy * JAnsi8Engine.C_WIDTH] & 0xF0;
		colorMatrix[cx + cy * JAnsi8Engine.C_WIDTH] = precBG | curColor;
	}

	@Override
	public void glDrawCharCenter(final int x, final int y, final char ch) {
		glDrawCharLeft(x, y, ch);
	}

	@Override
	public void glDrawCharRight(final int x, final int y, final char ch) {
		final int cx = x / JAnsi8Engine.C_MUL_X - 1;
		final int cy = y / JAnsi8Engine.C_MUL_Y;
		if (cx >= JAnsi8Engine.C_WIDTH || cy >= JAnsi8Engine.C_HEIGHT)
			return;
		charmatrix[cx + cy * JAnsi8Engine.C_WIDTH] = ch;
		final int precBG = colorMatrix[cx + cy * JAnsi8Engine.C_WIDTH] & 0xF0;
		colorMatrix[cx + cy * JAnsi8Engine.C_WIDTH] = precBG | curColor;
	}

	@Override
	public void glDrawStringLeft(final float x, final float y, final String text) {
		final int cx = (int) x / JAnsi8Engine.C_MUL_X;
		final int cy = (int) y / JAnsi8Engine.C_MUL_Y;
		int i = 0;
		for (final char c : text.toCharArray()) {
			if (cx + i >= JAnsi8Engine.C_WIDTH || cy >= JAnsi8Engine.C_HEIGHT)
				break;
			charmatrix[cx + i + cy * JAnsi8Engine.C_WIDTH] = c;
			final int precBG = colorMatrix[cx + i + cy * JAnsi8Engine.C_WIDTH] & 0xF0;
			colorMatrix[cx + i + cy * JAnsi8Engine.C_WIDTH] = precBG | curColor;
			i++;
		}
	}

	@Override
	public void glDrawStringCenter(final float x, final float y, final String text) {
		final int cx = (int) x / JAnsi8Engine.C_MUL_X - text.length() / 2;
		final int cy = (int) y / JAnsi8Engine.C_MUL_Y;
		int i = 0;
		for (final char c : text.toCharArray()) {
			if (cx + i >= JAnsi8Engine.C_WIDTH || cy >= JAnsi8Engine.C_HEIGHT)
				break;
			charmatrix[cx + i + cy * JAnsi8Engine.C_WIDTH] = c;
			final int precBG = colorMatrix[cx + i + cy * JAnsi8Engine.C_WIDTH] & 0xF0;
			colorMatrix[cx + i + cy * JAnsi8Engine.C_WIDTH] = precBG | curColor;
			i++;
		}
	}

	@Override
	public void glDrawStringRight(final float x, final float y, final String text) {
		// TODO Auto-generated method stub

	}

	private void glDrawSkin(int x0, int y0, int s0, int t0, int s1, int t1, final boolean transparent) {
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
		if (x0 >= JAnsi8Engine.C_WIDTH || y0 >= JAnsi8Engine.C_WIDTH)
			return;
		if (x0 + width >= JAnsi8Engine.C_WIDTH)
			s1 = JAnsi8Engine.C_WIDTH - x0 + s0;
		if (y0 + height >= JAnsi8Engine.C_HEIGHT)
			t1 = JAnsi8Engine.C_HEIGHT - y0 + t0;
		if (x0 < 0) {
			if (onex == -1) {
				width += x0;
				s1 += x0 + 1;
			} else
				s0 -= x0;
			x0 = 0;
		}
		if (y0 < 0) {
			if (oney == -1) {
				height += y0;
				t1 += y0 + 1;
			} else
				t0 -= y0;
			y0 = 0;
		}
		int pixelX;
		int pixelY;
		for (int texx = 0; texx < s1 - s0; texx++)
			for (int texy = 0; texy < t1 - t0; texy++) {
				pixelX = x0 + texx * onex + width;
				pixelY = y0 + texy * oney + height;
				if (pixelY < JAnsi8Engine.C_HEIGHT)
					if (pixelX - pixelX % JAnsi8Engine.C_WIDTH == 0) {
						newColor = currentSkin.skinData[s0 + texx + (t0 + texy) * currentSkin.skinSize[0]];
						if (transparent) {
							oldColor = colorUnion(hexColorReverse((colorMatrix[pixelX + pixelY * JAnsi8Engine.C_WIDTH] & 0xF0) >> 4));
							final float a2 = (newColor >> 24 & 0xFF) / 255f;
							final float a1 = 1f - a2;
							final int r = (int) ((oldColor >> 16 & 0xFF) * a1 + (newColor >> 16 & 0xFF) * a2);
							final int g = (int) ((oldColor >> 8 & 0xFF) * a1 + (newColor >> 8 & 0xFF) * a2);
							final int b = (int) ((oldColor & 0xFF) * a1 + (newColor & 0xFF) * a2);
							newColor = 0xFF000000 | r << 16 | g << 8 | b;
						}
						final int bgColor = colorMatrix[pixelX + pixelY * JAnsi8Engine.C_WIDTH] & 0xF0;
						colorMatrix[pixelX + pixelY * JAnsi8Engine.C_WIDTH] = bgColor | hexColor(newColor >> 16 & 0xFF, newColor >> 8 & 0xFF, newColor & 0xFF);
						if (JAnsi8Renderer.FILL == 0)
							initFill();
						charmatrix[pixelX + pixelY * JAnsi8Engine.C_WIDTH] = JAnsi8Renderer.FILL;
					}
			}
	}

	@Override
	public void glClearSkin() {
		currentSkin = null;
	}

	protected void clearAll() {
		for (int i = 0; i < JAnsi8Engine.C_WIDTH * JAnsi8Engine.C_HEIGHT; i++) {
			charmatrix[i] = ' ';
			colorMatrix[i] = clearColor << 4;
		}
	}

	@Override
	public JAnsi8Font getCurrentFont() {
		return currentFont;
	}

}

package it.cavallium.warppi.gui.graphicengine.html;

import org.teavm.jso.canvas.CanvasRenderingContext2D;

import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class HtmlRenderer implements Renderer {
	private String currentColor = "#000000ff";
	private String clearColor = "#000000ff";
	HtmlFont f = null;
	HtmlSkin currentSkin = null;
	private final CanvasRenderingContext2D g;
	private final HtmlEngine e;

	public HtmlRenderer(final HtmlEngine e, final CanvasRenderingContext2D g) {
		this.g = g;
		this.e = e;
	}

	private String toHex(final int r, final int g, final int b) {
		return String.format("#%02x%02x%02x", r, g, b);
	}

	private String toHex(final int r, final int g, final int b, final int a) {
		return String.format("#%02x%02x%02x%02x", r, g, b, a);
	}

	@Override
	public int glGetClearColor() {
		return hexToInt(clearColor);
	}

	private int hexToInt(final String hex) {
		switch (hex.length()) {
			case 6:
				return 0xFF << 24 | Integer.valueOf(hex.substring(0, 2), 16) << 16 | Integer.valueOf(hex.substring(2, 4), 16) << 8 | Integer.valueOf(hex.substring(4, 6), 16);
			case 6 + 1:
				return 0xFF << 24 | Integer.valueOf(hex.substring(0 + 1, 2 + 1), 16) << 16 | Integer.valueOf(hex.substring(2 + 1, 4 + 1), 16) << 8 | Integer.valueOf(hex.substring(4 + 1, 6 + 1), 16);
			case 8:
				return Integer.valueOf(hex.substring(6, 8), 16) << 24 | Integer.valueOf(hex.substring(0, 2), 16) << 16 | Integer.valueOf(hex.substring(2, 4), 16) << 8 | Integer.valueOf(hex.substring(4, 6), 16);
			case 8 + 1:
				return Integer.valueOf(hex.substring(6 + 1, 8 + 1), 16) << 24 | Integer.valueOf(hex.substring(0 + 1, 2 + 1), 16) << 16 | Integer.valueOf(hex.substring(2 + 1, 4 + 1), 16) << 8 | Integer.valueOf(hex.substring(4 + 1, 6 + 1), 16);
		}
		return 0xFF000000;
	}

	@Override
	public void glFillRect(final float x, final float y, final float width, final float height, final float uvX,
			final float uvY, final float uvWidth, final float uvHeight) {
		if (currentSkin != null)
			glDrawSkin((int) x, (int) y, (int) (x + width), (int) (y + height), (int) uvX, (int) uvY, (int) (uvWidth + uvX), (int) (uvHeight + uvY), true);
		else
			glFillColor(x, y, width, height);
	}

	@SuppressWarnings("unused")
	private void glDrawSkin(int x0, int y0, final int x1, final int y1, int s0, int t0, int s1, int t1,
			final boolean transparent) {
		final int[] size = e.getSize();

		x0 += StaticVars.screenPos[0];
		y0 += StaticVars.screenPos[1];
		final double incrementX = Math.abs((double) (x1 - x0) / (double) (s1 - s0));
		final double incrementY = Math.abs((double) (y1 - y0) / (double) (t1 - t0));
		final boolean flippedX = (x1 - x0) / (s1 - s0) < 0;
		final boolean flippedY = (y1 - y0) / (t1 - t0) < 0;
		final int oldColor = 0;
		final int newColor;
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
		if (x0 >= size[0] || y0 >= size[0])
			return;
		if (x0 + width >= size[0])
			s1 = size[0] - x0 + s0;
		if (y0 + height >= size[1])
			t1 = size[1] - y0 + t0;
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
		g.drawImage(currentSkin.getImgElement(), s0, t0, s1 - s0, t1 - t0, x0, y0, x1 - x0, y1 - y0);
	}

	@Override
	public void glFillColor(float x, float y, final float width, final float height) {
		x += StaticVars.screenPos[0];
		y += StaticVars.screenPos[1];
		g.setFillStyle(currentColor);
		g.fillRect(x, y, width, height);
	}

	@Override
	public void glDrawStringRight(final float x, final float y, final String text) {
		glDrawStringLeft(x - f.getStringWidth(text), y, text);
	}

	@Override
	public void glDrawStringLeft(float x, float y, final String textString) {
		x += StaticVars.screenPos[0];
		y += StaticVars.screenPos[1];

		f.imgElCtx.setGlobalCompositeOperation("source-in");
		f.imgElCtx.setFillStyle(currentColor);
		f.imgElCtx.fillRect(0, 0, f.imgEl.getWidth(), f.imgEl.getHeight());

		final int[] text = f.getCharIndexes(textString);
		int cpos;
		final int l = text.length;
		for (int i = 0; i < l; i++) {
			cpos = i * f.charW;
			final int charIndex = text[i];
			g.drawImage(f.imgEl, 0, charIndex * f.charH, f.charW, f.charH, x + cpos, y, f.charW, f.charH);
		}
	}

	@Override
	public void glDrawStringCenter(final float x, final float y, final String text) {
		glDrawStringLeft(x - f.getStringWidth(text) / 2, y, text);
	}

	@Override
	public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {
		if (x1 - x0 > 0 && y1 - y0 > 0) {
			g.beginPath();
			g.moveTo(x0, y0);
			g.lineTo(x1, y1);
			g.stroke();
		} else
			g.fillRect(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
	}

	@Override
	public void glDrawCharRight(final int x, final int y, final char ch) {
		glDrawStringRight(x, y, ch + "");
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
	public void glColor4i(final int red, final int green, final int blue, final int alpha) {
		g.setFillStyle(currentColor = toHex(red, green, blue, alpha));
	}

	@Override
	public void glColor4f(final float red, final float green, final float blue, final float alpha) {
		glColor4i((int) (red * 255d), (int) (green * 255d), (int) (blue * 255d), (int) (alpha * 255d));
	}

	@Override
	public void glColor3i(final int r, final int gg, final int b) {
		g.setFillStyle(currentColor = toHex(r, gg, b));
	}

	@Override
	public void glColor3f(final float red, final float green, final float blue) {
		glColor3i((int) (red * 255d), (int) (green * 255d), (int) (blue * 255d));
	}

	@Override
	public void glColor(final int c) {
		final int a = c >> 24 & 0xFF;
		final int r = c >> 16 & 0xFF;
		final int gg = c >> 8 & 0xFF;
		final int b = c & 0xFF;
		g.setFillStyle(currentColor = toHex(r, gg, b, a));
	}

	@Override
	public void glClearSkin() {
		currentSkin = null;
	}

	@Override
	public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {
		clearColor = toHex(red, green, blue, alpha);
	}

	@Override
	public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {
		clearColor = toHex((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
	}

	@Override
	public void glClearColor(final int c) {
		final int r = c >> 16 & 0xFF;
		final int gg = c >> 8 & 0xFF;
		final int b = c & 0xFF;
		clearColor = toHex(r, gg, b);
	}

	@Override
	public void glClear(final int screenWidth, final int screenHeight) {
		g.setFillStyle(clearColor);
		g.fillRect(0, 0, screenWidth, screenHeight);
		g.setFillStyle(currentColor);
	}

	@Override
	public HtmlFont getCurrentFont() {
		return f;
	}
}
package it.cavallium.warppi.gui.graphicengine.impl.common;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class ReboundedRenderer implements Renderer {
	private final Renderer renderer;
	private final int dx;
	private final int dy;
	private final int width;
	private final int height;

	public ReboundedRenderer(Renderer renderer, int dx, int dy, int width, int height) {
		this.renderer = renderer;
		this.dx = dx;
		this.dy = dy;
		this.width = width;
		this.height = height;
	}

	@Override
	public void glColor3i(int r, int gg, int b) {
		renderer.glColor3i(r, gg, b);
	}

	@Override
	public void glColor(int c) {
		renderer.glColor(c);
	}

	@Override
	public void glColor4i(int red, int green, int blue, int alpha) {
		renderer.glColor4i(red, green, blue, alpha);
	}

	@Override
	public void glColor3f(float red, float green, float blue) {
renderer.glColor3f(red, green, blue);
	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
renderer.glColor4f(red, green, blue, alpha);
	}

	@Override
	public void glClearColor4i(int red, int green, int blue, int alpha) {
renderer.glClearColor4i(red, green, blue, alpha);
	}

	@Override
	public void glClearColor4f(float red, float green, float blue, float alpha) {
renderer.glClearColor4f(red, green, blue, alpha);
	}

	@Override
	public int glGetClearColor() {
		return renderer.glGetClearColor();
	}

	@Override
	public void glClearColor(int c) {
		renderer.glClearColor(c);
	}

	@Override
	public void glClear(int screenWidth, int screenHeight) {
renderer.glClear(screenWidth, screenHeight);
	}

	@Override
	public void glDrawLine(float x0, float y0, float x1, float y1) {
renderer.glDrawLine(this.dx + x0, this.dy + y0, this.dx + x1, this.dy + y1);
	}

	@Override
	public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth, float uvHeight) {
renderer.glFillRect(this.dx + x, this.dy + y, width, height, uvX, uvY, uvWidth, uvHeight);
	}

	@Override
	public void glFillColor(float x, float y, float width, float height) {
renderer.glFillColor(this.dx + x, this.dy + y, width, height);
	}

	@Override
	public void glDrawCharLeft(int x, int y, char ch) {
renderer.glDrawCharLeft(this.dx + x, this.dy + y, ch);
	}

	@Override
	public void glDrawCharCenter(int x, int y, char ch) {
renderer.glDrawCharCenter(this.dx + x, this.dy + y, ch);
	}

	@Override
	public void glDrawCharRight(int x, int y, char ch) {
		renderer.glDrawCharRight(this.dx + x, this.dy + y, ch);
	}

	@Override
	public void glDrawStringLeft(float x, float y, String text) {
		renderer.glDrawStringLeft(this.dx + x, this.dy + y, text);
	}

	@Override
	public void glDrawStringCenter(float x, float y, String text) {
		renderer.glDrawStringCenter(this.dx + x, this.dy + y, text);
	}

	@Override
	public void glDrawStringRight(float x, float y, String text) {
		renderer.glDrawStringRight(this.dx + x, this.dy + y, text);
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
	public Renderer getBoundedInstance(int dx, int dy, int width, int height) {
		return renderer.getBoundedInstance(this.dx + dx, this.dy + dy, width, height);
	}
}

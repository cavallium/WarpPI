package it.cavallium.warppi.gui.graphicengine.impl.framebuffer;

import java.nio.MappedByteBuffer;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class FBRenderer implements Renderer {

	public FBRenderer(final FBEngine fbEngine, final MappedByteBuffer fb) {}

	@Override
	public void glColor3i(final int r, final int gg, final int b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor(final int c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor4i(final int red, final int green, final int blue, final int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor3f(final float red, final float green, final float blue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor4f(final float red, final float green, final float blue, final float alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetClearColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glClearColor(final int c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClear(final int screenWidth, final int screenHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFillRect(final float x, final float y, final float width, final float height, final float uvX,
			final float uvY, final float uvWidth, final float uvHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFillColor(final float x, final float y, final float width, final float height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawCharLeft(final int x, final int y, final char ch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawCharCenter(final int x, final int y, final char ch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawCharRight(final int x, final int y, final char ch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawStringLeft(final float x, final float y, final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawStringCenter(final float x, final float y, final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawStringRight(final float x, final float y, final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearSkin() {
		// TODO Auto-generated method stub

	}

	@Override
	public BinaryFont getCurrentFont() {
		// TODO Auto-generated method stub
		return null;
	}

}

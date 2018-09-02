package it.cavallium.warppi.gui.graphicengine.framebuffer;

import java.nio.MappedByteBuffer;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class FBRenderer implements Renderer {

	public FBRenderer(FBEngine fbEngine, MappedByteBuffer fb) {}

	@Override
	public void glColor3i(int r, int gg, int b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor(int c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor4i(int red, int green, int blue, int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor3f(float red, float green, float blue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glColor4f(float red, float green, float blue, float alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearColor4i(int red, int green, int blue, int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClearColor4f(float red, float green, float blue, float alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public int glGetClearColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void glClearColor(int c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glClear(int screenWidth, int screenHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawLine(float x0, float y0, float x1, float y1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth,
			float uvHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glFillColor(float x, float y, float width, float height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawCharLeft(int x, int y, char ch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawCharCenter(int x, int y, char ch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawCharRight(int x, int y, char ch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawStringLeft(float x, float y, String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawStringCenter(float x, float y, String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glDrawStringRight(float x, float y, String text) {
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

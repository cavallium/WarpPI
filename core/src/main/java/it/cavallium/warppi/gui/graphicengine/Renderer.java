package it.cavallium.warppi.gui.graphicengine;

public interface Renderer {
	public void glColor3i(int r, int gg, int b);

	public void glColor(int c);

	public void glColor4i(int red, int green, int blue, int alpha);

	public void glColor3f(float red, float green, float blue);

	public void glColor4f(float red, float green, float blue, float alpha);

	public void glClearColor4i(int red, int green, int blue, int alpha);

	public void glClearColor4f(float red, float green, float blue, float alpha);

	public int glGetClearColor();

	public void glClearColor(int c);

	public void glClear(int screenWidth, int screenHeight);

	public void glDrawLine(float x0, float y0, float x1, float y1);

	public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth,
			float uvHeight);

	public void glFillColor(float x, float y, float width, float height);

	public void glDrawCharLeft(int x, int y, char ch);

	public void glDrawCharCenter(int x, int y, char ch);

	public void glDrawCharRight(int x, int y, char ch);

	public void glDrawStringLeft(float x, float y, String text);

	public void glDrawStringCenter(float x, float y, String text);

	public void glDrawStringRight(float x, float y, String text);

	public void glClearSkin();

	public BinaryFont getCurrentFont();
}
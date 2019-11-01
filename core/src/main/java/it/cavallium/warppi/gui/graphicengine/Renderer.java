package it.cavallium.warppi.gui.graphicengine;

public interface Renderer {
	void glColor3i(int r, int gg, int b);

	void glColor(int c);

	void glColor4i(int red, int green, int blue, int alpha);

	void glColor3f(float red, float green, float blue);

	void glColor4f(float red, float green, float blue, float alpha);

	void glClearColor4i(int red, int green, int blue, int alpha);

	void glClearColor4f(float red, float green, float blue, float alpha);

	int glGetClearColor();

	void glClearColor(int c);

	void glClear(int screenWidth, int screenHeight);

	void glDrawLine(float x0, float y0, float x1, float y1);

	void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth, float uvHeight);

	void glFillColor(float x, float y, float width, float height);

	void glDrawCharLeft(int x, int y, char ch);

	void glDrawCharCenter(int x, int y, char ch);

	void glDrawCharRight(int x, int y, char ch);

	void glDrawStringLeft(float x, float y, String text);

	void glDrawStringCenter(float x, float y, String text);

	void glDrawStringRight(float x, float y, String text);

	void glClearSkin();

	BinaryFont getCurrentFont();

	Renderer getBoundedInstance(int dx, int dy, int width, int height);
}
package org.warp.picalculator.gui.graphicengine.nogui;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.warp.picalculator.Utils;
import org.warp.picalculator.deps.DSemaphore;
import org.warp.picalculator.gui.graphicengine.BinaryFont;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.Renderer;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;
import org.warp.picalculator.gui.graphicengine.Skin;

import io.reactivex.Observable;

public class NoGuiEngine implements GraphicEngine {

	private boolean initialized;
	public DSemaphore exitSemaphore = new DSemaphore(0);

	@Override
	public int[] getSize() {
		return new int[] { 2, 2 };
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(String title) {}

	@Override
	public void setResizable(boolean r) {}

	@Override
	public void setDisplayMode(int ww, int wh) {}

	@Override
	public void create(Runnable onInitialized) {
		initialized = true;
		if (onInitialized != null) {
			onInitialized.run();
		}
	}

	@Override
	public Observable<Integer[]> onResize() {
		return null;
	}

	@Override
	public int getWidth() {
		return 2;
	}

	@Override
	public int getHeight() {
		return 2;
	}

	@Override
	public void destroy() {
		initialized = false;
		exitSemaphore.release();
	}

	@Override
	public void start(RenderingLoop d) {}

	@Override
	public void repaint() {}

	@Override
	public Renderer getRenderer() {
		return new Renderer() {
			@Override
			public int glGetClearColor() {
				return 0;
			}

			@Override
			public void glFillRect(float x, float y, float width, float height, float uvX, float uvY, float uvWidth,
					float uvHeight) {}

			@Override
			public void glFillColor(float x, float y, float width, float height) {}

			@Override
			public void glDrawStringRight(float x, float y, String text) {}

			@Override
			public void glDrawStringLeft(float x, float y, String text) {}

			@Override
			public void glDrawStringCenter(float x, float y, String text) {}

			@Override
			public void glDrawLine(float x0, float y0, float x1, float y1) {}

			@Override
			public void glDrawCharRight(int x, int y, char ch) {}

			@Override
			public void glDrawCharLeft(int x, int y, char ch) {}

			@Override
			public void glDrawCharCenter(int x, int y, char ch) {}

			@Override
			public void glColor4i(int red, int green, int blue, int alpha) {}

			@Override
			public void glColor4f(float red, float green, float blue, float alpha) {}

			@Override
			public void glColor3i(int r, int gg, int b) {}

			@Override
			public void glColor3f(float red, float green, float blue) {}

			@Override
			public void glColor(int c) {}

			@Override
			public void glClearSkin() {}

			@Override
			public void glClearColor4i(int red, int green, int blue, int alpha) {}

			@Override
			public void glClearColor4f(float red, float green, float blue, float alpha) {}

			@Override
			public void glClearColor(int c) {}

			@Override
			public void glClear(int screenWidth, int screenHeight) {}

			@Override
			public BinaryFont getCurrentFont() {
				return null;
			}
		};
	}

	@Override
	public BinaryFont loadFont(String fontName) throws IOException {
		return new BinaryFont() {
			@Override
			public void use(GraphicEngine d) {}

			@Override
			public void load(String file) throws IOException {}

			@Override
			public boolean isInitialized() {
				return true;
			}

			@Override
			public void initialize(GraphicEngine d) {}

			@Override
			public int getStringWidth(String text) {
				return 1;
			}

			@Override
			public int getCharacterWidth() {
				return 1;
			}

			@Override
			public int getCharacterHeight() {
				return 1;
			}

			@Override
			public int getSkinWidth() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getSkinHeight() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
	}

	@Override
	public BinaryFont loadFont(String path, String fontName) throws IOException {
		return new BinaryFont() {
			@Override
			public void use(GraphicEngine d) {}

			@Override
			public void load(String file) throws IOException {}

			@Override
			public boolean isInitialized() {
				return true;
			}

			@Override
			public void initialize(GraphicEngine d) {}

			@Override
			public int getStringWidth(String text) {
				return 1;
			}

			@Override
			public int getCharacterWidth() {
				return 1;
			}

			@Override
			public int getCharacterHeight() {
				return 1;
			}

			@Override
			public int getSkinWidth() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getSkinHeight() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
	}

	@Override
	public Skin loadSkin(String file) throws IOException {
		return new Skin() {
			@Override
			public void use(GraphicEngine d) {}

			@Override
			public void load(String file) throws IOException {}

			@Override
			public boolean isInitialized() {
				return true;
			}

			@Override
			public void initialize(GraphicEngine d) {}

			@Override
			public int getSkinWidth() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getSkinHeight() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
	}

	@Override
	public void waitForExit() {
		try {
			exitSemaphore.acquire();
		} catch (final InterruptedException e) {}
	}

	@Override
	public boolean isSupported() {
		return true;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}

}

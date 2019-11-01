package it.cavallium.warppi.gui.graphicengine.impl.nogui;

import java.io.IOException;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.util.EventSubmitter;
import it.cavallium.warppi.util.EventSubscriber;

public class NoGuiEngine implements GraphicEngine {

	private boolean initialized;
	public Semaphore exitSemaphore = Engine.getPlatform().newSemaphore(0);

	@Override
	public int[] getSize() {
		return new int[] { 2, 2 };
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(final String title) {}

	@Override
	public void setResizable(final boolean r) {}

	@Override
	public void setDisplayMode(final int ww, final int wh) {}

	@Override
	public void create(final Runnable onInitialized) {
		initialized = true;
		if (onInitialized != null) {
			onInitialized.run();
		}
	}

	@Override
	public EventSubscriber<Integer[]> onResize() {
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
	public void start(final RenderingLoop d) {}

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
			public void glFillRect(final float x, final float y, final float width, final float height, final float uvX,
					final float uvY, final float uvWidth, final float uvHeight) {}

			@Override
			public void glFillColor(final float x, final float y, final float width, final float height) {}

			@Override
			public void glDrawStringRight(final float x, final float y, final String text) {}

			@Override
			public void glDrawStringLeft(final float x, final float y, final String text) {}

			@Override
			public void glDrawStringCenter(final float x, final float y, final String text) {}

			@Override
			public void glDrawLine(final float x0, final float y0, final float x1, final float y1) {}

			@Override
			public void glDrawCharRight(final int x, final int y, final char ch) {}

			@Override
			public void glDrawCharLeft(final int x, final int y, final char ch) {}

			@Override
			public void glDrawCharCenter(final int x, final int y, final char ch) {}

			@Override
			public void glColor4i(final int red, final int green, final int blue, final int alpha) {}

			@Override
			public void glColor4f(final float red, final float green, final float blue, final float alpha) {}

			@Override
			public void glColor3i(final int r, final int gg, final int b) {}

			@Override
			public void glColor3f(final float red, final float green, final float blue) {}

			@Override
			public void glColor(final int c) {}

			@Override
			public void glClearSkin() {}

			@Override
			public void glClearColor4i(final int red, final int green, final int blue, final int alpha) {}

			@Override
			public void glClearColor4f(final float red, final float green, final float blue, final float alpha) {}

			@Override
			public void glClearColor(final int c) {}

			@Override
			public void glClear(final int screenWidth, final int screenHeight) {}

			@Override
			public BinaryFont getCurrentFont() {
				return null;
			}
		};
	}

	@Override
	public BinaryFont loadFont(final String fontName) throws IOException {
		return new BinaryFont() {
			@Override
			public void use(final GraphicEngine d) {}

			@Override
			public void load(final String file) throws IOException {}

			@Override
			public boolean isInitialized() {
				return true;
			}

			@Override
			public void initialize(final GraphicEngine d) {}

			@Override
			public int getStringWidth(final String text) {
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
	public BinaryFont loadFont(final String path, final String fontName) throws IOException {
		return new BinaryFont() {
			@Override
			public void use(final GraphicEngine d) {}

			@Override
			public void load(final String file) throws IOException {}

			@Override
			public boolean isInitialized() {
				return true;
			}

			@Override
			public void initialize(final GraphicEngine d) {}

			@Override
			public int getStringWidth(final String text) {
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
	public Skin loadSkin(final String file) throws IOException {
		return new Skin() {
			@Override
			public void use(final GraphicEngine d) {}

			@Override
			public void load(final String file) throws IOException {}

			@Override
			public boolean isInitialized() {
				return true;
			}

			@Override
			public void initialize(final GraphicEngine d) {}

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

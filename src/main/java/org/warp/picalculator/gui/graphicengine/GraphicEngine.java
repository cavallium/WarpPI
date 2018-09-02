package org.warp.picalculator.gui.graphicengine;

import java.io.IOException;
import java.util.List;

import org.warp.picalculator.flow.Observable;

public interface GraphicEngine {

	public int[] getSize();

	public boolean isInitialized();

	public void setTitle(String title);

	public void setResizable(boolean r);

	public void setDisplayMode(final int ww, final int wh);

	public default void create() {
		create(null);
	};

	public void create(Runnable object);

	public Observable<Integer[]> onResize();

	public int getWidth();

	public int getHeight();

	public void destroy();

	public void start(RenderingLoop d);

	public void repaint();

	public Renderer getRenderer();

	public BinaryFont loadFont(String fontName) throws IOException;

	public BinaryFont loadFont(String path, String fontName) throws IOException;

	public Skin loadSkin(String file) throws IOException;

	public void waitForExit();

	public boolean isSupported();

	public boolean doesRefreshPauses();

	public default boolean supportsFontRegistering() {
		return false;
	}

	public default List<BinaryFont> getRegisteredFonts() {
		return null;
	}
}

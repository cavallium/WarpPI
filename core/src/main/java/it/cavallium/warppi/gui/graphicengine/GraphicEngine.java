package it.cavallium.warppi.gui.graphicengine;

import it.cavallium.warppi.util.EventSubscriber;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface GraphicEngine {

	int[] getSize();

	boolean isInitialized();

	void setTitle(String title);

	void setResizable(boolean r);

	void setDisplayMode(final int ww, final int wh);

	default void create() {
		create(null);
	};

	void create(Runnable object);

	EventSubscriber<Integer[]> onResize();

	int getWidth();

	int getHeight();

	void destroy();

	void start(RenderingLoop d);

	void repaint();

	Renderer getRenderer();

	BinaryFont loadFont(String fontName) throws IOException;

	BinaryFont loadFont(String path, String fontName) throws IOException;

	Skin loadSkin(String file) throws IOException;

	void waitForExit();

	boolean isSupported();

	boolean doesRefreshPauses();

	default boolean supportsFontRegistering() {
		return false;
	}

	default List<BinaryFont> getRegisteredFonts() {
		return null;
	}
}

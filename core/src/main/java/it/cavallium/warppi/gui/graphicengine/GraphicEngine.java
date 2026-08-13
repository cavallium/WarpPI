package it.cavallium.warppi.gui.graphicengine;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.util.EventSubscriber;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface GraphicEngine {

	int[] getSize();

	boolean isSupported();
	
	boolean isInitialized();

	void setTitle(String title);

	void setResizable(boolean r);

	void setDisplayMode(int ww, int wh);

	void create(Runnable object);
	
	default void create() {
		create(null);
	};

	EventSubscriber<Integer[]> onResize();

	int getWidth();

	int getHeight();

	void destroy();

	void start(RenderingLoop d);

	void repaint();

	/** Notifies engines that render a keypad when its visible mapping changes. */
	default void keyboardLayerChanged(final Layer layer) {}

	Renderer getRenderer();

	BinaryFont loadFont(String fontName) throws IOException;

	BinaryFont loadFont(String path, String fontName) throws IOException;

	Skin loadSkin(String file) throws IOException;

	/**
	 * Whether this engine uses the production-only, preprocessed texture layout.
	 * Other engines keep the original PNG resources and coordinates.
	 */
	default boolean usesProductionTextureAtlases() {
		return false;
	}

	boolean doesRefreshPauses();

	default boolean supportsFontRegistering() {
		return false;
	}

	default List<BinaryFont> getRegisteredFonts() {
		return null;
	}
}

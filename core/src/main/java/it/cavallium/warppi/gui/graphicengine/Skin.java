package it.cavallium.warppi.gui.graphicengine;

import java.io.IOException;
import java.net.URISyntaxException;

import it.cavallium.warppi.device.display.DisplayOutputDevice;

public interface Skin {

	void load(String file) throws IOException, URISyntaxException;

	void initialize(DisplayOutputDevice d);

	boolean isInitialized();

	void use(DisplayOutputDevice d);

	/**
	 * Releases decoded and device-side texture storage. Implementations may load
	 * the resource again if {@link #use(DisplayOutputDevice)} is called later.
	 */
	default void unload(final DisplayOutputDevice d) {
	}

	/**
	 * May not be available before initialization
	 * @return skin width
	 */
	int getSkinWidth();

	/**
	 * May not be available before initialization
	 * @return skin height
	 */
	int getSkinHeight();
}

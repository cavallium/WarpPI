package it.cavallium.warppi.gui.graphicengine.impl.common;

import java.io.IOException;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.Platform.ImageUtils.ImageReader;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.Skin;

public abstract class PngSkin implements Skin {

	public int[] skinData;
	public int[] skinSize;
	private String texturePath;
	private boolean initialized;

	public PngSkin(final String file) throws IOException {
		load(file);
	}

	@Override
	public void load(String file) throws IOException {
		if (!file.startsWith("/")) {
			file = "/" + file;
		}
		texturePath = file;
		final ImageReader r = WarpPI.getPlatform().getImageUtils().load(WarpPI.getPlatform().getStorageUtils().getResourceStream(file));
		if (r == null) {
			skinData = new int[0];
			skinSize = new int[] { 0, 0 };
			System.err.println("ERROR WHILE LOADING SKIN " + file);
		} else {
			skinData = r.getImageMatrix();
			skinSize = r.getSize();
		}
		initialized = true;
	}

	@Override
	public void initialize(final DisplayOutputDevice d) {
		if (initialized)
			return;
		try {
			load(texturePath);
		} catch (final IOException exception) {
			throw new IllegalStateException("Failed to reload texture '" + texturePath + "'", exception);
		}

	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void unload(final DisplayOutputDevice d) {
		skinData = null;
		skinSize = null;
		initialized = false;
	}

	@Override
	public int getSkinWidth() {
		return skinSize == null ? 0 : skinSize[0];
	}

	@Override
	public int getSkinHeight() {
		return skinSize == null ? 0 : skinSize[1];
	}

}

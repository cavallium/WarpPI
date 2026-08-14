package it.cavallium.warppi.gui.graphicengine.impl.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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
	public void load(final String file) throws IOException {
		texturePath = Objects.requireNonNull(file, "file");
		/*
		 * StorageUtils already resolves both class-path resources and filesystem
		 * fallbacks. Preserve a relative path here so CPU skins can load the same
		 * external diagnostic assets as JOGL instead of rewriting them to /... .
		 */
		try (InputStream input = WarpPI.getPlatform().getStorageUtils()
				.getResourceStream(texturePath)) {
			final ImageReader reader = WarpPI.getPlatform().getImageUtils().load(input);
			if (reader == null) {
				skinData = new int[0];
				skinSize = new int[] { 0, 0 };
				System.err.println("ERROR WHILE LOADING SKIN " + texturePath);
			} else {
				skinData = reader.getImageMatrix();
				skinSize = reader.getSize();
			}
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

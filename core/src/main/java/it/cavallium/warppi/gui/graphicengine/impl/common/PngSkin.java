package it.cavallium.warppi.gui.graphicengine.impl.common;

import java.io.File;
import java.io.IOException;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ImageUtils.ImageReader;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Skin;

public abstract class PngSkin implements Skin {

	public int[] skinData;
	public int[] skinSize;
	@SuppressWarnings("unused")
	private final boolean isResource;

	public PngSkin(final String file) throws IOException {
		isResource = !new File(file).exists();
		load(file);
	}

	@Override
	public void load(String file) throws IOException {
		if (!file.startsWith("/")) {
			file = "/" + file;
		}
		final ImageReader r = Engine.getPlatform().getImageUtils().load(Engine.getPlatform().getStorageUtils().getResourceStream(file));
		if (r == null) {
			skinData = new int[0];
			skinSize = new int[] { 0, 0 };
			System.err.println("ERROR WHILE LOADING SKIN " + file);
		} else {
			skinData = r.getImageMatrix();
			skinSize = r.getSize();
		}
	}

	@Override
	public void initialize(final GraphicEngine d) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public int getSkinWidth() {
		return skinSize[0];
	}

	@Override
	public int getSkinHeight() {
		return skinSize[1];
	}

}

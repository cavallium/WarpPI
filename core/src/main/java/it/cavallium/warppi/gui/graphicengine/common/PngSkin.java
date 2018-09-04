package it.cavallium.warppi.gui.graphicengine.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.deps.Platform.PngUtils.PngReader;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Skin;

public abstract class PngSkin implements Skin {

	public int[] skinData;
	public int[] skinSize;
	@SuppressWarnings("unused")
	private final boolean isResource;

	public PngSkin(String file) throws IOException {
		isResource = !new File(file).exists();
		load(file);
	}

	@SuppressWarnings("unused")
	@Override
	public void load(String file) throws IOException {
		if (!file.startsWith("/"))
			file = "/" + file;
		try {
			if (!file.endsWith(".png")) {
				File f = File.createTempFile("picalculator-png", ".png");
				f.deleteOnExit();
				BufferedImage img = ImageIO.read(Engine.getPlatform().getStorageUtils().getResourceStream(file));
				ImageIO.write(img, "PNG", f);
				file = f.toString();
			}
			PngReader r = Engine.getPlatform().getPngUtils().load(Engine.getPlatform().getStorageUtils().getResourceStream(file));
			if (r == null) {
				skinData = new int[0];
				skinSize = new int[] { 0, 0 };
				System.err.println("ERROR WHILE LOADING SKIN " + file);
			} else {
				skinData = r.getImageMatrix();
				skinSize = r.getSize();
			}
		} catch (URISyntaxException e) {
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public void initialize(GraphicEngine d) {
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

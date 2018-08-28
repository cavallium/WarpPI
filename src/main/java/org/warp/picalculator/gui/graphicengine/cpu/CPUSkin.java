package org.warp.picalculator.gui.graphicengine.cpu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.warp.picalculator.deps.DSystem;
import org.warp.picalculator.deps.StorageUtils;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.Skin;

import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

public class CPUSkin implements Skin {

	public int[] skinData;
	public int[] skinSize;
	private final boolean isResource;

	public CPUSkin(String file) throws IOException {
		isResource = !new File(file).exists();
		load(file);
	}

	@SuppressWarnings("unused")
	@Override
	public void load(String file) throws IOException {
		if (!file.startsWith("/"))
			file = "/" + file;
		try {
			PngReader r = new PngReader(StorageUtils.getResourceStream(file));
			if (r == null) {
				skinData = new int[0];
				skinSize = new int[] { 0, 0 };
				System.err.println("ERROR WHILE LOADING SKIN " + file);
			} else {
				skinData = getMatrixOfImage(r);
				skinSize = new int[] { r.imgInfo.cols, r.imgInfo.rows };
			}
		} catch (URISyntaxException e) {
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
		}
	}

	public static int[] getMatrixOfImage(PngReader r) {
		final int width = r.imgInfo.cols;
		final int height = r.imgInfo.rows;
		final int channels = r.imgInfo.channels;
		final int[] pixels = new int[width * height];
		int pi = 0;
		ImageLineInt lint;
		while (r.hasMoreRows()) {
			lint = (ImageLineInt) r.readRow();
			int[] scanLine = lint.getScanline();

			for (int i = 0; i < width; i++) {
				int offset = i * channels;

				// Adjust the following code depending on your source image.
				// I need the to set the alpha channel to 0xFF000000 since my destination image
				// is TRANSLUCENT : BufferedImage bi = CONFIG.createCompatibleImage( width, height, Transparency.TRANSLUCENT );
				// my source was 3 channels RGB without transparency
				int nextPixel;
				if (channels == 4) {
					nextPixel = (scanLine[offset] << 16) | (scanLine[offset + 1] << 8) | (scanLine[offset + 2]) | (scanLine[offset + 3] << 24);
				} else {
					nextPixel = (scanLine[offset] << 16) | (scanLine[offset + 1] << 8) | (scanLine[offset + 2]) | (0xFF << 24);
				}

				// I'm placing the pixels on a memory mapped file
				pixels[pi] = nextPixel;
				pi++;
			}

		}

		return pixels;
	}

	@Override
	public void initialize(GraphicEngine d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void use(GraphicEngine d) {
		if (d.getRenderer() instanceof CPURenderer) {
			((CPURenderer) d.getRenderer()).currentSkin = this;
		}
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

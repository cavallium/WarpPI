package it.cavallium.warppi.gui.graphicengine.impl.jansi24bitcolors;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class JAnsi24bitSkin implements Skin {

	public int[][] skinData;
	public int[] skinSize;

	JAnsi24bitSkin(String file) throws IOException {
		load(file);
	}

	@Override
	public void load(String file) throws IOException {
		final BufferedImage img = ImageIO.read(this.getClass().getResource("/" + file));
		skinData = getMatrixOfImage(img);
		skinSize = new int[] { img.getWidth(), img.getHeight() };
	}

	public static int[][] getMatrixOfImage(BufferedImage bufferedImage) {

		final int width = bufferedImage.getWidth(null);
		final int height = bufferedImage.getHeight(null);
		final int[][] pixels = new int[width * height][];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				final int rgb = bufferedImage.getRGB(i, j);
				final int r = (rgb >> 16) & 0xFF;
				final int g = (rgb >> 8) & 0xFF;
				final int b = rgb & 0xFF;
				final boolean transparent = ((rgb >> 24) & 0xFF) <= 128;
				final int[] curCol = JAnsi24bitRenderer.rgbToIntArray(r, g, b);
				pixels[i + j * width] = new int[] { curCol[0], curCol[1], curCol[2], transparent ? 1 : 0 };
			}
		}

		return pixels;
	}

	@Override
	public void initialize(GraphicEngine d) {

	}

	@Override
	public void use(GraphicEngine d) {
		((JAnsi24bitRenderer) d.getRenderer()).currentSkin = this;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public int getSkinWidth() {
		return 0;
	}

	@Override
	public int getSkinHeight() {
		return 0;
	}

}

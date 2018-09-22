package it.cavallium.warppi.gui.graphicengine.impl.jansi256colors;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class JAnsi256Skin implements Skin {

	public int[] skinData;
	public int[] skinSize;

	JAnsi256Skin(String file) throws IOException {
		load(file);
	}

	@Override
	public void load(String file) throws IOException {
		final BufferedImage img = ImageIO.read(this.getClass().getResource("/" + file));
		skinData = getMatrixOfImage(img);
		skinSize = new int[] { img.getWidth(), img.getHeight() };
	}

	public static int[] getMatrixOfImage(BufferedImage bufferedImage) {
		BufferedImage after = new BufferedImage(bufferedImage.getWidth(null), bufferedImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		final AffineTransform at = new AffineTransform();
		at.scale(1f / (JAnsi256Engine.C_MUL_X), 1f / (JAnsi256Engine.C_MUL_Y));
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(bufferedImage, after);

		final int width = after.getWidth(null);
		final int height = after.getHeight(null);
		final int[] pixels = new int[width * height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				final int rgb = after.getRGB(i, j);
				final int r = (rgb >> 16) & 0xFF;
				final int g = (rgb >> 8) & 0xFF;
				final int b = rgb & 0xFF;
				final boolean transparent = ((rgb >> 24) & 0xFF) <= 128;
				pixels[i + j * width] = JAnsi256Renderer.rgbToX256(r, g, b) | (transparent ? JAnsi256Renderer.TRANSPARENT : 0);
			}
		}

		return pixels;
	}

	@Override
	public void initialize(GraphicEngine d) {

	}

	@Override
	public void use(GraphicEngine d) {
		((JAnsi256Renderer) d.getRenderer()).currentSkin = this;
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

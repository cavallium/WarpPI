package it.cavallium.warppi.gui.graphicengine.impl.jansi8colors;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class JAnsi8Skin implements Skin {

	public int[] skinData;
	public int[] skinSize;

	JAnsi8Skin(final String file) throws IOException {
		load(file);
	}

	@Override
	public void load(final String file) throws IOException {
		final BufferedImage img = ImageIO.read(this.getClass().getResource("/" + file));
		skinData = JAnsi8Skin.getMatrixOfImage(img);
		skinSize = new int[] { img.getWidth(), img.getHeight() };
	}

	public static int[] getMatrixOfImage(final BufferedImage bufferedImage) {
		BufferedImage after = new BufferedImage(bufferedImage.getWidth(null), bufferedImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		final AffineTransform at = new AffineTransform();
		at.scale(1f / JAnsi8Engine.C_MUL_X, 1f / JAnsi8Engine.C_MUL_Y);
		final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(bufferedImage, after);

		final int width = after.getWidth(null);
		final int height = after.getHeight(null);
		final int[] pixels = new int[width * height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				pixels[i + j * width] = after.getRGB(i, j);

		return pixels;
	}

	@Override
	public void initialize(final GraphicEngine d) {

	}

	@Override
	public void use(final GraphicEngine d) {
		((JAnsi8Renderer) d.getRenderer()).currentSkin = this;
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

package it.cavallium.warppi.desktop;

import java.io.InputStream;

import ar.com.hjg.pngj.ImageLineInt;
import it.cavallium.warppi.deps.Platform.PngUtils.PngReader;

public class DesktopPngReader implements PngReader {

	private ar.com.hjg.pngj.PngReader r;
	
	public DesktopPngReader(InputStream resourceStream) {
		r = new ar.com.hjg.pngj.PngReader(resourceStream);
	}

	@Override
	public int[] getImageMatrix() {
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
				} else if (channels == 3) {
					nextPixel = (scanLine[offset] << 16) | (scanLine[offset + 1] << 8) | (scanLine[offset + 2]) | (0xFF << 24);
				} else if (channels == 2) {
					nextPixel = (scanLine[offset] << 16) | (scanLine[offset + 1] << 8) | 0xFF | (0xFF << 24);
				} else {
					nextPixel = (scanLine[offset] << 16) | (scanLine[offset] << 8) | scanLine[offset] | (0xFF << 24);
				}

				// I'm placing the pixels on a memory mapped file
				pixels[pi] = nextPixel;
				pi++;
			}

		}

		return pixels;
	}

	@Override
	public int[] getSize() {
		return new int[] {r.imgInfo.cols, r.imgInfo.rows};
	}

}

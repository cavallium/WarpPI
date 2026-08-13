package it.cavallium.warppi.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class DesktopImageReaderTest {

	@Test
	void indexedMarioAtlasDecodesToItsPaletteColorsAndTransparency() throws Exception {
		try (InputStream expectedInput = getClass().getResourceAsStream("/mario-atlas.png");
				InputStream decodedInput = getClass().getResourceAsStream("/mario-atlas.png")) {
			assertNotNull(expectedInput);
			assertNotNull(decodedInput);
			final BufferedImage expected = ImageIO.read(expectedInput);
			final DesktopImageReader reader = new DesktopImageReader(decodedInput);
			final int[] decoded = reader.getImageMatrix();

			assertEquals(expected.getWidth() * expected.getHeight(), decoded.length);
			assertPixel(expected, decoded, 0, 0);
			assertPixel(expected, decoded, 404, 350);
			assertPixel(expected, decoded, 374, 336);
			assertPixel(expected, decoded, 262, 366);
			assertPixel(expected, decoded, 511, 511);
		}
	}

	private static void assertPixel(final BufferedImage expected, final int[] decoded,
			final int x, final int y) {
		assertEquals(expected.getRGB(x, y), decoded[x + y * expected.getWidth()],
				"pixel " + x + "," + y);
	}
}

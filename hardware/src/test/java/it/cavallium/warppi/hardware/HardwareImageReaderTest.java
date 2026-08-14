package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class HardwareImageReaderTest {
	private static final int WIDE_PALETTE_WIDTH = 4_928;

	@Test
	void indexedMarioGameplayAtlasDecodesToItsPaletteColorsAndTransparency() throws Exception {
		try (InputStream expectedInput = getClass().getResourceAsStream("/mario-atlas.png");
				InputStream decodedInput = getClass().getResourceAsStream("/mario-atlas.png")) {
			assertNotNull(expectedInput);
			assertNotNull(decodedInput);
			final BufferedImage expected = ImageIO.read(expectedInput);
			final HardwareImageReader reader = new HardwareImageReader(decodedInput);
			final int[] decoded = reader.getImageMatrix();

			assertEquals(expected.getWidth() * expected.getHeight(), decoded.length);
			assertPixel(expected, decoded, 0, 0);
			assertPixel(expected, decoded, 3, 192);
			assertPixel(expected, decoded, 53, 192);
			assertPixel(expected, decoded, 184, 192);
			assertPixel(expected, decoded, 255, 255);
		}
	}

	@Test
	void wideOpaquePaletteDoesNotAssumeAnRgbaExpansionBuffer() throws Exception {
		final IndexColorModel palette = new IndexColorModel(8, 2,
				new byte[] { 0, (byte) 0xFF }, new byte[] { 0, 0 },
				new byte[] { 0, 0 });
		final BufferedImage image = new BufferedImage(WIDE_PALETTE_WIDTH, 1,
				BufferedImage.TYPE_BYTE_INDEXED, palette);
		for (int x = 0; x < image.getWidth(); x++)
			image.getRaster().setSample(x, 0, 0, x & 1);
		final byte[] encoded;
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			ImageIO.write(image, "PNG", output);
			encoded = output.toByteArray();
		}

		final int[] decoded = new HardwareImageReader(
				new ByteArrayInputStream(encoded)).getImageMatrix();

		assertEquals(WIDE_PALETTE_WIDTH, decoded.length);
		assertEquals(0xFF000000, decoded[0]);
		assertEquals(0xFFFF0000, decoded[WIDE_PALETTE_WIDTH - 1]);
	}

	private static void assertPixel(final BufferedImage expected, final int[] decoded,
			final int x, final int y) {
		assertEquals(expected.getRGB(x, y), decoded[x + y * expected.getWidth()],
				"pixel " + x + "," + y);
	}
}

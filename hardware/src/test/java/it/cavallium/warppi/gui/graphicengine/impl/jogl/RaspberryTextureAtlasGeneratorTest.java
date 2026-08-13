package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RaspberryTextureAtlasGeneratorTest {

	private static final String[] SOURCE_TEXTURES = {
		"skin.png",
		"tetrisskin.png",
		"mario-atlas.png",
		"mario-background-0.png",
		"mario-background-1.png",
		"mario-background-2.png"
	};
	private static final String[] PRODUCTION_TEXTURES = {
		"skin.png",
		"skin-loading.png",
		"skin-spinner.png",
		"tetrisskin.png",
		"mario-atlas.png",
		"mario-background-0.png",
		"mario-background-1.png",
		"mario-background-2.png"
	};

	@TempDir
	Path temporaryDirectory;

	@Test
	void mavenGeneratesEveryProductionTexture() throws Exception {
		for (final String texture : PRODUCTION_TEXTURES) {
			final String packed = JOGLPackedTexture.resourcePath(texture);
			assertNotNull(getClass().getResource(packed), packed);
		}
	}

	@Test
	void generatedTexturesMatchTheTargetPrecisionAndUseTheSmallestEncoding() throws Exception {
		for (final String sourceTexture : SOURCE_TEXTURES) {
			final List<Path> generatedTextures = RaspberryTextureAtlasGenerator.generate(
					temporaryDirectory, sourceTexture);
			for (final Path generated : generatedTextures) {
				final String texture = generated.getFileName().toString().replace(".wpt", ".png");
				assertTrue(Files.isRegularFile(generated));
				try (InputStream directInput = Files.newInputStream(generated);
						InputStream fallbackInput = Files.newInputStream(generated)) {
					final BufferedImage expected = expectedTexture(texture);

					final JOGLPackedTexture.Decoded direct = JOGLPackedTexture.read(directInput, true);
					final JOGLPackedTexture.Decoded fallback = JOGLPackedTexture.read(fallbackInput, false);
					assertEquals(expected.getWidth(), direct.width());
					assertEquals(expected.getHeight(), direct.height());
					assertEquals(expected.getWidth(), fallback.width());
					assertEquals(expected.getHeight(), fallback.height());
					assertEquals(expected.getWidth() * expected.getHeight() * Short.BYTES,
							fallback.pixels().remaining());
					assertEquals(direct.pixelFormat(), fallback.pixelFormat());
					assertTargetPrecisionPixels(expected, fallback, texture);
					assertOptimalPayload(texture, expected, direct);
					expected.flush();
				}
			}
		}
	}

	private static void assertOptimalPayload(final String texture, final BufferedImage source,
			final JOGLPackedTexture.Decoded direct) {
		final int pixels = source.getWidth() * source.getHeight();
		assertTrue(direct.compressed(), texture);
		if (texture.startsWith("mario-background-")) {
			assertEquals(JOGLPackedTexture.PixelFormat.RGB5_A1, direct.pixelFormat(), texture);
			assertEquals(16 * Short.BYTES + pixels / 2,
					direct.pixels().remaining(), texture);
		} else if (texture.equals("tetrisskin.png") || texture.equals("mario-atlas.png")) {
			assertEquals(JOGLPackedTexture.PixelFormat.RGB5_A1, direct.pixelFormat(), texture);
			assertEquals(256 * Short.BYTES + pixels, direct.pixels().remaining(), texture);
		} else if (texture.equals("skin-spinner.png")) {
			assertEquals(JOGLPackedTexture.PixelFormat.RGBA4, direct.pixelFormat(), texture);
			assertEquals(256 * Short.BYTES + pixels, direct.pixels().remaining(), texture);
		} else {
			assertTrue(texture.equals("skin.png") || texture.equals("skin-loading.png"), texture);
			assertEquals(JOGLPackedTexture.PixelFormat.RGB565, direct.pixelFormat(), texture);
			assertEquals(256 * Short.BYTES + pixels, direct.pixels().remaining(), texture);
		}
	}

	private BufferedImage expectedTexture(final String texture) throws Exception {
		if (!texture.startsWith("skin-") && !texture.equals("skin.png"))
			return loadImage("/" + texture);

		final BufferedImage source = loadImage("/skin.png");
		try {
			return switch (texture) {
				case "skin.png" -> expectedMainSkin(source);
				case "skin-loading.png" -> expectedLoadingSkin(source);
				case "skin-spinner.png" -> expectedSpinnerSkin(source);
				default -> throw new AssertionError(texture);
			};
		} finally {
			source.flush();
		}
	}

	private static BufferedImage expectedMainSkin(final BufferedImage source) {
		final BufferedImage expected = new BufferedImage(512, 32, BufferedImage.TYPE_INT_RGB);
		copyComposited(source, expected, 0, 0, 512, 32, 0, 0, 0xFFC5C2AF);
		copyComposited(source, expected, 160, 21, 4, 7, 160, 21, 0xFFFFFFFF);
		copyComposited(source, expected, 172, 21, 4, 7, 172, 21, 0xFFFFFFFF);
		return expected;
	}

	private static BufferedImage expectedLoadingSkin(final BufferedImage source) {
		final BufferedImage expected = new BufferedImage(512, 64, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < expected.getHeight(); y++)
			for (int x = 0; x < expected.getWidth(); x++)
				expected.setRGB(x, y, 0xFFC5C2AF);
		copyComposited(source, expected, 31, 32, 99, 48, 0, 0, 0xFFC5C2AF);
		copyComposited(source, expected, 3, 80, 217, 48, 99, 0, 0xFFC5C2AF);
		copyComposited(source, expected, 224, 80, 158, 48, 316, 0, 0xFFC5C2AF);
		return expected;
	}

	private static BufferedImage expectedSpinnerSkin(final BufferedImage source) {
		final BufferedImage expected = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 45; y++)
			for (int x = 0; x < 44; x++)
				expected.setRGB(x, y, source.getRGB(162 + x, 33 + y));
		return expected;
	}

	private static void copyComposited(final BufferedImage source, final BufferedImage target,
			final int sourceX, final int sourceY, final int width, final int height,
			final int targetX, final int targetY, final int background) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				target.setRGB(targetX + x, targetY + y,
						compositeRgba4(source.getRGB(sourceX + x, sourceY + y), background));
	}

	private static int compositeRgba4(final int argb, final int background) {
		final int packed = JOGLPackedTexture.PixelFormat.RGBA4.packArgb(argb);
		final int alpha = (packed & 0xF) * 17;
		final int red = (packed >>> 12 & 0xF) * 17;
		final int green = (packed >>> 8 & 0xF) * 17;
		final int blue = (packed >>> 4 & 0xF) * 17;
		return 0xFF000000
				| blend(red, background >>> 16 & 0xFF, alpha) << 16
				| blend(green, background >>> 8 & 0xFF, alpha) << 8
				| blend(blue, background & 0xFF, alpha);
	}

	private static int blend(final int foreground, final int background, final int alpha) {
		return (foreground * alpha + background * (255 - alpha) + 127) / 255;
	}

	private BufferedImage loadImage(final String resource) throws Exception {
		try (InputStream input = getClass().getResourceAsStream(resource)) {
			assertNotNull(input, resource);
			final BufferedImage image = ImageIO.read(input);
			assertNotNull(image, resource);
			return image;
		}
	}

	private static void assertTargetPrecisionPixels(final BufferedImage source,
			final JOGLPackedTexture.Decoded decoded, final String texture) {
		final ByteBuffer pixels = decoded.pixels().duplicate().order(ByteOrder.nativeOrder());
		for (int y = 0; y < source.getHeight(); y++)
			for (int x = 0; x < source.getWidth(); x++) {
				final int offset = ((source.getHeight() - 1 - y) * source.getWidth() + x)
						* Short.BYTES;
				final int expected = decoded.pixelFormat().packArgb(source.getRGB(x, y));
				assertEquals(expected, pixels.getShort(offset) & 0xFFFF,
						texture + " pixel " + x + "," + y);
			}
	}
}

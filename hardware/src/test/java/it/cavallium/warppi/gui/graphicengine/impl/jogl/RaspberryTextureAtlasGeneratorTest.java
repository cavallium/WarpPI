package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
		"skin-loading.png",
		"skin-spinner.png",
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

	@Test
	void packsIgnoredGpuSpritesWhenTheirPrivateSourceIsInstalled() throws Exception {
		final String sourceResource = "/font_gputest3.png";
		assumeTrue(getClass().getResource(sourceResource) != null,
				"Private GPU-test source is not installed");
		final String packedResource = JOGLPackedTexture.resourcePath(sourceResource);
		try (InputStream sourceInput = getClass().getResourceAsStream(sourceResource);
				InputStream directInput = getClass().getResourceAsStream(packedResource);
				InputStream fallbackInput = getClass().getResourceAsStream(packedResource)) {
			assertNotNull(sourceInput, sourceResource);
			assertNotNull(directInput, packedResource);
			assertNotNull(fallbackInput, packedResource);
			final BufferedImage source = ImageIO.read(sourceInput);
			assertNotNull(source, sourceResource);
			try {
				final JOGLPackedTexture.Decoded direct = JOGLPackedTexture.read(directInput, true);
				final JOGLPackedTexture.Decoded fallback = JOGLPackedTexture.read(fallbackInput, false);
				assertEquals(2_048, direct.width());
				assertEquals(512, direct.height());
				assertEquals(JOGLPackedTexture.PixelFormat.RGB565, direct.pixelFormat());
				assertTrue(direct.compressed());
				assertEquals(256 * Short.BYTES + direct.width() * direct.height(),
						direct.pixels().remaining());
				assertEquals(direct.pixelFormat(), fallback.pixelFormat());

				final int frames = source.getWidth() / RaspberryTextureAtlasGenerator.GPU_TEST_FRAME_WIDTH;
				final int columns = direct.width() / RaspberryTextureAtlasGenerator.GPU_TEST_FRAME_WIDTH;
				for (int frame = 0; frame < frames; frame++)
					for (final int x : new int[] { 0, 112, 223 })
						for (final int y : new int[] { 0, 64, 127 })
							assertTargetPrecisionPixel(source.getRGB(
									frame * RaspberryTextureAtlasGenerator.GPU_TEST_FRAME_WIDTH + x, y),
									fallback,
									frame % columns * RaspberryTextureAtlasGenerator.GPU_TEST_FRAME_WIDTH + x,
									frame / columns * RaspberryTextureAtlasGenerator.GPU_TEST_FRAME_HEIGHT + y,
									sourceResource + " frame " + frame + " pixel " + x + "," + y);
			} finally {
				source.flush();
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
		return loadImage("/" + texture);
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

	private static void assertTargetPrecisionPixel(final int sourceArgb,
			final JOGLPackedTexture.Decoded decoded, final int x, final int y,
			final String message) {
		final ByteBuffer pixels = decoded.pixels().duplicate().order(ByteOrder.nativeOrder());
		final int offset = ((decoded.height() - 1 - y) * decoded.width() + x) * Short.BYTES;
		final int expected = decoded.pixelFormat().packArgb(sourceArgb);
		assertEquals(expected, pixels.getShort(offset) & 0xFFFF, message);
	}
}

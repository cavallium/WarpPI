package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/** Generates Raspberry-only, directly uploadable textures during Maven builds. */
public final class RaspberryTextureAtlasGenerator {
	private static final String GPU_TEST_SPRITES_RESOURCE = "/font_gputest3.png";
	static final int GPU_TEST_FRAME_WIDTH = 224;
	static final int GPU_TEST_FRAME_HEIGHT = 128;

	private RaspberryTextureAtlasGenerator() {}

	public static void main(final String[] args) throws IOException {
		if (args.length < 2)
			throw new IllegalArgumentException(
					"Usage: RaspberryTextureAtlasGenerator <output-directory> <png-resource>...");

		final Path outputDirectory = Path.of(args[0]);
		Files.createDirectories(outputDirectory);
		for (int i = 1; i < args.length; i++)
			generate(outputDirectory, args[i]);
	}

	static List<Path> generate(final Path outputDirectory, String resource) throws IOException {
		resource = normalizeResource(resource);
		final BufferedImage source;
		try (InputStream input = RaspberryTextureAtlasGenerator.class.getResourceAsStream(resource)) {
			if (input == null)
				throw new IOException("Missing production texture resource: " + resource);
			source = ImageIO.read(input);
		}
		if (source == null)
			throw new IOException("Unsupported production texture: " + resource);

		final BufferedImage production = prepareProductionTexture(resource, source);
		try {
			return List.of(generateImage(outputDirectory, resource, production));
		} finally {
			if (production != source)
				production.flush();
			source.flush();
		}
	}

	static BufferedImage prepareProductionTexture(final String resource,
			final BufferedImage source) throws IOException {
		if (!GPU_TEST_SPRITES_RESOURCE.equals(resource))
			return source;
		if (source.getHeight() != GPU_TEST_FRAME_HEIGHT
				|| source.getWidth() % GPU_TEST_FRAME_WIDTH != 0)
			throw new IOException("Unexpected GPU-test sprite dimensions: "
					+ source.getWidth() + "x" + source.getHeight());

		final int frames = source.getWidth() / GPU_TEST_FRAME_WIDTH;
		final int columns = Math.min(frames,
				JOGLPackedTexture.MAX_TEXTURE_DIMENSION / GPU_TEST_FRAME_WIDTH);
		final int rows = (frames + columns - 1) / columns;
		final int atlasWidth = nextPowerOfTwo(columns * GPU_TEST_FRAME_WIDTH);
		final int atlasHeight = nextPowerOfTwo(rows * GPU_TEST_FRAME_HEIGHT);
		if (atlasWidth > JOGLPackedTexture.MAX_TEXTURE_DIMENSION
				|| atlasHeight > JOGLPackedTexture.MAX_TEXTURE_DIMENSION)
			throw new IOException("GPU-test sprites do not fit the Raspberry texture limit");

		final int imageType = source.getColorModel().hasAlpha()
				? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		final BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, imageType);
		final int[] framePixels = new int[GPU_TEST_FRAME_WIDTH * GPU_TEST_FRAME_HEIGHT];
		for (int frame = 0; frame < frames; frame++) {
			source.getRGB(frame * GPU_TEST_FRAME_WIDTH, 0,
					GPU_TEST_FRAME_WIDTH, GPU_TEST_FRAME_HEIGHT,
					framePixels, 0, GPU_TEST_FRAME_WIDTH);
			atlas.setRGB(frame % columns * GPU_TEST_FRAME_WIDTH,
					frame / columns * GPU_TEST_FRAME_HEIGHT,
					GPU_TEST_FRAME_WIDTH, GPU_TEST_FRAME_HEIGHT,
					framePixels, 0, GPU_TEST_FRAME_WIDTH);
		}
		return atlas;
	}

	private static int nextPowerOfTwo(final int value) {
		return value > 1 ? Integer.highestOneBit(value - 1) << 1 : 1;
	}

	private static Path generateImage(final Path outputDirectory, final String resource,
			final BufferedImage image) throws IOException {

		final int width = image.getWidth();
		final int height = image.getHeight();
		final int pixels = Math.multiplyExact(width, height);
		final JOGLPackedTexture.Encoding encoding = chooseEncoding(image);
		final String relativeTarget = resource.substring(1, resource.length() - 4) + ".wpt";
		final Path target = outputDirectory.resolve(relativeTarget);
		Files.createDirectories(target.getParent());
		final Path temporary = Files.createTempFile(target.getParent(), ".texture-", ".tmp");
		try {
			write(image, encoding, temporary);
			if (Files.isRegularFile(target) && Files.mismatch(temporary, target) == -1) {
				System.out.println("[texture-atlas] Up to date: " + relativeTarget);
				return target;
			}
			try {
				Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (final AtomicMoveNotSupportedException exception) {
				Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}

		System.out.println("[texture-atlas] Generated " + relativeTarget + " ("
				+ width + "x" + height + ", " + encoding
				+ ", " + encoding.payloadBytes(pixels) + " upload bytes)");
		return target;
	}

	private static String normalizeResource(String resource) throws IOException {
		resource = resource.replace('\\', '/');
		if (!resource.startsWith("/"))
			resource = "/" + resource;
		if (!resource.endsWith(".png") || resource.contains("..")
				|| !resource.matches("/[A-Za-z0-9_./-]+\\.png"))
			throw new IOException("Invalid PNG resource name: " + resource);
		return resource;
	}

	private static JOGLPackedTexture.Encoding chooseEncoding(final BufferedImage image) {
		final JOGLPackedTexture.PixelFormat pixelFormat = choosePixelFormat(image);
		final Map<Integer, Integer> palette = collectPalette(image, pixelFormat, 257);
		if (palette.size() <= 16)
			return JOGLPackedTexture.Encoding.forPixelFormat(pixelFormat, 16);
		if (palette.size() <= 256)
			return JOGLPackedTexture.Encoding.forPixelFormat(pixelFormat, 256);
		return JOGLPackedTexture.Encoding.forPixelFormat(pixelFormat, 0);
	}

	private static JOGLPackedTexture.PixelFormat choosePixelFormat(final BufferedImage image) {
		boolean opaque = true;
		boolean binaryAlpha = true;
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++) {
				final int alpha = image.getRGB(x, y) >>> 24;
				if (alpha != 0xFF)
					opaque = false;
				if (alpha != 0 && alpha != 0xFF)
					binaryAlpha = false;
			}
		if (opaque)
			return JOGLPackedTexture.PixelFormat.RGB565;
		if (binaryAlpha)
			return JOGLPackedTexture.PixelFormat.RGB5_A1;
		return JOGLPackedTexture.PixelFormat.RGBA4;
	}

	private static void write(final BufferedImage image,
			final JOGLPackedTexture.Encoding encoding, final Path target) throws IOException {
		try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(
				Files.newOutputStream(target)))) {
			JOGLPackedTexture.writeHeader(output, image.getWidth(), image.getHeight(), encoding);
			if (!encoding.isPaletted()) {
				writePackedPixels(image, output, encoding.pixelFormat);
				return;
			}

			final Map<Integer, Integer> palette = collectPalette(image,
					encoding.pixelFormat, encoding.paletteEntries);
			if (palette.size() > encoding.paletteEntries)
				throw new IOException("Texture no longer fits selected palette encoding");
			for (final int packedPixel : palette.keySet())
				writePacked16(output, packedPixel);
			for (int i = palette.size(); i < encoding.paletteEntries; i++)
				writePacked16(output, 0);
			writeIndexes(image, output, palette, encoding);
		}
	}

	private static Map<Integer, Integer> collectPalette(final BufferedImage image,
			final JOGLPackedTexture.PixelFormat pixelFormat, final int limit) {
		final Map<Integer, Integer> palette = new LinkedHashMap<>();
		for (int y = image.getHeight() - 1; y >= 0 && palette.size() < limit; y--)
			for (int x = 0; x < image.getWidth() && palette.size() < limit; x++) {
				final int packedPixel = pixelFormat.packArgb(image.getRGB(x, y));
				palette.computeIfAbsent(packedPixel, ignored -> palette.size());
			}
		return palette;
	}

	private static void writePackedPixels(final BufferedImage image,
			final DataOutputStream output, final JOGLPackedTexture.PixelFormat pixelFormat)
			throws IOException {
		for (int y = image.getHeight() - 1; y >= 0; y--)
			for (int x = 0; x < image.getWidth(); x++)
				writePacked16(output, pixelFormat.packArgb(image.getRGB(x, y)));
	}

	private static void writeIndexes(final BufferedImage image, final DataOutputStream output,
			final Map<Integer, Integer> palette, final JOGLPackedTexture.Encoding encoding)
			throws IOException {
		int pendingHighNibble = -1;
		for (int y = image.getHeight() - 1; y >= 0; y--)
			for (int x = 0; x < image.getWidth(); x++) {
				final int packedPixel = encoding.pixelFormat.packArgb(image.getRGB(x, y));
				final Integer index = palette.get(packedPixel);
				if (index == null)
					throw new IOException("Texture pixel is missing from its palette");
				if (encoding.paletteEntries == 256) {
					output.writeByte(index);
				} else if (pendingHighNibble < 0) {
					pendingHighNibble = index << 4;
				} else {
					output.writeByte(pendingHighNibble | index);
					pendingHighNibble = -1;
				}
			}
		if (pendingHighNibble >= 0)
			output.writeByte(pendingHighNibble);
	}

	private static void writePacked16(final DataOutputStream output, final int packedPixel)
			throws IOException {
		// The production ARMv6 and supported desktop development hosts are
		// little-endian. The runtime disables direct paletted upload elsewhere.
		output.writeByte(packedPixel);
		output.writeByte(packedPixel >>> 8);
	}

}

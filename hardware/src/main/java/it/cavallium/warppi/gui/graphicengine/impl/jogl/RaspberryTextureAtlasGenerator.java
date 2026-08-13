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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/** Generates Raspberry-only, directly uploadable textures during Maven builds. */
public final class RaspberryTextureAtlasGenerator {
	private static final String UI_SKIN_RESOURCE = "/skin.png";
	private static final int UI_BACKGROUND = 0xFFC5C2AF;
	private static final int UI_MAIN_WIDTH = 512;
	private static final int UI_MAIN_HEIGHT = 32;
	private static final int UI_LOADING_WIDTH = 512;
	private static final int UI_LOADING_HEIGHT = 64;
	private static final int UI_SPINNER_WIDTH = 64;
	private static final int UI_SPINNER_HEIGHT = 64;

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

		try {
			if (UI_SKIN_RESOURCE.equals(resource))
				return generateUiTextures(outputDirectory, source);
			return List.of(generateImage(outputDirectory, resource, source));
		} finally {
			source.flush();
		}
	}

	private static List<Path> generateUiTextures(final Path outputDirectory,
			final BufferedImage source) throws IOException {
		if (source.getWidth() != 512 || source.getHeight() != 128)
			throw new IOException("Unexpected UI skin dimensions: "
					+ source.getWidth() + "x" + source.getHeight());

		final List<GeneratedImage> generatedImages = List.of(
				new GeneratedImage("/skin.png", createMainUiTexture(source)),
				new GeneratedImage("/skin-loading.png", createLoadingUiTexture(source)),
				new GeneratedImage("/skin-spinner.png", createSpinnerUiTexture(source)));
		final List<Path> generatedPaths = new ArrayList<>(generatedImages.size());
		try {
			for (final GeneratedImage generated : generatedImages)
				generatedPaths.add(generateImage(outputDirectory,
						generated.resource(), generated.image()));
		} finally {
			for (final GeneratedImage generated : generatedImages)
				generated.image().flush();
		}
		return List.copyOf(generatedPaths);
	}

	private static BufferedImage createMainUiTexture(final BufferedImage source) {
		final BufferedImage output = opaqueImage(UI_MAIN_WIDTH, UI_MAIN_HEIGHT);
		copyComposited(source, output, 0, 0, UI_MAIN_WIDTH, UI_MAIN_HEIGHT,
				0, 0, UI_BACKGROUND);
		// These two arrows are drawn inside the white variable popup.
		copyComposited(source, output, 160, 21, 4, 7, 160, 21, 0xFFFFFFFF);
		copyComposited(source, output, 172, 21, 4, 7, 172, 21, 0xFFFFFFFF);
		return output;
	}

	private static BufferedImage createLoadingUiTexture(final BufferedImage source) {
		final BufferedImage output = opaqueImage(UI_LOADING_WIDTH, UI_LOADING_HEIGHT);
		fill(output, UI_BACKGROUND);
		// Transparent padding is omitted and the three opaque results share one row.
		copyComposited(source, output, 31, 32, 99, 48, 0, 0, UI_BACKGROUND);
		copyComposited(source, output, 3, 80, 217, 48, 99, 0, UI_BACKGROUND);
		copyComposited(source, output, 224, 80, 158, 48, 316, 0, UI_BACKGROUND);
		return output;
	}

	private static BufferedImage createSpinnerUiTexture(final BufferedImage source) {
		final BufferedImage output = new BufferedImage(UI_SPINNER_WIDTH,
				UI_SPINNER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		copy(source, output, 162, 33, 44, 45, 0, 0);
		return output;
	}

	private static BufferedImage opaqueImage(final int width, final int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	private static void fill(final BufferedImage image, final int argb) {
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++)
				image.setRGB(x, y, argb);
	}

	private static void copy(final BufferedImage source, final BufferedImage target,
			final int sourceX, final int sourceY, final int width, final int height,
			final int targetX, final int targetY) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				target.setRGB(targetX + x, targetY + y,
						source.getRGB(sourceX + x, sourceY + y));
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
		final int rgba4 = JOGLPackedTexture.PixelFormat.RGBA4.packArgb(argb);
		final int alpha = (rgba4 & 0xF) * 17;
		final int red = (rgba4 >>> 12 & 0xF) * 17;
		final int green = (rgba4 >>> 8 & 0xF) * 17;
		final int blue = (rgba4 >>> 4 & 0xF) * 17;
		final int outputRed = blend(red, background >>> 16 & 0xFF, alpha);
		final int outputGreen = blend(green, background >>> 8 & 0xFF, alpha);
		final int outputBlue = blend(blue, background & 0xFF, alpha);
		return 0xFF000000 | outputRed << 16 | outputGreen << 8 | outputBlue;
	}

	private static int blend(final int foreground, final int background, final int alpha) {
		return (foreground * alpha + background * (255 - alpha) + 127) / 255;
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

	private record GeneratedImage(String resource, BufferedImage image) {}
}

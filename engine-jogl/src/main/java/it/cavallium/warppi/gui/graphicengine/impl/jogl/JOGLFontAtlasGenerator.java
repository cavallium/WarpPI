package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/** Generates the JOGL-only font atlases during Maven's process-classes phase. */
public final class JOGLFontAtlasGenerator {

	private JOGLFontAtlasGenerator() {}

	public static void main(final String[] args) throws IOException {
		if (args.length < 2)
			throw new IllegalArgumentException("Usage: JOGLFontAtlasGenerator <output-directory> <font-name>...");

		final Path outputDirectory = Path.of(args[0]);
		Files.createDirectories(outputDirectory);
		for (int i = 1; i < args.length; i++)
			generate(outputDirectory, args[i]);
	}

	static Path generate(final Path outputDirectory, final String fontName) throws IOException {
		if (!fontName.matches("[A-Za-z0-9_-]+"))
			throw new IOException("Invalid font name: " + fontName);
		final String sourceResource = "/font_" + fontName + ".rft";
		final JOGLFontAtlas atlas;
		try (InputStream input = JOGLFontAtlasGenerator.class.getResourceAsStream(sourceResource)) {
			if (input == null)
				throw new IOException("Missing font resource: " + sourceResource);
			atlas = JOGLFontAtlas.fromRft(input);
		}

		final byte[] encoded;
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			atlas.write(output);
			encoded = output.toByteArray();
		}

		Files.createDirectories(outputDirectory);
		final Path target = outputDirectory.resolve("font_" + fontName + ".wfa");
		if (Files.isRegularFile(target) && Arrays.equals(Files.readAllBytes(target), encoded)) {
			System.out.println("[font-atlas] Up to date: " + target.getFileName());
			return target;
		}

		final Path temporary = Files.createTempFile(outputDirectory, ".font_" + fontName + "-", ".tmp");
		try {
			Files.write(temporary, encoded);
			try {
				Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (final AtomicMoveNotSupportedException ex) {
				Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
		System.out.println("[font-atlas] Generated " + target.getFileName() + " (" + atlas.textureW + "x"
				+ atlas.textureH + ", 8-bit alpha)");
		return target;
	}
}

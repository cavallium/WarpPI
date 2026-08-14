package it.cavallium.warppi.gui.graphicengine.impl.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.desktop.DesktopPlatform;

class SwingExternalAssetPathTest {

	@Test
	void cpuRendererLoadsRelativeExternalSkinsAndFontsWithoutBreakingResources()
			throws Exception {
		final Path target = Path.of("target");
		Files.createDirectories(target);
		final Path assetDirectory = Files.createTempDirectory(target, "cpu-assets-");
		assertFalse(assetDirectory.isAbsolute());
		final Path texture = assetDirectory.resolve("sprite.png");
		final Path font = assetDirectory.resolve("font_external.rft");

		final IndexColorModel palette = new IndexColorModel(8, 2,
				new byte[] { 0, (byte) 0xFF }, new byte[] { 0, 0 },
				new byte[] { 0, 0 });
		final BufferedImage image = new BufferedImage(4_928, 1,
				BufferedImage.TYPE_BYTE_INDEXED, palette);
		for (int x = 0; x < image.getWidth(); x++)
			image.getRaster().setSample(x, 0, 0, x & 1);
		assertTrue(ImageIO.write(image, "PNG", texture.toFile()));
		try (InputStream source = getClass().getResourceAsStream("/font_smal.rft")) {
			assertNotNull(source);
			Files.copy(source, font);
		}

		final Field platformField = WarpPI.class.getDeclaredField("platform");
		platformField.setAccessible(true);
		final Platform previousPlatform = (Platform) platformField.get(null);
		try {
			platformField.set(null, new DesktopPlatform());
			final SwingSkin externalSkin = new SwingSkin(texture.toString());
			final SwingFont externalFont = new SwingFont(assetDirectory.toString(), "external");
			final SwingSkin resourceSkin = new SwingSkin("skin.png");
			final SwingFont resourceFont = new SwingFont("smal");

			assertEquals(4_928, externalSkin.getSkinWidth());
			assertEquals(1, externalSkin.getSkinHeight());
			assertTrue(externalFont.getCharacterWidth() > 0);
			assertTrue(externalFont.getCharacterHeight() > 0);
			assertTrue(resourceSkin.getSkinWidth() > 0);
			assertTrue(resourceSkin.getSkinHeight() > 0);
			assertTrue(resourceFont.getCharacterWidth() > 0);
			assertTrue(resourceFont.getCharacterHeight() > 0);
		} finally {
			platformField.set(null, previousPlatform);
			Files.deleteIfExists(font);
			Files.deleteIfExists(texture);
			Files.deleteIfExists(assetDirectory);
		}
	}
}

package it.cavallium.warppi.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistentSettingsTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void survivesReconstructionThroughThePropertiesStore() {
		final Path file = temporaryDirectory.resolve("settings.properties");
		final PersistentSettings first = new PersistentSettings(
				new PropertiesSettingsStore(file), false);
		assertFalse(first.isDebugEnabled());
		assertEquals(0.2d, first.getBrightness());

		first.setDebugEnabled(true);
		first.setBrightness(0.7d);

		final PersistentSettings restored = new PersistentSettings(
				new PropertiesSettingsStore(file), false);
		assertTrue(restored.isDebugEnabled());
		assertEquals(0.7d, restored.getBrightness());
	}

	@Test
	void rejectsBrightnessOutsideTheSupportedRange() {
		final PersistentSettings settings = new PersistentSettings(
				new PropertiesSettingsStore(temporaryDirectory.resolve("settings.properties")), false);

		assertThrows(IllegalArgumentException.class, () -> settings.setBrightness(-0.1d));
		assertThrows(IllegalArgumentException.class, () -> settings.setBrightness(1.1d));
		assertThrows(IllegalArgumentException.class, () -> settings.setBrightness(Double.NaN));
	}

	@Test
	void remainsUsableWhenPersistenceIsUnavailable() {
		final SettingsStore unavailableStore = new SettingsStore() {
			@Override
			public String get(final String key) throws IOException {
				throw new IOException("read only");
			}

			@Override
			public void put(final String key, final String value) throws IOException {
				throw new IOException("read only");
			}
		};
		final PersistentSettings settings = new PersistentSettings(unavailableStore, false);

		settings.setDebugEnabled(true);
		settings.setBrightness(0.5d);
		assertTrue(settings.isDebugEnabled());
		assertEquals(0.5d, settings.getBrightness());
	}
}

package it.cavallium.warppi.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

	@Test
	void skipsPersistenceWhenASettingIsUnchanged() {
		final CountingStore store = new CountingStore();
		final PersistentSettings settings = new PersistentSettings(store, false);

		settings.setDebugEnabled(false);
		settings.setBrightness(0.2d);
		assertEquals(0, store.writes);

		settings.setDebugEnabled(true);
		settings.setDebugEnabled(true);
		settings.setBrightness(0.3d);
		settings.setBrightness(0.3d);
		assertEquals(2, store.writes);
	}

	@Test
	void retriesAnUnchangedSettingAfterPersistenceRecovers() {
		final CountingStore store = new CountingStore();
		final PersistentSettings settings = new PersistentSettings(store, false);

		store.failWrites = true;
		settings.setDebugEnabled(true);
		assertTrue(settings.isDebugEnabled());
		assertEquals(1, store.writeAttempts);

		store.failWrites = false;
		settings.setDebugEnabled(true);
		assertEquals(2, store.writeAttempts);
		assertEquals("true", store.values.get(PersistentSettings.DEBUG_KEY));
	}

	private static final class CountingStore implements SettingsStore {
		private final Map<String, String> values = new HashMap<>();
		private int writes;
		private int writeAttempts;
		private boolean failWrites;

		@Override
		public String get(final String key) {
			return values.get(key);
		}

		@Override
		public void put(final String key, final String value) throws IOException {
			writeAttempts++;
			if (failWrites)
				throw new IOException("temporarily unavailable");
			values.put(key, value);
			writes++;
		}
	}
}

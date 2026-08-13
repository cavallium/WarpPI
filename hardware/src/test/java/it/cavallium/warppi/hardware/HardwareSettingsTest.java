package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HardwareSettingsTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void persistsACompactBinarySnapshot() throws IOException {
		final Path file = temporaryDirectory.resolve("settings.bin");
		final HardwareSettings settings = new HardwareSettings(file);

		assertFalse(settings.isDebugEnabled());
		assertEquals(205d / 1_024d, settings.getBrightness());
		settings.setDebugEnabled(true);
		settings.setBrightness(0.7346d);

		assertEquals(12, Files.size(file));
		final HardwareSettings restored = new HardwareSettings(file);
		assertTrue(restored.isDebugEnabled());
		assertEquals(753d / 1_024d, restored.getBrightness());
	}

	@Test
	void rejectsInvalidBrightnessBeforeWriting() {
		final Path file = temporaryDirectory.resolve("settings.bin");
		final HardwareSettings settings = new HardwareSettings(file);

		assertThrows(IllegalArgumentException.class, () -> settings.setBrightness(-0.1d));
		assertThrows(IllegalArgumentException.class, () -> settings.setBrightness(1.1d));
		assertThrows(IllegalArgumentException.class, () -> settings.setBrightness(Double.NaN));
		assertFalse(Files.exists(file));
	}

	@Test
	void doesNotWriteAnUnchangedQuantizedSnapshot() {
		final Path file = temporaryDirectory.resolve("settings.bin");
		final HardwareSettings settings = new HardwareSettings(file);

		settings.setDebugEnabled(false);
		settings.setBrightness(0.2d);

		assertFalse(Files.exists(file));
	}

	@Test
	void retriesAnUnchangedSnapshotAfterStorageRecovers() throws IOException {
		final Path file = temporaryDirectory.resolve("settings.bin");
		Files.createDirectory(file);
		final HardwareSettings settings = new HardwareSettings(file);

		settings.setDebugEnabled(true);
		assertTrue(settings.isDebugEnabled());
		assertTrue(Files.isDirectory(file));

		Files.delete(file);
		settings.setDebugEnabled(true);
		assertTrue(Files.isRegularFile(file));
		assertTrue(new HardwareSettings(file).isDebugEnabled());
	}

	@Test
	void fallsBackSafelyWhenTheSnapshotIsCorrupt() throws IOException {
		final Path file = temporaryDirectory.resolve("settings.bin");
		final HardwareSettings settings = new HardwareSettings(file);
		settings.setDebugEnabled(true);
		settings.setBrightness(0.8d);
		final byte[] corrupt = Files.readAllBytes(file);
		corrupt[6] ^= 1;
		Files.write(file, corrupt);

		final HardwareSettings recovered = new HardwareSettings(file);

		assertFalse(recovered.isDebugEnabled());
		assertEquals(205d / 1_024d, recovered.getBrightness());
	}

	@Test
	void ignoresTheRemovedPropertiesFormat() throws IOException {
		final Path file = temporaryDirectory.resolve("settings.bin");
		Files.writeString(file, "debug=true\nbrightness=0.9\n");

		final HardwareSettings settings = new HardwareSettings(file);

		assertFalse(settings.isDebugEnabled());
		assertEquals(205d / 1_024d, settings.getBrightness());
	}
}

package it.cavallium.warppi.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;

/** JVM settings store using one atomically replaced properties file. */
public final class PropertiesSettingsStore implements SettingsStore {
	private final Path file;

	public PropertiesSettingsStore(final Path file) {
		this.file = Objects.requireNonNull(file).toAbsolutePath().normalize();
	}

	@Override
	public synchronized String get(final String key) throws IOException {
		return load().getProperty(Objects.requireNonNull(key));
	}

	@Override
	public synchronized void put(final String key, final String value) throws IOException {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		final Properties properties = load();
		properties.setProperty(key, value);
		final Path directory = file.getParent();
		if (directory != null)
			Files.createDirectories(directory);
		final Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
		try {
			try (OutputStream output = Files.newOutputStream(temporary,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				properties.store(output, "WarpPI persistent settings");
			}
			try {
				Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (final IOException atomicMoveFailure) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private Properties load() throws IOException {
		final Properties properties = new Properties();
		if (Files.isRegularFile(file)) {
			try (InputStream input = Files.newInputStream(file)) {
				properties.load(input);
			}
		}
		return properties;
	}
}

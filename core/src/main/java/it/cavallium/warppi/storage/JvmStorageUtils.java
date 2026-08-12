package it.cavallium.warppi.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import it.cavallium.warppi.Platform.StorageUtils;
import it.cavallium.warppi.util.ClassUtils;

/**
 * File and class-path resource access shared by the desktop and Raspberry JVMs.
 */
public class JvmStorageUtils implements StorageUtils {
	private final Map<String, File> resourceCache = new HashMap<>();

	@Override
	public boolean exists(final File file) {
		return Objects.requireNonNull(file).exists();
	}

	@Override
	public File get(final String path) {
		return Paths.get(Objects.requireNonNull(path)).toFile();
	}

	@Override
	public File get(final String... path) {
		Objects.requireNonNull(path);
		if (path.length == 0)
			throw new IllegalArgumentException("At least one path component is required");
		return Paths.get(path[0], Arrays.copyOfRange(path, 1, path.length)).toFile();
	}

	@Override
	@Deprecated
	public synchronized File getResource(final String path) throws IOException, URISyntaxException {
		final String resourceName = normalizeResourceName(path);
		final URL resource = findResource(resourceName);
		if (resource == null)
			return fallbackPath(path).toFile();
		if ("file".equalsIgnoreCase(resource.getProtocol()))
			return Paths.get(resource.toURI()).toFile();

		final File cached = resourceCache.get(resourceName);
		if (cached != null && cached.isFile())
			return cached;

		final Path temporaryFile = Files.createTempFile("warppi-resource-", ".bin");
		try (InputStream input = resource.openStream()) {
			Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException | RuntimeException exception) {
			Files.deleteIfExists(temporaryFile);
			throw exception;
		}
		final File extracted = temporaryFile.toFile();
		extracted.deleteOnExit();
		resourceCache.put(resourceName, extracted);
		return extracted;
	}

	@Override
	public boolean doesResourceExist(final String path) {
		return findResource(normalizeResourceName(path)) != null || Files.exists(fallbackPath(path));
	}

	@Override
	public InputStream getResourceStream(final String path) throws IOException {
		final URL resource = findResource(normalizeResourceName(path));
		return resource != null ? resource.openStream() : Files.newInputStream(fallbackPath(path));
	}

	@Override
	public List<String> readAllLines(final File file) throws IOException {
		return Files.readAllLines(Objects.requireNonNull(file).toPath(), StandardCharsets.UTF_8);
	}

	@Override
	public String read(final InputStream input) throws IOException {
		try (input) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Override
	public List<File> walk(final File directory) throws IOException {
		try (Stream<Path> paths = Files.walk(Objects.requireNonNull(directory).toPath())) {
			return paths.filter(Files::isRegularFile).sorted().map(Path::toFile).toList();
		}
	}

	@Override
	public File relativize(final File base, final File file) {
		return Objects.requireNonNull(base).toPath().relativize(Objects.requireNonNull(file).toPath()).toFile();
	}

	@Override
	public File resolve(final File file, final String child) {
		return Objects.requireNonNull(file).toPath().resolve(Objects.requireNonNull(child)).toFile();
	}

	@Override
	public File getParent(final File file) {
		final Path parent = Objects.requireNonNull(file).toPath().getParent();
		return parent == null ? null : parent.toFile();
	}

	@Override
	public void createDirectories(final File directory) throws IOException {
		Files.createDirectories(Objects.requireNonNull(directory).toPath());
	}

	@Override
	public void write(final File file, final byte[] bytes, final int... options) throws IOException {
		Objects.requireNonNull(file);
		Objects.requireNonNull(bytes);
		Objects.requireNonNull(options);
		final List<StandardOpenOption> nioOptions = new ArrayList<>(options.length);
		for (final int option : options) {
			switch (option) {
				case StorageUtils.OpenOptionWrite -> nioOptions.add(StandardOpenOption.WRITE);
				case StorageUtils.OpenOptionCreate -> nioOptions.add(StandardOpenOption.CREATE);
				default -> throw new IllegalArgumentException("Unknown storage open option " + option);
			}
		}
		Files.write(file.toPath(), bytes, nioOptions.toArray(StandardOpenOption[]::new));
	}

	@Override
	public List<String> readAllLines(final InputStream input) throws IOException {
		try (input) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
		}
	}

	@Override
	public String getBasePath() {
		return "";
	}

	/**
	 * Finds a root-relative class-path resource. Overridable for isolated tests.
	 */
	protected URL findResource(final String resourceName) {
		final Class<?> anchor = ClassUtils.classLoader;
		if (anchor != null) {
			final URL resource = anchor.getResource('/' + resourceName);
			if (resource != null)
				return resource;
		}
		return JvmStorageUtils.class.getResource('/' + resourceName);
	}

	private static String normalizeResourceName(final String path) {
		String normalized = Objects.requireNonNull(path).replace('\\', '/');
		while (normalized.startsWith("/"))
			normalized = normalized.substring(1);
		return normalized;
	}

	private static Path fallbackPath(final String path) {
		return Paths.get(Objects.requireNonNull(path));
	}
}

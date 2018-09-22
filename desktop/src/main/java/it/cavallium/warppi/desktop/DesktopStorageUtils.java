package it.cavallium.warppi.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import it.cavallium.warppi.Platform.StorageUtils;
import it.cavallium.warppi.util.ClassUtils;

public class DesktopStorageUtils implements StorageUtils {
	@Override
	public boolean exists(final File f) {
		return f.exists();
	}

	@Override
	public File get(final String path) {
		return Paths.get(path).toFile();
	}

	@Override
	public File get(final String... path) {
		if (path.length <= 1)
			return Paths.get(path[0]).toFile();
		else
			return Paths.get(path[0], Arrays.copyOfRange(path, 1, path.length)).toFile();
	}

	private final Map<String, File> resourcesCache = new HashMap<>();

	@Override
	@Deprecated()
	public File getResource(final String string) throws IOException, URISyntaxException {
		final URL res = ClassUtils.classLoader.getResource(string);
		final boolean isResource = res != null;
		if (isResource)
			try {
				final URI uri = res.toURI();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					if (resourcesCache.containsKey(string)) {
						File f;
						if ((f = resourcesCache.get(string)).exists())
							return f;
						else
							resourcesCache.remove(string);
					}
					try {
						FileSystems.newFileSystem(uri, Collections.emptyMap());
					} catch (final FileSystemAlreadyExistsException e) {
						FileSystems.getFileSystem(uri);
					}
					final Path myFolderPath = Paths.get(uri);

					final InputStream is = Files.newInputStream(myFolderPath);
					final File tempFile = File.createTempFile("picalcresource-", "");
					tempFile.deleteOnExit();
					try (FileOutputStream out = new FileOutputStream(tempFile)) {
						IOUtils.copy(is, out, (int) tempFile.length());
					}
					resourcesCache.put(string, tempFile);

					return tempFile;
				} else
					return Paths.get(uri).toFile();
			} catch (final java.lang.IllegalArgumentException e) {
				throw e;
			}
		else
			return Paths.get(string.substring(1)).toFile();
	}

	@Override
	public InputStream getResourceStream(String string) throws IOException, URISyntaxException {
		final URL res = ClassUtils.classLoader.getResource(string);
		final boolean isResource = res != null;
		if (isResource)
			try {
				final URI uri = res.toURI();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					try {
						FileSystems.newFileSystem(uri, Collections.emptyMap());
					} catch (final FileSystemAlreadyExistsException e) {
						FileSystems.getFileSystem(uri);
					}
					final Path myFolderPath = Paths.get(uri);
					return Files.newInputStream(myFolderPath);
				} else
					return Files.newInputStream(Paths.get(uri));
			} catch (final java.lang.IllegalArgumentException e) {
				throw e;
			}
		else {
			if (string.length() > 0) {
				final char ch = string.charAt(0);
				if (ch == '/' || ch == File.separatorChar)
					string = string.substring(1);
			}
			return Files.newInputStream(Paths.get(string));
		}
	}

	@Override
	public List<String> readAllLines(final File file) throws IOException {
		return Files.readAllLines(file.toPath());
	}

	@Override
	public String read(final InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

	@Override
	public List<File> walk(final File dir) throws IOException {
		final List<File> out = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(dir.toPath())) {
			paths.filter(Files::isRegularFile).forEach((final Path p) -> {
				out.add(p.toFile());
			});
		}
		return out;
	}

	@Override
	public File relativize(final File rulesPath, final File f) {
		return rulesPath.toPath().relativize(f.toPath()).toFile();
	}

	@Override
	public File resolve(final File file, final String string) {
		return file.toPath().resolve(string).toFile();
	}

	@Override
	public File getParent(final File f) {
		return f.toPath().getParent().toFile();
	}

	@Override
	public void createDirectories(final File dir) throws IOException {
		Files.createDirectories(dir.toPath());
	}

	@Override
	public void write(final File f, final byte[] bytes, final int... options) throws IOException {
		final StandardOpenOption[] noptions = new StandardOpenOption[options.length];
		int i = 0;
		for (final int opt : options) {
			switch (opt) {
				case StorageUtils.OpenOptionCreate: {
					noptions[i] = StandardOpenOption.CREATE;
					break;
				}
				case StorageUtils.OpenOptionWrite: {
					noptions[i] = StandardOpenOption.WRITE;
					break;
				}
				default: {
					break;
				}
			}
			i++;
		}
		Files.write(f.toPath(), bytes, noptions);
	}

	@Override
	public List<String> readAllLines(final InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.toList());
		}
	}

	@Override
	public String getBasePath() {
		return "";
	}
}

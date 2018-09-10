package it.cavallium.warppi.hardware;

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

import it.cavallium.warppi.ClassUtils;
import it.cavallium.warppi.deps.Platform.StorageUtils;

public class HardwareStorageUtils implements StorageUtils {
	public boolean exists(File f) {
		return f.exists();
	}

	public File get(String path) {
		return Paths.get(path).toFile();
	}

	public File get(String... path) {
		if (path.length <= 1) {
			return Paths.get(path[0]).toFile();
		} else {
			return Paths.get(path[0], Arrays.copyOfRange(path, 1, path.length)).toFile();
		}
	}

	private Map<String, File> resourcesCache = new HashMap<String, File>();

	@Deprecated()
	public File getResource(String string) throws IOException, URISyntaxException {
		final URL res = ClassUtils.classLoader.getResource(string);
		final boolean isResource = res != null;
		if (isResource) {
			try {
				final URI uri = res.toURI();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					if (resourcesCache.containsKey(string)) {
						File f;
						if ((f = resourcesCache.get(string)).exists()) {
							return f;
						} else {
							resourcesCache.remove(string);
						}
					}
					try {
						FileSystems.newFileSystem(uri, Collections.emptyMap());
					} catch (final FileSystemAlreadyExistsException e) {
						FileSystems.getFileSystem(uri);
					}
					final Path myFolderPath = Paths.get(uri);

					InputStream is = Files.newInputStream(myFolderPath);
					final File tempFile = File.createTempFile("picalcresource-", "");
					tempFile.deleteOnExit();
					try (FileOutputStream out = new FileOutputStream(tempFile)) {
						IOUtils.copy(is, out, (int) tempFile.length());
					}
					resourcesCache.put(string, tempFile);

					return tempFile;
				} else {
					return Paths.get(uri).toFile();
				}
			} catch (final java.lang.IllegalArgumentException e) {
				throw e;
			}
		} else {
			return Paths.get(string.substring(1)).toFile();
		}
	}

	public InputStream getResourceStream(String string) throws IOException, URISyntaxException {
		final URL res = ClassUtils.classLoader.getResource(string);
		final boolean isResource = res != null;
		if (isResource) {
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
				} else {
					return Files.newInputStream(Paths.get(uri));
				}
			} catch (final java.lang.IllegalArgumentException e) {
				throw e;
			}
		} else {
			if (string.length() > 0) {
				char ch = string.charAt(0);
				if (ch == '/' || ch == File.separatorChar) {
					string = string.substring(1);
				}
			}
			return Files.newInputStream(Paths.get(string));
		}
	}

	public List<String> readAllLines(File file) throws IOException {
		return Files.readAllLines(file.toPath());
	}

	public String read(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}

	public List<File> walk(File dir) throws IOException {
		List<File> out = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(dir.toPath())) {
			paths.filter(Files::isRegularFile).forEach((Path p) -> {
				out.add(p.toFile());
			});
		}
		return out;
	}

	public File relativize(File rulesPath, File f) {
		return rulesPath.toPath().relativize(f.toPath()).toFile();
	}

	public File resolve(File file, String string) {
		return file.toPath().resolve(string).toFile();
	}

	public File getParent(File f) {
		return f.toPath().getParent().toFile();
	}

	public void createDirectories(File dir) throws IOException {
		Files.createDirectories(dir.toPath());
	}

	public void write(File f, byte[] bytes, int... options) throws IOException {
		StandardOpenOption[] noptions = new StandardOpenOption[options.length];
		int i = 0;
		for (int opt : options) {
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

	public List<String> readAllLines(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines().collect(Collectors.toList());
		}
	}

	@Override
	public String getBasePath() {
		return "";
	}
}

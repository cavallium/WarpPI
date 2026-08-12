package it.cavallium.warppi.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JvmStorageUtilsTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void readsUntilEofWhenAvailableReportsZero() throws IOException {
		final byte[] expected = "complete UTF-8 payload: π".getBytes(StandardCharsets.UTF_8);
		final InputStream input = new ByteArrayInputStream(expected) {
			@Override
			public int available() {
				return 0;
			}
		};

		assertEquals(new String(expected, StandardCharsets.UTF_8), new JvmStorageUtils().read(input));
	}

	@Test
	void findsRootClasspathResourcesWithoutPlatformInitialization() throws IOException {
		final JvmStorageUtils storage = new JvmStorageUtils();

		assertTrue(storage.doesResourceExist("/skin.png"));
		try (InputStream input = storage.getResourceStream("skin.png")) {
			assertTrue(input.readAllBytes().length > 0);
		}
	}

	@Test
	void extractsCompleteJarResourcesAndCachesThem() throws Exception {
		final byte[] expected = new byte[32 * 1024 + 7];
		for (int index = 0; index < expected.length; index++)
			expected[index] = (byte) (index * 31);
		final Path jar = temporaryDirectory.resolve("resources.jar");
		try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
			output.putNextEntry(new JarEntry("payload.bin"));
			output.write(expected);
			output.closeEntry();
		}
		final URL resource = new URL("jar:" + jar.toUri().toURL() + "!/payload.bin");
		final JvmStorageUtils storage = new FixedResourceStorage(resource);

		final File extracted = storage.getResource("/payload.bin");

		assertArrayEquals(expected, Files.readAllBytes(extracted.toPath()));
		assertSame(extracted, storage.getResource("payload.bin"));
		try (InputStream input = storage.getResourceStream("payload.bin")) {
			assertArrayEquals(expected, input.readAllBytes());
		}
	}

	@Test
	void walksNestedFilesOnceInDeterministicOrder() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("b"));
		Files.createDirectories(temporaryDirectory.resolve("a"));
		Files.writeString(temporaryDirectory.resolve("b/two.rules"), "two");
		Files.writeString(temporaryDirectory.resolve("a/one.rules"), "one");
		Files.writeString(temporaryDirectory.resolve("z.rules"), "z");
		final JvmStorageUtils storage = new JvmStorageUtils();

		final List<String> relativePaths = storage.walk(temporaryDirectory.toFile()).stream()
				.map(file -> storage.relativize(temporaryDirectory.toFile(), file).toString())
				.map(path -> path.replace(File.separatorChar, '/'))
				.toList();

		assertEquals(List.of("a/one.rules", "b/two.rules", "z.rules"), relativePaths);
	}

	@Test
	void rejectsUnknownOpenOptions() {
		assertThrows(IllegalArgumentException.class,
				() -> new JvmStorageUtils().write(temporaryDirectory.resolve("output").toFile(), new byte[0], 99));
	}

	private static final class FixedResourceStorage extends JvmStorageUtils {
		private final URL resource;

		private FixedResourceStorage(final URL resource) {
			this.resource = resource;
		}

		@Override
		protected URL findResource(final String resourceName) {
			return resource;
		}
	}
}

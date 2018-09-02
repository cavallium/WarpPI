package it.cavallium.warppi.deps.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class DFiles {

	public static boolean deleteIfExists(DPath path) throws IOException {
		return Files.deleteIfExists(path.toPath());

	}

	public static boolean exists(DPath f) {
		return Files.exists(f.toPath());
	}

	public static DPath createTempFile(String prefix, String suffix) throws IOException {
		return new DPath(Files.createTempFile(prefix, suffix));
	}

	public static List<String> readAllLines(DPath p) throws IOException {
		return Files.readAllLines(p.toPath());
	}

}

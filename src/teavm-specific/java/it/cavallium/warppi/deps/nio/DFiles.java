package it.cavallium.warppi.deps.nio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;

import it.cavallium.warppi.deps.nio.DPaths.DPathImpl;

public class DFiles {

	public static List<String> readAllLines(DPath osRelease) throws IOException {
		return FileUtils.readLines(osRelease.toFile(), Charset.defaultCharset());
	}

	public static boolean exists(DPath f) {
		return f.toFile().exists();
	}

	public static DPath createTempFile(String prefix, String suffix) throws IOException {
		return new DPathImpl(File.createTempFile(prefix, suffix));
	}

	public static void deleteIfExists(DPath f) throws IOException {
		if (f.toFile().exists()) {
			f.toFile().delete();
		}
	}

}

package it.cavallium.warppi.deps.nio;

import java.nio.file.Path;

public class DPath {

	private final Path p;

	public DPath(Path p) {
		this.p = p;
	}

	Path toPath() {
		return p;
	}

	public DPath toAbsolutePath() {
		return new DPath(p.toAbsolutePath());
	}
}

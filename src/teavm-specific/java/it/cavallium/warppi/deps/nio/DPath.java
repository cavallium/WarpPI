package it.cavallium.warppi.deps.nio;

import java.io.File;
import java.nio.file.Path;

public interface DPath {

	DPath toAbsolutePath();

	File toFile();

}

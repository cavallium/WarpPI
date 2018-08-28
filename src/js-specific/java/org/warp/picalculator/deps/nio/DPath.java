package org.warp.picalculator.deps.nio;

import java.io.File;
import java.nio.file.Path;

public interface DPath {

	DPath toAbsolutePath();

	File toFile();

}

package org.warp.picalculator.deps.nio;

import java.nio.file.Paths;

public class DPaths {

	public static DPath get(String first, String... more) {
		return new DPath(Paths.get(first, more));
	}

}

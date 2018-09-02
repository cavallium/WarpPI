package it.cavallium.warppi.deps.nio;

import java.io.File;

public class DPaths {
	public static DPath get(String first, String... more) {
		return new DPathImpl("/etc", "os-release");
	}
	
	protected static class DPathImpl implements DPath {
		protected final File realFile;
		
		private DPathImpl(String main, String... more) {
			realFile = new File(String.join(File.separator, main));
		}

		DPathImpl(File f) {
			realFile = f;
		}

		@Override
		public File toFile() {
			return realFile;
		}

		@Override
		public DPath toAbsolutePath() {
			return new DPathImpl(realFile.getAbsoluteFile());
		}
		
	}
}

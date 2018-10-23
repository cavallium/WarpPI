package it.cavallium.warppi.teavm;

import java.io.InputStream;

import it.cavallium.warppi.Platform.ImageUtils.ImageReader;

public class TeaVMImageReader implements ImageReader {
	
	public TeaVMImageReader(final InputStream resourceStream) {
		throw new RuntimeException("Not supported by this platform");
	}

	@Override
	public int[] getImageMatrix() {
		throw new RuntimeException("Not supported by this platform");
	}

	@Override
	public int[] getSize() {
		throw new RuntimeException("Not supported by this platform");
	}

}

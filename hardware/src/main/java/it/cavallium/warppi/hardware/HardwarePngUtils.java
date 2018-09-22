package it.cavallium.warppi.hardware;

import java.io.InputStream;

import it.cavallium.warppi.Platform.PngUtils;

public class HardwarePngUtils implements PngUtils {

	@Override
	public PngReader load(final InputStream resourceStream) {
		return new HardwarePngReader(resourceStream);
	}

}

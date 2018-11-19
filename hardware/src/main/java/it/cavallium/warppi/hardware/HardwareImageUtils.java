package it.cavallium.warppi.hardware;

import java.io.InputStream;

import it.cavallium.warppi.Platform.ImageUtils;

public class HardwareImageUtils implements ImageUtils {

	@Override
	public ImageReader load(final InputStream resourceStream) {
		return new HardwareImageReader(resourceStream);
	}

}

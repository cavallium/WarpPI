package it.cavallium.warppi.teavm;

import java.io.InputStream;

import it.cavallium.warppi.Platform.ImageUtils;

public class TeaVMImageUtils implements ImageUtils {

	@Override
	public ImageReader load(final InputStream resourceStream) {
		return new TeaVMImageReader(resourceStream);
	}

}

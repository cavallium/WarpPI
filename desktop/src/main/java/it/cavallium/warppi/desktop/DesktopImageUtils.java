package it.cavallium.warppi.desktop;

import java.io.IOException;
import java.io.InputStream;

import it.cavallium.warppi.Platform.ImageUtils.ImageReader;
import it.cavallium.warppi.Platform.ImageUtils;

public class DesktopImageUtils implements ImageUtils {

	@Override
	public ImageReader load(final InputStream resourceStream) throws IOException {
		return new DesktopImageReader(resourceStream);
	}

}

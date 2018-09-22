package it.cavallium.warppi.desktop;

import java.io.InputStream;

import it.cavallium.warppi.Platform.PngUtils;

public class DesktopPngUtils implements PngUtils {

	@Override
	public PngReader load(final InputStream resourceStream) {
		return new DesktopPngReader(resourceStream);
	}

}

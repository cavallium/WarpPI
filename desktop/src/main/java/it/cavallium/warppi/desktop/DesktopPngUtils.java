package it.cavallium.warppi.desktop;

import java.io.InputStream;

import it.cavallium.warppi.deps.Platform.PngUtils;

public class DesktopPngUtils implements PngUtils {

	@Override
	public PngReader load(InputStream resourceStream) {
		return new DesktopPngReader(resourceStream);
	}

}

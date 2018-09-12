package it.cavallium.warppi.teavm;

import java.io.InputStream;

import it.cavallium.warppi.Platform.PngUtils;

public class TeaVMPngUtils implements PngUtils {

	@Override
	public PngReader load(InputStream resourceStream) {
		return new TeaVMPngReader(resourceStream);
	}

}

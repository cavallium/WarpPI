package it.cavallium.warppi.gui.graphicengine.impl.framebuffer;

import java.io.IOException;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.common.PngSkin;

public class FBSkin extends PngSkin {

	public FBSkin(final String file) throws IOException {
		super(file);
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		@SuppressWarnings("unused")
		final FBEngine dfb = (FBEngine) d;
		// TODO: implement
	}

}

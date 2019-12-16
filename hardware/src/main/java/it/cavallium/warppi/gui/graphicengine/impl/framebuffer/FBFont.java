package it.cavallium.warppi.gui.graphicengine.impl.framebuffer;

import java.io.IOException;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class FBFont extends RFTFont {

	public FBFont(final String fontName) throws IOException {
		super(fontName);
	}

	public FBFont(final String path, final String fontName) throws IOException {
		super(path, fontName);
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		@SuppressWarnings("unused")
		final FBEngine dfb = (FBEngine) d;
		// TODO: implement
	}

}

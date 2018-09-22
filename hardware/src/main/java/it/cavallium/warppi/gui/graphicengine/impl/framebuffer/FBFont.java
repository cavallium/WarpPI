package it.cavallium.warppi.gui.graphicengine.impl.framebuffer;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class FBFont extends RFTFont {

	public FBFont(String fontName) throws IOException {
		super(fontName);
	}
	
	public FBFont(String path, String fontName) throws IOException {
		super(path, fontName);
	}

	@Override
	public void use(GraphicEngine d) {
		FBEngine dfb = (FBEngine) d;
		// TODO: implement
	}


}

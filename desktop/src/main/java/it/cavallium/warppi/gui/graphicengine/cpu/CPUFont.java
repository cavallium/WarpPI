package it.cavallium.warppi.gui.graphicengine.cpu;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.common.RFTFont;

public class CPUFont extends RFTFont {

	public CPUFont(String fontName) throws IOException {
		super(fontName);
	}

	public CPUFont(String path, String fontName) throws IOException {
		super(path, fontName);
	}

	@Override
	public void use(GraphicEngine d) {
		if (d.getRenderer() instanceof CPURenderer) {
			((CPURenderer) d.getRenderer()).currentFont = this;
		}
	}
}

package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class SwingFont extends RFTFont {

	public SwingFont(String fontName) throws IOException {
		super(fontName);
	}

	public SwingFont(String path, String fontName) throws IOException {
		super(path, fontName);
	}

	@Override
	public void use(GraphicEngine d) {
		if (d.getRenderer() instanceof SwingRenderer) {
			((SwingRenderer) d.getRenderer()).currentFont = this;
		}
	}
}

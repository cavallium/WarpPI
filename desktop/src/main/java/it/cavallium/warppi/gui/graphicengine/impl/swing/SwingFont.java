package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class SwingFont extends RFTFont {

	public SwingFont(final String fontName) throws IOException {
		super(fontName);
	}

	public SwingFont(final String path, final String fontName) throws IOException {
		super(path, fontName);
	}

	@Override
	public void use(final GraphicEngine d) {
		if (d.getRenderer() instanceof SwingRenderer)
			((SwingRenderer) d.getRenderer()).currentFont = this;
	}
}

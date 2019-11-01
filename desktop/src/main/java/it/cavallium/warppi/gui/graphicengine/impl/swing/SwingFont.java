package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.io.IOException;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class SwingFont extends RFTFont {

	public SwingFont(final String fontName) throws IOException {
		super(fontName);
	}

	public SwingFont(final String path, final String fontName) throws IOException {
		super(path, fontName);
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		if (d.getGraphicEngine().getRenderer() instanceof SwingRenderer)
			((SwingRenderer) d.getGraphicEngine().getRenderer()).currentFont = this;
	}
}

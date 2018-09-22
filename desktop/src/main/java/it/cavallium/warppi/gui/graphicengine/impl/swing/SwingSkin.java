package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.impl.common.PngSkin;

public class SwingSkin extends PngSkin {

	public SwingSkin(String file) throws IOException {
		super(file);
	}

	@Override
	public void use(GraphicEngine d) {
		if (d.getRenderer() instanceof SwingRenderer) {
			((SwingRenderer) d.getRenderer()).currentSkin = this;
		}
	}
}

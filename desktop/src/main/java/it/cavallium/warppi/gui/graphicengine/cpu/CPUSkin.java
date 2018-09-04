package it.cavallium.warppi.gui.graphicengine.cpu;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.common.PngSkin;

public class CPUSkin extends PngSkin {

	public CPUSkin(String file) throws IOException {
		super(file);
	}

	@Override
	public void use(GraphicEngine d) {
		if (d.getRenderer() instanceof CPURenderer) {
			((CPURenderer) d.getRenderer()).currentSkin = this;
		}
	}
}

package it.cavallium.warppi.gui.graphicengine.impl.swing;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public class SwingDisplayOutputDevice implements DisplayOutputDevice {
	
	private final SwingEngine engine;
	
	public SwingDisplayOutputDevice() {
		this.engine = new SwingEngine();
	}

	@Override
	public GraphicEngine getGraphicEngine() {
		return engine;
	}

	@Override
	public int[] getDisplaySize() {
		return engine.getSize();
	}
}

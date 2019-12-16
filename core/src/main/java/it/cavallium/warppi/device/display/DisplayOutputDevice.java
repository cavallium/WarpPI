package it.cavallium.warppi.device.display;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public interface DisplayOutputDevice {
	public GraphicEngine getGraphicEngine();
	public int[] getDisplaySize();
}
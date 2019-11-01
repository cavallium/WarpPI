package it.cavallium.warppi.gui.graphicengine.html;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public class HtmlDisplayOutputDevice implements DisplayOutputDevice {

	private final HtmlEngine engine;

	public HtmlDisplayOutputDevice() {
		this.engine = new HtmlEngine();
	}

	@Override
	public HtmlEngine getGraphicEngine() {
		return engine;
	}

	@Override
	public int[] getDisplaySize() {
		return engine.getSize();
	}
}

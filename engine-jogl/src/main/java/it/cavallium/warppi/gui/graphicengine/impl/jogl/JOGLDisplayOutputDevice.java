package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public class JOGLDisplayOutputDevice implements DisplayOutputDevice {
	private JOGLEngine engine;

	public JOGLDisplayOutputDevice() {
		this.engine = new JOGLEngine();
	}
	
	@Override
	public int[] getDisplaySize() {
		return engine.getSize();
	}
	
	@Override
	public JOGLEngine getGraphicEngine() {
		return engine;
	}
}

package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import it.cavallium.warppi.device.display.DisplayOutputDevice;

public class JOGLDisplayOutputDevice implements DisplayOutputDevice {
    private JOGLEngine engine;

    public JOGLDisplayOutputDevice() {
		this(1f);
	}

	public JOGLDisplayOutputDevice(final float desktopScale) {
		this.engine = new JOGLEngine(480, 320, desktopScale);
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

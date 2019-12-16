package it.cavallium.warppi.gui;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.util.Error;

public class GUIErrorMessage {

	@SuppressWarnings("unused")
	private final String err;
	private final long creationTime;

	public GUIErrorMessage(final Error e) {
		err = e.getLocalizedMessage();
		creationTime = System.currentTimeMillis();
	}

	public GUIErrorMessage(final Exception ex) {
		err = ex.getLocalizedMessage();
		creationTime = System.currentTimeMillis();
	}

	public void draw(final DisplayOutputDevice g, final Renderer r, final String msg) {
		final int scrW = g.getGraphicEngine().getWidth();
		final int scrH = g.getGraphicEngine().getHeight();
		final int width = 200;
		final int height = 20;
		final int margin = 4;
		r.glClearSkin();
		r.glColor(0x00000000);
		r.glFillRect(scrW - width - margin, scrH - height - margin, width, height, 0, 0, 0, 0);
	}

	public long getCreationTime() {
		return creationTime;
	}
}

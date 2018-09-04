package it.cavallium.warppi.gui;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class GUIErrorMessage {

	@SuppressWarnings("unused")
	private final String err;
	private final long creationTime;

	public GUIErrorMessage(Error e) {
		err = e.getLocalizedMessage();
		creationTime = System.currentTimeMillis();
	}

	public GUIErrorMessage(Exception ex) {
		err = ex.getLocalizedMessage();
		creationTime = System.currentTimeMillis();
	}

	public void draw(GraphicEngine g, Renderer r, String msg) {
		final int scrW = g.getWidth();
		final int scrH = g.getHeight();
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

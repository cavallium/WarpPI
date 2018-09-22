package it.cavallium.warppi.desktop;

import it.cavallium.warppi.Platform.Settings;

public class DesktopSettings implements Settings {

	private boolean debug;

	public DesktopSettings() {
		debug = true;
	}

	@Override
	public boolean isDebugEnabled() {
		return debug;
	}

	@Override
	public void setDebugEnabled(final boolean debug) {
		this.debug = debug;
	}

}

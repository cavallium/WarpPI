package it.cavallium.warppi.hardware;

import it.cavallium.warppi.Platform.Settings;

public class HardwareSettings implements Settings {

	private boolean debug;

	public HardwareSettings() {
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

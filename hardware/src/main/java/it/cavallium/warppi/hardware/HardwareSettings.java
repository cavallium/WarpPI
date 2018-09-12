package it.cavallium.warppi.hardware;

import it.cavallium.warppi.Platform.Settings;

public class HardwareSettings implements Settings {

	private boolean debug;

	public HardwareSettings() {
		this.debug = true;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return debug;
	}

	@Override
	public void setDebugEnabled(boolean debug) {
		this.debug = debug;
	}

}

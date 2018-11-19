package it.cavallium.warppi.teavm;

import it.cavallium.warppi.Platform.Settings;

public class TeaVMSettings implements Settings {

	private boolean debug;

	public TeaVMSettings() {
		debug = false;
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

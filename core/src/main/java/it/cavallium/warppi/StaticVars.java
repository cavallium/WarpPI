package it.cavallium.warppi;

import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.util.EventSubmitter;

/*
 * TODO: Move everything to Engine.Settings
 */
public class StaticVars {
	public static final boolean zoomedFonts = true;
	public static int outputLevel = 0;
	public static EventSubmitter<Float> windowZoom = new EventSubmitter<>(1F);
	public static StartupArguments startupArguments;

	private StaticVars() {

	}
}

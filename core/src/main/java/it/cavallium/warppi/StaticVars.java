package it.cavallium.warppi;

import java.util.function.Function;

import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.util.EventSubmitter;

/*
 * TODO: Move everything to Engine.Settings
 */
public class StaticVars {
	public static final boolean zoomed = true;
	public static int outputLevel = 0;
	public static boolean debugWindow2x = false;
	public static EventSubmitter<Float> windowZoom = new EventSubmitter<>(1F);
	public static Function<Float, Float> windowZoomFunction = (val) -> {
		if (StaticVars.debugWindow2x) {
			return val + 1;
		} else {
			return val;
		}
	};
	public static EventSubmitter<Float> windowZoom$ = StaticVars.windowZoom.map(StaticVars.windowZoomFunction);
	public static StartupArguments startupArguments;

	private StaticVars() {

	}
}

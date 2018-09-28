package it.cavallium.warppi;

import java.util.function.Function;

import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.flow.BehaviorSubject;
import it.cavallium.warppi.flow.Observable;

/*
 * TODO: Move everything to Engine.Settings
 */
public class StaticVars {
	public static final boolean zoomed = true;
	public static int[] screenPos = new int[] { 0, 0 };
	public static final int[] screenSize = new int[] { 480, 320 };
	public static int outputLevel = 0;
	public static boolean debugWindow2x = false;
	public static BehaviorSubject<Float> windowZoom = BehaviorSubject.create(2F);
	public static Function<Float, Float> windowZoomFunction = (val) -> {
		if (StaticVars.debugWindow2x) {
			return val + 1;
		} else {
			return val;
		}
	};
	public static Observable<Float> windowZoom$ = StaticVars.windowZoom.map(StaticVars.windowZoomFunction);
	public static StartupArguments startupArguments;

	private StaticVars() {

	}
}

package org.warp.picalculator;

import java.util.function.Function;

import org.warp.picalculator.boot.StartupArguments;
import org.warp.picalculator.flow.Observable;
import org.warp.picalculator.flow.BehaviorSubject;

public class StaticVars {
	public static final boolean enableVBO = true;
	public static final String calculatorName = "WarpPI";
	public static final String calculatorNameLOWER = "warppi";
	public static final String calculatorNameUPPER = "WARPPI";
	public static boolean haxMode = true;
	public static final boolean zoomed = true;
	public static int[] screenPos = new int[] { 0, 0 };
	public static final int[] screenSize = new int[] {480, 320};
	public static boolean debugOn;
	public static int outputLevel = 0;
	public static boolean debugWindow2x = false;
	public static BehaviorSubject<Float> windowZoom = BehaviorSubject.create(2F);
	public static Function<Float, Float> windowZoomFunction = (val) -> {
		if (StaticVars.debugOn & StaticVars.debugWindow2x) {
			return val + 1;
		} else {
			return val;
		}
	};
	public static Observable<Float> windowZoom$ = windowZoom.map(windowZoomFunction);
	public static StartupArguments startupArguments;

	private StaticVars() {

	}
}

package org.warp.picalculator;

import org.warp.picalculator.boot.StartupArguments;

public class StaticVars {
	public static final boolean enableVBO = true;
	public static final String calculatorName = "WarpPI";
	public static final String calculatorNameLOWER = "warppi";
	public static final String calculatorNameUPPER = "WARPPI";
	public static boolean haxMode = true;
	public static final boolean zoomed = true;
	public static int[] screenPos = new int[] { 0, 0 };
	public static final int[] screenSize = new int[] { 480, 320 };
	public static boolean debugOn;
	public static int outputLevel = 0;
	public static boolean debugWindow2x = false;
	public static float windowZoom = 2;
	public static StartupArguments startupArguments;

	private StaticVars() {

	}

	public static float getCurrentZoomValue() {
		if (StaticVars.debugOn & StaticVars.debugWindow2x) {
			return 2;
		} else {
			return StaticVars.windowZoom;
		}
	}
}

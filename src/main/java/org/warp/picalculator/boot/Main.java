package org.warp.picalculator.boot;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.warp.picalculator.ConsoleUtils;
import org.warp.picalculator.PICalculator;
import org.warp.picalculator.Utils;
import org.warp.picalculator.device.PIHardwareDisplay;
import org.warp.picalculator.gui.CalculatorHUD;
import org.warp.picalculator.gui.screens.LoadingScreen;

public class Main {
	public static void main(String[] args) throws Exception {
		new PICalculator(parseStartupArguments(args));
	}

	public static StartupArguments parseStartupArguments(final String[] a) {
		final StartupArgumentsImpl args = new StartupArgumentsImpl();
		Arrays.asList(a).stream().parallel().map(String::toLowerCase).forEach(arg -> {
			switch (arg) {
				case "2x":
					args.setZoomed(true);
					break;
				case "verbose":
					args.setVerboseLoggingEnabled(true);
					break;
				case "-noraspi":
					args.setRaspberryModeAllowed(false);
					break;
				case "nogui":
					args.setNoGUIEngineForced(true);
					break;
				case "ms-dos":
					args.setMSDOSModeEnabled(true);
					break;
				case "headless-8":
					args.setHeadless8EngineForced(true);
					break;
				case "headless-256":
					args.setHeadless256EngineForced(true);
					break;
				case "headless-24bit":
					args.setHeadless24bitEngineForced(true);
					break;
				case "headless":
					args.setHeadlessEngineForced(true);
					break;
				case "html":
					args.setHTMLEngineForced(true);
					break;
				case "gpu":
					args.setGPUEngineForced(true);
					break;
				case "cpu":
					args.setCPUEngineForced(true);
					break;
				case "framebuffer":
					args.setFrameBufferEngineForced(true);
					break;
				case "debug":
					args.setDebugEnabled(true);
					break;
				case "uncached":
					args.setUncached(true);
					break;
				default:
					ConsoleUtils.out.print(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "Unrecognized argument " + arg);
					break;
			}
		});
		args.setHeadlessEngineForced(args.isHeadlessEngineForced() || args.isHeadless8EngineForced() || args.isHeadless256EngineForced() || args.isHeadless24bitEngineForced());
		return args;
	}
}

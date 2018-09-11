package it.cavallium.warppi.boot;

import java.util.Arrays;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Engine.LoadingStatus;
import it.cavallium.warppi.Error;
import it.cavallium.warppi.deps.Platform;
import it.cavallium.warppi.device.HardwareDevice;
import it.cavallium.warppi.device.PIHardwareDisplay;
import it.cavallium.warppi.device.PIHardwareTouchDevice;
import it.cavallium.warppi.gui.CalculatorHUD;
import it.cavallium.warppi.gui.screens.LoadingScreen;
import it.cavallium.warppi.math.rules.RulesManager;

public class Boot {
	
	public static void boot(Platform platform, String[] args) throws Exception {
		Engine.start(platform, new LoadingScreen(), new PIHardwareDisplay(), new PIHardwareTouchDevice(false, false, false), new CalculatorHUD(), parseStartupArguments(args));
		Engine.INSTANCE.getLoadPhase().subscribe(Boot::loadCalculator);
	}

	private static void loadCalculator(LoadingStatus loading) {
		try {
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().setBrightness(0.2f);
			RulesManager.initialize();
			RulesManager.warmUp();
			loading.done();
		} catch (InterruptedException | Error e) {
			e.printStackTrace();
		}
		loading.failed();
	}

	public static StartupArguments parseStartupArguments(final String[] a) {
		final StartupArgumentsImpl args = new StartupArgumentsImpl();
		Arrays.asList(a).stream().parallel().map(String::toLowerCase).forEach(arg -> parseArgument(args, arg));
		args.setHeadlessEngineForced(args.isHeadlessEngineForced() || args.isHeadless8EngineForced() || args.isHeadless256EngineForced() || args.isHeadless24bitEngineForced());
		return args;
	}

	public static void parseArgument(StartupArgumentsImpl args, String arg) {
		switch (arg) {
			case "-zoomed":
				args.setZoomed(true);
				break;
			case "-verbose":
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
			case "-headless":
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
			case "-debug":
				args.setDebugEnabled(true);
				break;
			case "-uncached":
				args.setUncached(true);
				break;
			default:
				Engine.getPlatform().getConsoleUtils().out().println("Unrecognized argument " + arg);
				break;
		}
	}
}

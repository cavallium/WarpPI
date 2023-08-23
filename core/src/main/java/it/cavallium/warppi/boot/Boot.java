package it.cavallium.warppi.boot; 
 
import java.util.Arrays; 
import java.util.concurrent.Future; 
 
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.Platform;
import it.cavallium.warppi.gui.CalculatorHUD; 
import it.cavallium.warppi.gui.screens.LoadingScreen; 
import it.cavallium.warppi.math.rules.RulesManager; 
 
public class Boot { 
 
	public static void boot(final Platform platform, final String[] args) throws Exception { 
		WarpPI.start(
				platform, 
				new LoadingScreen(), 
				new CalculatorHUD(), 
				Boot.parseStartupArguments(args), 
				Boot::loadCalculator);
	} 
 
	private static void loadCalculator() throws Exception { 
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setBrightness(0.2f); 
		//TODO: plugins system: PluginsManager.initialize(); 
		RulesManager.initialize(); 
		RulesManager.warmUp(); 
	} 
 
	public static StartupArguments parseStartupArguments(final String[] a) { 
		final StartupArgumentsImpl args = new StartupArgumentsImpl(); 
		Arrays.asList(a).stream().parallel().filter((x) -> x != null).map(String::toLowerCase).forEach(arg -> Boot.parseArgument(args, arg)); 
		return args; 
	} 
 
	public static void parseArgument(final StartupArgumentsImpl args, final String arg) { 
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
				// Not using ConsoleUtils because it isn't initialized at this point. 
				System.out.println("Unrecognized argument " + arg); 
				break; 
		} 
	} 
} 

package it.cavallium.warppi;

import java.io.IOException;

import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.device.HardwareDevice;
import it.cavallium.warppi.device.HardwareTouchDevice;
import it.cavallium.warppi.device.InputManager;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.gui.DisplayManager;
import it.cavallium.warppi.gui.HUD;
import it.cavallium.warppi.gui.HardwareDisplay;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.ClassUtils;
import it.cavallium.warppi.util.EventSubmitter;

public class Engine {
	public static final Engine INSTANCE = new Engine();
	private static Platform platform;
	private static boolean running = false;
	private static EventSubmitter<LoadingStatus> loadPhase = new EventSubmitter<>();
	private final EventSubmitter<Boolean> loaded = new EventSubmitter<>(false);
	private HardwareDevice hardwareDevice;

	private Engine() {}

	/**
	 * Start an instance of the calculator.
	 *
	 * @param platform
	 *            Platform implementation
	 * @param screen
	 *            Default screen to show at startup
	 * @param disp
	 *            Hardware display
	 * @param hud
	 *            Head-up display
	 * @param args
	 *            Startup arguments
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void start(final Platform platform, final Screen screen, final HardwareDisplay disp,
	                         final HardwareTouchDevice touchdevice, final HUD hud, final StartupArguments args)
		throws InterruptedException, IOException {
		if (Engine.running) {
			throw new RuntimeException("Already running!");
		} else {
			Engine.running = true;
			Engine.INSTANCE.startInstance(platform, screen, disp, touchdevice, hud, args);
		}
	}

	private void startInstance(final Platform platform, final Screen screen, final HardwareDisplay disp,
	                           final HardwareTouchDevice touchdevice, final HUD hud, final StartupArguments args)
		throws InterruptedException, IOException {
		Engine.platform = platform;
		platform.getConsoleUtils().out().println("WarpPI Calculator");
		initializeEnvironment(args);

		final Thread currentThread = Thread.currentThread();
		currentThread.setPriority(Thread.MAX_PRIORITY);
		Engine.getPlatform().setThreadName(currentThread, "Main thread");

		final DisplayManager dm = new DisplayManager(disp, hud, screen, "WarpPI Calculator by Andrea Cavalli (@Cavallium)");
		final Keyboard k = new Keyboard();
		final InputManager im = new InputManager(k, touchdevice);
		hardwareDevice = new HardwareDevice(dm, im);

		hardwareDevice.setup(() -> Engine.loadPhase.submit(new LoadingStatus()));
	}

	private void onShutdown() {
		Engine.platform.getConsoleUtils().out().println(1, "Shutdown...");
		beforeShutdown();
		Engine.platform.getConsoleUtils().out().println(1, "");
		Engine.platform.getConsoleUtils().out().println(1, "Closed.");
		Engine.getPlatform().exit(0);
	}

	private void initializeEnvironment(final StartupArguments args) throws IOException {
		ClassUtils.classLoader = this.getClass();
		StaticVars.startupArguments = args;
		StaticVars.debugWindow2x = args.isZoomed();
		if (args.isVerboseLoggingEnabled() || args.isDebugEnabled()) {
			StaticVars.outputLevel = ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE;
		}
		Engine.platform.getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, args);
		checkDeviceType();
		if (args.isRaspberryModeAllowed() == false) {
			Engine.getPlatform().setRunningOnRaspberry(false);
		}
		if (Engine.getPlatform().isRunningOnRaspberry()) {
			Engine.getPlatform().getGpio().wiringPiSetupPhys();
			Engine.getPlatform().getGpio().pinMode(12, Engine.getPlatform().getGpio().valuePwmOutput());
		} else {
			StaticVars.screenPos = new int[] { 0, 0 };
			if (Engine.getPlatform().isJavascript() == false) {
				Engine.getPlatform().getSettings().setDebugEnabled(true);
			}
		}
	}

	private void checkDeviceType() {
		// TODO Auto-generated method stub

	}

	public void beforeShutdown() {
		Keyboard.stopKeyboard();
	}

	public EventSubmitter<Boolean> isLoaded() {
		return loaded;
	}

	public EventSubmitter<LoadingStatus> getLoadPhase() {
		return Engine.loadPhase;
	}

	public HardwareDevice getHardwareDevice() {
		return hardwareDevice;
	}

	public static Platform getPlatform() {
		return Engine.platform;
	}

	public static class LoadingStatus {
		protected LoadingStatus() {

		}

		public void done() {
			Engine.INSTANCE.loaded.submit(true);
			Engine.INSTANCE.hardwareDevice.getDisplayManager().waitForExit();
			Engine.INSTANCE.onShutdown();
		}

		public void failed() {
			Engine.INSTANCE.onShutdown();
		}
	}
}

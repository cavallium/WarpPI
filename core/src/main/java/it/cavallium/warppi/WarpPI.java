package it.cavallium.warppi;

import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.device.Device;
import it.cavallium.warppi.device.DeviceStateDevice;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.input.InputManager;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.device.input.KeyboardInputDevice;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.gui.DisplayManager;
import it.cavallium.warppi.gui.HUD;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.ClassUtils;
import it.cavallium.warppi.util.EventSubmitter;
import it.cavallium.warppi.util.RunnableWithException;

import java.io.IOException;

public class WarpPI {
	public static final WarpPI INSTANCE = new WarpPI();
	private static Platform platform;
	private static boolean running = false;
	private final EventSubmitter<Boolean> loaded = EventSubmitter.create(false);
	private Device device;

	private WarpPI() {
	}

	/**
	 * Start an instance of the calculator.
	 *
	 * @param platform Platform implementation
	 * @param screen   Default screen to show at startup
	 * @param hud      Head-up display
	 * @param args     Startup arguments
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void start(final Platform platform, final Screen screen, final HUD hud, final StartupArguments args, final RunnableWithException onLoading) throws IOException {
		if (WarpPI.running) {
			throw new RuntimeException("Already running!");
		} else {
			WarpPI.running = true;
			WarpPI.INSTANCE.startInstance(platform, screen, hud, args, onLoading);
		}
	}

	private void startInstance(final Platform platform, final Screen screen,
	                           final HUD hud, final StartupArguments args, final RunnableWithException onLoading)
		throws IOException {
		WarpPI.platform = platform;
		// Set arguments on platform before everything else
		platform.setArguments(args);

		platform.getConsoleUtils().out().println("WarpPI Calculator");
		initializeEnvironment(args);

		final Thread currentThread = Thread.currentThread();
		currentThread.setPriority(Thread.MAX_PRIORITY);
		WarpPI.getPlatform().setThreadName(currentThread, "Main thread");

		try {
			final DisplayOutputDevice display = platform.getDisplayOutputDevice();
			final BacklightOutputDevice backlight = platform.getBacklightOutputDevice();
			final DisplayManager dm = new DisplayManager(display, backlight, hud, screen, "WarpPI Calculator by Andrea Cavalli (@Cavallium)");
			final KeyboardInputDevice keyboard = platform.getKeyboardInputDevice();
			final TouchInputDevice touchscreen = platform.getTouchInputDevice();
			final DeviceStateDevice deviceState = platform.getDeviceStateDevice();
			final InputManager im = new InputManager(keyboard, touchscreen);
			device = new Device(dm, im, deviceState);
			device.setup();
			onLoading.run();
			this.loadingCompleted();
		} catch (Exception ex) {
			this.loadingFailed(ex);
		}
		this.onShutdown();
	}

	private void onShutdown() {
		WarpPI.platform.getConsoleUtils().out().println(1, "Shutdown...");
		beforeShutdown();
		WarpPI.platform.getConsoleUtils().out().println(1, "");
		WarpPI.platform.getConsoleUtils().out().println(1, "Closed.");
		WarpPI.getPlatform().exit(0);
	}

	private void initializeEnvironment(final StartupArguments args) throws IOException {
		ClassUtils.classLoader = this.getClass();
		StaticVars.startupArguments = args;
		StaticVars.windowZoom.submit(args.isZoomed() ? 2f : 1f);
		if (args.isVerboseLoggingEnabled() || args.isDebugEnabled()) {
			StaticVars.outputLevel = ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE;
		}
		WarpPI.platform.getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, args);
		checkDeviceType();
		if (args.isRaspberryModeAllowed() == false) {
			WarpPI.getPlatform().setRunningOnRaspberry(false);
		}
		if (WarpPI.getPlatform().isRunningOnRaspberry()) {
			WarpPI.getPlatform().getGpio().wiringPiSetupPhys();
			WarpPI.getPlatform().getGpio().pinMode(12, WarpPI.getPlatform().getGpio().valuePwmOutput());
		} else {
			if (WarpPI.getPlatform().isJavascript() == false) {
				WarpPI.getPlatform().getSettings().setDebugEnabled(true);
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

	public Device getHardwareDevice() {
		return device;
	}

	public static Platform getPlatform() {
		return WarpPI.platform;
	}


	private void loadingCompleted() {
		WarpPI.INSTANCE.loaded.submit(true);
		WarpPI.INSTANCE.device.getDeviceStateDevice().waitForExit();
	}

	private void loadingFailed(Exception e) {
		e.printStackTrace();
	}
}

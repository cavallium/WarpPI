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
	public static final int STARTUP_DISPLAY_READY = 8;
	public static final int STARTUP_SKIN_RESOURCES_READY = 16;
	public static final int STARTUP_FONT_RESOURCES_READY = 24;
	public static final int STARTUP_DEVICE_READY = 30;
	public static final int STARTUP_BRIGHTNESS_READY = 32;
	public static final int STARTUP_BUILTIN_RULES_READY = 42;
	public static final int STARTUP_DSL_RULES_START = 44;
	public static final int STARTUP_DSL_RULES_READY = 68;
	public static final int STARTUP_RULE_WARMUP_START = 70;
	public static final int STARTUP_RULE_WARMUP_READY = 96;
	public static final int STARTUP_SOLVER_WARMUP_READY = 99;
	public static final int STARTUP_COMPLETE = 100;
	private static Platform platform;
	private static boolean running = false;
	private final EventSubmitter<Boolean> loaded = EventSubmitter.create(false);
	private volatile int loadingProgressPercent;
	private volatile int activeLoadingOperations;
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
		loadingProgressPercent = 0;
		WarpPI.platform = platform;
		// Set arguments on platform before everything else
		platform.setArguments(args);

		platform.getConsoleUtils().out().println("WarpPI Calculator");
		initializeEnvironment(args);

		final Thread currentThread = Thread.currentThread();
		currentThread.setPriority(Thread.MAX_PRIORITY);
		WarpPI.getPlatform().setThreadName(currentThread, "Main thread");

		int exitStatus = 0;
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
		} catch (final Exception ex) {
			this.loadingFailed(ex);
			exitStatus = 1;
		}
		this.onShutdown(exitStatus);
	}

	private void onShutdown(final int exitStatus) {
		WarpPI.platform.getConsoleUtils().out().println(1, "Shutdown...");
		beforeShutdown();
		WarpPI.platform.getConsoleUtils().out().println(1, "");
		WarpPI.platform.getConsoleUtils().out().println(1, "Closed.");
		WarpPI.getPlatform().exit(exitStatus);
	}

	private void initializeEnvironment(final StartupArguments args) throws IOException {
		ClassUtils.classLoader = this.getClass();
		StaticVars.startupArguments = args;
		StaticVars.initializeWindowZoom(args.isZoomed());
		if (args.isVerboseLoggingEnabled() || args.isDebugEnabled()) {
			StaticVars.outputLevel = ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE;
		}
		if (args.isDebugEnabled()) {
			WarpPI.getPlatform().getSettings().setDebugEnabled(true);
		}
		WarpPI.platform.getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, args);
		checkDeviceType();
		if (args.isRaspberryModeAllowed() == false) {
			WarpPI.getPlatform().setRunningOnRaspberry(false);
		}
		if (WarpPI.getPlatform().isRunningOnRaspberry()) {
			WarpPI.getPlatform().getGpio().initialize();
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

	public int getLoadingProgressPercent() {
		return loadingProgressPercent;
	}

	/**
	 * Reports completed startup work. Startup has a single writer; the volatile
	 * field only provides visibility to the render thread.
	 */
	public void reportLoadingProgress(final int percent) {
		final int boundedPercent = Math.max(0, Math.min(STARTUP_COMPLETE, percent));
		if (boundedPercent > loadingProgressPercent)
			loadingProgressPercent = boundedPercent;
	}

	public static int interpolateLoadingProgress(final int start, final int end,
			final int completed, final int total) {
		if (total <= 0)
			return end;
		final int boundedCompleted = Math.max(0, Math.min(completed, total));
		return start + (end - start) * boundedCompleted / total;
	}

	public boolean isLoading() {
		return activeLoadingOperations > 0;
	}

	public synchronized void beginLoadingOperation() {
		if (activeLoadingOperations == Integer.MAX_VALUE)
			throw new IllegalStateException("Too many active loading operations");
		if (activeLoadingOperations++ == 0)
			requestLoadingIndicatorRefresh();
	}

	public synchronized void endLoadingOperation() {
		if (activeLoadingOperations <= 0)
			throw new IllegalStateException("No active loading operation to end");
		if (--activeLoadingOperations == 0)
			requestLoadingIndicatorRefresh();
	}

	private void requestLoadingIndicatorRefresh() {
		final Device currentDevice = device;
		if (currentDevice != null)
			currentDevice.getDisplayManager().requestRefresh();
	}

	public Device getHardwareDevice() {
		return device;
	}

	public static Platform getPlatform() {
		return WarpPI.platform;
	}


	private void loadingCompleted() {
		WarpPI.INSTANCE.reportLoadingProgress(STARTUP_COMPLETE);
		WarpPI.INSTANCE.loaded.submit(true);
		WarpPI.INSTANCE.device.getDeviceStateDevice().waitForExit();
	}

	private void loadingFailed(Exception e) {
		e.printStackTrace();
	}
}

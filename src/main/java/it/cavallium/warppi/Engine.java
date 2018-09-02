package it.cavallium.warppi;

import java.io.IOException;

import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.deps.DGpio;
import it.cavallium.warppi.deps.DSystem;
import it.cavallium.warppi.device.HardwareDevice;
import it.cavallium.warppi.device.HardwareTouchDevice;
import it.cavallium.warppi.device.InputManager;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.flow.BehaviorSubject;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.DisplayManager;
import it.cavallium.warppi.gui.HUD;
import it.cavallium.warppi.gui.HardwareDisplay;
import it.cavallium.warppi.gui.screens.Screen;

public class Engine {
	public static final Engine INSTANCE = new Engine();
	private static boolean running = false;
	private static BehaviorSubject<LoadingStatus> loadPhase = BehaviorSubject.create();
	private BehaviorSubject<Boolean> loaded = BehaviorSubject.create(false);

	private Engine() {

	}

	/**
	 * Start an instance of the calculator.
	 * 
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
	public static void start(Screen screen, HardwareDisplay disp, HardwareTouchDevice touchdevice, HUD hud,
			StartupArguments args) throws InterruptedException, IOException {
		if (running) {
			throw new IllegalAccessError("Already running!");
		} else {
			running = true;
			INSTANCE.startInstance(screen, disp, touchdevice, hud, args);
		}
	}

	private void startInstance(Screen screen, HardwareDisplay disp, HardwareTouchDevice touchdevice, HUD hud,
			StartupArguments args) throws InterruptedException, IOException {
		ConsoleUtils.out.println("WarpPI Calculator");
		initializeEnvironment(args);

		Thread currentThread = Thread.currentThread();
		currentThread.setPriority(Thread.MAX_PRIORITY);
		PlatformUtils.setThreadName(currentThread, "Main thread");

		DisplayManager dm = new DisplayManager(disp, hud, screen, "WarpPI Calculator by Andrea Cavalli (@Cavallium)");
		Keyboard k = new Keyboard();
		InputManager im = new InputManager(k, touchdevice);
		HardwareDevice hardwareDevice = new HardwareDevice(dm, im);

		hardwareDevice.setup(() -> loadPhase.onNext(new LoadingStatus()));
	}

	private void onShutdown() {
		ConsoleUtils.out.println(1, "Shutdown...");
		beforeShutdown();
		ConsoleUtils.out.println(1, "");
		ConsoleUtils.out.println(1, "Closed.");
		DSystem.exit(0);
	}

	private void initializeEnvironment(StartupArguments args) throws IOException {
		ClassUtils.classLoader = this.getClass();
		StaticVars.startupArguments = args;
		Utils.debugThirdScreen = StaticVars.debugOn & false;
		StaticVars.debugWindow2x = args.isZoomed();
		if (args.isVerboseLoggingEnabled() || args.isDebugEnabled()) {
			StaticVars.outputLevel = ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE;
		}
		ConsoleUtils.out.println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, args);
		checkDeviceType();
		if (Utils.isRunningOnRaspberry() && args.isRaspberryModeAllowed()) {
			DGpio.wiringPiSetupPhys();
			DGpio.pinMode(12, DGpio.PWM_OUTPUT);
		} else {
			StaticVars.screenPos = new int[] { 0, 0 };
			StaticVars.debugOn = true;
		}
	}

	private void checkDeviceType() {
		// TODO Auto-generated method stub

	}

	public void beforeShutdown() {
		Keyboard.stopKeyboard();
	}

	public Observable<Boolean> isLoaded() {
		return loaded;
	}

	public Observable<LoadingStatus> getLoadPhase() {
		return loadPhase;
	}

	public static class LoadingStatus {
		protected LoadingStatus() {

		}

		public void done() {
			Engine.INSTANCE.loaded.onNext(true);
			HardwareDevice.INSTANCE.getDisplayManager().waitForExit();
			Engine.INSTANCE.onShutdown();
		}

		public void failed() {
			Engine.INSTANCE.onShutdown();
		}
	}
}

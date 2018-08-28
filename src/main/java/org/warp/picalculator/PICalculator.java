package org.warp.picalculator;

import java.io.IOException;

import org.warp.picalculator.boot.StartupArguments;
import org.warp.picalculator.deps.DGpio;
import org.warp.picalculator.deps.DSystem;
import org.warp.picalculator.device.HardwareDevice;
import org.warp.picalculator.device.HardwareTouchDevice;
import org.warp.picalculator.device.InputManager;
import org.warp.picalculator.device.Keyboard;
import org.warp.picalculator.device.PIHardwareDisplay;
import org.warp.picalculator.device.PIHardwareTouchDevice;
import org.warp.picalculator.gui.CalculatorHUD;
import org.warp.picalculator.gui.DisplayManager;
import org.warp.picalculator.gui.HUD;
import org.warp.picalculator.gui.HardwareDisplay;
import org.warp.picalculator.gui.screens.LoadingScreen;
import org.warp.picalculator.gui.screens.Screen;
import org.warp.picalculator.math.rules.RulesManager;

public class PICalculator {
	public static PICalculator instance;

	public PICalculator(StartupArguments args) throws InterruptedException, IOException {
		this(new LoadingScreen(), new PIHardwareDisplay(), new CalculatorHUD(), args);
	}

	public PICalculator(Screen screen, HardwareDisplay disp, HUD hud, StartupArguments args) throws InterruptedException, IOException {
		ConsoleUtils.out.println("WarpPI Calculator");
		initializeEnvironment(args);
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		PlatformUtils.setThreadName(Thread.currentThread(), "Main thread");
		DisplayManager dm = new DisplayManager(disp, hud, screen, "WarpPI Calculator by Andrea Cavalli (@Cavallium)");
		Keyboard k = new Keyboard();
		HardwareTouchDevice touch = new PIHardwareTouchDevice(false, false, false);
		InputManager im = new InputManager(k, touch);
		HardwareDevice hardwareDevice = new HardwareDevice(dm, im);
		hardwareDevice.setup(() -> {
			try {
				HardwareDevice.INSTANCE.getDisplayManager().setBrightness(0.2f);
				RulesManager.initialize();
				RulesManager.warmUp();
				if (screen instanceof LoadingScreen) {
					((LoadingScreen) screen).loaded = true;
				}
				HardwareDevice.INSTANCE.getDisplayManager().waitForExit();
			} catch (InterruptedException | Error e) {
				e.printStackTrace();
			}
			ConsoleUtils.out.println(1, "Shutdown...");
			beforeShutdown();
			ConsoleUtils.out.println(1, "");
			ConsoleUtils.out.println(1, "Closed.");
			DSystem.exit(0);
		});
	}

	public void initializeEnvironment(StartupArguments args) throws IOException {
		instance = this;
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
}

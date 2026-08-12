package it.cavallium.warppi.hardware;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.device.DeviceStateDevice;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.display.NoDisplaysAvailableException;
import it.cavallium.warppi.device.display.NullBacklightOutputDevice;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.device.input.KeyboardInputDevice;
import it.cavallium.warppi.device.input.PIHardwareTouchDevice;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLDisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLEngine;
import it.cavallium.warppi.util.Error;

public final class HardwarePlatform implements Platform {
	private final HardwareConsoleUtils consoleUtils = new HardwareConsoleUtils();
	private final HardwareGpio gpio = new HardwareGpio();
	private final HardwareStorageUtils storageUtils = new HardwareStorageUtils();
	private final ImageUtils imageUtils = new HardwareImageUtils();
	private final String osName = System.getProperty("os.name").toLowerCase();
	private final HardwareSettings settings = new HardwareSettings();
	private final BacklightOutputDevice backlightOutputDevice = new NullBacklightOutputDevice();
	private Boolean runningOnRaspberryOverride;
	private StartupArguments arguments;
	private DisplayOutputDevice displayOutputDevice;
	private KeyboardInputDevice keyboardInputDevice;
	private TouchInputDevice touchInputDevice;
	private DeviceStateDevice deviceStateDevice;

	@Override
	public ConsoleUtils getConsoleUtils() {
		return consoleUtils;
	}

	@Override
	public Gpio getGpio() {
		return gpio;
	}

	@Override
	public StorageUtils getStorageUtils() {
		return storageUtils;
	}

	@Override
	public ImageUtils getImageUtils() {
		return imageUtils;
	}

	@Override
	public HardwareSettings getSettings() {
		return settings;
	}

	@Override
	public void setThreadName(final Thread thread, final String name) {
		thread.setName(name);
	}

	@Override
	public void setThreadDaemon(final Thread thread) {
		thread.setDaemon(true);
	}

	@Override
	public void setThreadDaemon(final Thread thread, final boolean value) {
		thread.setDaemon(value);
	}

	@Override
	public void exit(final int value) {
		try {
			gpio.close();
		} finally {
			System.exit(value);
		}
	}

	@Override
	public void gc() {
		System.gc();
	}

	@Override
	public boolean isJavascript() {
		return false;
	}

	@Override
	public String getOsName() {
		return osName;
	}

	@Override
	public void alphaChanged(final boolean value) {
	}

	@Override
	public void shiftChanged(final boolean value) {
	}

	@Override
	public Semaphore newSemaphore() {
		return new HardwareSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(final int permits) {
		return new HardwareSemaphore(permits);
	}

	@Override
	public URLClassLoader newURLClassLoader(final URL[] urls) {
		return new HardwareURLClassLoader(urls);
	}

	@Override
	public void throwNewExceptionInInitializerError(final String text) {
		throw new ExceptionInInitializerError(text);
	}

	@Override
	public String[] stacktraceToString(final Error error) {
		final StringWriter writer = new StringWriter();
		error.printStackTrace(new PrintWriter(writer));
		return writer.toString().toUpperCase().replace("\t", "    ").replace("\r", "").split("\n");
	}

	@Override
	public List<String> getRuleFilePaths() throws IOException {
		final File rulesPath = storageUtils.get("rules/");
		final List<String> paths = new ArrayList<>();
		if (rulesPath.exists()) {
			for (final File file : storageUtils.walk(rulesPath)) {
				final String path = file.toString();
				if (path.endsWith(".rules"))
					paths.add(path);
			}
		}
		return paths;
	}

	@Override
	public void setRunningOnRaspberry(final boolean value) {
		runningOnRaspberryOverride = value;
	}

	@Override
	public boolean isRunningOnRaspberry() {
		return runningOnRaspberryOverride == null || runningOnRaspberryOverride;
	}

	@Override
	public TouchInputDevice getTouchInputDevice() {
		return touchInputDevice;
	}

	@Override
	public KeyboardInputDevice getKeyboardInputDevice() {
		return keyboardInputDevice;
	}

	@Override
	public DisplayOutputDevice getDisplayOutputDevice() {
		return displayOutputDevice;
	}

	@Override
	public BacklightOutputDevice getBacklightOutputDevice() {
		return backlightOutputDevice;
	}

	@Override
	public DeviceStateDevice getDeviceStateDevice() {
		return deviceStateDevice;
	}

	@Override
	public void setArguments(final StartupArguments arguments) {
		this.arguments = arguments;
		chooseDevices();
	}

	private void chooseDevices() {
		final List<DisplayOutputDevice> availableDevices = new ArrayList<>();
		if (!arguments.isNoGUIEngineForced())
			availableDevices.add(new JOGLDisplayOutputDevice());
		if (availableDevices.isEmpty())
			throw new NoDisplaysAvailableException();

		displayOutputDevice = availableDevices.get(0);
		final JOGLEngine engine = (JOGLEngine) displayOutputDevice.getGraphicEngine();
		touchInputDevice = new PIHardwareTouchDevice(false, false, false, engine);
		final Keyboard keyboard = new Keyboard();
		keyboardInputDevice = keyboard::startKeyboard;
		deviceStateDevice = new DeviceStateDevice() {
			@Override
			public void initialize() {
			}

			@Override
			public void waitForExit() {
				try {
					engine.waitForExit();
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					engine.destroy();
				}
			}

			@Override
			public void powerOff() {
				engine.destroy();
			}
		};
	}
}

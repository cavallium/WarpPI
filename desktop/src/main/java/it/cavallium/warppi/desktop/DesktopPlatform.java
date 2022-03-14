package it.cavallium.warppi.desktop;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import it.cavallium.warppi.event.TouchEvent;
import it.cavallium.warppi.gui.graphicengine.impl.jogl.JOGLDisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.swing.SwingDeviceState;
import it.cavallium.warppi.gui.graphicengine.impl.swing.SwingTouchInputDevice;
import org.apache.commons.io.FileUtils;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.device.DeviceStateDevice;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.display.NoDisplaysAvailableException;
import it.cavallium.warppi.device.display.NullBacklightOutputDevice;
import it.cavallium.warppi.device.input.KeyboardInputDevice;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.Platform;
import it.cavallium.warppi.gui.graphicengine.impl.swing.SwingDisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.impl.swing.SwingEngine;
import it.cavallium.warppi.util.CacheUtils;
import it.cavallium.warppi.util.Error;

public class DesktopPlatform implements Platform {

	private final DesktopConsoleUtils cu;
	private final DesktopGpio gi;
	private final DesktopPlatformStorage su;
	private final ImageUtils pu;
	private final String on;
	private final DesktopSettings settings;
	private Boolean runningOnRaspberryOverride = null;
	private StartupArguments args;
	private DisplayOutputDevice displayOutputDevice;
	private DeviceStateDevice deviceStateDevice;
	private TouchInputDevice touchInputDevice;
	private KeyboardInputDevice keyboardInputDevice;

	public DesktopPlatform() {
		cu = new DesktopConsoleUtils();
		gi = new DesktopGpio();
		su = new DesktopPlatformStorage();
		pu = new DesktopImageUtils();
		on = System.getProperty("os.name").toLowerCase();
		settings = new DesktopSettings();
	}

	@Override
	public ConsoleUtils getConsoleUtils() {
		return cu;
	}

	@Override
	public Gpio getGpio() {
		return gi;
	}

	@Override
	public PlatformStorage getPlatformStorage() {
		return su;
	}

	@Override
	public ImageUtils getImageUtils() {
		return pu;
	}

	@Override
	public DesktopSettings getSettings() {
		return settings;
	}

	@Override
	public void setThreadName(final Thread t, final String name) {
		t.setName(name);
	}

	@Override
	public void setThreadDaemon(final Thread t) {
		t.setDaemon(true);
	}

	@Override
	public void setThreadDaemon(final Thread t, final boolean value) {
		t.setDaemon(value);
	}

	@Override
	public void exit(final int value) {
		System.exit(value);
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
		return on;
	}

	@Override
	public void alphaChanged(final boolean val) {
		final DisplayOutputDevice currentEngine = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display;
		if (currentEngine instanceof SwingEngine)
			((SwingEngine) currentEngine).setAlphaChanged(val);
	}

	@Override
	public void shiftChanged(final boolean val) {
		final DisplayOutputDevice currentEngine = WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display;
		if (currentEngine instanceof SwingEngine)
			((SwingEngine) currentEngine).setShiftChanged(val);
	}

	@Override
	public Semaphore newSemaphore() {
		return new DesktopSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(final int i) {
		return new DesktopSemaphore(i);
	}

	@Override
	public URLClassLoader newURLClassLoader(final URL[] urls) {
		return new DesktopURLClassLoader(urls);
	}

	@Override
	public void throwNewExceptionInInitializerError(final String text) {
		throw new ExceptionInInitializerError();
	}

	@Override
	public String[] stacktraceToString(final Error e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString().toUpperCase().replace("\t", "    ").replace("\r", "").split("\n");
	}

	@Override
	public List<String> getRuleFilePaths() throws IOException {
		final File dslRulesPath = getPlatformStorage().get("rules/");
		List<String> paths = new ArrayList<>();
		if (dslRulesPath.exists()) {
			for (final File file : getPlatformStorage().walk(dslRulesPath)) {
				final String path = file.toString();
				if (path.endsWith(".rules")) {
					paths.add(path);
				}
			}
		}
		return paths;
	}

	@Override
	public void setRunningOnRaspberry(boolean b) {
		if (isRunningOnRaspberry()) {
			runningOnRaspberryOverride = b;
		} else {
			runningOnRaspberryOverride = false;
		}
	}

	@Override
	public boolean isRunningOnRaspberry() {
		if (runningOnRaspberryOverride != null)
			return runningOnRaspberryOverride;
		return CacheUtils.get("isRunningOnRaspberry", 24 * 60 * 60 * 1000, () -> {
			if (WarpPI.getPlatform().isJavascript())
				return false;
			if (WarpPI.getPlatform().getOsName().equals("Linux"))
				try {
					final File osRelease = new File("/etc", "os-release");
					return FileUtils.readLines(osRelease, "UTF-8").stream().map(String::toLowerCase).anyMatch(line -> line.contains("raspbian") && line.contains("name"));
				} catch (final IOException readException) {
					return false;
				}
			else
				return false;
		});
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
		return this.displayOutputDevice;
	}

	@Override
	public BacklightOutputDevice getBacklightOutputDevice() {
		return new NullBacklightOutputDevice();
	}

	@Override
	public DeviceStateDevice getDeviceStateDevice() {
		return this.deviceStateDevice;
	}

	@Override
	public void setArguments(StartupArguments args) {
		this.args = args;
		this.chooseDevices();
	}

	private void chooseDevices() {
		List<DisplayOutputDevice> availableDevices = new ArrayList<>();
		List<DisplayOutputDevice> guiDevices = List.of(new SwingDisplayOutputDevice(), new JOGLDisplayOutputDevice());
		List<DisplayOutputDevice> consoleDevices = List.of();

		if (args.isMSDOSModeEnabled() || args.isNoGUIEngineForced()) {
			availableDevices.addAll(consoleDevices);
		}
		if (!args.isNoGUIEngineForced()) {
			availableDevices.addAll(guiDevices);
		}
		
		if (availableDevices.size() == 0) {
			throw new NoDisplaysAvailableException();
		}

		for (DisplayOutputDevice device : availableDevices) {
			if (device instanceof SwingDisplayOutputDevice) {
				if (args.isCPUEngineForced()) {
					this.displayOutputDevice = device;
					break;
				}
			} else if (device instanceof JOGLDisplayOutputDevice) {
				if (args.isGPUEngineForced()) {
					this.displayOutputDevice = device;
					break;
				}
			}
		}

		if (this.displayOutputDevice == null) this.displayOutputDevice = availableDevices.get(0);


		if (displayOutputDevice instanceof SwingDisplayOutputDevice) {
			this.touchInputDevice = new SwingTouchInputDevice((SwingEngine) displayOutputDevice.getGraphicEngine());

			//TODO: implement a keyboard input device
			this.keyboardInputDevice = new KeyboardInputDevice() {
				@Override
				public void initialize() {

				}
			};

			this.deviceStateDevice = new SwingDeviceState((SwingEngine) displayOutputDevice.getGraphicEngine());

		} else if (displayOutputDevice instanceof JOGLDisplayOutputDevice) {
			//TODO: implement a touch input device
			this.touchInputDevice = new TouchInputDevice() {
				@Override
				public boolean getSwappedAxes() {
					return false;
				}

				@Override
				public boolean getInvertedX() {
					return false;
				}

				@Override
				public boolean getInvertedY() {
					return false;
				}

				@Override
				public void listenTouchEvents(Consumer<TouchEvent> touchEventListener) {

				}

				@Override
				public void initialize() {

				}
			};
			//TODO: implement a keyboard input device
			this.keyboardInputDevice = new KeyboardInputDevice() {
				@Override
				public void initialize() {

				}
			};
			this.deviceStateDevice = new DeviceStateDevice() {
				@Override
				public void initialize() {

				}

				@Override
				public void waitForExit() {
					while(true) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void powerOff() {

				}
			}; //TODO: Implement
		}
	}

}

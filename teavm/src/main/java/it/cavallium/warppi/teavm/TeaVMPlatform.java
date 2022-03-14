package it.cavallium.warppi.teavm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import it.cavallium.warppi.boot.StartupArguments;
import it.cavallium.warppi.device.DeviceStateDevice;
import it.cavallium.warppi.device.display.BacklightOutputDevice;
import it.cavallium.warppi.device.display.NoDisplaysAvailableException;
import it.cavallium.warppi.device.display.NullBacklightOutputDevice;
import it.cavallium.warppi.device.input.KeyboardInputDevice;
import it.cavallium.warppi.device.input.TouchInputDevice;
import it.cavallium.warppi.event.TouchEvent;
import it.cavallium.warppi.gui.graphicengine.html.HtmlDeviceState;
import it.cavallium.warppi.gui.graphicengine.html.HtmlDisplayOutputDevice;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.html.HtmlEngine;
import it.cavallium.warppi.util.Error;

public class TeaVMPlatform implements Platform {

	private final TeaVMConsoleUtils cu;
	private final TeaVMGpio gi;
	private final TeaVMPlatformStorage su;
	private final String on;
	private final TeaVMImageUtils pu;
	private final TeaVMSettings settings;
	private Boolean runningOnRaspberryOverride = null;
	private StartupArguments args;
	private DisplayOutputDevice displayOutputDevice;
	private DeviceStateDevice deviceStateDevice;
	private TouchInputDevice touchInputDevice;
	private KeyboardInputDevice keyboardInputDevice;

	public TeaVMPlatform() {
		cu = new TeaVMConsoleUtils();
		gi = new TeaVMGpio();
		su = new TeaVMPlatformStorage();
		pu = new TeaVMImageUtils();
		on = "JavaScript";
		settings = new TeaVMSettings();
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
	public TeaVMSettings getSettings() {
		return settings;
	}

	@Override
	public void setThreadName(final Thread t, final String name) {}

	@Override
	public void setThreadDaemon(final Thread t) {}

	@Override
	public void setThreadDaemon(final Thread t, final boolean value) {}

	@Override
	public void exit(final int value) {
		System.err.println("====================PROGRAM END====================");
	}

	@Override
	public void gc() {

	}

	@Override
	public boolean isJavascript() {
		return true;
	}

	@Override
	public String getOsName() {
		return on;
	}

	private boolean shift, alpha;

	@Override
	public void alphaChanged(final boolean alpha) {
		this.alpha = alpha;
		final HTMLDocument doc = Window.current().getDocument();
		doc.getBody().setClassName((shift ? "shift " : "") + (alpha ? "alpha" : ""));
	}

	@Override
	public void shiftChanged(final boolean shift) {
		this.shift = shift;
		final HTMLDocument doc = Window.current().getDocument();
		doc.getBody().setClassName((shift ? "shift " : "") + (alpha ? "alpha" : ""));
	}

	@Override
	public Semaphore newSemaphore() {
		return new TeaVMSemaphore(0);
	}

	@Override
	public Semaphore newSemaphore(final int i) {
		return new TeaVMSemaphore(i);
	}

	@Override
	public URLClassLoader newURLClassLoader(final URL[] urls) {
		return new TeaVMURLClassLoader(urls);
	}

	@Override
	public void throwNewExceptionInInitializerError(final String text) {
		throw new NullPointerException();
	}

	@Override
	public String[] stacktraceToString(final Error e) {
		return e.getMessage().toUpperCase().replace("\r", "").split("\n");
	}

	/**
	 * Fetches the list of resource files containing DSL rules to load from the <code>/rules.list</code> resource on the server.
	 * <p>
	 * The <code>/rules.list</code> resource must exist and be a text file with zero or more lines.
	 * Each line specifies the name of another resource containing DSL source code.
	 * Blank lines aren't allowed, and resource names are interpreted exactly as written (without stripping
	 * leading/trailing spaces, etc.)
	 */
	@Override
	public List<String> getRuleFilePaths() throws IOException {
		try (final InputStream listStream = getPlatformStorage().getResourceStream("/rules.list")) {
			return getPlatformStorage().readAllLines(listStream);
		}
	}

	@Override
	public void setRunningOnRaspberry(boolean b) {
	}
	
	@Override
	public boolean isRunningOnRaspberry() {
		return false;
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
		List<DisplayOutputDevice> guiDevices = new ArrayList<>();
		guiDevices.add(new HtmlDisplayOutputDevice());
		List<DisplayOutputDevice> consoleDevices = new ArrayList<>();

		if (args.isMSDOSModeEnabled() || args.isNoGUIEngineForced()) {
			availableDevices.addAll(consoleDevices);
		}
		if (!args.isNoGUIEngineForced()) {
			availableDevices.addAll(guiDevices);
		}

		if (availableDevices.size() == 0) {
			throw new NoDisplaysAvailableException();
		}

		if (this.displayOutputDevice == null) this.displayOutputDevice = availableDevices.get(0);


		if (displayOutputDevice instanceof HtmlDisplayOutputDevice) {
			//this.touchInputDevice = new HtmlTouchInputDevice((HtmlEngine) displayOutputDevice.getGraphicEngine());

			//todo: implement
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

			//todo: implement
			this.keyboardInputDevice = new KeyboardInputDevice() {
				@Override
				public void initialize() {

				}
			};

			this.deviceStateDevice = new HtmlDeviceState((HtmlEngine) displayOutputDevice.getGraphicEngine());

		}
	}
}

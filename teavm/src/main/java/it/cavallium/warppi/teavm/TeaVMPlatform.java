package it.cavallium.warppi.teavm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.html.HtmlEngine;
import it.cavallium.warppi.util.Error;

public class TeaVMPlatform implements Platform {

	private final TeaVMConsoleUtils cu;
	private final TeaVMGpio gi;
	private final TeaVMStorageUtils su;
	private final String on;
	private final Map<String, GraphicEngine> el;
	private final TeaVMImageUtils pu;
	private final TeaVMSettings settings;
	private Boolean runningOnRaspberryOverride = null;

	public TeaVMPlatform() {
		cu = new TeaVMConsoleUtils();
		gi = new TeaVMGpio();
		su = new TeaVMStorageUtils();
		pu = new TeaVMImageUtils();
		on = "JavaScript";
		el = new HashMap<>();
		el.put("HTML5 engine", new HtmlEngine());
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
	public StorageUtils getStorageUtils() {
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
	public Map<String, GraphicEngine> getGraphicEnginesList() {
		return el;
	}

	@Override
	public DisplayOutputDevice getGraphicEngine(final String string) throws NullPointerException {
		return el.get(string);
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
		try (final InputStream listStream = getStorageUtils().getResourceStream("/rules.list")) {
			return getStorageUtils().readAllLines(listStream);
		}
	}

	@Override
	public void setRunningOnRaspberry(boolean b) {
	}
	
	@Override
	public boolean isRunningOnRaspberry() {
		return false;
	}

}
